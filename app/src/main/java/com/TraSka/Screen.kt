package com.TraSka

sealed class Screen(val route: String) {
    object WelcomeScreen : Screen("welcome_screen")
    object RegisterScreen : Screen("register_screen")
    object HomeScreenLogged : Screen("home_screen_logged")
    object RegisterSuccessful : Screen("register_successful")
    object RegisterError : Screen("register_error")
    object RoutePlanner : Screen("route_planner")
    object LocationScreen : Screen("route_location")
    object SavedRoutesScreen : Screen("saved_routes")
    object StartScreen : Screen("start_screen")
}
