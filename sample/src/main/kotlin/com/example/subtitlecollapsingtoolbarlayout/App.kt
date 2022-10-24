package com.example.subtitlecollapsingtoolbarlayout

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.hendraanggrian.auto.prefs.android.preferences

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(
            preferences.getInt(
                "theme",
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        )
    }
}
