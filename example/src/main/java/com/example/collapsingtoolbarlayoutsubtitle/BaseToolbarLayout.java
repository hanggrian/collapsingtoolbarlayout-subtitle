package com.example.collapsingtoolbarlayoutsubtitle;

import android.support.annotation.Nullable;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
interface BaseToolbarLayout {

    void setTitle(@Nullable CharSequence text);

    void setSubtitle(@Nullable CharSequence text);

    void setExpandedTitleGravity(int gravity);

    void setCollapsedTitleGravity(int gravity);
}