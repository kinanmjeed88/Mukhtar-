package com.kinan.mukhtar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kinan.mukhtar.data.PersonEntity
import com.kinan.mukhtar.util.PdfGenerator
import com.kinan.mukhtar.util.PrintUtil
import com.kinan.mukhtar.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidencyScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()
    val people by viewModel.allPersons.collectAsState()

    var search by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<PersonEntity?>(null) }
    var documentText by remember { mutableStateOf("") }

    val suggestions = remember(search, people) {
        if (search.isBlank()) people.take(20)
        else people.filter { it.fullName.contains(search.trim(), true) }.take(20)
    }

    fun regenerate(person: PersonEntity) {
        documentText = PdfGenerator.buildDocumentText(
            governorate = config?.governorate ?: "",
            district = config?.district ?: "",
            region = config?.region ?: "",
            personName = person.fullName,
            spouseName = person.spouseName,
            job = person.job,
            notes = person.notes
        )
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

        Text("تأييد سكن", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it; expanded = true },
                label = { Text("اختر الشخص") },
                placeholder = { Text("ابحث بالاسم...") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (suggestions.isEmpty()) {
                    DropdownMenuItem(text = { Text("لا توجد نتائج") }, onClick = { expanded = false })
                }
                suggestions.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.fullName) },
                        onClick = {
                            selected = p
                            search = p.fullName
                            regenerate(p)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (selected == null) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("يرجى اختيار شخص لعرض معاينة المستند", textAlign = TextAlign.Center)
            }
        } else {
            Text("معاينة ورقة A4", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // معاينة بنسبة A4 (595 × 842)
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(595f / 842f)
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .padding(14.dp)
            ) {
                Text(
                    text = documentText,
                    color = Color.Black,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("تحرير نص المستند قبل الحفظ", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = documentText,
                onValueChange = { documentText = it },
                minLines = 8,
                label = { Text("نص المضبطة") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { selected?.let { regenerate(it) } }) {
                Text("إعادة توليد النص الافتراضي")
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val name = "تأييد_سكن_${selected?.fullName?.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
                        val uri = PdfGenerator.saveToDownloads(context, documentText, name)
                        Toast.makeText(
                            context,
                            if (uri != null) "تم حفظ الملف في مجلد التنزيلات" else "تعذر حفظ الملف",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("حفظ كـ PDF")
                }
                OutlinedButton(
                    onClick = { PrintUtil.print(context, documentText) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("طباعة")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
