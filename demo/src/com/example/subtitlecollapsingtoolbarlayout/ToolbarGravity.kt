package com.example.subtitlecollapsingtoolbarlayout

import android.view.Gravity

enum class ToolbarGravity(val value: Int) {
    START(Gravity.START),
    TOP(Gravity.TOP),
    END(Gravity.END),
    BOTTOM(Gravity.BOTTOM),
    CENTER_HORIZONTAL(Gravity.CENTER_HORIZONTAL),
    CENTER_VERTICAL(Gravity.CENTER_VERTICAL),
    CENTER(Gravity.CENTER)
}