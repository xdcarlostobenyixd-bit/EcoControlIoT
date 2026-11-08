package com.ecocontrol.iot.security

import android.content.Context
import java.security.MessageDigest

class AuthStore(context: Context) {
    private val prefs = context.getSharedPreferences("secure_session", Context.MODE_PRIVATE)
    fun isLoggedIn() = prefs.getBoolean("logged", false)
    fun login(user: String, password: String): Boolean {
        val valid = user == "admin" && sha256(password) == "3ed40045bb2b2b90f6c2f3ccbf30d4afc3f25ae9fd760124270f6fe4b446c4a7"
        if (valid) prefs.edit().putBoolean("logged", true).putString("user", user).apply()
        return valid
    }
    fun logout() = prefs.edit().clear().apply()
    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
