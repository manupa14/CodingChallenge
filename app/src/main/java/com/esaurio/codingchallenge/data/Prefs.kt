package com.esaurio.codingchallenge.data

import android.content.Context
import android.content.SharedPreferences
import com.esaurio.codingchallenge.MyApplication

class Prefs {
    companion object {
        val sharedInstance : Prefs by lazy { Prefs() }

        private const val KEY_AUTH_TOKEN = "AuthorizationToken"
        private const val KEY_REFRESH_TOKEN = "RefreshToken"
        private const val KEY_TOKEN_EXPIRED = "TokenExpired"
        private const val KEY_USER_EMAIL = "UserEmail"
    }

    private val preferences : SharedPreferences by lazy {
        MyApplication.instance.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
    }

    fun saveAuthTokens(auth : String?, refresh : String?) {
        preferences.edit()
            .putString(KEY_AUTH_TOKEN, auth)
            .putString(KEY_REFRESH_TOKEN, refresh)
            .putBoolean(KEY_TOKEN_EXPIRED, false)
            .apply()
    }

    fun logout(){
        userEmail = null
        saveAuthTokens(null, null)
    }

    var userEmail : String?
        get() = preferences.getString(KEY_USER_EMAIL, null)
        set(value) {
            preferences.edit()
                .putString(KEY_USER_EMAIL, value)
                .apply()
        }

    val authorizationToken: String?
        get() {
            return preferences.getString(KEY_AUTH_TOKEN, null)
        }

    val refreshToken : String?
        get() {
            return preferences.getString(KEY_REFRESH_TOKEN, null)
        }

    var isTokenExpired : Boolean
        get() = preferences.getBoolean(KEY_TOKEN_EXPIRED, false)
        set(value) {
            preferences.edit()
                .putBoolean(KEY_TOKEN_EXPIRED, value)
                .apply()
        }

}