package com.kinan.mukhtar.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kinan.mukhtar.data.PersonEntity
import com.kinan.mukhtar.util.BorderStyle
import com.kinan.mukhtar.util.DocumentStyle
import com.kinan.mukhtar.util.PdfGenerator
import com.kinan.mukhtar.util.PrintUtil
import com.kinan.mukhtar.vm.MainViewModel

/** محرر تأييد السكن المتقدم (WYSIWYG) */
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
    var style by remember { mutableStateOf(DocumentStyle()) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            style = style.copy(backgroundUri = uri, border = BorderStyle.IMAGE)
        }
    }

    val suggestions = remember(search, people) {
        if (search.isBlank()) people.take(20)
        else people.filter { it.fullName.contains(search.trim(), true) }.take(20)
    }

    fun regenerate(person: PersonEntity) {
        documentText = PdfGenerator.buildDocumentText(
            governorate = config?.governorate.orEmpty(),
            district = config?.district.orEmpty(),
            region = config?.region.orEmpty(),
            mukhtarName = config?.mukhtarName.orEmpty(),
            personName = person.fullName,
            spouseName = person.spouseName,
            job = person.job,
            notes = person.notes
        )
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it; expanded = true },
                label = { Text("اختر الشخص") },
                placeholder = { Text("ابحث بالاسم...") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                singleLine = true,
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
            return@Column
        }

        // ==================== معاينة A4 ====================
        Text("المعاينة (A4)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        A4Preview(text = documentText, style = style)

        Spacer(Modifier.height(20.dp))

        // ==================== حجم الخط ====================
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("حجم الخط", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.weight(1f))
                    Text("${style.fontSize.toInt()}", style = MaterialTheme.typography.bodyMedium)
                }
                Slider(
                    value = style.fontSize,
                    onValueChange = { style = style.copy(fontSize = it) },
                    valueRange = 9f..26f,
                    steps = 16
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ==================== خيارات الإطار ====================
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("خيارات الإطار", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(10.dp))
                BorderStyle.entries.filter { it != BorderStyle.IMAGE }.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = style.border == option,
                            onClick = { style = style.copy(border = option) }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = style.border == BorderStyle.IMAGE,
                        onClick = {
                            if (style.backgroundUri != null) {
                                style = style.copy(border = BorderStyle.IMAGE)
                            } else {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(BorderStyle.IMAGE.label, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("اختيار صورة")
                    }
                    if (style.backgroundUri != null) {
                        TextButton(onClick = {
                            style = style.copy(backgroundUri = null, border = BorderStyle.SINGLE)
                        }) { Text("إزالة الصورة") }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ==================== تحرير النص ====================
        OutlinedTextField(
            value = documentText,
            onValueChange = { documentText = it },
            minLines = 8,
            label = { Text("نص المضبطة") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { selected?.let { regenerate(it) } }) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("إعادة توليد النص الافتراضي")
        }

        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val name = "تأييد_سكن_${System.currentTimeMillis()}.pdf"
                    val uri = PdfGenerator.saveToDownloads(context, documentText, style, name)
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
                Text("حفظ PDF")
            }
            OutlinedButton(
                onClick = { PrintUtil.print(context, documentText, style) },
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

/** معاينة حية بنسبة A4 تعكس الخط والإطار والخلفية */
@Composable
private fun A4Preview(text: String, style: DocumentStyle) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(595f / 842f)
            .background(Color.White)
    ) {
        // خلفية الصورة
        if (style.border == BorderStyle.IMAGE && style.backgroundUri != null) {
            AsyncImage(
                model = style.backgroundUri,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            BorderCanvas(style.border)
        }

        Text(
            text = text,
            color = Color.Black,
            fontSize = (style.fontSize * 0.62f).sp,
            lineHeight = (style.fontSize * 1.05f).sp,
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState())
        )
    }
}

/** رسم الإطارات بـ Canvas مطابقاً لما يُرسم في الـ PDF */
@Composable
private fun BorderCanvas(border: BorderStyle) {
    if (border == BorderStyle.NONE || border == BorderStyle.IMAGE) return
    val color = MaterialTheme.colorScheme.primary

    Canvas(Modifier.fillMaxSize()) {
        val outer = size.minDimension * 0.035f
        val w = size.width
        val h = size.height

        fun rect(inset: Float, stroke: Stroke) {
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(w - inset * 2, h - inset * 2),
                style = stroke
            )
        }

        when (border) {
            BorderStyle.SINGLE -> rect(outer, Stroke(width = 2f))

            BorderStyle.DOUBLE -> {
                rect(outer, Stroke(width = 2f))
                rect(outer + 7f, Stroke(width = 2f))
            }

            BorderStyle.SOLID_DASHED -> {
                rect(outer, Stroke(width = 2f))
                rect(
                    outer + 8f,
                    Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                )
            }

            else -> Unit
        }
    }
}
