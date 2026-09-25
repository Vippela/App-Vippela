package br.com.vippela.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import br.com.vippela.data.DemoState
import br.com.vippela.ui.components.Header
import br.com.vippela.ui.screens.*
import br.com.vippela.ui.theme.*
import kotlinx.coroutines.launch

private val mainRoutes = listOf("home", "learn", "reports", "profile")
private val authRoutes =
    setOf("welcome", "access", "register-choice", "login", "register", "recover")
private val parentRoutes = setOf("family", "add-member", "limits", "requests", "plan")

@Composable
fun VippelaApp(state: DemoState = viewModel()) {
    VippelaTheme(dark = state.darkMode) { VippelaContent(state) }
}

@Composable
private fun VippelaContent(state: DemoState) {
    var registrationRole by rememberSaveable {
        mutableStateOf(br.com.vippela.data.Role.RESPONSAVEL)
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: "welcome"
    val go: (String) -> Unit = { destination ->
        if (destination in mainRoutes)
            nav.navigate(destination) {
                popUpTo("home") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        else nav.navigate(destination) { launchSingleTop = true }
    }
    val back: () -> Unit = { nav.popBackStack() }
    val enter: () -> Unit = { nav.navigate("home") { popUpTo(nav.graph.id) { inclusive = true } } }
    val logout: () -> Unit = {
        mainRoutes.forEach { nav.clearBackStack(it) }
        scope.launch { br.com.vippela.auth.GoogleSignIn(context).signOut() }
        state.logout()
        nav.navigate("login") { popUpTo(nav.graph.id) { inclusive = true } }
    }
    LaunchedEffect(route, state.role) {
        if (route !in authRoutes && state.role == null)
            nav.navigate("login") { popUpTo(nav.graph.id) { inclusive = true } }
        else if (route in parentRoutes && !state.isParent)
            nav.navigate("home") { popUpTo("home") { inclusive = true } }
    }
    val title =
        when (route) {
            "home" -> "Início"
            "learn" -> "Aprendizado"
            "reports" -> "Relatórios"
            "profile",
            "member" -> "Perfil"
            "settings" -> "Configurações"
            "family" -> "Minha família"
            "apps" -> "Aplicativos"
            "limits" -> "Limite diário"
            "requests" -> "Pedidos"
            "pair" -> "Vínculo familiar"
            "performance" -> "Desempenho"
            "goal" -> "Objetivos"
            "notifications" -> "Notificações"
            "alerts",
            "alerts/{filter}",
            "alert/{index}" -> "Alertas"
            "lesson/{index}",
            "privacy-lesson" -> "Aprendizado"
            "release",
            "release/{app}" -> "Liberação"
            "register" -> "Cadastro"
            "recover" -> "Recuperar acesso"
            "add-member" -> "Adicionar familiar"
            "edit-profile" -> "Editar perfil"
            else -> "Configurações"
        }
    Surface(Modifier.fillMaxSize(), color = Lavender) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Scaffold(
                Modifier.widthIn(max = 520.dp).fillMaxSize(),
                containerColor = Lavender,
                topBar = {
                    if (route in setOf("register", "recover"))
                        Row(
                            Modifier.statusBarsPadding().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(back) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar")
                            }
                            Text(title, style = MaterialTheme.typography.titleLarge)
                        }
                    else if (
                        route !in setOf("welcome", "access", "register-choice", "login") &&
                            !(route == "pair" && !state.isParent)
                    )
                        Header(
                            title,
                            if (route in mainRoutes) null else back,
                            { go("notifications") },
                            { go("settings") },
                        )
                },
                bottomBar = {
                    if (route in mainRoutes && state.role != null)
                        Surface(
                            Modifier.navigationBarsPadding()
                                .padding(horizontal = 28.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0),
                            ) {
                                val labels = listOf("Início", "Trilhas", "Relatório", "Perfil")
                                val icons =
                                    listOf(
                                        Icons.Outlined.Home,
                                        Icons.Outlined.School,
                                        Icons.Outlined.BarChart,
                                        Icons.Outlined.AccountCircle,
                                    )
                                mainRoutes.forEachIndexed { i, destination ->
                                    NavigationBarItem(
                                        selected = route == destination,
                                        onClick = { go(destination) },
                                        icon = { Icon(icons[i], labels[i]) },
                                        label = {
                                            Text(
                                                labels[i],
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        },
                                        colors =
                                            NavigationBarItemDefaults.colors(
                                                selectedIconColor = Orange,
                                                selectedTextColor = Orange,
                                                indicatorColor = Color(0xFFFFF1E7),
                                                unselectedIconColor =
                                                    MaterialTheme.colorScheme.onSurface,
                                                unselectedTextColor =
                                                    MaterialTheme.colorScheme.onSurface,
                                            ),
                                    )
                                }
                            }
                        }
                },
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    NavHost(
                        nav,
                        startDestination = "welcome",
                        enterTransition = {
                            fadeIn(tween(320)) + slideInHorizontally(tween(320)) { it / 4 }
                        },
                        exitTransition = {
                            fadeOut(tween(260)) + slideOutHorizontally(tween(320)) { -it / 4 }
                        },
                        popEnterTransition = {
                            fadeIn(tween(320)) + slideInHorizontally(tween(320)) { -it / 4 }
                        },
                        popExitTransition = {
                            fadeOut(tween(260)) + slideOutHorizontally(tween(320)) { it / 4 }
                        },
                    ) {
                        composable("welcome") { WelcomeScreen { nav.navigate("access") } }
                        composable("access") {
                            AccessScreen(
                                state,
                                { go("register-choice") },
                                {
                                    state.cancelGoogleRegistration()
                                    go("login")
                                },
                                enter,
                            )
                        }
                        composable("register-choice") {
                            RegistrationChoiceScreen {
                                if (state.pendingGoogle != null) {
                                    state.completeGoogleRegistration(it)
                                    enter()
                                } else {
                                    registrationRole = it
                                    go("register")
                                }
                            }
                        }
                        composable("login") {
                            LoginScreen(
                                state,
                                enter,
                                { nav.navigate("register-choice") },
                                { nav.navigate("recover") },
                            )
                        }
                        composable("register") { RegisterScreen(state, enter, registrationRole) }
                        composable("recover") { RecoverScreen(back) }
                        composable("home") { if (state.role != null) HomeScreen(state, go) }
                        composable("learn") { LearningScreen(state, go) }
                        composable("reports") { ReportsScreen(state, go) }
                        composable("profile") { ProfileScreen(state, go, logout) }
                        composable("family") { if (state.isParent) FamilyListScreen(state, go) }
                        composable("member") { MemberScreen(state, go) }
                        composable("settings") { SettingsScreen(state, go) }
                        composable("apps") { AppsScreen(state, go) }
                        composable("limits") { if (state.isParent) LimitsScreen(state, back) }
                        composable("requests") { if (state.isParent) RequestsScreen(state) }
                        composable("release") { ReleaseScreen(state, "YouTube") }
                        composable("release/{app}") {
                            ReleaseScreen(state, it.arguments?.getString("app") ?: "YouTube")
                        }
                        composable("pair") {
                            if (state.isParent) PairScreen(state) { go("home") }
                            else FamilyConnectionScreen(state, back) { go("home") }
                        }
                        composable("add-member") {
                            if (state.isParent)
                                AddMemberScreen(state) {
                                    nav.navigate("pair") {
                                        popUpTo("add-member") { inclusive = true }
                                    }
                                }
                        }
                        composable("performance") { PerformanceScreen(state, go) }
                        composable("goal") { GoalScreen(state, back) }
                        composable(
                            "lesson/{index}",
                            arguments = listOf(navArgument("index") { type = NavType.IntType }),
                        ) {
                            LessonScreen(
                                (it.arguments?.getInt("index") ?: 0).coerceIn(0, 8),
                                state,
                                back,
                            )
                        }
                        composable("privacy-lesson") { PrivacyLessonScreen(back) }
                        composable("alerts") { AlertsScreen("Todos", go) }
                        composable("alerts/{filter}") {
                            AlertsScreen(it.arguments?.getString("filter") ?: "Todos", go)
                        }
                        composable(
                            "alert/{index}",
                            arguments = listOf(navArgument("index") { type = NavType.IntType }),
                        ) {
                            AlertDetailScreen(
                                (it.arguments?.getInt("index") ?: 0).coerceIn(0, 2),
                                go,
                            )
                        }
                        composable("edit-profile") { EditProfileScreen(state, back) }
                        composable("notifications") { NotificationsScreen(state, go) }
                        composable("notification-settings") { NotificationSettingsScreen(state) }
                        listOf("account", "contact", "theme", "plan").forEach { item ->
                            composable(item) { InfoScreen(item, state) }
                        }
                    }
                }
            }
        }
    }
}
