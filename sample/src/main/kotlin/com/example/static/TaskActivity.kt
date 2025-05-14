package com.example.static

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import com.example.R
import com.google.android.material.tabs.TabLayout

class TaskActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var tabs: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        toolbar = findViewById(R.id.toolbar)
        tabs = findViewById(R.id.tabs)
        setSupportActionBar(toolbar)

        tabs.addTab(tabs.newTab().setText("Overview"))
        tabs.addTab(tabs.newTab().setText("Messages"))

        WindowCompat
            .getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_task, menu)
        return true
    }
}
