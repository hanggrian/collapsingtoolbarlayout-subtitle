package com.example.collapsingtoolbarlayoutsubtitle

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
internal interface BaseToolbarLayout {

    fun setTitle(text: CharSequence?)

    fun setSubtitle(text: CharSequence?)

    fun setExpandedTitleGravity(gravity: Int)

    fun setCollapsedTitleGravity(gravity: Int)
}