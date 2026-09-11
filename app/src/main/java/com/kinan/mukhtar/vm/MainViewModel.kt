package com.kinan.mukhtar.vm

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kinan.mukhtar.data.AppConfigEntity
import com.kinan.mukhtar.data.AppDatabase
import com.kinan.mukhtar.data.AppRepository
import com.kinan.mukhtar.data.PersonEntity
import com.kinan.mukhtar.util.BackupManager
import kotlinx.coroutines.Dispatchers
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

    val persons: StateFlow<List<PersonEntity>> =
        combine(repo.persons, _query) { list, q ->
            if (q.isBlank()) list else list.filter { it.fullName.contains(q.trim(), true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val families: StateFlow<List<PersonEntity>> =
        combine(repo.families, _query) { list, q ->
            if (q.isBlank()) list else list.filter {
                it.fullName.contains(q.trim(), true) || (it.spouseName ?: "").contains(q.trim(), true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(value: String) { _query.value = value }
    fun toggleDarkMode(value: Boolean) { _darkMode.value = value }
    fun consumeMessage() { _message.value = null }
    private fun notify(text: String) { _message.value = UiMessage(text) }

    fun saveConfig(
        governorate: String,
        district: String,
        region: String,
        onSaved: () -> Unit = {}
    ) = viewModelScope.launch {
        repo.saveConfig(
            AppConfigEntity(1, governorate.trim(), district.trim(), region.trim(), true)
        )
        notify("تم حفظ معلومات المنطقة بنجاح")
        onSaved()
    }

    fun savePerson(person: PersonEntity) = viewModelScope.launch {
        val clean = if (person.isMarried) person else person.copy(
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
