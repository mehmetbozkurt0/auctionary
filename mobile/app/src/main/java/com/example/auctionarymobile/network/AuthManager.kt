package com.example.auctionarymobile.network

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.autofill.ContentType

object AuthManager {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("auctionary_prefs", Context.MODE_PRIVATE)
        }
    }

    fun saveUser(username: String, token: String) {
        prefs?.edit()?.putString("username",username)?.putString("token",token)?.commit()
    }

    fun getUsername(): String? = prefs?.getString("username",null)
    fun getToken(): String? = prefs?.getString("token",null)

    fun clearUser(){
        prefs?.edit()?.clear()?.commit()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}