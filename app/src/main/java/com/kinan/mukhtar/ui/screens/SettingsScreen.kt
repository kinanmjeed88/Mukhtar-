package com.kinan.mukhtar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kinan.mukhtar.vm.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()
    val dark by viewModel.darkMode.collectAsState()

    var showRegionDialog by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val importNamesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importNames(context, it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportBackup(context, it) } }

    val importBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importBackup(context, it) } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

        SettingRow(Icons.Filled.LocationCity, "تعديل معلومات المنطقة", config?.headerLine ?: "") {
            showRegionDialog = true
        }

        ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("المظهر", style = MaterialTheme.typography.titleSmall)
                    Text(if (dark) "الوضع الليلي" else "الوضع النهاري", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = dark, onCheckedChange = viewModel::toggleDarkMode)
            }
        }

        SettingRow(Icons.Filled.UploadFile, "استيراد الأسماء من ملف", "ملف نصي أو CSV، كل سطر اسم كامل") {
            importNamesLauncher.launch(arrayOf("text/plain", "text/csv", "text/comma-separated-values", "*/*"))
        }

        SettingRow(Icons.Filled.Backup, "تصدير نسخة احتياطية", "حفظ جميع البيانات بصيغة JSON") {
            exportLauncher.launch("mukhtar_backup_${System.currentTimeMillis()}.json")
        }

        SettingRow(Icons.Filled.Restore, "استيراد نسخة احتياطية", "استبدال البيانات الحالية بالكامل") {
            importBackupLauncher.launch(arrayOf("application/json", "*/*"))
        }

        SettingRow(Icons.Filled.Info, "حول مطور التطبيق", "معلومات التواصل") { showAbout = true }

        Spacer(Modifier.height(24.dp))
    }

    if (showRegionDialog) {
        EditRegionDialog(
            initialGovernorate = config?.governorate ?: "",
            initialDistrict = config?.district ?: "",
            initialRegion = config?.region ?: "",
            onDismiss = { showRegionDialog = false },
            onSave = { g, d, r -> viewModel.saveConfig(g, d, r); showRegionDialog = false }
        )
    }

    if (showAbout) AboutDeveloperDialog { showAbout = false }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick() }) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.Filled.ChevronLeft, contentDescription = null)
        }
    }
}

@Composable
private fun EditRegionDialog(
    initialGovernorate: String,
    initialDistrict: String,
    initialRegion: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var g by remember { mutableStateOf(initialGovernorate) }
    var d by remember { mutableStateOf(initialDistrict) }
    var r by remember { mutableStateOf(initialRegion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل معلومات المنطقة") },
        text = {
            Column {
                GovernorateDropdown(value = g, onValueChange = { g = it })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("القضاء / الناحية") })
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = r, onValueChange = { r = it }, label = { Text("اسم المنطقة / الحي") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (g.isNotBlank() && d.isNotBlank() && r.isNotBlank()) onSave(g, d, r) }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun AboutDeveloperDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    fun open(url: String) {
        if (url.isBlank()) return
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("حول مطور التطبيق", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("المطور", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Text("كنان الصائغ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Kinan Al-Sayegh", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Text(
                    "التطبيق حالياً مجاني - نسخة تجريبية",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text("للتواصل", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SocialIcon(Icons.Filled.Send, Color(0xFF29A9EB), "تيليجرام") {
                        open("https://t.me/techtouch7")
                    }
                    SocialIcon(Icons.Filled.PlayArrow, Color(0xFFFF0000), "يوتيوب") {
                        open("https://youtube.com/@kinanmajeed?si=I2yuzJT2rRnEHLVg")
                    }
                    SocialIcon(Icons.Filled.Facebook, Color(0xFF1877F2), "فيسبوك") { open("") }
                    SocialIcon(Icons.Filled.MusicNote, Color(0xFF010101), "تيك توك") { open("") }
                    SocialIcon(Icons.Filled.CameraAlt, Color(0xFFE1306C), "إنستغرام") { open("") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

@Composable
private fun SocialIcon(icon: ImageVector, color: Color, desc: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(color).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = Color.White)
    }
}
