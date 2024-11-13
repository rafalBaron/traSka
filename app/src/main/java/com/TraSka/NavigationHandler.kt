package com.TraSka

import LoginScreen
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.TraSka.com.TraSka.HomeScreen
import com.TraSka.com.TraSka.RegisterScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(viewModel: LocationViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = ScreenFlowHandler.StartScreen.route) {
        composable("login_screen",
            exitTransition = {
                fadeOut(animationSpec = tween(500))
                /*slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )*/
            }) { LoginScreen(navController, viewModel) }
        composable("register_screen",
            enterTransition = {
                fadeIn(animationSpec = tween(500))
                /*slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(500)
                )*/
            },
            exitTransition = {
                fadeOut(animationSpec = tween(500))
                /*slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )*/
            }) { RegisterScreen(navController, viewModel) }
        composable(
            "home_screen",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            /*exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            }*/
        ) {

            BackHandler(true) {
                Log.i("LOG_TAG", "Clicked back, do nothing! There is no back!")
            }

            HomeScreen(navController, viewModel)
        }
        composable(
            "register_successful_screen",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            /*exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }*/
        ) {

            BackHandler(true) {
                Log.i("LOG_TAG", "Clicked back, do nothing! There is no back!")
            }

            RegisterSuccessfulScreen(navController)
        }
        composable(
            "register_error_screen",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            /*exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }*/
        ) {

            BackHandler(true) {
                Log.i("LOG_TAG", "Clicked back, do nothing! There is no back!")
            }

            RegisterErrorScreen(navController)
        }

        composable(
            "route_planner_screen",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            /*exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }*/
        ) {

            RoutePlannerScreen(navController, viewModel)
        }

        composable("saved_routes_screen",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            }) {

            SavedRoutesScreen(navController, viewModel)
        }

        composable(
            "start_screen",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            },
            /*exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }*/
        ) {

            StartScreen(navController, viewModel)
        }
    }
}

