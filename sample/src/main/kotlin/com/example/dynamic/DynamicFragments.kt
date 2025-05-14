package com.example.dynamic

import android.os.Bundle
import androidx.annotation.ArrayRes
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.R

abstract class AbstractMainTextFragment : PreferenceFragmentCompat() {
    @get:XmlRes
    abstract val xml: Int
    abstract val text: String
    abstract val lineSpacingAdd: String
    abstract val lineSpacingMultiplier: String
    abstract val hyphenationFrequencies: String
    abstract val collapsedColor: String
    abstract val expandedColor: String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(xml)
        bindSummary<EditTextPreference, String>(text, { text })
        bindSummary<ListPreference, String>(lineSpacingAdd, { value })
        bindSummary<ListPreference, String>(lineSpacingMultiplier, { value })
        bindSummary<ListPreference, String>(hyphenationFrequencies, { value }) {
            getActualString(
                it,
                R.array.hyphenation_frequency_values,
                R.array.hyphenation_frequencies,
            )
        }
        bindSummary<EditTextPreference, String>(collapsedColor, { text })
        bindSummary<EditTextPreference, String>(expandedColor, { text })
    }
}

class MainTitleFragment : AbstractMainTextFragment() {
    override val xml: Int = R.xml.fragment_main_title
    override val text: String = PREFERENCE_TITLE_TEXT
    override val lineSpacingAdd: String = PREFERENCE_TITLE_LINE_SPACING_ADD
    override val lineSpacingMultiplier: String = PREFERENCE_TITLE_LINE_SPACING_MULTIPLIER
    override val hyphenationFrequencies: String = PREFERENCE_TITLE_HYPHENATION_FREQUENCIES
    override val collapsedColor: String = PREFERENCE_TITLE_COLLAPSED_COLOR
    override val expandedColor: String = PREFERENCE_TITLE_EXPANDED_COLOR
}

class MainSubtitleFragment : AbstractMainTextFragment() {
    override val xml: Int = R.xml.fragment_main_subtitle
    override val text: String = PREFERENCE_SUBTITLE_TEXT
    override val lineSpacingAdd: String = PREFERENCE_SUBTITLE_LINE_SPACING_ADD
    override val lineSpacingMultiplier: String = PREFERENCE_SUBTITLE_LINE_SPACING_MULTIPLIER
    override val hyphenationFrequencies: String = PREFERENCE_SUBTITLE_HYPHENATION_FREQUENCIES
    override val collapsedColor: String = PREFERENCE_SUBTITLE_COLLAPSED_COLOR
    override val expandedColor: String = PREFERENCE_SUBTITLE_EXPANDED_COLOR
}

class MainOthersFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_main_others)
        bindSummary<EditTextPreference, String>(PREFERENCE_STATUS_BAR_SCRIM, { text })
        bindSummary<EditTextPreference, String>(PREFERENCE_CONTENT_SCRIM, { text })
        bindSummary<ListPreference, String>(PREFERENCE_COLLAPSE_MODE, { value }) {
            getActualString(it, R.array.collapse_mode_values, R.array.collapse_modes)
        }
        bindSummary<MultiSelectListPreference, Set<String>>(
            PREFERENCE_COLLAPSED_GRAVITY,
            { values },
        ) { set ->
            set.joinToString(" | ") {
                getActualString(it, R.array.gravity_values, R.array.gravities)
            }
        }
        bindSummary<MultiSelectListPreference, Set<String>>(
            PREFERENCE_EXPANDED_GRAVITY,
            { values },
        ) { set ->
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
    convert: (T) -> CharSequence? = { it.toString() },
) {
    val preference = findPreference<P>(key)!!
    preference.initial()?.let { preference.summary = convert(it) }
    preference.onPreferenceChangeListener =
        Preference.OnPreferenceChangeListener { _, newValue ->
            @Suppress("UNCHECKED_CAST")
            preference.summary = convert(newValue as T)
            true
        }
}

/** Obtain string value from list preference entries. */
fun Fragment.getActualString(
    s: CharSequence,
    @ArrayRes arrayValuesRes: Int,
    @ArrayRes arraysRes: Int,
): CharSequence {
    val arrayValues = resources.getStringArray(arrayValuesRes)
    val arrays = resources.getStringArray(arraysRes)
    return arrays[arrayValues.indexOf(s)]
}
