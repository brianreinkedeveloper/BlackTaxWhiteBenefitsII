package com.sppaeknierrnairb.blacktaxandwhitebenefits.ObjectEnumClasses

import android.content.Context
import android.content.Context.MODE_PRIVATE

object AppSharedPreferences {
    // SharedPreferences constants
    const val SHAREDPREFFILE = "BLACKTAXWHITEBENEFITS_SHAREDPREF_FILE"
    const val SHAREDPREF_BLOGTITLE = "SHAREDPREF_BLOGTITLE"

    // Gets app Shared Preferences
    fun setAppSharedPreferences(context: Context, prefvar: String, getSetValue: String) {
        context.getSharedPreferences(SHAREDPREFFILE, MODE_PRIVATE).edit().apply {
            putString(prefvar, getSetValue)
            commit()
        }
    }

    fun getAppSharedPreferences(context: Context, prefvar: String): String {
        val valueFromSharedPref = context.getSharedPreferences(SHAREDPREFFILE, MODE_PRIVATE)
            .getString(prefvar, "")
        return valueFromSharedPref
    }
}