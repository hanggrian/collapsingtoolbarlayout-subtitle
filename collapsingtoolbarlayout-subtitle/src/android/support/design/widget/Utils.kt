@file:JvmName("UtilsKt")
@file:Suppress("NOTHING_TO_INLINE")

import android.content.Context
import android.content.res.Configuration

internal inline fun Context.isScreenSizeAtLeast(size: Int): Boolean = (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK).let { screenSize ->
    screenSize != Configuration.SCREENLAYOUT_SIZE_UNDEFINED && screenSize >= size
}