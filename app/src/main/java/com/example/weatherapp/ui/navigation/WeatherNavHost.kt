package com.example.weatherapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.weatherapp.ui.screens.ResultsScreen
import com.example.weatherapp.ui.screens.SearchScreen
import com.example.weatherapp.ui.screens.SearchViewModel

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
            val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory)
            // collectAsStateWithLifecycle: підписка на Flow лише коли екран видно
            val recentCities by viewModel.recentCities.collectAsStateWithLifecycle()
            val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()

            SearchScreen(
                recentCities = recentCities,
                apiKey = apiKey,
                onSearch = { city ->
                    viewModel.onSearch(city)                 // запис в історію (Room)
                    navController.navigate(ResultsRoute(city))
                },
                onRemoveCity = viewModel::removeCity,
                onClearHistory = viewModel::clearHistory,
                onSaveApiKey = viewModel::saveApiKey,
                onClearApiKey = viewModel::clearApiKey
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
