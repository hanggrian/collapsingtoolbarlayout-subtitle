package com.example.dynamic

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jakewharton.processphoenix.ProcessPhoenix

class DynamicActivity :
    AppCompatActivity(),
    AppBarLayout.OnOffsetChangedListener,
    OnSharedPreferenceChangeListener {
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var toolbarLayout: SubtitleCollapsingToolbarLayout
    private lateinit var appbarLayout: AppBarLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var floatingButton: FloatingActionButton
    private lateinit var preferences: SharedPreferences

    @Px
    private var marginScale = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic)

        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        toolbarLayout = findViewById(R.id.toolbarLayout)
        appbarLayout = findViewById(R.id.appbarLayout)
        viewPager = findViewById(R.id.viewPager)
        floatingButton = findViewById(R.id.floatingButton)
        setSupportActionBar(toolbar)

        appbarLayout.addOnOffsetChangedListener(this)
        viewPager.adapter = MainAdapter()
        mainMediator.attach()
        preferences = getDefaultSharedPreferences(this)
        marginScale = resources.getDimensionPixelSize(R.dimen.margin_scale)
        onSharedPreferenceChanged(preferences, "")
    }

    override fun onResume() {
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_dynamic, menu)
        menu
            .findItem(
                when (preferences.getInt(PREFERENCE_THEME, MODE_NIGHT_FOLLOW_SYSTEM)) {
                    MODE_NIGHT_NO -> R.id.lightThemeItem
                    MODE_NIGHT_YES -> R.id.darkThemeItem
                    else -> R.id.defaultThemeItem
                },
            ).isChecked = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.defaultThemeItem, R.id.lightThemeItem, R.id.darkThemeItem -> {
                val theme =
                    when (item.itemId) {
                        R.id.lightThemeItem -> MODE_NIGHT_NO
                        R.id.darkThemeItem -> MODE_NIGHT_YES
                        else -> MODE_NIGHT_FOLLOW_SYSTEM
                    }
                preferences.edit {
                    putInt(PREFERENCE_THEME, theme)
                }
                AppCompatDelegate.setDefaultNightMode(theme)
            }

            R.id.resetItem -> {
                runCatching { preferences.edit { clear() } } // FIXME this line throws error
                ProcessPhoenix.triggerRebirth(this)
            }

            R.id.compareToRegularItem -> startActivity(Intent(this, DummyActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        when (verticalOffset) {
            0 -> if (floatingButton.isShown) floatingButton.hide()
            else -> if (!floatingButton.isShown) floatingButton.show()
        }
    }

    override fun onSharedPreferenceChanged(p: SharedPreferences?, key: String?) {
        toolbarLayout.title = preferences.getString(PREFERENCE_TITLE_TEXT, "Title")
        toolbarLayout.subtitle = preferences.getString(PREFERENCE_SUBTITLE_TEXT, "Subtitle")
        toolbarLayout.titleMaxLines =
            if (preferences.getBoolean(PREFERENCE_TITLE_MULTILINE, false)) 2 else 1
        toolbarLayout.subtitleMaxLines =
            if (preferences.getBoolean(PREFERENCE_SUBTITLE_MULTILINE, false)) 2 else 1
        toolbarLayout.titleLineSpacingAdd =
            preferences.getString(PREFERENCE_TITLE_LINE_SPACING_ADD, "0f")!!.toFloat()
        toolbarLayout.subtitleLineSpacingAdd =
            preferences.getString(PREFERENCE_SUBTITLE_LINE_SPACING_ADD, "0f")!!.toFloat()
        toolbarLayout.titleLineSpacingMultiplier =
            preferences.getString(PREFERENCE_TITLE_LINE_SPACING_MULTIPLIER, "0f")!!.toFloat()
        toolbarLayout.subtitleLineSpacingMultiplier =
            preferences.getString(PREFERENCE_SUBTITLE_LINE_SPACING_MULTIPLIER, "0f")!!.toFloat()
        toolbarLayout.titleHyphenationFrequency =
            preferences.getInt(PREFERENCE_TITLE_HYPHENATION_FREQUENCIES, 0)
        toolbarLayout.subtitleHyphenationFrequency =
            preferences.getInt(PREFERENCE_SUBTITLE_HYPHENATION_FREQUENCIES, 0)
        toolbarLayout.titleCollapseMode =
            preferences.getString(PREFERENCE_COLLAPSE_MODE, "0")!!.toInt()
        preferences
            .getString(PREFERENCE_TITLE_COLLAPSED_COLOR, "#00000000")!!
            .ifConfigured { toolbarLayout.setCollapsedTitleTextColor(it) }
        preferences
            .getString(PREFERENCE_SUBTITLE_COLLAPSED_COLOR, "#00000000")!!
            .ifConfigured { toolbarLayout.setCollapsedSubtitleTextColor(it) }
        preferences
            .getString(PREFERENCE_TITLE_EXPANDED_COLOR, "#00000000")!!
            .ifConfigured { toolbarLayout.setExpandedTitleTextColor(it) }
        preferences
            .getString(PREFERENCE_SUBTITLE_EXPANDED_COLOR, "#00000000")!!
            .ifConfigured { toolbarLayout.setExpandedSubtitleTextColor(it) }
        toolbarLayout.statusBarScrim =
            preferences
                .getString(PREFERENCE_STATUS_BAR_SCRIM, "#00000000")!!
                .takeIf { it.isConfigured() }
                ?.toColorInt()
                ?.toDrawable()
        toolbarLayout.contentScrim =
            preferences
                .getString(PREFERENCE_CONTENT_SCRIM, "#00000000")!!
                .takeIf { it.isConfigured() }
                ?.toColorInt()
                ?.toDrawable()
        toolbarLayout.collapsedTitleGravity =
            preferences.getGravity(
                PREFERENCE_COLLAPSED_GRAVITY,
                GravityCompat.START or Gravity.CENTER_VERTICAL,
            )
        toolbarLayout.expandedTitleGravity =
            preferences.getGravity(
                PREFERENCE_EXPANDED_GRAVITY,
                GravityCompat.START or Gravity.BOTTOM,
            )
        preferences
            .getInt(PREFERENCE_EXPANDED_MARGIN_LEFT, 0)
            .takeIf { it != 0 }
            ?.let { toolbarLayout.expandedTitleMarginStart = it * marginScale }
        preferences
            .getInt(PREFERENCE_EXPANDED_MARGIN_TOP, 0)
            .takeIf { it != 0 }
            ?.let { toolbarLayout.expandedTitleMarginTop = it * marginScale }
        preferences
            .getInt(PREFERENCE_EXPANDED_MARGIN_RIGHT, 0)
            .takeIf { it != 0 }
            ?.let { toolbarLayout.expandedTitleMarginEnd = it * marginScale }
        preferences
            .getInt(PREFERENCE_EXPANDED_MARGIN_BOTTOM, 0)
            .takeIf { it != 0 }
            ?.let { toolbarLayout.expandedTitleMarginBottom = it * marginScale }
    }

    fun expand(view: View) = appbarLayout.setExpanded(true)

    private val mainMediator
        get() =
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text =
                    when (position) {
                        0 -> "Title"
                        1 -> "Subtitle"
                        else -> "Others"
                    }
            }

    inner class MainAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = 3

        override fun createFragment(position: Int) =
            when (position) {
                0 -> MainTitleFragment()
                1 -> MainSubtitleFragment()
                else -> MainOthersFragment()
            }
    }

    private companion object {
        fun SharedPreferences.getGravity(key: String, def: Int): Int {
            val iterator = getStringSet(key, emptySet())!!.iterator()
            var gravity: Int? = null
            while (iterator.hasNext()) {
                val next = iterator.next().toInt()
                gravity =
                    when (gravity) {
                        null -> next
                        else -> gravity or next
                    }
            }
            return gravity ?: def
        }

        fun String.isConfigured(): Boolean = this != "#00000000"

        fun String.ifConfigured(action: (Int) -> Unit) {
            if (!isConfigured()) {
                return
            }
            action("#${removePrefix("#")}".toColorInt())
        }
    }
}
