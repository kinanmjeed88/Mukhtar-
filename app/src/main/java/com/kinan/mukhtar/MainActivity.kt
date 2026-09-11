package com.kinan.mukhtar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.kinan.mukhtar.ui.AppRoot
import com.kinan.mukhtar.ui.theme.MukhtarTheme
import com.kinan.mukhtar.util.CrashLogger
import com.kinan.mukhtar.vm.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        CrashLogger.install(this)
        super.onCreate(savedInstanceState)
        setContent {
            val dark by viewModel.darkMode.collectAsState()
            MukhtarTheme(darkTheme = dark) {
                // فرض اتجاه الواجهة من اليمين إلى اليسار في كل التطبيق
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(viewModel)
                    CrashReportDialog()
                }
            }
        }
    }
}

/** يعرض تفاصيل آخر انهيار عند إعادة فتح التطبيق */
@Composable
private fun CrashReportDialog() {
    val context = LocalContext.current
    var crash by remember { mutableStateOf(CrashLogger.read(context)) }

    crash?.let { text ->
        AlertDialog(
            onDismissRequest = { },
            title = { Text("تقرير خطأ سابق") },
            text = {
                Text(
                    text = text.take(3000),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    CrashLogger.clear(context)
                    crash = null
                }) { Text("حسناً") }
            }
        )
    }
}
