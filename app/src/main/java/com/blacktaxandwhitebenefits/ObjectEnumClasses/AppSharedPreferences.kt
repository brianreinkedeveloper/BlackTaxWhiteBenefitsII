package com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses

import android.content.Context
import android.content.Context.MODE_PRIVATE

object AppSharedPreferences {
    // SharedPreferences constants
    const val SHAREDPREFFILE = "BLACKTAXWHITEBENEFITS_SHAREDPREF_FILE"
    const val SHAREDPREF_BLOGTITLE = "SHAREDPREF_BLOGTITLE"

    // Gets app Shared Preferences
    fun setAppSharedPreferencesAsync(context: Context, prefvar: String, getSetValue: Any) {
        context.getSharedPreferences(SHAREDPREFFILE, MODE_PRIVATE).edit().apply {
            if (getSetValue is Int) {
                putInt(prefvar, getSetValue.toInt())
                apply()
            }
        }
    }

    fun setAppSharedPreferencesSync(context: Context, prefvar: String, getSetValue: String) {
        context.getSharedPreferences(SHAREDPREFFILE, MODE_PRIVATE).edit().apply {
            putString(prefvar, getSetValue)
            commit()
        }
    }

    fun getAppSharedPreferences(context: Context, prefvar: String): String? {
        // If shared preference is not found assign an empty string.
        val valueFromSharedPref = context.getSharedPreferences(SHAREDPREFFILE, MODE_PRIVATE)
            .getString(prefvar, "")
        return valueFromSharedPref
    }

    fun getAppSharedPreferencesInt(context: Context, prefvar: String): Int {
        // If shared preference is not found assign an empty string.
        val valueFromSharedPref = context.getSharedPreferences(SHAREDPREFFILE, MODE_PRIVATE)
            .getInt(prefvar, 0)
        return valueFromSharedPref
    }
}