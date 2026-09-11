package com.kinan.mukhtar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kinan.mukhtar.vm.MainViewModel

/** لوحة الإحصائيات */
@Composable
fun StatisticsScreen(viewModel: MainViewModel) {
    val stats by viewModel.stats.collectAsState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text(
            "الإحصائيات العامة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "ملخص بيانات سكنة المنطقة",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(18.dp))

        // بطاقة الإجمالي البارزة
        ElevatedCard(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "مجموع الأفراد",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stats.total.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        StatRow {
            StatCard("العوائل", stats.families, Icons.Filled.Group, Color(0xFF0F5132), Modifier.weight(1f))
            StatCard("العزّاب", stats.singles, Icons.Filled.PersonOutline, Color(0xFF6A4C93), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        StatRow {
            StatCard("الذكور", stats.males, Icons.Filled.Male, Color(0xFF1565C0), Modifier.weight(1f))
            StatCard("الإناث", stats.females, Icons.Filled.Female, Color(0xFFC2185B), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        StatRow {
            StatCard("أرقام مسجلة", stats.withPhone, Icons.Filled.Phone, Color(0xFF00796B), Modifier.weight(1f))
            StatCard("متوسط العائلة", if (stats.families > 0) stats.total / stats.families else 0,
                Icons.Filled.Person, Color(0xFFB8860B), Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // توزيع الجنس كشريط نسبي
        if (stats.total > 0) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("نسبة التوزيع حسب الجنس", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(12.dp))
                    val malesRatio = stats.males.toFloat() / stats.total.toFloat()
                    LinearProgressIndicator(
                        progress = { malesRatio },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = Color(0xFF1565C0),
                        trackColor = Color(0xFFC2185B)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ذكور: ${stats.males}", style = MaterialTheme.typography.bodySmall)
                        Text("إناث: ${stats.females}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = MaterialTheme.shapes.small, color = accent.copy(alpha = 0.12f)) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(8.dp).size(24.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
