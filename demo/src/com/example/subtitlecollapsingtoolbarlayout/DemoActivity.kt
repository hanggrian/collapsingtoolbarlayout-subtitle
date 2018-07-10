package com.example.subtitlecollapsingtoolbarlayout

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        setSupportActionBar(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, DemoFragment())
            .commitNow()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(this)
        toolbarLayout.isTitleEnabled = preferences.getBoolean(PREFERENCE_TITLE_ENABLED, true)
        toolbarLayout.title = preferences.getString(PREFERENCE_TITLE, null)
        toolbarLayout.subtitle = preferences.getString(PREFERENCE_SUBTITLE, null)
        toolbarLayout.setScrimsShown(preferences.getBoolean(PREFERENCE_SCRIMS_SHOWN, true))
    }

    override fun onSharedPreferenceChanged(
        preferences: SharedPreferences,
        key: String
    ) = when (key) {
        PREFERENCE_TITLE_ENABLED -> toolbarLayout.isTitleEnabled = preferences.getBoolean(key, true)
        PREFERENCE_TITLE -> toolbarLayout.title = preferences.getString(key, null)
        PREFERENCE_SUBTITLE -> toolbarLayout.subtitle = preferences.getString(key, null)
        PREFERENCE_SCRIMS_SHOWN-> toolbarLayout.setScrimsShown(preferences.getBoolean(key, true))
        else -> {

        }
    }
}