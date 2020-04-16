package com.example.subtitlecollapsingtoolbarlayout

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class ExampleFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_example)
    }
}