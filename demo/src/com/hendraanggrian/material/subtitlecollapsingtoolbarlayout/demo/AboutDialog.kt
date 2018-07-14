package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans

class AboutDialogFragment : AppCompatDialogFragment() {

    @SuppressLint("PrivateResource")
    override fun onCreateDialog(state: Bundle?): Dialog = AlertDialog.Builder(context!!)
        .setTitle(BuildConfig.RELEASE_ARTIFACT)
        .setView(FrameLayout(context).apply {
            setPadding(
                resources.getDimensionPixelSize(R.dimen.padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.padding_vertical),
                resources.getDimensionPixelSize(R.dimen.padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.padding_vertical)
            )
            addView(TextView(context).apply {
                setTextAppearance2(R.style.TextAppearance_Widget_AppCompat_Toolbar_Subtitle)
                text = buildSpannedString {
                    append("See it on ")
                    inSpans(URLSpan(BuildConfig.RELEASE_WEBSITE)) {
                        append("GitHub")
                    }
                }
                movementMethod = LinkMovementMethod()
            })
        })
        .setNegativeButton(android.R.string.ok, null)
        .create()

    private companion object {

        fun TextView.setTextAppearance2(resId: Int) = when {
            Build.VERSION.SDK_INT >= 23 -> setTextAppearance(resId)
            else -> @Suppress("DEPRECATION") setTextAppearance(context, resId)
        }
    }
}