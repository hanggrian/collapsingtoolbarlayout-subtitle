package com.example.subtitlecollapsingtoolbarlayout

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hendraanggrian.prefy.PreferencesLogger
import com.hendraanggrian.prefy.Prefy
import com.hendraanggrian.prefy.android.Android
import com.hendraanggrian.prefy.android.get

class ExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Prefy.setLogger(PreferencesLogger.Android)
        val theme = Prefy[this].getInt("theme")
        if (theme != null && theme != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(theme)
        }
    }
}