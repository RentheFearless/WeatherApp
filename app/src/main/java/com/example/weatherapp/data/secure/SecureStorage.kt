@file:Suppress("DEPRECATION") // EncryptedSharedPreferences позначено deprecated, але воно потрібне за завданням

package com.example.weatherapp.data.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Зашифроване сховище для секретів (API-ключ OpenWeatherMap).
 * Ключі шифруються AES-256-SIV, значення — AES-256-GCM.
 * Майстер-ключ лежить в Android Keystore і не покидає пристрій.
 */
class SecureStorage(context: Context) {

    private val prefs: SharedPreferences = create(context.applicationContext)

    fun getApiKey(): String? = prefs.getString(KEY_API, null)

    fun saveApiKey(key: String) = prefs.edit { putString(KEY_API, key) }

    fun clearApiKey() = prefs.edit { remove(KEY_API) }

    companion object {
        private const val FILE_NAME = "secure_prefs"
        private const val KEY_API = "openweather_api_key"

        private fun create(context: Context): SharedPreferences {
            fun build(): SharedPreferences {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                return EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
            // Якщо файл пошкоджений (наприклад, після перевстановлення) — створюємо заново
            return try {
                build()
            } catch (e: Exception) {
                context.deleteSharedPreferences(FILE_NAME)
                build()
            }
        }
    }
}
