package com.example.collapsingtoolbarlayoutsubtitle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.collapsingtoolbarlayoutsubtitle.R
import kotlinx.android.synthetic.main.activity_main.*

class DummyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy)
        setSupportActionBar(toolbar)
    }
}
