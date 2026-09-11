package com.kinan.mukhtar.vm

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kinan.mukhtar.data.AppConfigEntity
import com.kinan.mukhtar.data.AppDatabase
import com.kinan.mukhtar.data.AppRepository
import com.kinan.mukhtar.data.Gender
import com.kinan.mukhtar.data.PersonEntity
import com.kinan.mukhtar.util.BackupManager
import com.kinan.mukhtar.util.NameMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiMessage(val text: String, val id: Long = System.currentTimeMillis())

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.get(app))

    val config: StateFlow<AppConfigEntity?> = repo.config
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** null = جاري التحميل */
    val isSetupComplete: StateFlow<Boolean?> = repo.config
        .map { it?.isSetupComplete ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _message = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = _message.asStateFlow()

    val allPersons: StateFlow<List<PersonEntity>> = repo.persons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** البحث يتم في قاعدة البيانات ويشمل اسم الزوجة */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val persons: StateFlow<List<PersonEntity>> = _query
        .debounce(200)
        .flatMapLatest { repo.searchAll(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val families: StateFlow<List<PersonEntity>> = _query
        .debounce(200)
        .flatMapLatest { repo.searchFamilies(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== دليل الهاتف ====================

    private val _phoneQuery = MutableStateFlow("")
    val phoneQuery: StateFlow<String> = _phoneQuery.asStateFlow()
    fun setPhoneQuery(v: String) { _phoneQuery.value = v }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val phoneDirectory: StateFlow<List<PersonEntity>> = _phoneQuery
        .debounce(200)
        .flatMapLatest { repo.searchPhoneDirectory(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== الإحصائيات ====================

    data class Stats(
        val total: Int = 0,
        val families: Int = 0,
        val males: Int = 0,
        val females: Int = 0,
        val withPhone: Int = 0
    ) {
        val singles: Int get() = (total - families).coerceAtLeast(0)
    }

    val stats: StateFlow<Stats> = combine(
        repo.countAll,
        repo.countFamilies,
        repo.countByGender(Gender.MALE),
        repo.countByGender(Gender.FEMALE),
        repo.countWithPhone
    ) { total, fam, males, females, phones ->
        Stats(total, fam, males, females, phones)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Stats())

    // ==================== فحص تكرار الاسم ====================

    private data class NameProbe(val name: String, val excludeId: Long)

    private val _nameProbe = MutableStateFlow(NameProbe("", 0L))

    /** true إذا كان الاسم موجوداً مسبقاً - تحذير فقط ولا يمنع الحفظ */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val isDuplicateName: StateFlow<Boolean> = _nameProbe
        .debounce(300)
        .distinctUntilChanged()
        .mapLatest { probe ->
            val name = probe.name.trim()
            if (name.length < 3) false else repo.isDuplicateName(name, probe.excludeId)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun checkDuplicateName(name: String, excludeId: Long) {
        _nameProbe.value = NameProbe(name, excludeId)
    }

    fun resetDuplicateCheck() { _nameProbe.value = NameProbe("", 0L) }

    // ==================== ربط الأبناء تلقائياً ====================

    /** أبناء رب العائلة عبر مطابقة سلسلة النسب في الاسم الرباعي */
    fun childrenOf(father: PersonEntity): StateFlow<List<PersonEntity>> =
        repo.persons
            .map { all ->
                all.filter { !it.isMarried && NameMatcher.isChildOf(it.fullName, father.fullName) }
                    .sortedBy { it.birthDate }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(value: String) { _query.value = value }
    fun toggleDarkMode(value: Boolean) { _darkMode.value = value }
    fun consumeMessage() { _message.value = null }
    private fun notify(text: String) { _message.value = UiMessage(text) }

    fun saveConfig(
        mukhtarName: String,
        governorate: String,
        district: String,
        region: String,
        onSaved: () -> Unit = {}
    ) = viewModelScope.launch {
        repo.saveConfig(
            AppConfigEntity(
                id = 1,
                governorate = governorate.trim(),
                district = district.trim(),
                region = region.trim(),
                mukhtarName = mukhtarName.trim(),
                isSetupComplete = true
            )
        )
        notify("تم حفظ معلومات المنطقة بنجاح")
        onSaved()
    }

    fun savePerson(person: PersonEntity) = viewModelScope.launch {
        val normalized = person.copy(
            fullName = person.fullName.trim().replace(Regex("\\s+"), " "),
            job = person.job.trim()
        )
        // عند العزوبية تُمسح كل حقول الزوجة بما فيها مهنتها
        val clean = if (normalized.isMarried) normalized.copy(
            spouseName = normalized.spouseName?.trim()?.replace(Regex("\\s+"), " ")?.takeIf { it.isNotBlank() },
            spouseJob = normalized.spouseJob?.trim()?.takeIf { it.isNotBlank() }
        ) else normalized.copy(
            spouseName = null, spouseBirthDate = null, spouseJob = null
        )
        if (clean.id == 0L) repo.addPerson(clean) else repo.updatePerson(clean)
        notify("تم حفظ البيانات بنجاح")
    }

    fun deletePerson(person: PersonEntity) = viewModelScope.launch {
        repo.deletePerson(person)
        notify("تم حذف السجل")
    }

    suspend fun personById(id: Long): PersonEntity? = repo.personById(id)

    fun importNames(context: Context, uri: Uri) = viewModelScope.launch {
        val count = withContext(Dispatchers.IO) {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext 0
            val names = BackupManager.parseNames(raw)
            repo.addPersons(names.map { PersonEntity(fullName = it, isMarried = false) })
            names.size
        }
        notify(if (count > 0) "تم استيراد $count اسماً" else "لم يتم العثور على أسماء صالحة")
    }

    fun exportBackup(context: Context, uri: Uri) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            try {
                val json = BackupManager.toJson(repo.currentConfig(), repo.allPersonsOnce())
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                true
            } catch (e: Exception) { false }
        }
        notify(if (ok) "تم تصدير النسخة الاحتياطية" else "فشل التصدير")
    }

    fun importBackup(context: Context, uri: Uri) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            try {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: return@withContext false
                val payload = BackupManager.fromJson(raw)
                repo.replaceAll(payload.config, payload.persons)
                true
            } catch (e: Exception) { false }
        }
        notify(if (ok) "تم استيراد النسخة الاحتياطية" else "فشل الاستيراد: الملف غير صالح")
    }
}
