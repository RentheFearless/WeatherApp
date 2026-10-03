package com.example.weatherapp.ui.navigation

import kotlinx.serialization.Serializable

// Type-Safe маршрути (Navigation Compose 2.8+).
// Кожен екран — окремий @Serializable тип, аргументи — поля класу.

/** Екран пошуку міста (стартовий, без аргументів). */
@Serializable
object SearchRoute

/** Екран результатів. Назва міста передається як типізований аргумент. */
@Serializable
data class ResultsRoute(val city: String)
