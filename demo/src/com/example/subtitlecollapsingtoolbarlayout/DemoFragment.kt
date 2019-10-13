package com.example.subtitlecollapsingtoolbarlayout

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_demo.*

class DemoFragment : PreferenceFragmentCompat() {

    private companion object {
        const val SEPARATOR_LINE = " | "
        const val SEPARATOR_COMMA = ", "
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_demo)
        find<MultiSelectListPreference>(PREFERENCE_SHOW_BUTTONS).bindSummary({ values }) {
            it.joinToString(SEPARATOR_COMMA) {
                getActualString(it, R.array.button_values, R.array.buttons)
            }
        }
        find<EditTextPreference>(PREFERENCE_IMAGE_URL).bindSummary({ text })

        find<EditTextPreference>(PREFERENCE_TITLE).bindSummary({ text })
        find<EditTextPreference>(PREFERENCE_SUBTITLE).bindSummary({ text })

        find<ListPreference>(PREFERENCE_CONTENT_SCRIM).bindSummary({ value }) {
            getActualString(it, R.array.color_values, R.array.colors)
        }
        find<ListPreference>(PREFERENCE_STATUSBAR_SCRIM).bindSummary({ value }) {
            getActualString(it, R.array.color_values, R.array.colors)
        }

        find<ListPreference>(PREFERENCE_COLLAPSED_TITLE_TEXT_APPEARANCE).bindSummary({ value }) {
            getActualString(it, R.array.text_appearance_values, R.array.text_appearances)
        }
        find<ListPreference>(PREFERENCE_EXPANDED_TITLE_TEXT_APPEARANCE).bindSummary({ value }) {
            getActualString(it, R.array.text_appearance_values, R.array.text_appearances)
        }
        find<ListPreference>(PREFERENCE_COLLAPSED_SUBTITLE_TEXT_APPEARANCE).bindSummary({ value }) {
            getActualString(it, R.array.text_appearance_values, R.array.text_appearances)
        }
        find<ListPreference>(PREFERENCE_EXPANDED_SUBTITLE_TEXT_APPEARANCE).bindSummary({ value }) {
            getActualString(it, R.array.text_appearance_values, R.array.text_appearances)
        }

        find<ListPreference>(PREFERENCE_COLLAPSED_TITLE_TEXT_COLOR).bindSummary({ value }) {
            getActualString(it, R.array.color_values, R.array.colors)
        }
        find<ListPreference>(PREFERENCE_EXPANDED_TITLE_TEXT_COLOR).bindSummary({ value }) {
            getActualString(it, R.array.color_values, R.array.colors)
        }
        find<ListPreference>(PREFERENCE_COLLAPSED_SUBTITLE_TEXT_COLOR).bindSummary({ value }) {
            getActualString(it, R.array.color_values, R.array.colors)
        }
        find<ListPreference>(PREFERENCE_EXPANDED_SUBTITLE_TEXT_COLOR).bindSummary({ value }) {
            getActualString(it, R.array.color_values, R.array.colors)
        }

        find<MultiSelectListPreference>(PREFERENCE_COLLAPSED_GRAVITY).bindSummary({ values }) {
            it.joinToString(SEPARATOR_LINE) {
                getActualString(it, R.array.gravity_values, R.array.gravities)
            }
        }
        find<MultiSelectListPreference>(PREFERENCE_EXPANDED_GRAVITY).bindSummary({ values }) {
            it.joinToString(SEPARATOR_LINE) {
                getActualString(it, R.array.gravity_values, R.array.gravities)
            }
        }

        find<ListPreference>(PREFERENCE_COLLAPSED_TITLE_TYPEFACE).bindSummary({ value }) {
            getActualString(it, R.array.typeface_values, R.array.typefaces)
        }
        find<ListPreference>(PREFERENCE_EXPANDED_TITLE_TYPEFACE).bindSummary({ value }) {
            getActualString(it, R.array.typeface_values, R.array.typefaces)
        }
        find<ListPreference>(PREFERENCE_COLLAPSED_SUBTITLE_TYPEFACE).bindSummary({ value }) {
            getActualString(it, R.array.typeface_values, R.array.typefaces)
        }
        find<ListPreference>(PREFERENCE_EXPANDED_SUBTITLE_TYPEFACE).bindSummary({ value }) {
            getActualString(it, R.array.typeface_values, R.array.typefaces)
        }

        find<EditTextPreference>(PREFERENCE_LEFT_MARGIN).bindSummary({ text })
        find<EditTextPreference>(PREFERENCE_TOP_MARGIN).bindSummary({ text })
        find<EditTextPreference>(PREFERENCE_RIGHT_MARGIN).bindSummary({ text })
        find<EditTextPreference>(PREFERENCE_BOTTOM_MARGIN).bindSummary({ text })

        find<Preference>(PREFERENCE_RESET).setOnPreferenceClickListener {
            ConfirmDialogFragment()
                .show(childFragmentManager, null)
            true
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
        findPreference(key)!!

    private inline fun <T : Preference> find(key: CharSequence, block: T.() -> Unit): T =
        find<T>(key).apply(block)

    /**
     * @param initial starting value can be obtained from its value, text, etc.
     * @param convert its preference value to representable summary text.
     */
    private fun <P : Preference, T> P.bindSummary(
        initial: P.() -> T?,
        convert: (T) -> CharSequence? = { it?.toString() }
    ) {
        initial()?.let { summary = convert(it) }
        onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
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

    class ConfirmDialogFragment : AppCompatDialogFragment() {
        override fun onCreateDialog(state: Bundle?): Dialog = AlertDialog.Builder(context!!)
            .setTitle("Reset")
            .setMessage("Are you sure?")
            .setPositiveButton(android.R.string.yes) { _, _ -> context!!.reset() }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}