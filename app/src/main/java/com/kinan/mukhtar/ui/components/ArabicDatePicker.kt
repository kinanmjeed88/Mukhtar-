package com.kinan.mukhtar.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import com.kinan.mukhtar.util.DateUtils

/** حقل اختيار تاريخ بصيغة يوم/شهر/سنة */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArabicDateField(
    label: String,
    value: Long?,
    modifier: Modifier = Modifier,
    onValueChange: (Long) -> Unit
) {
    var show by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = value?.takeIf { it > 0 })

    OutlinedTextField(
        value = DateUtils.format(value),
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.fillMaxWidth().clickableField { show = true }
    )

    if (show) {
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onValueChange)
                    show = false
                }) { Text("اختيار") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("إلغاء") } }
        ) {
            DatePicker(state = state, title = { Text("  $label", Modifier) })
        }
    }
}

private fun Modifier.clickableField(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
