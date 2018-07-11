package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.os.Bundle
import android.view.View
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_demo.*

class DemoFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    lateinit var titleEnabled: CheckBoxPreference
    lateinit var title: EditTextPreference
    lateinit var subtitle: EditTextPreference

    lateinit var scrimsShown: CheckBoxPreference
    lateinit var contentScrim: ListPreference
    lateinit var statusbarScrim: ListPreference

    /*lateinit var collapsedTitleTextAppeareance : ListPreference
    lateinit var expandedTitleTextAppeareance : ListPreference
    lateinit var collapsedSubtitleTextAppeareance : ListPreference
    lateinit var expandedSubtitleTextAppeareance : ListPreference*/

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
        contentScrim = find(PREFERENCE_CONTENT_SCRIM) {
            summary = value
            onPreferenceChangeListener = this@DemoFragment
        }
        statusbarScrim = find(PREFERENCE_STATUSBAR_SCRIM) {
            summary = value
            onPreferenceChangeListener = this@DemoFragment
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        preference.summary = newValue.toString()
        return true
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
}