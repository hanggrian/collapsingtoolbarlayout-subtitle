[![Travis CI](https://img.shields.io/travis/com/hendraanggrian/collapsingtoolbarlayout-subtitle)](https://www.travis-ci.com/github/hendraanggrian/collapsingtoolbarlayout-subtitle/)
[![Codecov](https://img.shields.io/codecov/c/github/hendraanggrian/collapsingtoolbarlayout-subtitle)](https://app.codecov.io/gh/hendraanggrian/collapsingtoolbarlayout-subtitle/)
[![Maven Central](https://img.shields.io/maven-central/v/com.hendraanggrian/collapsingtoolbarlayout-subtitle)](https://search.maven.org/artifact/com.hendraanggrian/collapsingtoolbarlayout-subtitle/)

# SubtitleCollapsingToolbarLayout

![Light example](images/example_light.gif)
![Dark example](images/example_dark.gif)

Standard CollapsingToolbarLayout with subtitle support.

- When collapsed, a subtitle would still appear as Toolbar's.
- Separate configuration for title and subtitle: text color, gravity, etc.

### Caveats

Since it uses a lot of CollapsingToolbarLayout resources and API, there are a few:

- Doesn't support multiline text, even though `CollapsingToolbarLayout` supports multiline since version `1.2.0`.
- Only safe to use with the same version of material components.
- Deceptive package name.

### Also...

It is detabable if we even need this library.
If the material guidelines says it's ok to have a multiline text in toolbar layout, then they surely would've already implemented such feature.
If it doesn't say anything about subtitle (which is odds because Toolbar has it), then we probably shouldn't use it out of respect to the guidelines.

## Download

This library follows [AndroidX's revisions](https://developer.android.com/topic/libraries/support-library/androidx-rn/).

```gradle
repositories {
    mavenCentral()
    google()
}
dependencies {
    implementation "com.google.android.material:material:$version"
    implementation "com.hendraanggrian.material:collapsingtoolbarlayout-subtitle:$version"
}
```

Snapshots of the development version are available in [Sonatype's snapshots repository](https://s01.oss.sonatype.org/content/repositories/snapshots/).

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

`SubtitleCollapsingToolbarLayout` has all the attributes of `CollapsingToolbarLayout`, and a few extras.

| Attribute                         | Description                                | Default value/behavior                               |
|-----------------------------------|--------------------------------------------|------------------------------------------------------|
| `subtitle`                        | subtitle text                              | disabled                                             |
| `collapsedSubtitleTextAppearance` | text appearance of subtitle when collapsed | `TextAppearance.AppCompat.Widget.ActionBar.Subtitle` |
| `expandedSubtitleTextAppearance`  | text appearance of subtitle when expanded  | `TextAppearance.AppCompat.Headline`                  |
