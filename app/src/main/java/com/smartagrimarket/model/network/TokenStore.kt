package com.smartagrimarket.model.network

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object TokenStore {
    private const val PREFS_NAME = "agri_session"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER = "auth_user"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    /**
     * Called once from MainActivity.onCreate before any UI is composed.
     */
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    var token: String?
        get() = if (::prefs.isInitialized) prefs.getString(KEY_TOKEN, null) else null
        set(value) {
            if (!::prefs.isInitialized) return
            prefs.edit().putString(KEY_TOKEN, value).apply()
        }

    var currentUser: UserDto?
        get() {
            if (!::prefs.isInitialized) return null
            val json = prefs.getString(KEY_USER, null) ?: return null
            return try {
                gson.fromJson(json, UserDto::class.java)
            } catch (_: Exception) {
                null
            }
        }
        set(value) {
            if (!::prefs.isInitialized) return
            val json = if (value == null) null else gson.toJson(value)
            prefs.edit().putString(KEY_USER, json).apply()
        }

    /**
     * Clears the persisted session. Call on logout and account deletion.
     */
    fun clear() {
        if (!::prefs.isInitialized) return
        prefs.edit().clear().apply()
    }

    /** True when both a token and user are stored. */
    val hasSession: Boolean
        get() = token != null && currentUser != null
}