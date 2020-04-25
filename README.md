[![download](https://api.bintray.com/packages/hendraanggrian/material/collapsingtoolbarlayout-subtitle/images/download.svg)](https://bintray.com/hendraanggrian/material/collapsingtoolbarlayout-subtitle/_latestVersion)
[![build](https://travis-ci.com/hendraanggrian/collapsingtoolbarlayout-subtitle.svg)](https://travis-ci.com/hendraanggrian/collapsingtoolbarlayout-subtitle)
[![license](https://img.shields.io/github/license/hendraanggrian/collapsingtoolbarlayout-subtitle)](http://www.apache.org/licenses/LICENSE-2.0)

SubtitleCollapsingToolbarLayout
===============================
![example_light][example_light]
![example_dark][example_dark]

Standard CollapsingToolbarLayout with subtitle support.
* Just like the title, subtitle text will have to be **1 line**.
* When collapsed, a subtitle would still appear as Toolbar's.
* Separate configuration for title and subtitle: text color, gravity, etc.

### Caveats
Since it uses a lot of CollapsingToolbarLayout resources and API, there are a few: 
* Only safe to use with the same version of material components.
* Deceptive package name.

### Also...
It is detabable if we even need this library.
If the material guidelines says it's ok to have a multiline text in toolbar layout,
then they surely would've already implemented such feature.
If it doesn't say anything about subtitle (which is odds because Toolbar has it),
then we probably shouldn't use it out of respect to the guidelines.

Download
--------
This library follows [AndroidX's revisions][androidx-rn].

```gradle
repositories {
    google()
    jcenter()
}

dependencies {
    implementation "com.google.android.material:material:$version"
    implementation "com.hendraanggrian.material:collapsingtoolbarlayout-subtitle:$version"
}
```

For older support library, use [legacy artifacts](https://bintray.com/hendraanggrian/maven/collapsingtoolbarlayout-subtitle).

```gradle
implementation "com.android.support:design:$version"
implementation "com.hendraanggrian:collapsingtoolbarlayout-subtitle:$version"
```

Usage
-----
Treat `SubtitleCollapsingToolbarLayout` just like a regular `CollapsingToolbarLayout`.

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
            android:id="@+id/subtitlecollapsingtoolbarlayout"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:contentScrim="?colorPrimary"
            app:layout_scrollFlags="scroll|exitUntilCollapsed"
            app:subtitle="Papua, Indonesia"
            app:title="Raja Ampat">

            <!-- collapsing toolbar content goes here -->

            <androidx.appcompat.widget.Toolbar
                android:layout_width="match_parent"
                android:layout_height="?actionBarSize"
                app:layout_collapseMode="pin"/>
        </android.support.design.widget.SubtitleCollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>

    <!-- content goes here -->

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Attributes
`SubtitleCollapsingToolbarLayout` has all the attributes of `CollapsingToolbarLayout`,
and a few extras.

| Attribute                         | Description                                | Default value/behavior                               |
|-----------------------------------|--------------------------------------------|------------------------------------------------------|
| `subtitle`                        | subtitle text                              | disabled                                             |
| `collapsedSubtitleTextAppearance` | text appearance of subtitle when collapsed | `TextAppearance.AppCompat.Widget.ActionBar.Subtitle` |
| `expandedSubtitleTextAppearance`  | text appearance of subtitle when expanded  | `TextAppearance.AppCompat.Headline`                  |

[example_light]: /art/example_light.gif
[example_dark]: /art/example_dark.gif
[androidx-rn]: https://developer.android.com/topic/libraries/support-library/androidx-rn
