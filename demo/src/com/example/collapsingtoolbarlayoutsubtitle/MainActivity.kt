package com.example.collapsingtoolbarlayoutsubtitle

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.get
import com.flipboard.bottomsheet.commons.MenuSheetView
import com.flipboard.bottomsheet.commons.MenuSheetView.MenuType.GRID
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var menuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbarLayout.setExpandedTitleTextColor(Color.WHITE)
        toolbarLayout.setExpandedSubtitleTextColor(Color.WHITE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.article, menu)
        menuItem = menu[0]
        return true
    }

    fun onClick(view: View) {
        sheetLayout.showWithSheetView(MenuSheetView(this, GRID, "Demo") {
            when (it.itemId) {
                R.id.title -> inputDialog("Title") { toolbarLayout.title = it }
                R.id.subtitle -> inputDialog("Subtitle") { toolbarLayout.subtitle = it }
            }
            true
        }.apply { inflateMenu(R.menu.activity_main) })
    }
}