package com.example.dynamic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.R

class DummyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummy)
        setSupportActionBar(findViewById(R.id.toolbar))
    }
}
