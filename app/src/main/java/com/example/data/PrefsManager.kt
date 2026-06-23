package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("xhub_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOCK_STYLE = "app_lock_style" // "4_pin", "6_pin", "pattern", "biometrics"
        private const val KEY_PIN = "app_pin"             // Digit code string
        private const val KEY_PATTERN = "app_pattern"     // Comma separated indices "0,1,2,5"
        private const val KEY_SETUP_COMPLETED = "setup_completed"
        private const val KEY_WELCOME_DISMISSED = "welcome_dismissed"
        private const val KEY_APP_THEME = "app_theme"
    }

    fun getAppTheme(): String {
        return prefs.getString(KEY_APP_THEME, "light") ?: "light"
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString(KEY_APP_THEME, theme).apply()
    }

    fun getLockStyle(): String {
        return prefs.getString(KEY_LOCK_STYLE, "4_pin") ?: "4_pin"
    }

    fun setLockStyle(style: String) {
        prefs.edit().putString(KEY_LOCK_STYLE, style).apply()
    }

    fun isSetupCompleted(): Boolean {
        return prefs.getBoolean(KEY_SETUP_COMPLETED, false)
    }

    fun setSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply()
    }

    fun getPin(): String {
        return prefs.getString(KEY_PIN, "") ?: ""
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getPattern(): String {
        return prefs.getString(KEY_PATTERN, "") ?: ""
    }

    fun setPattern(pattern: String) {
        prefs.edit().putString(KEY_PATTERN, pattern).apply()
    }

    fun isPinSet(): Boolean {
        val pin = prefs.getString(KEY_PIN, null)
        return !pin.isNullOrEmpty()
    }

    fun verifyPin(input: String): Boolean {
        val pin = prefs.getString(KEY_PIN, "")
        return pin == input
    }

    fun verifyPattern(inputPattern: String): Boolean {
        val saved = prefs.getString(KEY_PATTERN, "")
        return saved == inputPattern
    }

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN)
            .remove(KEY_PATTERN)
            .remove(KEY_LOCK_STYLE)
            .remove(KEY_SETUP_COMPLETED)
            .remove(KEY_WELCOME_DISMISSED)
            .apply()
    }

    fun isWelcomeDismissed(): Boolean {
        return prefs.getBoolean(KEY_WELCOME_DISMISSED, false)
    }

    fun setWelcomeDismissed(dismissed: Boolean) {
        prefs.edit().putBoolean(KEY_WELCOME_DISMISSED, dismissed).apply()
    }

    fun getFavoriteUrls(): Set<String> {
        return prefs.getStringSet("favorite_urls", emptySet()) ?: emptySet()
    }

    fun setFavoriteUrls(urls: Set<String>) {
        prefs.edit().putStringSet("favorite_urls", urls).apply()
    }
}
