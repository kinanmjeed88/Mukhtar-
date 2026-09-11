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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.kinan.mukhtar.ui.screens.*
import com.kinan.mukhtar.vm.MainViewModel

object Routes {
    const val SETUP = "setup"
    const val MAIN = "main"
    const val PERSON_EDIT = "person_edit"
}

sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    data object Individuals : BottomTab("tab_individuals", "الأفراد", Icons.Filled.Person)
    data object Families : BottomTab("tab_families", "العوائل", Icons.Filled.Group)
    data object Residency : BottomTab("tab_residency", "تأييد سكن", Icons.Filled.Description)
    data object Settings : BottomTab("tab_settings", "الإعدادات", Icons.Filled.Settings)

    companion object { val all = listOf(Individuals, Families, Residency, Settings) }
}

@Composable
fun AppRoot(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val setupComplete by viewModel.isSetupComplete.collectAsState()

    // يجب تثبيت وجهة البداية مرة واحدة فقط.
    // تغييرها بعد إنشاء NavHost يعيد بناء الرسم البياني ويسبب انهيار التطبيق.
    val startDestination = rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(setupComplete) {
        if (startDestination.value == null && setupComplete != null) {
            startDestination.value = if (setupComplete == true) Routes.MAIN else Routes.SETUP
        }
    }

    val start = startDestination.value
    if (start == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.SETUP) {
            SetupScreen(viewModel) {
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.SETUP) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        composable(Routes.MAIN) { MainScaffold(viewModel, navController) }
        composable(
            route = "${Routes.PERSON_EDIT}/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { entry ->
            AddEditPersonScreen(
                viewModel = viewModel,
                personId = entry.arguments?.getLong("personId") ?: 0L,
                onDone = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: MainViewModel, navController: NavHostController) {
    val innerNav = rememberNavController()
    val config by viewModel.config.collectAsState()
    val backStack by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: BottomTab.Individuals.route
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
                BottomTab.all.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                innerNav.navigate(tab.route) {
                                    popUpTo(innerNav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = BottomTab.Individuals.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomTab.Individuals.route) {
                PeopleListScreen(viewModel, familiesOnly = false) { id ->
                    navController.navigate("${Routes.PERSON_EDIT}/$id")
                }
            }
            composable(BottomTab.Families.route) {
                PeopleListScreen(viewModel, familiesOnly = true) { id ->
                    navController.navigate("${Routes.PERSON_EDIT}/$id")
                }
            }
            composable(BottomTab.Residency.route) { ResidencyScreen(viewModel) }
            composable(BottomTab.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}
