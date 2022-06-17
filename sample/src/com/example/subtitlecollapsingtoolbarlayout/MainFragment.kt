package com.example.subtitlecollapsingtoolbarlayout

import android.os.Bundle
import androidx.annotation.ArrayRes
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class MainFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_main)
        findPreference<MultiSelectListPreference>("collapsedGravity")!!.bindSummary({ values }) {
            it.joinToString(" | ") { getActualString(it, R.array.gravity_values, R.array.gravities) }
        }
        findPreference<MultiSelectListPreference>("expandedGravity")!!.bindSummary({ values }) {
            it.joinToString(" | ") { getActualString(it, R.array.gravity_values, R.array.gravities) }
        }
    }

    /**
     * @param initial starting value can be obtained from its value, text, etc.
     * @param convert its preference value to representable summary text.
     */
    private fun <P : Preference, T> P.bindSummary(
        initial: P.() -> T?,
        convert: (T) -> CharSequence? = { it?.toString() }
    ) {
        initial()?.let { summary = convert(it) }
        onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            @Suppress("UNCHECKED_CAST")
            preference.summary = convert(newValue as T)
            true
        }
    }

    private fun getActualString(
        s: CharSequence,
        @ArrayRes arrayValuesRes: Int,
        @ArrayRes arraysRes: Int
    ): CharSequence {
        val arrayValues = resources.getStringArray(arrayValuesRes)
        val arrays = resources.getStringArray(arraysRes)
        return arrays[arrayValues.indexOf(s)]
    }
}
