[![CircleCI](https://img.shields.io/circleci/build/gh/hanggrian/collapsingtoolbarlayout-subtitle)](https://app.circleci.com/pipelines/github/hanggrian/collapsingtoolbarlayout-subtitle/)
[![Codecov](https://img.shields.io/codecov/c/gh/hanggrian/collapsingtoolbarlayout-subtitle)](https://app.codecov.io/gh/hanggrian/collapsingtoolbarlayout-subtitle/)
[![Maven Central](https://img.shields.io/maven-central/v/com.hendraanggrian.material/collapsingtoolbarlayout-subtitle)](https://central.sonatype.com/artifact/com.hendraanggrian.material/collapsingtoolbarlayout-subtitle/)
[![Android SDK](https://img.shields.io/badge/sdk-14%2B-informational)](https://developer.android.com/studio/releases/platforms/#4.0) \
[![Figma](https://img.shields.io/badge/design-figma-f24e1e)](https://www.figma.com/community/file/1504588079447609935/)
[![Layers](https://img.shields.io/badge/showcase-layers-000)](https://layers.to/layers/cmap7zppt0008ii0cdrujw1wl/)
[![Pinterest](https://img.shields.io/badge/pin-pinterest-bd081c)](https://www.pinterest.com/pin/1107322627133947562/)

# SubtitleCollapsingToolbarLayout

![](https://github.com/hendraanggrian/collapsingtoolbarlayout-subtitle/raw/assets/preview_material.gif "Material preview")
![](https://github.com/hendraanggrian/collapsingtoolbarlayout-subtitle/raw/assets/preview_material3.gif "Material You preview")

A carbon copy of [CollapsingToolbarLayout](https://developer.android.com/reference/com/google/android/material/appbar/CollapsingToolbarLayout/)
with subtitle support. During collapsed state, the subtitle would still appear
as Toolbar's. There should be no learning curve because it works just like
`CollapsingToolbarLayout`. Supports Material Design 3 styling.

But because this library uses restricted APIs and private resources from [Material Components](https://github.com/material-components/material-components-android/),
there are a few caveats:

- Only safe to use with the same version of material components.
- Deceptive package name.

### Also...

It is detabable if we even need this library. If the material guidelines says
it's ok to have a subtitle in toolbar layout, then they surely would've already
implemented such feature. If it doesn't say anything about subtitle (which is
odds because Toolbar has it), then we probably shouldn't use it out of respect
to the guidelines.

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

Treat `SubtitleCollapsingToolbarLayout` just like a regular
`CollapsingToolbarLayout`.

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
        app:layout_collapseMode="pin"/>
    </android.support.design.widget.SubtitleCollapsingToolbarLayout>
  </com.google.android.material.appbar.AppBarLayout>

  <!-- content goes here -->

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Material Design 3

![Material3 medium style.](https://github.com/hendraanggrian/collapsingtoolbarlayout-subtitle/raw/assets/material3_style_medium.png)
![Material3 large style.](https://github.com/hendraanggrian/collapsingtoolbarlayout-subtitle/raw/assets/material3_style_large.png)

`SubtitleCollapsingToolbarLayout` will automatically switch to Material Design 3
style by using `Theme.Material3.*` in your app, no configuration needed.

By default, medium style is applied. To use large style, reference an attribute
`subtitleCollapsingToolbarLayoutLargeStyle` in your XML.

```xml
<com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
  style="?attr/subtitleCollapsingToolbarLayoutLargeStyle"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"/>
```
