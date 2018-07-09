package com.example.subtitlecollapsingtoolbarlayout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hendraanggrian.material.errorbar.errorbar
import kotlinx.android.synthetic.main.activity_main.*

class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        coordinatorLayout.errorbar("Tap tune button to customize toolbar layout") {
            setContentMarginLeft(resources.getDimensionPixelOffset(R.dimen.padding_vertical))
            setContentMarginRight(resources.getDimensionPixelOffset(R.dimen.padding_vertical))
        }
    }

    fun onClick(v: View) {
    }
}