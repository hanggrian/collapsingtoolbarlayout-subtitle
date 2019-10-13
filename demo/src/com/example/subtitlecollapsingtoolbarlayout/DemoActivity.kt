package com.example.subtitlecollapsingtoolbarlayout

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color.parseColor
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils.isDigitsOnly
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.snackbar.Snackbar
import com.hendraanggrian.material.errorbar.indefiniteErrorbar
import com.hendraanggrian.pikasso.palette.PaletteCallbackBuilder
import com.hendraanggrian.pikasso.palette.palette
import com.hendraanggrian.pikasso.picasso
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private lateinit var preferences: Preferences
    private lateinit var menuItem: MenuItem
    private val paletteBuilder: PaletteCallbackBuilder.() -> Unit = {
        onSuccess {
            preferences.edit {
                useVibrant { putString(PREFERENCE_EXPANDED_TITLE_TEXT_COLOR, it.toHex()) }
                useMuted { putString(PREFERENCE_EXPANDED_SUBTITLE_TEXT_COLOR, it.toHex()) }
            }
        }
        onError { Snackbar.make(container, it.message.toString(), Snackbar.LENGTH_SHORT).show() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        setSupportActionBar(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, DemoFragment())
            .commitNow()
        preferences = getDefaultSharedPreferences(this)
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
        menuInflater.inflate(R.menu.activity_demo, menu)
        menuItem = menu[0]
        preferences.run {
            doIfContains(PREFERENCE_SHOW_BUTTONS) {
                updateButtons(getStringSet(it, null)!!)
            }
            doIfContains(PREFERENCE_IMAGE_URL) {
                picasso.load(getStringNotNull(it)).palette(image, builder = paletteBuilder)
            }
            doIfContains(PREFERENCE_TITLE_ENABLED) {
                toolbarLayout.isTitleEnabled = getBoolean(it, false)
            }
            doIfContains(PREFERENCE_TITLE) {
                toolbarLayout.title = getStringNotNull(it)
            }
            doIfContains(PREFERENCE_SUBTITLE) {
                toolbarLayout.subtitle = getStringNotNull(it)
            }
            doIfContains(PREFERENCE_SCRIMS_SHOWN) {
                toolbarLayout.setScrimsShown(getBoolean(it, false))
            }
            doIfContains(PREFERENCE_CONTENT_SCRIM) {
                toolbarLayout.setContentScrimColor(parseColor(getStringNotNull(it)))
            }
            doIfContains(PREFERENCE_STATUSBAR_SCRIM) {
                toolbarLayout.setStatusBarScrimColor(parseColor(getStringNotNull(it)))
            }
            doIfContains(PREFERENCE_COLLAPSED_TITLE_TEXT_APPEARANCE) {
                toolbarLayout.setCollapsedTitleTextAppearance(getStringNotNull(it).toStyle())
            }
            doIfContains(PREFERENCE_EXPANDED_TITLE_TEXT_APPEARANCE) {
                toolbarLayout.setExpandedTitleTextAppearance(getStringNotNull(it).toStyle())
            }
            doIfContains(PREFERENCE_COLLAPSED_SUBTITLE_TEXT_APPEARANCE) {
                toolbarLayout.setCollapsedSubtitleTextAppearance(getStringNotNull(it).toStyle())
            }
            doIfContains(PREFERENCE_EXPANDED_SUBTITLE_TEXT_APPEARANCE) {
                toolbarLayout.setExpandedSubtitleTextAppearance(getStringNotNull(it).toStyle())
            }
            doIfContains(PREFERENCE_COLLAPSED_TITLE_TEXT_COLOR) {
                toolbarLayout.setCollapsedTitleTextColor(getStringNotNull(it).toColor())
            }
            doIfContains(PREFERENCE_EXPANDED_TITLE_TEXT_COLOR) {
                toolbarLayout.setExpandedTitleTextColor(getStringNotNull(it).toColor())
            }
            doIfContains(PREFERENCE_COLLAPSED_SUBTITLE_TEXT_COLOR) {
                toolbarLayout.setCollapsedSubtitleTextColor(getStringNotNull(it).toColor())
            }
            doIfContains(PREFERENCE_EXPANDED_SUBTITLE_TEXT_COLOR) {
                toolbarLayout.setExpandedSubtitleTextColor(getStringNotNull(it).toColor())
            }
            doIfContains(PREFERENCE_COLLAPSED_GRAVITY) {
                toolbarLayout.collapsedTitleGravity = getStringSet(it, null)!!.toGravity()
            }
            doIfContains(PREFERENCE_EXPANDED_GRAVITY) {
                toolbarLayout.expandedTitleGravity = getStringSet(it, null)!!.toGravity()
            }
            doIfContains(PREFERENCE_COLLAPSED_TITLE_TYPEFACE) {
                toolbarLayout.setCollapsedTitleTypeface(getStringNotNull(it).toTypeface())
            }
            doIfContains(PREFERENCE_COLLAPSED_TITLE_TYPEFACE) {
                toolbarLayout.setExpandedTitleTypeface(getStringNotNull(it).toTypeface())
            }
            doIfContains(PREFERENCE_COLLAPSED_TITLE_TYPEFACE) {
                toolbarLayout.setCollapsedSubtitleTypeface(getStringNotNull(it).toTypeface())
            }
            doIfContains(PREFERENCE_COLLAPSED_TITLE_TYPEFACE) {
                toolbarLayout.setExpandedSubtitleTypeface(getStringNotNull(it).toTypeface())
            }
            doIfContains(PREFERENCE_LEFT_MARGIN) {
                toolbarLayout.expandedTitleMarginStart = getStringNotNull(it).toMargin()
            }
            doIfContains(PREFERENCE_TOP_MARGIN) {
                toolbarLayout.expandedTitleMarginTop = getStringNotNull(it).toMargin()
            }
            doIfContains(PREFERENCE_RIGHT_MARGIN) {
                toolbarLayout.expandedTitleMarginEnd = getStringNotNull(it).toMargin()
            }
            doIfContains(PREFERENCE_BOTTOM_MARGIN) {
                toolbarLayout.expandedTitleMarginBottom = getStringNotNull(it).toMargin()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.info -> AboutDialogFragment().show(supportFragmentManager, null)
        }
        return super.onOptionsItemSelected(item)
    }

    fun scrollToTop(@Suppress("UNUSED_PARAMETER") view: View) {
        appbarLayout.setExpanded(true)
        if (fab.isShown) {
            fab.hide()
        }
    }

    override fun onSharedPreferenceChanged(
        preferences: Preferences,
        key: String
    ) = preferences.run {
        when (key) {
            PREFERENCE_SHOW_BUTTONS -> updateButtons(getStringSet(key, null)!!)
            PREFERENCE_IMAGE_URL -> picasso.load(getStringNotNull(key))
                .palette(image, builder = paletteBuilder)
            PREFERENCE_TITLE_ENABLED -> toolbarLayout.isTitleEnabled = getBoolean(key, false)
            PREFERENCE_TITLE -> toolbarLayout.title = getStringNotNull(key)
            PREFERENCE_SUBTITLE -> toolbarLayout.subtitle = getStringNotNull(key)
            PREFERENCE_SCRIMS_SHOWN -> toolbarLayout.setScrimsShown(getBoolean(key, false))
            PREFERENCE_CONTENT_SCRIM -> toolbarLayout
                .setContentScrimColor(getStringNotNull(key).toColor())
            PREFERENCE_STATUSBAR_SCRIM -> toolbarLayout
                .setStatusBarScrimColor(getStringNotNull(key).toColor())
            PREFERENCE_COLLAPSED_TITLE_TEXT_APPEARANCE -> toolbarLayout
                .setCollapsedTitleTextAppearance(getStringNotNull(key).toStyle())
            PREFERENCE_EXPANDED_TITLE_TEXT_APPEARANCE -> toolbarLayout
                .setExpandedTitleTextAppearance(getStringNotNull(key).toStyle())
            PREFERENCE_COLLAPSED_SUBTITLE_TEXT_APPEARANCE -> toolbarLayout
                .setCollapsedSubtitleTextAppearance(getStringNotNull(key).toStyle())
            PREFERENCE_EXPANDED_SUBTITLE_TEXT_APPEARANCE -> toolbarLayout
                .setExpandedSubtitleTextAppearance(getStringNotNull(key).toStyle())
            PREFERENCE_COLLAPSED_TITLE_TEXT_COLOR -> toolbarLayout
                .setCollapsedTitleTextColor(getStringNotNull(key).toColor())
            PREFERENCE_EXPANDED_TITLE_TEXT_COLOR -> toolbarLayout
                .setExpandedTitleTextColor(getStringNotNull(key).toColor())
            PREFERENCE_COLLAPSED_SUBTITLE_TEXT_COLOR -> toolbarLayout
                .setCollapsedSubtitleTextColor(getStringNotNull(key).toColor())
            PREFERENCE_EXPANDED_SUBTITLE_TEXT_COLOR -> toolbarLayout
                .setExpandedSubtitleTextColor(getStringNotNull(key).toColor())
            PREFERENCE_COLLAPSED_GRAVITY -> toolbarLayout
                .collapsedTitleGravity = getStringSet(key, null)!!.toGravity()
            PREFERENCE_EXPANDED_GRAVITY -> toolbarLayout
                .expandedTitleGravity = getStringSet(key, null)!!.toGravity()
            PREFERENCE_COLLAPSED_TITLE_TYPEFACE -> toolbarLayout
                .setCollapsedTitleTypeface(getStringNotNull(key).toTypeface())
            PREFERENCE_EXPANDED_TITLE_TYPEFACE -> toolbarLayout
                .setExpandedTitleTypeface(getStringNotNull(key).toTypeface())
            PREFERENCE_COLLAPSED_SUBTITLE_TYPEFACE -> toolbarLayout
                .setCollapsedSubtitleTypeface(getStringNotNull(key).toTypeface())
            PREFERENCE_EXPANDED_SUBTITLE_TYPEFACE -> toolbarLayout
                .setExpandedSubtitleTypeface(getStringNotNull(key).toTypeface())
            PREFERENCE_LEFT_MARGIN -> toolbarLayout
                .expandedTitleMarginStart = getStringNotNull(key).toMargin()
            PREFERENCE_TOP_MARGIN -> toolbarLayout
                .expandedTitleMarginTop = getStringNotNull(key).toMargin()
            PREFERENCE_BOTTOM_MARGIN -> toolbarLayout
                .expandedTitleMarginBottom = getStringNotNull(key).toMargin()
            PREFERENCE_RIGHT_MARGIN -> toolbarLayout
                .expandedTitleMarginEnd = getStringNotNull(key).toMargin()
        }
    }

    private fun updateButtons(set: Set<String>) {
        val values = resources.getStringArray(R.array.button_values)
        toolbar.navigationIcon = when {
            set.contains(values[0]) -> navigationIconDrawable
            else -> null
        }
        menuItem.isVisible = set.contains(values[1])
    }

    private val navigationIconDrawable: Drawable
        get() {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.homeAsUpIndicator, typedValue, true)
            return ContextCompat.getDrawable(this, typedValue.resourceId)!!
        }

    private fun String.toTypeface(): Typeface = Typeface.createFromAsset(assets, this)

    @Px
    private fun String.toMargin(): Int = when {
        isDigitsOnly(this) -> toInt()
        else -> {
            toolbarLayout.indefiniteErrorbar("Wrong margin input.", getText(R.string.reset)) {
                reset(preferences)
            }
            Int.MIN_VALUE
        }
    }

    private companion object {

        inline fun SharedPreferences.doIfContains(key: String, action: (String) -> Unit) {
            if (contains(key)) {
                action(key)
            }
        }

        @ColorInt
        fun String.toColor(): Int = parseColor(this)

        fun @receiver:ColorInt Int.toHex(): String = "#%06X".format(0xFFFFFF and this)

        @StyleRes
        fun String.toStyle(): Int = when (this) {
            "small" -> R.style.TextAppearance_AppCompat_Small
            "medium" -> R.style.TextAppearance_AppCompat_Medium
            else -> R.style.TextAppearance_AppCompat_Large
        }

        fun Set<String>.toGravity(): Int {
            val iterator = iterator()
            var gravity: Int? = null
            while (iterator.hasNext()) {
                val next = iterator.next().toInt()
                gravity = when (gravity) {
                    null -> next
                    else -> gravity or next
                }
            }
            return gravity ?: Gravity.BOTTOM or GravityCompat.END
        }
    }
}