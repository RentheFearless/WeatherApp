package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.data.AppContainer

/** Клас застосунку: тримає один AppContainer з базою, мережею та репозиторіями. */
class WeatherApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
