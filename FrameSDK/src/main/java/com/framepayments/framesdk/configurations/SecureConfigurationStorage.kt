package com.framepayments.framesdk.configurations

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SecureConfigurationStorage {
    private const val PREFS_NAME = "config_store_encrypted"

    fun prefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun <T> save(context: Context, key: String, value: T) {
        val json = Gson().toJson(value)
        prefs(context).edit { putString(key, json) }
    }

    inline fun <reified T> retrieve(context: Context, key: String): T? {
        val json = prefs(context).getString(key, null) ?: return null
        val type = object : TypeToken<T>() {}.type
        return runCatching { Gson().fromJson<T>(json, type) }.getOrNull()
    }

    fun getRaw(context: Context, key: String): String? =
        prefs(context).getString(key, null)

    fun remove(context: Context, key: String) {
        prefs(context).edit { remove(key) }
    }
}