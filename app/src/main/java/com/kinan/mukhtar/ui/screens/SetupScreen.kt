package com.kinan.mukhtar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kinan.mukhtar.data.StaticData
import com.kinan.mukhtar.vm.MainViewModel

@Composable
fun SetupScreen(viewModel: MainViewModel, onDone: () -> Unit) {
    var governorate by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var region by rememberSaveable { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Icon(
                Icons.Filled.Home, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("مختار المنطقة", style = MaterialTheme.typography.headlineSmall)
            Text(
                "الإعداد الأولي - يرجى إدخال معلومات منطقتك",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.height(28.dp))

            GovernorateDropdown(
                value = governorate,
                onValueChange = { governorate = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("القضاء / الناحية") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("اسم المنطقة / الحي") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (showError) {
                Spacer(Modifier.height(8.dp))
                Text("يرجى ملء جميع الحقول", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (governorate.isBlank() || district.isBlank() || region.isBlank()) {
                        showError = true
                    } else {
                        viewModel.saveConfig(governorate, district, region)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("حفظ") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernorateDropdown(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("المحافظة") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            StaticData.iraqiGovernorates.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = { onValueChange(item); expanded = false }
                )
            }
        }
    }
}
