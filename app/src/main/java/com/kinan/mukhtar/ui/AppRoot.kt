package com.kinan.mukhtar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kinan.mukhtar.ui.screens.*
import com.kinan.mukhtar.vm.MainViewModel
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Setup : Screen
    data object Main : Screen
    data class PersonEdit(val personId: Long) : Screen
}

/** أقسام التطبيق الستة */
enum class Section(val label: String, val icon: ImageVector) {
    INDIVIDUALS("الأفراد", Icons.Filled.Person),
    FAMILIES("العوائل", Icons.Filled.Group),
    RESIDENCY("تأييد سكن", Icons.Filled.Description),
    PHONES("أرقام الهواتف", Icons.Filled.Contacts),
    STATISTICS("الإحصائيات", Icons.Filled.BarChart),
    SETTINGS("الإعدادات", Icons.Filled.Settings)
}

@Composable
fun AppRoot(viewModel: MainViewModel) {
    val setupComplete by viewModel.isSetupComplete.collectAsState()
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
    var section by rememberSaveable { mutableStateOf(Section.INDIVIDUALS) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it.text)
            viewModel.consumeMessage()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(config?.mukhtarName.orEmpty(), config?.headerLine.orEmpty())
                Spacer(Modifier.height(8.dp))
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Section.entries.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = section == item,
                            onClick = {
                                section = item
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "القائمة")
                        }
                    },
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = config?.headerLine?.takeIf { it.isNotBlank() } ?: "مختار المنطقة",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = section.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (section) {
                    Section.INDIVIDUALS -> PeopleListScreen(viewModel, false, onOpenPersonEditor)
                    Section.FAMILIES -> PeopleListScreen(viewModel, true, onOpenPersonEditor)
                    Section.RESIDENCY -> ResidencyScreen(viewModel)
                    Section.PHONES -> PhoneDirectoryScreen(viewModel)
                    Section.STATISTICS -> StatisticsScreen(viewModel)
                    Section.SETTINGS -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader(mukhtarName: String, headerLine: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp)
    ) {
        Text(
            "مختار المنطقة",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (mukhtarName.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                mukhtarName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        if (headerLine.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                headerLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}
