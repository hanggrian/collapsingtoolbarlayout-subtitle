package com.example.collapsingtoolbarlayoutsubtitle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import butterknife.BindView
import com.hendraanggrian.bundler.Bundler

class MainActivity : BaseActivity() {

    override val contentView: Int
        get() = R.layout.activity_main

    @BindView(R.id.button_main_default) lateinit var buttonDefault: Button
    @BindView(R.id.button_main_subtitle) lateinit var buttonSubtitle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonDefault.setOnClickListener { start(R.layout.activity_article_default) }
        buttonSubtitle.setOnClickListener { start(R.layout.activity_article_subtitle) }
    }

    fun start(layoutRes: Int) {
        startActivity(Intent(this, ArticleActivity::class.java)
                .putExtras(Bundler.wrap(ArticleActivity::class.java, layoutRes)))
    }
}