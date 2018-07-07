package com.hendraanggrian.collapsingtoolbarlayout.subtitle.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class InstrumentedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.hendraanggrian.collapsingtoolbarlayout.subtitle.test.R.layout.activity_instrumented)
    }
}