package com.example.subtitlecollapsingtoolbarlayout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder

class TintedPreferenceCategory @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @SuppressLint("RestrictedApi") defStyleRes: Int = TypedArrayUtils.getAttr(
        context, R.attr.preferenceCategoryStyle,
        android.R.attr.preferenceCategoryStyle
    ),
    defStyleAttr: Int = 0
) : PreferenceCategory(context, attrs, defStyleRes, defStyleAttr) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView
            .findViewById<TextView>(android.R.id.title)
            .setTextColor(getColor(context, R.color.red))
    }
}