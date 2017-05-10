package com.example.collapsingtoolbarlayoutsubtitle;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
interface BaseToolbarLayout {

    void setTitle(CharSequence text);

    void setExpandedTitleGravity(int gravity);

    void setCollapsedTitleGravity(int gravity);
}