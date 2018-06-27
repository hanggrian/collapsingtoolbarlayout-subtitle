package com.example.collapsingtoolbarlayoutsubtitle

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.AttrRes
import android.support.annotation.StringRes
import android.support.design.widget.errorbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.flipboard.bottomsheet.commons.MenuSheetView
import com.flipboard.bottomsheet.commons.MenuSheetView.MenuType.LIST
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ColorChooserDialog.ColorCallback {

    private var isTitle = false
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbarLayout.setExpandedTitleTextColor(Color.WHITE)
        toolbarLayout.setExpandedSubtitleTextColor(Color.WHITE)
        coordinatorLayout.errorbar("Tap tune button to customize toolbar layout") {
            setContentMarginLeft(resources.getDimensionPixelOffset(R.dimen.padding_vertical))
            setContentMarginRight(resources.getDimensionPixelOffset(R.dimen.padding_vertical))
        }
    }

    fun onClick(v: View) {
        sheetLayout.showWithSheetView(MenuSheetView(this, LIST, "Set ...") {
            when (it.itemId) {
                R.id.title -> inputDialog("Title", toolbarLayout.title) { toolbarLayout.title = it }
                R.id.titleExpandedColor -> colorPickerDialog(R.string.title_expanded_color, true, true)
                R.id.titleCollapsedColor -> colorPickerDialog(R.string.title_collapsed_color, true, false)
                R.id.subtitle -> inputDialog("Subtitle", toolbarLayout.subtitle) { toolbarLayout.subtitle = it }
                R.id.subtitleExpandedColor -> colorPickerDialog(R.string.subtitle_expanded_color, false, true)
                R.id.subtitleCollapsedColor -> colorPickerDialog(R.string.subtitle_collapsed_color, false, false)
                R.id.expandedGravity -> gravityDialog("Expanded gravity") { toolbarLayout.expandedTitleGravity = it }
                R.id.collapsedGravity -> gravityDialog("Collapsed gravity") { toolbarLayout.collapsedTitleGravity = it }
                R.id.navigationIcon -> toolbar.navigationIcon = when (toolbar.navigationIcon) {
                    null -> getDrawableAttr(R.attr.homeAsUpIndicator)
                    else -> null
                }
                R.id.menuItems -> when {
                    toolbar.menu.hasVisibleItems() -> toolbar.menu.clear()
                    else -> toolbar.menu.run {
                        add("1").setShowAsActionFlags(SHOW_AS_ACTION_ALWAYS).setIcon(R.drawable.ic_action_back)
                        add("2").setShowAsActionFlags(SHOW_AS_ACTION_ALWAYS).setIcon(R.drawable.ic_action_color)
                        add("3").setShowAsActionFlags(SHOW_AS_ACTION_ALWAYS).setIcon(R.drawable.ic_action_gravity)
                    }
                }
            }
            if (sheetLayout.isSheetShowing) sheetLayout.dismissSheet()
            true
        }.apply { inflateMenu(R.menu.activity_main) })
    }

    override fun onColorChooserDismissed(dialog: ColorChooserDialog) {}

    override fun onColorSelection(dialog: ColorChooserDialog, selectedColor: Int) = when {
        isTitle -> when {
            isExpanded -> toolbarLayout.setExpandedTitleTextColor(selectedColor)
            else -> toolbarLayout.setCollapsedTitleTextColor(selectedColor)
        }
        else -> when {
            isExpanded -> toolbarLayout.setExpandedSubtitleTextColor(selectedColor)
            else -> toolbarLayout.setCollapsedSubtitleTextColor(selectedColor)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private companion object {

        inline fun Context.inputDialog(
            title: CharSequence,
            prefill: CharSequence?,
            noinline onOK: (s: CharSequence) -> Unit
        ): MaterialDialog = MaterialDialog.Builder(this)
            .title(title)
            .input(title, prefill, true) { _, s -> onOK(s) }
            .negativeText(android.R.string.cancel)
            .positiveText(android.R.string.ok)
            .show()

        inline fun MainActivity.colorPickerDialog(
            @StringRes title: Int,
            isTitle: Boolean,
            isExpanded: Boolean
        ): ColorChooserDialog {
            this.isTitle = isTitle
            this.isExpanded = isExpanded
            return ColorChooserDialog.Builder(this, title).show(this)
        }

        inline fun Context.gravityDialog(
            title: CharSequence,
            noinline onOK: (s: Int) -> Unit
        ) {
            MaterialDialog.Builder(this)
                .title(title)
                .items(ToolbarGravity.values().toList())
                .itemsCallbackMultiChoice(null) { _, which, _ ->
                    var flags: Int? = null
                    for (i in which) {
                        val flag = ToolbarGravity.values()[i].value
                        flags = if (flags == null) flag else flags or flag
                    }
                    if (flags != null) {
                        onOK(flags)
                    }
                    false
                }
                .negativeText(android.R.string.cancel)
                .positiveText("Choose")
                .show()
        }

        inline fun Context.getDrawableAttr(@AttrRes attr: Int): Drawable? = obtainStyledAttributes(null, intArrayOf(attr)).let {
            try {
                return it.getDrawable(0)
            } finally {
                it.recycle()
            }
        }
    }
}