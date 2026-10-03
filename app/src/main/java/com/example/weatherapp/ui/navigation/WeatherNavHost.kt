package com.example.weatherapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.weatherapp.ui.screens.ResultsScreen
import com.example.weatherapp.ui.screens.SearchScreen

private const val ANIM_MS = 350

@Composable
fun WeatherNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SearchRoute,
        modifier = modifier,
        // Slide-in анімація: новий екран виїжджає справа, старий іде вліво
        enterTransition = {
            slideIntoContainer(SlideDirection.Start, tween(ANIM_MS)) + fadeIn(tween(ANIM_MS))
        },
        exitTransition = {
            slideOutOfContainer(SlideDirection.Start, tween(ANIM_MS)) + fadeOut(tween(ANIM_MS))
        },
        // Кнопка «Назад»: рух у зворотний бік
        popEnterTransition = {
            slideIntoContainer(SlideDirection.End, tween(ANIM_MS)) + fadeIn(tween(ANIM_MS))
        },
        popExitTransition = {
            slideOutOfContainer(SlideDirection.End, tween(ANIM_MS)) + fadeOut(tween(ANIM_MS))
        }
    ) {
        composable<SearchRoute> {
            SearchScreen(
                onSearch = { city -> navController.navigate(ResultsRoute(city)) }
            )
        }

        composable<ResultsRoute> { backStackEntry ->
            // toRoute() відновлює типізований об'єкт маршруту з аргументами
            val route: ResultsRoute = backStackEntry.toRoute()
            ResultsScreen(
                city = route.city,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
