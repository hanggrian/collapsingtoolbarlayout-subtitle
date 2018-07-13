package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.jakewharton.processphoenix.ProcessPhoenix

fun Context.reset(preferences: SharedPreferences = getDefaultSharedPreferences(this)) {
    preferences.edit(true) { clear() }
    ProcessPhoenix.triggerRebirth(this)
}