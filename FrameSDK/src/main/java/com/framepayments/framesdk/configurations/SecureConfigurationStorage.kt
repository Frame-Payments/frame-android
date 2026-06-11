package com.framepayments.framesdk.configurations

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Provides encrypted persistent storage for SDK configuration values using
 * [EncryptedSharedPreferences] backed by AES256-GCM keys.
 *
 * All values are serialized to JSON before encryption and deserialized on retrieval, so any
 * serializable type can be stored.
 */
object SecureConfigurationStorage {
    private const val PREFS_NAME = "config_store_encrypted"

    /**
     * Returns an [EncryptedSharedPreferences] instance backed by an AES256-GCM master key.
     *
     * @param context The Android context used to access the application's key store and storage.
     * @return A [SharedPreferences] whose keys and values are encrypted at rest.
     */
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

    /**
     * Serializes [value] to JSON and writes it to encrypted storage under [key].
     *
     * @param T The type of the value to store.
     * @param context The Android context used to access encrypted storage.
     * @param key The storage key under which the value is saved.
     * @param value The value to serialize and persist.
     */
    fun <T> save(context: Context, key: String, value: T) {
        val json = Gson().toJson(value)
        prefs(context).edit { putString(key, json) }
    }

    /**
     * Reads and deserializes the value stored under [key] into type [T].
     *
     * @param T The expected type to deserialize the stored JSON into.
     * @param context The Android context used to access encrypted storage.
     * @param key The storage key to look up.
     * @return The deserialized value, or `null` if the key is absent or deserialization fails.
     */
    inline fun <reified T> retrieve(context: Context, key: String): T? {
        val json = prefs(context).getString(key, null) ?: return null
        val type = object : TypeToken<T>() {}.type
        return runCatching { Gson().fromJson<T>(json, type) }.getOrNull()
    }

    /**
     * Reads the raw encrypted JSON string stored under [key] without deserializing it.
     *
     * @param context The Android context used to access encrypted storage.
     * @param key The storage key to look up.
     * @return The raw JSON string, or `null` if the key is absent.
     */
    fun getRaw(context: Context, key: String): String? =
        prefs(context).getString(key, null)

    /**
     * Removes the entry stored under [key] from encrypted storage.
     *
     * @param context The Android context used to access encrypted storage.
     * @param key The storage key to remove.
     */
    fun remove(context: Context, key: String) {
        prefs(context).edit { remove(key) }
    }
}