package com.framepayments.framesdk.configurations

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object SecureConfigurationStorage {
    private const val PREFS_NAME = "config_store"

    fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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