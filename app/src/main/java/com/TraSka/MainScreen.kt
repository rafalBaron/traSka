package com.TraSka

import LoginScreen
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.TraSka.com.TraSka.AccountScreen
import com.TraSka.com.TraSka.HomeScreen
import com.TraSka.com.TraSka.RegisterScreen
import com.TraSka.ui.theme.TraSkaTheme

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(viewModel: LocationViewModel) {
    val isLoggingLoading by viewModel.isLoggingLoading.observeAsState(true)
    val navController = rememberNavController()

    val accountTab = TabBarItem(
        title = "Account",
        screenName = "account_screen",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )
    val homeTab = TabBarItem(
        title = "Home",
        screenName = "home_screen",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )
    val routePlannerTab = TabBarItem(
        title = "Route planner",
        screenName = "route_planner_screen",
        selectedIcon = Icons.Filled.Create,
        unselectedIcon = Icons.Outlined.Create
    )

    val tabBarItems = listOf(accountTab, homeTab, routePlannerTab)

    if (isLoggingLoading) {
        LoadingScreen()
    } else {
        val startDestination = if (viewModel.isLogged) {
            ScreenFlowHandler.HomeScreen.route
        } else {
            ScreenFlowHandler.StartScreen.route
        }
        TraSkaTheme {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val screenTitle = when (currentRoute) {
                    "home_screen" -> "Home"
                    "account_screen" -> "Account"
                    "route_planner_screen" -> "Route planner"
                    else -> ""
                }

                Scaffold(
                    modifier = Modifier.background(color = Color(0xFF222831)),
                    topBar = {
                        if (viewModel.isLogged && currentRoute !in listOf(
                                "login_screen", "register_screen"
                            )
                        ) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Column(
                                        modifier = Modifier.fillMaxHeight(),
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(
                                            text = screenTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color(0xFF222831),
                                    titleContentColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(50.dp)
                                    .shadow(10.dp)
                            )
                        }
                    },
                    bottomBar = {
                        if (viewModel.isLogged && currentRoute !in listOf(
                                "login_screen", "register_screen"
                            )
                        ) {
                            TabView(tabBarItems, navController)
                        }
                    }) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = {
                            fadeIn(tween(100))
                        },
                        exitTransition = {
                            fadeOut(tween(100))
                        }) {
                        composable("login_screen") { LoginScreen(navController, viewModel) }
                        composable("register_screen") {
                            RegisterScreen(
                                navController,
                                viewModel
                            )
                        }
                        composable("home_screen") {
                            BackHandler(true) {
                                Log.i(
                                    "LOG_TAG",
                                    "Clicked back, do nothing! There is no back!"
                                )
                            }
                            HomeScreen(navController, viewModel)
                        }
                        composable("account_screen") {
                            AccountScreen(
                                navController,
                                viewModel
                            )
                        }
                        composable("register_successful_screen") {
                            BackHandler(true) {
                                Log.i(
                                    "LOG_TAG",
                                    "Clicked back, do nothing! There is no back!"
                                )
                            }
                            RegisterSuccessfulScreen(navController)
                        }
                        composable("register_error_screen") {
                            BackHandler(true) {
                                Log.i(
                                    "LOG_TAG",
                                    "Clicked back, do nothing! There is no back!"
                                )
                            }
                            RegisterErrorScreen(navController)
                        }
                        composable("route_planner_screen") {
                            RoutePlannerScreen(
                                navController,
                                viewModel
                            )
                        }
                        composable("saved_routes_screen") {
                            SavedRoutesScreen(
                                navController,
                                viewModel
                            )
                        }
                        composable("start_screen") { StartScreen(navController, viewModel) }
                    }
                }
            }
        }
    }
}

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(1)
    }

    NavigationBar(
        containerColor = Color(0xFF222831),
        contentColor = Color.White,
        modifier = Modifier
            .height(55.dp)
            .shadow(10.dp, ambientColor = Color.White, spotColor = Color.White)
    ) {
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                modifier = Modifier.padding(top = 10.dp),
                selected = selectedTabIndex == index, onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.screenName)
                }, icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount,
                    )
                }, label = {
                    Text(
                        tabBarItem.title,
                        modifier = Modifier.offset(y = (-10).dp),
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF0D99FF),
                    unselectedIconColor = Color.White,
                    selectedTextColor = Color(0xFF0D99FF),
                    unselectedTextColor = Color.White,
                )
            )
        }
    }
}

@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = if (isSelected) {
                selectedIcon
            } else {
                unselectedIcon
            }, contentDescription = title
        )
    }
}

@Composable
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}

@Composable
fun LoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF222831)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}