SubtitleCollapsingToolbarLayout
===============================
[![bintray](https://img.shields.io/badge/bintray-material-brightgreen.svg)](https://bintray.com/hendraanggrian/material)
[![download](https://api.bintray.com/packages/hendraanggrian/material/collapsingtoolbarlayout-subtitle/images/download.svg)](https://bintray.com/hendraanggrian/material/collapsingtoolbarlayout-subtitle/_latestVersion)
[![build](https://travis-ci.com/hendraanggrian/collapsingtoolbarlayout-subtitle.svg)](https://travis-ci.com/hendraanggrian/collapsingtoolbarlayout-subtitle)
[![license](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

![demo1][demo1]
![demo2][demo2]

Standard `CollapsingToolbarLayout` with subtitle support. Using internal
components of support design library allows it to behave similarly to its
sibling `CollapsingToolbarLayout`, while also ensuring minimum library size.

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

For older support library, use legacy artifact.

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

#### Attributes
`SubtitleCollapsingToolbarLayout` has all the attributes of `CollapsingToolbarLayout`,
and a few extras.

| Attribute                         | Description                                | Default value/behavior                               |
|-----------------------------------|--------------------------------------------|------------------------------------------------------|
| `subtitle`                        | subtitle text                              | disabled                                             |
| `collapsedSubtitleTextAppearance` | text appearance of subtitle when collapsed | `TextAppearance.AppCompat.Widget.ActionBar.Subtitle` |
| `expandedSubtitleTextAppearance`  | text appearance of subtitle when expanded  | `TextAppearance.AppCompat.Headline`                  |

License
-------
    Copyright 2017 Hendra Anggrian

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[demo1]: /art/demo1.gif
[demo2]: /art/demo2.gif
[androidx-rn]: https://developer.android.com/topic/libraries/support-library/androidx-rn
