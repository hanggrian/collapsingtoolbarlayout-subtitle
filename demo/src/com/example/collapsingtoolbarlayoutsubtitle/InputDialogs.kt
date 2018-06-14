@file:Suppress("NOTHING_TO_INLINE")

package com.example.collapsingtoolbarlayoutsubtitle

import android.content.Context
import android.support.v7.app.AlertDialog
import android.widget.EditText
import android.widget.FrameLayout

inline fun Context.inputDialog(
    title: CharSequence,
    noinline onOK: (s: CharSequence) -> Unit
): AlertDialog.Builder {
    val input = EditText(this).apply { hint = title }
    return AlertDialog.Builder(this).apply {
        setTitle(title)
        setView(FrameLayout(this@inputDialog).apply {
            val horizontal = resources.getDimensionPixelSize(R.dimen.input_dialog_padding_horizontal)
            val vertical = resources.getDimensionPixelSize(R.dimen.input_dialog_padding_vertical)
            setPadding(horizontal, vertical, horizontal, vertical)
            addView(input)
        })
        setNegativeButton(android.R.string.cancel) { _, _ -> }
        setPositiveButton(android.R.string.ok) { _, _ -> onOK(input.text) }
        show()
    }
}