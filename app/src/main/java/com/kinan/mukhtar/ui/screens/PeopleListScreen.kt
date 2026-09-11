package com.kinan.mukhtar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kinan.mukhtar.data.PersonEntity
import com.kinan.mukhtar.util.DateUtils
import com.kinan.mukhtar.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
    viewModel: MainViewModel,
    familiesOnly: Boolean,
    onEdit: (Long) -> Unit
) {
    val list by (if (familiesOnly) viewModel.families else viewModel.persons).collectAsState()
    val query by viewModel.query.collectAsState()
    var detail by remember { mutableStateOf<PersonEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<PersonEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { onEdit(0L) }) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text(if (familiesOnly) "ابحث عن عائلة..." else "ابحث عن اسم...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = if (familiesOnly) "عدد العوائل: ${list.size}" else "عدد الأفراد: ${list.size}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد بيانات لعرضها", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(list, key = { it.id }) { person ->
                        PersonCard(
                            person = person,
                            onClick = { detail = person },
                            onEdit = { onEdit(person.id) },
                            onDelete = { confirmDelete = person }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    detail?.let { person ->
        ModalBottomSheet(onDismissRequest = { detail = null }) {
            PersonDetails(person)
        }
    }

    confirmDelete?.let { person ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل تريد حذف السجل الخاص بـ (${person.fullName}) ؟") },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePerson(person); confirmDelete = null }) {
                    Text("حذف")
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun PersonCard(
    person: PersonEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(person.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("العمر: ${DateUtils.ageText(person.birthDate)}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                AssistChip(
                    onClick = onClick,
                    label = { Text(if (person.isMarried) "متزوج" else "أعزب") }
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "تعديل") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun PersonDetails(person: PersonEntity) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text(person.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        DetailRow("تاريخ الميلاد", DateUtils.format(person.birthDate))
        DetailRow("العمر", DateUtils.ageText(person.birthDate))
        DetailRow("الحالة الزوجية", if (person.isMarried) "متزوج" else "أعزب")
        DetailRow("المهنة", person.job.ifBlank { "غير محدد" })
        if (person.isMarried) {
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Text("بيانات الزوجة", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            DetailRow("اسم الزوجة", person.spouseName ?: "غير محدد")
            DetailRow("مواليد الزوجة", DateUtils.format(person.spouseBirthDate))
            DetailRow("عمر الزوجة", DateUtils.ageText(person.spouseBirthDate))
            DetailRow("مهنة الزوجة", person.spouseJob?.ifBlank { "غير محدد" } ?: "غير محدد")
        }
        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        DetailRow("الملاحظات", person.notes?.ifBlank { "لا توجد" } ?: "لا توجد")
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text("$label:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
