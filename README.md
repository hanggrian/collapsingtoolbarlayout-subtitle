[![Travis CI](https://img.shields.io/travis/com/hendraanggrian/collapsingtoolbarlayout-subtitle)](https://www.travis-ci.com/github/hendraanggrian/collapsingtoolbarlayout-subtitle/)
[![Codecov](https://img.shields.io/codecov/c/github/hendraanggrian/collapsingtoolbarlayout-subtitle)](https://app.codecov.io/gh/hendraanggrian/collapsingtoolbarlayout-subtitle/)
[![Maven Central](https://img.shields.io/maven-central/v/com.hendraanggrian.material/collapsingtoolbarlayout-subtitle)](https://search.maven.org/artifact/com.hendraanggrian.material/collapsingtoolbarlayout-subtitle/)
[![Nexus Snapshot](https://img.shields.io/nexus/s/com.hendraanggrian.material/collapsingtoolbarlayout-subtitle?server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/hendraanggrian/material/collapsingtoolbarlayout-subtitle/)
[![Android SDK](https://img.shields.io/badge/sdk-14%2B-informational)](https://developer.android.com/studio/releases/platforms/#4.0)

# SubtitleCollapsingToolbarLayout

![Light Preview](https://raw.githubusercontent.com/hendraanggrian/collapsingtoolbarlayout-subtitle/assets/preview_light.gif)
![Dark Preview](https://raw.githubusercontent.com/hendraanggrian/collapsingtoolbarlayout-subtitle/assets/preview_dark.gif)

A carbon copy of [CollapsingToolbarLayout](https://developer.android.com/reference/com/google/android/material/appbar/CollapsingToolbarLayout/)
with subtitle support. During collapsed state, the subtitle would still appear as Toolbar's.
There should be no learning curve because it works just like `CollapsingToolbarLayout`.
Supports Material Design 3 styling.

But because this library uses restricted APIs and private resources from [Material Components](https://github.com/material-components/material-components-android/),
there are a few caveats:

- Only safe to use with the same version of material components.
- Deceptive package name.

### Also...

It is detabable if we even need this library.
If the material guidelines says it's ok to have a subtitle in toolbar layout,
then they surely would've already implemented such feature.
If it doesn't say anything about subtitle (which is odds because Toolbar has it),
then we probably shouldn't use it out of respect to the guidelines.

## Download

This library's versioning follows [Material Components releases](https://github.com/material-components/material-components-android/releases/).
Which in turn, follows [AndroidX releases](https://developer.android.com/jetpack/androidx/versions/).

```gradle
repositories {
    mavenCentral()
    google()
}
dependencies {
    implementation "com.hendraanggrian.material:collapsingtoolbarlayout-subtitle:$version"
}
```

## Usage

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
                app:layout_collapseMode="pin" />
        </android.support.design.widget.SubtitleCollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>

    <!-- content goes here -->

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Material Design 3

![Material3 Medium Style](https://raw.githubusercontent.com/hendraanggrian/collapsingtoolbarlayout-subtitle/assets/material3_style_medium.png)
![Material3 Large Style](https://raw.githubusercontent.com/hendraanggrian/collapsingtoolbarlayout-subtitle/assets/material3_style_large.png)

`SubtitleCollapsingToolbarLayout` will automatically switch to Material Design 3 style
by using `Theme.Material3.*` in your app, no configuration needed.

By default, medium style is applied. To use large style, reference an attribute
`subtitleCollapsingToolbarLayoutLargeStyle` in your XML.

```xml
<com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
    style="?attr/subtitleCollapsingToolbarLayoutLargeStyle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```
