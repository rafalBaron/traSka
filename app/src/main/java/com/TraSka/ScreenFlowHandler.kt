package com.TraSka

sealed class ScreenFlowHandler(val route: String) {
    object LoginScreen : ScreenFlowHandler("login_screen")
    object RegisterScreen : ScreenFlowHandler("register_screen")
    object HomeScreen : ScreenFlowHandler("home_screen")
    object AccountScreen : ScreenFlowHandler("account_screen")
    object RegisterSuccessfulScreen : ScreenFlowHandler("register_successful_screen")
    object RegisterErrorScreen : ScreenFlowHandler("register_error_screen")
    object RoutePlannerScreen : ScreenFlowHandler("route_planner_screen")
    object OptimizedRouteScreen : ScreenFlowHandler("optimized_route_screen")
    object LocationScreen : ScreenFlowHandler("location_screen")
    object SavedRoutesScreen : ScreenFlowHandler("saved_routes_screen")
    object StartScreen : ScreenFlowHandler("start_screen")
}
