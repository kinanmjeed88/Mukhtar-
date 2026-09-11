package com.kinan.mukhtar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.LayoutDirection
import com.kinan.mukhtar.ui.AppRoot
import com.kinan.mukhtar.ui.theme.MukhtarTheme
import com.kinan.mukhtar.vm.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark by viewModel.darkMode.collectAsState()
            MukhtarTheme(darkTheme = dark) {
                // فرض اتجاه الواجهة من اليمين إلى اليسار في كل التطبيق
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(viewModel)
                }
            }
        }
    }
}
