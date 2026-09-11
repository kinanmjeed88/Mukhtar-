package com.kinan.mukhtar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.kinan.mukhtar.ui.screens.*
import com.kinan.mukhtar.vm.MainViewModel

/** شاشات التطبيق - تنقل بسيط بالحالة بدلاً من NavHost المتداخل */
private sealed interface Screen {
    data object Setup : Screen
    data object Main : Screen
    data class PersonEdit(val personId: Long) : Screen
}

sealed class BottomTab(val label: String, val icon: ImageVector) {
    data object Individuals : BottomTab("الأفراد", Icons.Filled.Person)
    data object Families : BottomTab("العوائل", Icons.Filled.Group)
    data object Residency : BottomTab("تأييد سكن", Icons.Filled.Description)
    data object Settings : BottomTab("الإعدادات", Icons.Filled.Settings)

    companion object { val all = listOf(Individuals, Families, Residency, Settings) }
}

@Composable
fun AppRoot(viewModel: MainViewModel) {
    val setupComplete by viewModel.isSetupComplete.collectAsState()

    // تُحسب مرة واحدة فقط ولا تتغير بعد ذلك
    var screen by remember { mutableStateOf<Screen?>(null) }

    LaunchedEffect(setupComplete) {
        if (screen == null && setupComplete != null) {
            screen = if (setupComplete == true) Screen.Main else Screen.Setup
        }
    }

    when (val current = screen) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is Screen.Setup -> SetupScreen(viewModel) { screen = Screen.Main }

        is Screen.Main -> MainScaffold(
            viewModel = viewModel,
            onOpenPersonEditor = { id -> screen = Screen.PersonEdit(id) }
        )

        is Screen.PersonEdit -> AddEditPersonScreen(
            viewModel = viewModel,
            personId = current.personId,
            onDone = { screen = Screen.Main }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: MainViewModel, onOpenPersonEditor: (Long) -> Unit) {
    val config by viewModel.config.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it.text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = config?.headerLine?.takeIf { it.isNotBlank() } ?: "مختار المنطقة",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                BottomTab.all.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> PeopleListScreen(viewModel, familiesOnly = false, onEdit = onOpenPersonEditor)
                1 -> PeopleListScreen(viewModel, familiesOnly = true, onEdit = onOpenPersonEditor)
                2 -> ResidencyScreen(viewModel)
                else -> SettingsScreen(viewModel)
            }
        }
    }
}
