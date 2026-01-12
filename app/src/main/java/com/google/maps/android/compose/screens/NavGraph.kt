package com.google.maps.android.compose.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.maps.android.compose.client.ClientScreen
import com.google.maps.android.compose.driver.DriverScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Choose.route) {
        route(Route.Choose) {
            ChooseScreen(navigateToDriver = { navController.popAndNavigate(Route.Driver) },
                navigatetoClient = { navController.popAndNavigate(Route.Client) })
        }
        route(Route.Client) {
            ClientScreen()
        }
        route(Route.Driver) {
            DriverScreen(navigateToProfile = { navController.popAndNavigate(Route.Profile) })
        }
        route(Route.Profile) {
            ProfileScreen()
        }

    }
}


sealed class Route(val route: String) {
    data object Choose : Route("choose")
    data object Client : Route("client")
    data object Driver : Route("driver")
    data object Profile : Route("profile")
}

fun NavGraphBuilder.route(
    route: Route,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(route.route, arguments, deepLinks, content = content)
}

fun NavHostController.popAndNavigate(route: Route) {
    navigate(route.route) {
        popUpTo(route.route) {
            inclusive = true
        }
    }
}
