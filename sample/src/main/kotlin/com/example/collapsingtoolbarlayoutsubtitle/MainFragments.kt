package com.example.collapsingtoolbarlayoutsubtitle

import android.os.Bundle
import androidx.annotation.ArrayRes
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class AbstractMainTextFragment : PreferenceFragmentCompat() {
    @get:XmlRes abstract val xml: Int
    abstract val text: String
    abstract val lineSpacingAdd: String
    abstract val lineSpacingMultiplier: String
    abstract val hyphenationFrequencies: String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(xml)
        bindSummary<EditTextPreference, String>(text, { text })
        bindSummary<ListPreference, String>(lineSpacingAdd, { value })
        bindSummary<ListPreference, String>(lineSpacingMultiplier, { value })
        bindSummary<ListPreference, String>(hyphenationFrequencies, { value }) {
            getActualString(
                it,
                R.array.hyphenation_frequency_values,
                R.array.hyphenation_frequencies
            )
        }
    }
}

class MainTitleFragment : AbstractMainTextFragment() {
    override val xml: Int = R.xml.fragment_main_title
    override val text: String = "titleText"
    override val lineSpacingAdd: String = "titleLineSpacingAdd"
    override val lineSpacingMultiplier: String = "titleLineSpacingMultiplier"
    override val hyphenationFrequencies: String = "titleHyphenationFrequencies"
}

class MainSubtitleFragment : AbstractMainTextFragment() {
    override val xml: Int = R.xml.fragment_main_subtitle
    override val text: String = "subtitleText"
    override val lineSpacingAdd: String = "subtitleLineSpacingAdd"
    override val lineSpacingMultiplier: String = "subtitleLineSpacingMultiplier"
    override val hyphenationFrequencies: String = "subtitleHyphenationFrequencies"
}

class MainOthersFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_main_others)
        bindSummary<ListPreference, String>("collapseMode", { value }) {
            getActualString(it, R.array.collapse_mode_values, R.array.collapse_modes)
        }
        bindSummary<MultiSelectListPreference, Set<String>>("collapsedGravity", { values }) { set ->
            set.joinToString(" | ") {
                getActualString(it, R.array.gravity_values, R.array.gravities)
            }
        }
        bindSummary<MultiSelectListPreference, Set<String>>("expandedGravity", { values }) { set ->
            set.joinToString(" | ") {
                getActualString(it, R.array.gravity_values, R.array.gravities)
            }
        }
    }
}

/**
 * @param initial starting value can be obtained from its value, text, etc.
 * @param convert its preference value to representable summary text.
 */
fun <P : Preference, T> PreferenceFragmentCompat.bindSummary(
    key: String,
    initial: P.() -> T?,
    convert: (T) -> CharSequence? = { it.toString() }
) {
    val preference = findPreference<P>(key)!!
    preference.initial()?.let { preference.summary = convert(it) }
    preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
        @Suppress("UNCHECKED_CAST")
        preference.summary = convert(newValue as T)
        true
    }
}

/** Obtain string value from list preference entries. */
fun Fragment.getActualString(
    s: CharSequence,
    @ArrayRes arrayValuesRes: Int,
    @ArrayRes arraysRes: Int
): CharSequence {
    val arrayValues = resources.getStringArray(arrayValuesRes)
    val arrays = resources.getStringArray(arraysRes)
    return arrays[arrayValues.indexOf(s)]
}
