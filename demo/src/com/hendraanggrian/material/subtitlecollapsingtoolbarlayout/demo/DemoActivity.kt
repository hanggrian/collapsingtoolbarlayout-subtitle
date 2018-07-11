package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        setSupportActionBar(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, DemoFragment())
            .commitNow()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this).apply {
            toolbarLayout.isTitleEnabled = getBoolean(PREFERENCE_TITLE_ENABLED, true)
            toolbarLayout.title = getString(PREFERENCE_TITLE, null)
            toolbarLayout.subtitle = getString(PREFERENCE_SUBTITLE, null)
            toolbarLayout.setScrimsShown(getBoolean(PREFERENCE_SCRIMS_SHOWN, true))
        }
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(
        preferences: Preferences,
        key: String
    ) = preferences.let {
        when (key) {
            PREFERENCE_TITLE_ENABLED -> toolbarLayout.isTitleEnabled = it.getBoolean(key, true)
            PREFERENCE_TITLE -> toolbarLayout.title = it.getString(key, null)
            PREFERENCE_SUBTITLE -> toolbarLayout.subtitle = it.getString(key, null)
            PREFERENCE_SCRIMS_SHOWN -> toolbarLayout.setScrimsShown(it.getBoolean(key, true))
            else -> ColorPickerFragment().run {
                show(supportFragmentManager, ColorPickerFragment.TAG)
                dialog.setOnDismissListener {
                    arguments?.getInt(ColorPickerFragment.TAG)?.let {
                        toolbarLayout.setContentScrimColor(it)
                    }
                }
            }
        }
    }
}