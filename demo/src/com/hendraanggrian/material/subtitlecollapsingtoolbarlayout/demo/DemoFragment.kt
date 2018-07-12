package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.edit
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.processphoenix.ProcessPhoenix.triggerRebirth
import kotlinx.android.synthetic.main.activity_demo.*

class DemoFragment : PreferenceFragmentCompat() {

    private val stringOnChange = OnPreferenceChangeListener { preference, newValue ->
        preference.summary = newValue.toString()
        true
    }
    private val colorOnChange = OnPreferenceChangeListener { preference, newValue ->
        val index = resources.getStringArray(R.array.color_values).indexOf(newValue.toString())
        preference.summary = resources.getStringArray(R.array.colors)[index]
        true
    }
    private val gravityOnChange = OnPreferenceChangeListener { preference, newValue ->
        preference.summary = (newValue as Set<*>).joinToString("|")
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_demo)
        find<EditTextPreference>(PREFERENCE_IMAGE_URL) {
            summary = text
            onPreferenceChangeListener = stringOnChange
        }

        find<EditTextPreference>(PREFERENCE_TITLE) {
            summary = text
            onPreferenceChangeListener = stringOnChange
        }
        find<EditTextPreference>(PREFERENCE_SUBTITLE) {
            summary = text
            onPreferenceChangeListener = stringOnChange
        }
        find<CheckBoxPreference>(PREFERENCE_SCRIMS_SHOWN)

        find<ListPreference>(PREFERENCE_CONTENT_SCRIM) {
            summary = value
            onPreferenceChangeListener = colorOnChange
        }
        find<ListPreference>(PREFERENCE_STATUSBAR_SCRIM) {
            summary = value
            onPreferenceChangeListener = colorOnChange
        }

        find<ListPreference>(PREFERENCE_COLLAPSED_TITLE_TEXT_APPEARANCE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }
        find<ListPreference>(PREFERENCE_EXPANDED_TITLE_TEXT_APPEARANCE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }
        find<ListPreference>(PREFERENCE_COLLAPSED_SUBTITLE_TEXT_APPEARANCE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }
        find<ListPreference>(PREFERENCE_EXPANDED_SUBTITLE_TEXT_APPEARANCE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }

        find<ListPreference>(PREFERENCE_COLLAPSED_TITLE_TEXT_COLOR) {
            summary = value
            onPreferenceChangeListener = colorOnChange
        }
        find<ListPreference>(PREFERENCE_EXPANDED_TITLE_TEXT_COLOR) {
            summary = value
            onPreferenceChangeListener = colorOnChange
        }
        find<ListPreference>(PREFERENCE_COLLAPSED_SUBTITLE_TEXT_COLOR) {
            summary = value
            onPreferenceChangeListener = colorOnChange
        }
        find<ListPreference>(PREFERENCE_EXPANDED_SUBTITLE_TEXT_COLOR) {
            summary = value
            onPreferenceChangeListener = colorOnChange
        }

        find<MultiSelectListPreference>(PREFERENCE_COLLAPSED_GRAVITY) {
            summary = values.joinToString("|")
            onPreferenceChangeListener = gravityOnChange
        }
        find<MultiSelectListPreference>(PREFERENCE_EXPANDED_GRAVITY) {
            summary = values.joinToString("|")
            onPreferenceChangeListener = gravityOnChange
        }

        find<ListPreference>(PREFERENCE_COLLAPSED_TITLE_TYPEFACE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }
        find<ListPreference>(PREFERENCE_EXPANDED_TITLE_TYPEFACE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }
        find<ListPreference>(PREFERENCE_COLLAPSED_SUBTITLE_TYPEFACE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }
        find<ListPreference>(PREFERENCE_EXPANDED_SUBTITLE_TYPEFACE) {
            summary = value
            onPreferenceChangeListener = stringOnChange
        }

        find<EditTextPreference>(PREFERENCE_LEFT_MARGIN) {
            summary = text
            onPreferenceChangeListener = stringOnChange
        }
        find<EditTextPreference>(PREFERENCE_TOP_MARGIN) {
            summary = text
            onPreferenceChangeListener = stringOnChange
        }
        find<EditTextPreference>(PREFERENCE_RIGHT_MARGIN) {
            summary = text
            onPreferenceChangeListener = stringOnChange
        }
        find<EditTextPreference>(PREFERENCE_BOTTOM_MARGIN) {
            summary = text
            onPreferenceChangeListener = stringOnChange
        }

        find<Preference>(PREFERENCE_RESET) {
            setOnPreferenceClickListener {
                ConfirmDialogFragment().show(childFragmentManager, null)
                true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab = activity!!.fab
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab.isShown) {
                    fab.hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val fullyExpanded = activity!!.appbarLayout.height -
                    activity!!.appbarLayout.bottom == 0
                when {
                    fullyExpanded -> fab.hide()
                    else -> if (newState == RecyclerView.SCROLL_STATE_IDLE && !fullyExpanded) {
                        fab.show()
                    }
                }
            }
        })
    }

    @Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
    private inline fun <T : Preference> find(key: CharSequence): T =
        findPreference(key) as T

    private inline fun <T : Preference> find(key: CharSequence, block: T.() -> Unit): T =
        find<T>(key).apply(block)

    class ConfirmDialogFragment : AppCompatDialogFragment() {
        override fun onCreateDialog(state: Bundle?): Dialog = AlertDialog.Builder(context!!)
            .setTitle("Reset")
            .setMessage("Are you sure?")
            .setPositiveButton(android.R.string.yes) { _, _ ->
                getDefaultSharedPreferences(context).edit(true) { clear() }
                triggerRebirth(context)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
    }
}