package com.hybridApp.sample.util

import android.content.Context

class PrefManager(
    val context: Context
) {
    private val pref = context.getSharedPreferences("pref_belleforet", Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String?): String? {
        return pref.getString(key, defValue)?.toString()
    }

    fun setString(key: String, value: String) {
        pref.edit().putString(key, value).apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return pref.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, value: Boolean) {
        pref.edit().putBoolean(key, value).apply()
    }
}