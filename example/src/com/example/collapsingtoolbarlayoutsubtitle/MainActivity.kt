package com.example.collapsingtoolbarlayoutsubtitle

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import kota.contents.getAttrColor
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        floatingActionButton.setOnClickListener(this)
        toolbarLayout.setCollapsedTitleColor(getAttrColor(R.attr.colorAccent))
    }

    override fun onClick(v: View) {
        MaterialDialog.Builder(this)
                .items(OPTION_SET_TITLE,
                        OPTION_SET_SUBTITLE,
                        OPTION_SET_EXPANDED_GRAVITY,
                        OPTION_SET_COLLAPSED_GRAVITY,
                        OPTION_DISABLE_BACK_BUTTON,
                        OPTION_TOGGLE_MENU_ITEM)
                .itemsCallback { _, _, _, text ->
                    when (text.toString()) {
                        OPTION_SET_TITLE, OPTION_SET_SUBTITLE -> MaterialDialog.Builder(this@MainActivity)
                                .input("Text", "") { _, input ->
                                    when (text.toString()) {
                                        OPTION_SET_TITLE -> toolbarLayout.title = input
                                        OPTION_SET_SUBTITLE -> toolbarLayout.subtitle = input
                                    }
                                }
                                .show()
                        OPTION_SET_EXPANDED_GRAVITY, OPTION_SET_COLLAPSED_GRAVITY -> MaterialDialog.Builder(this@MainActivity)
                                .items(GRAVITY.keys)
                                .itemsCallbackMultiChoice(null) { _, _, texts ->
                                    var flags: Int? = null
                                    for (text in texts) {
                                        val flag = GRAVITY[text]
                                        if (flags == null)
                                            flags = flag
                                        else
                                            flags = flags or flag as Int
                                    }
                                    if (flags != null) {
                                        when (text.toString()) {
                                            OPTION_SET_EXPANDED_GRAVITY -> toolbarLayout.expandedTitleGravity = flags
                                            OPTION_SET_COLLAPSED_GRAVITY -> toolbarLayout.collapsedTitleGravity = flags
                                        }
                                    }
                                    false
                                }
                                .positiveText(android.R.string.ok)
                                .show()
                        OPTION_DISABLE_BACK_BUTTON -> toolbar.navigationIcon = null
                        OPTION_TOGGLE_MENU_ITEM -> menuItem!!.isVisible = !menuItem!!.isVisible
                    }
                }
                .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.article, menu)
        menuItem = menu.findItem(R.id.item_article_bookmark)
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        private val OPTION_SET_TITLE = "Set title"
        private val OPTION_SET_SUBTITLE = "Set subtitle"
        private val OPTION_SET_EXPANDED_GRAVITY = "Set expanded gravity"
        private val OPTION_SET_COLLAPSED_GRAVITY = "Set collapsed gravity"
        private val OPTION_DISABLE_BACK_BUTTON = "Disable back button"
        private val OPTION_TOGGLE_MENU_ITEM = "Toggle menu item visible"
        private val GRAVITY = LinkedHashMap<CharSequence, Int>()

        init {
            GRAVITY.put("START", Gravity.START)
            GRAVITY.put("TOP", Gravity.TOP)
            GRAVITY.put("END", Gravity.END)
            GRAVITY.put("BOTTOM", Gravity.BOTTOM)
            GRAVITY.put("CENTER_HORIZONTAL", Gravity.CENTER_HORIZONTAL)
            GRAVITY.put("CENTER_VERTICAL", Gravity.CENTER_VERTICAL)
            GRAVITY.put("CENTER", Gravity.CENTER)
        }
    }
}