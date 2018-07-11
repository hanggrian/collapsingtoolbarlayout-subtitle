package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class DemoFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    lateinit var titleEnabled: CheckBoxPreference
    lateinit var title: EditTextPreference
    lateinit var subtitle: EditTextPreference

    lateinit var scrimsShown: CheckBoxPreference
    lateinit var scrimColor: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_demo)
        titleEnabled = find(PREFERENCE_TITLE_ENABLED)
        title = find(PREFERENCE_TITLE) {
            summary = text
            onPreferenceChangeListener = this@DemoFragment
        }
        subtitle = find(PREFERENCE_SUBTITLE) {
            summary = text
            onPreferenceChangeListener = this@DemoFragment
        }
        scrimsShown = find(PREFERENCE_SCRIMS_SHOWN)
        scrimColor = find(PREFERENCE_SCRIM_COLOR)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        preference.summary = newValue.toString()
        return true
    }

    @Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
    private inline fun <T : Preference> find(key: CharSequence): T =
        findPreference(key) as T

    private inline fun <T : Preference> find(key: CharSequence, block: T.() -> Unit): T =
        find<T>(key).apply(block)
}