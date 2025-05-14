package com.example

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.example.dynamic.PREFERENCE_THEME

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(
            PreferenceManager
                .getDefaultSharedPreferences(this)
                .getInt(PREFERENCE_THEME, MODE_NIGHT_FOLLOW_SYSTEM),
        )
    }
}
