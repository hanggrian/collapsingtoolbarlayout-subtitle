SubtitleCollapsingToolbarLayout
===============================
Standard CollapsingToolbarLayout with subtitle support.

![demo](/art/demo_collapsingtoolbarlayout-subtitle.gif)

Download
--------
```gradle
dependencies {
    compile 'com.hendraanggrian:collapsingtoolbarlayout-subtitle:0.3.0'
}
```

Usage
-----
Treat `SubtitleCollapsingToolbarLayout` just like a regular `CollapsingToolbarLayout`:
```xml
<android.support.design.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <android.support.design.widget.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.hendraanggrian.widget.SubtitleCollapsingToolbarLayout
            android:id="@+id/subtitlecollapsingtoolbarlayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:contentScrim="?colorPrimary"
            app:layout_scrollFlags="scroll|exitUntilCollapsed"
            app:subtitle="CollapsingToolbarLayout"
            app:title="Subtitle">

            <!-- collapsing toolbar content goes here -->

            <android.support.v7.widget.Toolbar
                android:layout_width="match_parent"
                android:layout_height="?actionBarSize"
                app:layout_collapseMode="pin"/>
        </com.hendraanggrian.widget.SubtitleCollapsingToolbarLayout>
    </android.support.design.widget.AppBarLayout>

    <!-- content goes here -->

</android.support.design.widget.CoordinatorLayout>
```

Extra attributes on xml:
 * `app:subtitle` - sets subtitle text
 * `app:collapsedSubtitleTextAppearance` - sets text appearance of subtitle when collapsed, default is `TextAppearance.AppCompat.Widget.ActionBar.Subtitle`
 * `app:expandedSubtitleTextAppearance` - sets text appearance of subtitle when expanded, default is `TextAppearance.AppCompat.Headline`
