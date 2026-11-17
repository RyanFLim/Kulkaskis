package org.umn.kulkaskis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.umn.kulkaskis.data.FridgeItem
import org.umn.kulkaskis.data.FridgeViewModel
import org.umn.kulkaskis.data.remote.AuthViewModel
import org.umn.kulkaskis.data.remote.SyncScheduler
import org.umn.kulkaskis.notify.ExpiryScheduler
import org.umn.kulkaskis.ui.auth.AuthScreen
import org.umn.kulkaskis.ui.detail.FoodDetailScreen
import org.umn.kulkaskis.ui.edit.EditItemScreen
import org.umn.kulkaskis.ui.fridge.FridgeScreen
import org.umn.kulkaskis.ui.home.HomeScreen
import org.umn.kulkaskis.ui.profile.ProfileScreen
import org.umn.kulkaskis.ui.recipes.RecipesScreen
import org.umn.kulkaskis.ui.scan.ScanScreen
import org.umn.kulkaskis.ui.profile.AppearanceSection
import org.umn.kulkaskis.ui.theme.Kulkaskis
import org.umn.kulkaskis.ui.theme.KulkaskisTheme
import org.umn.kulkaskis.ui.theme.ThemeController
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ExpiryScheduler.createChannel(this)
        ExpiryScheduler.schedule(this)
        SyncScheduler.request(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val themeController = ThemeController(this)
        setContent {
            KulkaskisTheme(mode = themeController.mode) {
                FridgeApp(themeController)
            }
        }
    }
}

/**
 * Pindah ke tab. Kalau tab itu sudah ada di tumpukan layar, kembali ke sana
 * (layar di atasnya dibuang). Kalau belum, buka satu salinan saja.
 * Dipakai oleh bottom bar DAN tombol di dalam layar, jadi tidak pernah bentrok.
 */
fun NavHostController.goTab(route: String) {
    if (currentDestination?.route == route) return
    val returned = popBackStack(route, inclusive = false)
    if (!returned) {
        navigate(route) { launchSingleTop = true }
    }
}

@Composable
fun FridgeApp(themeController: ThemeController) {
    val nav = rememberNavController()
    val vm: FridgeViewModel = viewModel()
    val authVm: AuthViewModel = viewModel()

    val items by vm.items.collectAsState()
    val userName by authVm.displayName.collectAsState()
    val session by authVm.session.collectAsState()
    val authLoading by authVm.loading.collectAsState()
    val authMessage by authVm.message.collectAsState()

    var regName by remember { mutableStateOf("") }
    val startDestination = remember { if (authVm.session.value != null) "home" else "login" }

    val tabs = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("fridge", "Fridge", Icons.Default.Kitchen),
        Triple("scan", "Scan", Icons.Default.CameraAlt),
        Triple("recipes", "Recipes", Icons.Default.RestaurantMenu),
        Triple("profile", "Profile", Icons.Default.Person)
    )
    val backStackEntry by nav.currentBackStackEntryAsState()
    val current = backStackEntry?.destination?.route
    val showBar = current != null && current != "login" && current != "register"

    // Setelah login/register/guest berhasil, pindah ke Home dan buang layar auth dari back stack
    LaunchedEffect(session) {
        if (session != null && (current == "login" || current == "register")) {
            nav.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                nav.goTab(route)
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Kulkaskis.status.navIcon,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Kulkaskis.status.navIndicator,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(160)) },
            exitTransition = { fadeOut(tween(100)) },
            popEnterTransition = { fadeIn(tween(160)) },
            popExitTransition = { fadeOut(tween(100)) }
        ) {
            composable("login") {
                AuthScreen(
                    onGuest = { authVm.continueAsGuest() },
                    isRegister = false,
                    loading = authLoading,
                    message = authMessage,
                    onSubmit = { email, password -> authVm.signIn(email, password) },
                    onSwitch = {
                        authVm.clearMessage()
                        nav.navigate("register")
                    }
                )
            }
            composable("register") {
                AuthScreen(
                    isRegister = true,
                    loading = authLoading,
                    message = authMessage,
                    onNameChange = { regName = it },
                    onSubmit = { email, password -> authVm.signUp(email, password, regName) },
                    onSwitch = {
                        authVm.clearMessage()
                        nav.popBackStack()
                    }
                )
            }

            composable("home") {
                HomeScreen(
                    items = items,
                    userName = userName,
                    onScan = { nav.goTab("scan") },
                    onSeeAll = { nav.goTab("fridge") },
                    onItemClick = { id -> nav.navigate("detail/$id") }
                )
            }
            composable("fridge") {
                FridgeScreen(
                    items = items,
                    onItemClick = { id -> nav.navigate("detail/$id") },
                    onAdd = { nav.navigate("add") }
                )
            }
            composable("add") {
                val newItem = remember {
                    FridgeItem(
                        name = "",
                        quantity = 1,
                        expiry = LocalDate.now().plusDays(7)
                    )
                }
                EditItemScreen(
                    title = "Add Item",
                    item = newItem,
                    onSave = { created ->
                        vm.addItem(created)
                        nav.popBackStack()
                    },
                    onCancel = { nav.popBackStack() }
                )
            }
            composable("scan") {
                ScanScreen(
                    onAddItem = { vm.addItem(it) },
                    onManualAdd = { nav.navigate("add") }
                )
            }
            composable("recipes") { RecipesScreen(items = items) }
            composable("profile") {
                ProfileScreen(
                    userName = userName,
                    email = session?.email.orEmpty(),
                    items = items,
                    onSaveName = { authVm.setDisplayName(it) },
                    appearance = {
                        AppearanceSection(mode = themeController.mode, onChange = { themeController.set(it) })
                    },
                    onLogout = {
                        authVm.signOut()
                        nav.navigate("login") {
                            popUpTo(nav.graph.id) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id")
                FoodDetailScreen(
                    item = items.find { it.id == id },
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("edit/$id") },
                    onDelete = {
                        nav.popBackStack()
                        if (id != null) vm.deleteItem(id)
                    }
                )
            }
            composable(
                route = "edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id")
                EditItemScreen(
                    item = items.find { it.id == id },
                    onSave = { updated ->
                        vm.updateItem(updated)
                        nav.popBackStack()
                    },
                    onCancel = { nav.popBackStack() }
                )
            }
        }
    }
}