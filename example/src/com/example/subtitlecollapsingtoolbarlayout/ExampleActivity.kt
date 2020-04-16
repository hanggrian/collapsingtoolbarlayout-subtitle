package com.example.subtitlecollapsingtoolbarlayout

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.hendraanggrian.prefy.BindPreference
import com.hendraanggrian.prefy.PreferencesSaver
import com.hendraanggrian.prefy.Prefy
import com.hendraanggrian.prefy.android.AndroidPreferences
import com.hendraanggrian.prefy.android.get
import com.hendraanggrian.prefy.bind
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.android.synthetic.main.activity_example.*

class ExampleActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    @JvmField @BindPreference("theme") var theme2 = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    private lateinit var preferences: AndroidPreferences
    private lateinit var saver: PreferencesSaver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        setSupportActionBar(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, ExampleFragment())
            .commitNow()
        preferences = Prefy[this]
        saver = preferences.bind(this)
    }

    override fun onResume() {
        super.onResume()
        preferences.setChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferences.removeChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_example, menu)
        menu.findItem(
            when (theme2) {
                AppCompatDelegate.MODE_NIGHT_NO -> R.id.lightThemeItem
                AppCompatDelegate.MODE_NIGHT_YES -> R.id.darkThemeItem
                else -> R.id.defaultThemeItem
            }
        ).isChecked = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.defaultThemeItem, R.id.lightThemeItem, R.id.darkThemeItem -> {
                theme2 = when (item.itemId) {
                    R.id.lightThemeItem -> AppCompatDelegate.MODE_NIGHT_NO
                    R.id.darkThemeItem -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                saver.save()
                AppCompatDelegate.setDefaultNightMode(theme2)
            }
            R.id.resetItem -> {
                preferences.edit { clear() }
                ProcessPhoenix.triggerRebirth(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(preferences: Preferences, key: String) {
    }
}