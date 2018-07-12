package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color.parseColor
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils.isDigitsOnly
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.hendraanggrian.material.errorbar.indefiniteErrorbar
import com.hendraanggrian.pikasso.picasso
import com.jakewharton.processphoenix.ProcessPhoenix.triggerRebirth
import kotlinx.android.synthetic.main.activity_demo.*

class DemoActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        setSupportActionBar(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, DemoFragment())
            .commitNow()
        preferences = getDefaultSharedPreferences(this).apply {
            doIfContains(PREFERENCE_SHOW_BACK_BUTTON) {
                toolbar.navigationIcon = when {
                    getBoolean(it, false) -> navigationIconDrawable
                    else -> null
                }
            }
            doIfContains(PREFERENCE_IMAGE_URL) {
                picasso.load(getStringNotNull(it))
                    .placeholder(R.drawable.bg)
                    .error(R.drawable.bg)
                    .into(image)
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
                toolbarLayout.setCollapsedTitleTextAppearance(getStringNotNull(it).toSyle())
            }
            doIfContains(PREFERENCE_EXPANDED_TITLE_TEXT_APPEARANCE) {
                toolbarLayout.setExpandedTitleTextAppearance(getStringNotNull(it).toSyle())
            }
            doIfContains(PREFERENCE_COLLAPSED_SUBTITLE_TEXT_APPEARANCE) {
                toolbarLayout.setCollapsedSubtitleTextAppearance(getStringNotNull(it).toSyle())
            }
            doIfContains(PREFERENCE_EXPANDED_SUBTITLE_TEXT_APPEARANCE) {
                toolbarLayout.setExpandedSubtitleTextAppearance(getStringNotNull(it).toSyle())
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
                toolbarLayout.collapsedTitleGravity = getStringSet(it, null).toGravity()
            }
            doIfContains(PREFERENCE_EXPANDED_GRAVITY) {
                toolbarLayout.collapsedTitleGravity = getStringSet(it, null).toGravity()
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
    }

    override fun onResume() {
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
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
    ) = preferences.let {
        when (key) {
            PREFERENCE_SHOW_BACK_BUTTON -> toolbar.navigationIcon = when {
                it.getBoolean(key, false) -> navigationIconDrawable
                else -> null
            }
            PREFERENCE_IMAGE_URL -> picasso.load(it.getStringNotNull(key))
                .placeholder(R.drawable.bg)
                .error(R.drawable.bg)
                .into(image)
            PREFERENCE_TITLE_ENABLED -> toolbarLayout.isTitleEnabled = it.getBoolean(key, false)
            PREFERENCE_TITLE -> toolbarLayout.title = it.getStringNotNull(key)
            PREFERENCE_SUBTITLE -> toolbarLayout.subtitle = it.getStringNotNull(key)
            PREFERENCE_SCRIMS_SHOWN -> toolbarLayout.setScrimsShown(it.getBoolean(key, false))
            PREFERENCE_CONTENT_SCRIM -> toolbarLayout
                .setContentScrimColor(it.getStringNotNull(key).toColor())
            PREFERENCE_STATUSBAR_SCRIM -> toolbarLayout
                .setStatusBarScrimColor(it.getStringNotNull(key).toColor())
            PREFERENCE_COLLAPSED_TITLE_TEXT_APPEARANCE -> toolbarLayout
                .setCollapsedTitleTextAppearance(it.getStringNotNull(key).toSyle())
            PREFERENCE_EXPANDED_TITLE_TEXT_APPEARANCE -> toolbarLayout
                .setExpandedTitleTextAppearance(it.getStringNotNull(key).toSyle())
            PREFERENCE_COLLAPSED_SUBTITLE_TEXT_APPEARANCE -> toolbarLayout
                .setCollapsedSubtitleTextAppearance(it.getStringNotNull(key).toSyle())
            PREFERENCE_EXPANDED_SUBTITLE_TEXT_APPEARANCE -> toolbarLayout
                .setExpandedSubtitleTextAppearance(it.getStringNotNull(key).toSyle())
            PREFERENCE_COLLAPSED_TITLE_TEXT_COLOR -> toolbarLayout
                .setCollapsedTitleTextColor(it.getStringNotNull(key).toColor())
            PREFERENCE_EXPANDED_TITLE_TEXT_COLOR -> toolbarLayout
                .setExpandedTitleTextColor(it.getStringNotNull(key).toColor())
            PREFERENCE_COLLAPSED_SUBTITLE_TEXT_COLOR -> toolbarLayout
                .setCollapsedSubtitleTextColor(it.getStringNotNull(key).toColor())
            PREFERENCE_EXPANDED_SUBTITLE_TEXT_COLOR -> toolbarLayout
                .setExpandedSubtitleTextColor(it.getStringNotNull(key).toColor())
            PREFERENCE_COLLAPSED_GRAVITY -> toolbarLayout
                .collapsedTitleGravity =
                it.getStringSet(key, null).toGravity()
            PREFERENCE_EXPANDED_GRAVITY -> toolbarLayout
                .expandedTitleGravity =
                it.getStringSet(key, null).toGravity()
            PREFERENCE_COLLAPSED_TITLE_TYPEFACE -> toolbarLayout
                .setCollapsedTitleTypeface(it.getStringNotNull(key).toTypeface())
            PREFERENCE_EXPANDED_TITLE_TYPEFACE -> toolbarLayout
                .setExpandedTitleTypeface(it.getStringNotNull(key).toTypeface())
            PREFERENCE_COLLAPSED_SUBTITLE_TYPEFACE -> toolbarLayout
                .setCollapsedSubtitleTypeface(it.getStringNotNull(key).toTypeface())
            PREFERENCE_EXPANDED_SUBTITLE_TYPEFACE -> toolbarLayout
                .setExpandedSubtitleTypeface(it.getStringNotNull(key).toTypeface())
            PREFERENCE_LEFT_MARGIN -> toolbarLayout
                .expandedTitleMarginStart = it.getStringNotNull(key).toMargin()
            PREFERENCE_TOP_MARGIN -> toolbarLayout
                .expandedTitleMarginTop = it.getStringNotNull(key).toMargin()
            PREFERENCE_BOTTOM_MARGIN -> toolbarLayout
                .expandedTitleMarginBottom = it.getStringNotNull(key).toMargin()
            PREFERENCE_RIGHT_MARGIN -> toolbarLayout
                .expandedTitleMarginEnd = it.getStringNotNull(key).toMargin()
        }
    }

    private val navigationIconDrawable: Drawable
        get() {
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.homeAsUpIndicator, typedValue, true)
            return ContextCompat.getDrawable(this, typedValue.resourceId)!!
        }

    private fun String.toTypeface(): Typeface = Typeface.createFromAsset(assets, this)

    @Px private fun String.toMargin(): Int = when {
        isDigitsOnly(this) -> toInt()
        else -> {
            toolbarLayout.indefiniteErrorbar("Wrong margin input.") {
                setAction(R.string.reset) {
                    preferences.edit(true) { clear() }
                    triggerRebirth(this@DemoActivity)
                }
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

        @ColorInt fun String.toColor(): Int = parseColor(this)

        @StyleRes fun String.toSyle(): Int = when (this) {
            "small" -> R.style.TextAppearance_AppCompat_Small
            "medium" -> R.style.TextAppearance_AppCompat_Medium
            else -> R.style.TextAppearance_AppCompat_Large
        }

        fun Set<String>.toGravity(): Int {
            val iterator = iterator()
            var gravity: Int? = null
            if (iterator.hasNext()) {
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