package com.example.collapsingtoolbarlayoutsubtitle

import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.SubtitleCollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import butterknife.BindView
import com.afollestad.materialdialogs.MaterialDialog
import com.hendraanggrian.bundler.BindExtra
import com.hendraanggrian.support.utils.widget.Toasts
import java.util.*

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class ArticleActivity : BaseActivity(), View.OnClickListener {

    override val contentView: Int
        get() = layoutRes

    @JvmField @BindExtra var layoutRes: Int = 0
    @BindView(R.id.collapsingtoolbarlayout_article) lateinit var collapsingToolbarLayout: View
    @BindView(R.id.toolbar_article) lateinit var toolbar: Toolbar
    @BindView(R.id.floatingactionbutton_article) lateinit var floatingActionButton: FloatingActionButton
    var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        floatingActionButton.setOnClickListener(this)

        if (collapsingToolbarLayout is SubtitleCollapsingToolbarLayout) {
            (collapsingToolbarLayout as SubtitleCollapsingToolbarLayout).setCollapsedTitleColorAttr(R.attr.colorAccent)
        }
    }

    override fun onClick(v: View) {
        val toolbarLayout = toolbarLayout
        MaterialDialog.Builder(this)
                .items(OPTION_SET_TITLE,
                        OPTION_SET_SUBTITLE,
                        OPTION_SET_EXPANDED_GRAVITY,
                        OPTION_SET_COLLAPSED_GRAVITY,
                        OPTION_DISABLE_BACK_BUTTON,
                        OPTION_TOGGLE_MENU_ITEM)
                .itemsCallback { _, _, _, text ->
                    when (text.toString()) {
                        OPTION_SET_TITLE, OPTION_SET_SUBTITLE -> MaterialDialog.Builder(this@ArticleActivity)
                                .input("Text", "") { _, input ->
                                    when (text.toString()) {
                                        OPTION_SET_TITLE -> toolbarLayout.setTitle(input)
                                        OPTION_SET_SUBTITLE -> toolbarLayout.setSubtitle(input)
                                    }
                                }
                                .show()
                        OPTION_SET_EXPANDED_GRAVITY, OPTION_SET_COLLAPSED_GRAVITY -> MaterialDialog.Builder(this@ArticleActivity)
                                .items(GRAVITY.keys)
                                .itemsCallbackMultiChoice(null) { dialog, which, texts ->
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
                                            OPTION_SET_EXPANDED_GRAVITY -> toolbarLayout.setExpandedTitleGravity(flags)
                                            OPTION_SET_COLLAPSED_GRAVITY -> toolbarLayout.setCollapsedTitleGravity(flags)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

    internal val toolbarLayout: BaseToolbarLayout
        get() = object : BaseToolbarLayout {
            override fun setTitle(text: CharSequence?) {
                if (collapsingToolbarLayout is CollapsingToolbarLayout)
                    (collapsingToolbarLayout as CollapsingToolbarLayout).title = text
                else if (collapsingToolbarLayout is SubtitleCollapsingToolbarLayout)
                    (collapsingToolbarLayout as SubtitleCollapsingToolbarLayout).title = text
            }

            override fun setSubtitle(text: CharSequence?) {
                if (collapsingToolbarLayout is CollapsingToolbarLayout)
                    Toasts.showShort(this@ArticleActivity, "Unsupported.")
                else if (collapsingToolbarLayout is SubtitleCollapsingToolbarLayout)
                    (collapsingToolbarLayout as SubtitleCollapsingToolbarLayout).subtitle = text
            }

            override fun setExpandedTitleGravity(gravity: Int) {
                if (collapsingToolbarLayout is CollapsingToolbarLayout)
                    (collapsingToolbarLayout as CollapsingToolbarLayout).expandedTitleGravity = gravity
                else if (collapsingToolbarLayout is SubtitleCollapsingToolbarLayout)
                    (collapsingToolbarLayout as SubtitleCollapsingToolbarLayout).expandedTitleGravity = gravity
            }

            override fun setCollapsedTitleGravity(gravity: Int) {
                if (collapsingToolbarLayout is CollapsingToolbarLayout)
                    (collapsingToolbarLayout as CollapsingToolbarLayout).collapsedTitleGravity = gravity
                else if (collapsingToolbarLayout is SubtitleCollapsingToolbarLayout)
                    (collapsingToolbarLayout as SubtitleCollapsingToolbarLayout).collapsedTitleGravity = gravity
            }
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