package com.kinan.mukhtar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kinan.mukhtar.data.Gender
import com.kinan.mukhtar.data.PersonEntity
import com.kinan.mukhtar.ui.components.ArabicDateField
import com.kinan.mukhtar.util.DateUtils
import com.kinan.mukhtar.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonScreen(viewModel: MainViewModel, personId: Long, onDone: () -> Unit) {

    var loaded by remember { mutableStateOf(personId == 0L) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf(0L) }
    var isMarried by rememberSaveable { mutableStateOf(false) }
    var spouseName by rememberSaveable { mutableStateOf("") }
    var spouseBirthDate by rememberSaveable { mutableStateOf(0L) }
    var job by rememberSaveable { mutableStateOf("") }
    var spouseJob by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf(Gender.MALE) }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val isDuplicate by viewModel.isDuplicateName.collectAsState()

    DisposableEffect(Unit) { onDispose { viewModel.resetDuplicateCheck() } }

    LaunchedEffect(personId) {
        if (personId != 0L) {
            viewModel.personById(personId)?.let { p ->
                fullName = p.fullName
                birthDate = p.birthDate
                isMarried = p.isMarried
                spouseName = p.spouseName ?: ""
                spouseBirthDate = p.spouseBirthDate ?: 0L
                job = p.job
                spouseJob = p.spouseJob ?: ""
                notes = p.notes ?: ""
                gender = p.gender.ifBlank { Gender.MALE }
                phoneNumber = p.phoneNumber ?: ""
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (personId == 0L) "إضافة شخص جديد" else "تعديل البيانات") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    error = false
                    viewModel.checkDuplicateName(it, personId)
                },
                label = { Text("الاسم الرباعي مع اللقب") },
                isError = isDuplicate,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // تحذير غير معطِّل - زر الحفظ يبقى مفعلاً
            AnimatedVisibility(visible = isDuplicate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "تنبيه: هذا الاسم موجود مسبقاً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            ArabicDateField(label = "تاريخ الميلاد", value = birthDate.takeIf { it > 0L }, onValueChange = { birthDate = it })
            Spacer(Modifier.height(8.dp))
            Text(
                "العمر: ${DateUtils.ageText(birthDate)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(14.dp))

            // الجنس - حقل إلزامي
            Text("الجنس", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Gender.all.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = gender == option,
                        onClick = { gender = option },
                        shape = SegmentedButtonDefaults.itemShape(index, Gender.all.size)
                    ) { Text(option) }
                }
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input -> phoneNumber = input.filter { it.isDigit() || it in "+- " } },
                label = { Text("رقم الهاتف") },
                placeholder = { Text("اختياري") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الحالة الزوجية", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Text(if (isMarried) "متزوج" else "أعزب", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = isMarried, onCheckedChange = { isMarried = it })
                }
            }

            AnimatedVisibility(visible = isMarried) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = spouseName,
                        onValueChange = { spouseName = it },
                        label = { Text("اسم الزوجة الرباعي مع اللقب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    ArabicDateField(label = "مواليد الزوجة", value = spouseBirthDate.takeIf { it > 0L }, onValueChange = { spouseBirthDate = it })
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "عمر الزوجة: ${DateUtils.ageText(spouseBirthDate)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            // الحقول تتغير ديناميكياً حسب الحالة الزوجية
            if (isMarried) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = job,
                        onValueChange = { job = it },
                        label = { Text("مهنة الزوج") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = spouseJob,
                        onValueChange = { spouseJob = it },
                        label = { Text("مهنة الزوجة") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = { Text("المهنة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("الملاحظات") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            if (error) {
                Spacer(Modifier.height(8.dp))
                Text("يرجى إدخال الاسم الكامل", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = {
                    if (fullName.isBlank()) { error = true; return@Button }
                    viewModel.savePerson(
                        PersonEntity(
                            id = personId,
                            fullName = fullName.trim(),
                            birthDate = birthDate,
                            isMarried = isMarried,
                            spouseName = spouseName.trim().takeIf { it.isNotBlank() },
                            spouseBirthDate = spouseBirthDate.takeIf { it > 0 },
                            job = job.trim(),
                            spouseJob = spouseJob.trim().takeIf { it.isNotBlank() },
                            notes = notes.trim().takeIf { it.isNotBlank() },
                            gender = gender,
                            phoneNumber = phoneNumber.trim().takeIf { it.isNotBlank() }
                        )
                    )
                    onDone()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("حفظ") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
