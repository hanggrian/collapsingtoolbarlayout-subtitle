package com.example.subtitlecollapsingtoolbarlayout

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hendraanggrian.auto.prefs.BindPreference
import com.hendraanggrian.auto.prefs.PreferencesSaver
import com.hendraanggrian.auto.prefs.Prefs
import com.hendraanggrian.auto.prefs.android.AndroidPreferences
import com.hendraanggrian.auto.prefs.android.preferences
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener, OnSharedPreferenceChangeListener {
    @JvmField @BindPreference("theme") var theme2 = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    @JvmField @BindPreference var titleText = "Title"
    @JvmField @BindPreference var subtitleText = "Subtitle"
    @JvmField @BindPreference var titleMultiline = false
    @JvmField @BindPreference var subtitleMultiline = false
    @JvmField @BindPreference var titleLineSpacingAdd = "0f"
    @JvmField @BindPreference var subtitleLineSpacingAdd = "0f"
    @JvmField @BindPreference var titleLineSpacingMultiplier = "1f"
    @JvmField @BindPreference var subtitleLineSpacingMultiplier = "1f"
    @JvmField @BindPreference var titleHyphenationFrequencies = "1"
    @JvmField @BindPreference var subtitleHyphenationFrequencies = "1"
    @JvmField @BindPreference var collapseMode = "0"
    @JvmField @BindPreference @ColorInt var titleCollapsedColor = Color.TRANSPARENT
    @JvmField @BindPreference @ColorInt var subtitleCollapsedColor = Color.TRANSPARENT
    @JvmField @BindPreference @ColorInt var titleExpandedColor = Color.TRANSPARENT
    @JvmField @BindPreference @ColorInt var subtitleExpandedColor = Color.TRANSPARENT
    @JvmField @BindPreference @ColorInt var statusBarScrim = Color.TRANSPARENT
    @JvmField @BindPreference @ColorInt var contentScrim = Color.TRANSPARENT
    @JvmField @BindPreference var expandedMarginLeft = 0
    @JvmField @BindPreference var expandedMarginTop = 0
    @JvmField @BindPreference var expandedMarginRight = 0
    @JvmField @BindPreference var expandedMarginBottom = 0

    private lateinit var prefs: AndroidPreferences
    private lateinit var saver: PreferencesSaver

    @Px private var marginScale = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        barLayout.addOnOffsetChangedListener(this)
        viewPager.adapter = MainAdapter()
        mainMediator.attach()
        prefs = preferences
        marginScale = resources.getDimensionPixelSize(R.dimen.margin_scale)
        onSharedPreferenceChanged(prefs, "")
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
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
                prefs.edit { clear() }
                ProcessPhoenix.triggerRebirth(this)
            }
            R.id.compareToRegularItem -> startActivity(Intent(this, DummyActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        when (verticalOffset) {
            0 -> if (actionButton.isShown) actionButton.hide()
            else -> if (!actionButton.isShown) actionButton.show()
        }
    }

    override fun onSharedPreferenceChanged(p: SharedPreferences, key: String) {
        saver = Prefs.bind(prefs, this)
        toolbarLayout.title = titleText
        toolbarLayout.subtitle = subtitleText
        toolbarLayout.titleMaxLines = if (titleMultiline) 2 else 1
        toolbarLayout.subtitleMaxLines = if (subtitleMultiline) 2 else 1
        toolbarLayout.titleLineSpacingAdd = titleLineSpacingAdd.toFloat()
        toolbarLayout.subtitleLineSpacingAdd = subtitleLineSpacingAdd.toFloat()
        toolbarLayout.titleLineSpacingMultiplier = titleLineSpacingMultiplier.toFloat()
        toolbarLayout.subtitleLineSpacingMultiplier = subtitleLineSpacingMultiplier.toFloat()
        toolbarLayout.titleHyphenationFrequency = titleHyphenationFrequencies.toInt()
        toolbarLayout.subtitleHyphenationFrequency = subtitleHyphenationFrequencies.toInt()
        toolbarLayout.titleCollapseMode = collapseMode.toInt()
        titleCollapsedColor.ifConfigured { toolbarLayout.setCollapsedTitleTextColor(it) }
        subtitleCollapsedColor.ifConfigured { toolbarLayout.setCollapsedSubtitleTextColor(it) }
        titleExpandedColor.ifConfigured { toolbarLayout.setExpandedTitleTextColor(it) }
        subtitleExpandedColor.ifConfigured { toolbarLayout.setExpandedSubtitleTextColor(it) }
        toolbarLayout.statusBarScrim = if (statusBarScrim.isConfigured()) ColorDrawable(statusBarScrim) else null
        toolbarLayout.contentScrim = if (contentScrim.isConfigured()) ColorDrawable(contentScrim) else null
        toolbarLayout.collapsedTitleGravity =
            prefs.getGravity("collapsedGravity", GravityCompat.START or Gravity.CENTER_VERTICAL)
        toolbarLayout.expandedTitleGravity = prefs.getGravity("expandedGravity", GravityCompat.START or Gravity.BOTTOM)
        if (expandedMarginLeft != 0) toolbarLayout.expandedTitleMarginStart = expandedMarginLeft * marginScale
        if (expandedMarginTop != 0) toolbarLayout.expandedTitleMarginTop = expandedMarginTop * marginScale
        if (expandedMarginRight != 0) toolbarLayout.expandedTitleMarginEnd = expandedMarginRight * marginScale
        if (expandedMarginBottom != 0) toolbarLayout.expandedTitleMarginBottom = expandedMarginBottom * marginScale
    }

    fun expand(view: View) = barLayout.setExpanded(true)

    private val mainMediator
        get() = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Title"
                1 -> "Subtitle"
                else -> "Others"
            }
        }

    inner class MainAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = 3
        override fun createFragment(position: Int) = when (position) {
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
                gravity = when (gravity) {
                    null -> next
                    else -> gravity or next
                }
            }
            return gravity ?: def
        }

        fun @receiver:ColorInt Int.isConfigured(): Boolean = this != Color.TRANSPARENT

        fun @receiver:ColorInt Int.ifConfigured(action: (Int) -> Unit) {
            if (isConfigured()) action(this)
        }
    }
}
