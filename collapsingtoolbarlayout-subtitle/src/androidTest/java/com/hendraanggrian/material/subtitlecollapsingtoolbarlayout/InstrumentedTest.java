package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    @Rule public ActivityTestRule<TestActivity> rule = new ActivityTestRule(TestActivity.class, false, true);

    @Test
    public void title() {
        onView(withId(R.id.toolbarLayout))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> {
                view.setTitle("Title");
                view.setSubtitle("Subtitle");
            }))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view -> {
                assertEquals("Title", view.getTitle());
                assertEquals("Subtitle", view.getSubtitle());
            }))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> view.setTitleEnabled(false)))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view -> assertFalse(view.isTitleEnabled())));
    }

    @Test
    public void contentScrim() {
        final Drawable drawable = new ColorDrawable(Color.RED);
        onView(withId(R.id.toolbarLayout))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> view.setContentScrim(drawable)))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view ->
                assertEquals(drawable, view.getContentScrim())));
    }

    @Test
    public void statusScrim() {
        final Drawable drawable = new ColorDrawable(Color.GREEN);
        onView(withId(R.id.toolbarLayout))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> view.setStatusBarScrim(drawable)))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view ->
                assertEquals(drawable, view.getStatusBarScrim())));
    }

    @Test
    public void gravity() {
        onView(withId(R.id.toolbarLayout))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> {
                view.setCollapsedTitleGravity(Gravity.TOP);
                view.setExpandedTitleGravity(Gravity.BOTTOM);
            }))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view -> {
                assertEquals(Gravity.TOP, view.getCollapsedTitleGravity());
                assertEquals(Gravity.BOTTOM, view.getExpandedTitleGravity());
            }));
    }

    @Test
    public void typefaces() {
        AssetManager assets = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets();
        Typeface bold1 = Typeface.createFromAsset(assets, "OpenSans-Bold.ttf");
        Typeface regular1 = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf");
        Typeface bold2 = Typeface.createFromAsset(assets, "Lato-Bold.ttf");
        Typeface regular2 = Typeface.createFromAsset(assets, "Lato-Regular.ttf");
        onView(withId(R.id.toolbarLayout))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> {
                view.setCollapsedTitleTypeface(bold1);
                view.setCollapsedSubtitleTypeface(regular1);
                view.setExpandedTitleTypeface(bold2);
                view.setExpandedSubtitleTypeface(regular2);
            }))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view -> {
                assertEquals(bold1, view.getCollapsedTitleTypeface());
                assertEquals(regular1, view.getCollapsedSubtitleTypeface());
                assertEquals(bold2, view.getExpandedTitleTypeface());
                assertEquals(regular2, view.getExpandedSubtitleTypeface());
            }));
    }

    @Test
    public void margins() {
        onView(withId(R.id.toolbarLayout))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view ->
                view.setExpandedTitleMargin(1, 2, 3, 4)))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view -> {
                assertEquals(1, view.getExpandedTitleMarginStart());
                assertEquals(2, view.getExpandedTitleMarginTop());
                assertEquals(3, view.getExpandedTitleMarginEnd());
                assertEquals(4, view.getExpandedTitleMarginBottom());
            }))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> {
                view.setExpandedTitleMarginStart(5);
                view.setExpandedTitleMarginTop(6);
                view.setExpandedTitleMarginEnd(7);
                view.setExpandedTitleMarginBottom(8);
            }))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view -> {
                assertEquals(5, view.getExpandedTitleMarginStart());
                assertEquals(6, view.getExpandedTitleMarginTop());
                assertEquals(7, view.getExpandedTitleMarginEnd());
                assertEquals(8, view.getExpandedTitleMarginBottom());
            }));
    }

    @Test
    public void scrims() {
        onView(withId(R.id.toolbarLayout))
            .perform(Views.perform(SubtitleCollapsingToolbarLayout.class, view -> {
                view.setScrimVisibleHeightTrigger(10);
                view.setScrimAnimationDuration(20);
            }))
            .check(Views.<SubtitleCollapsingToolbarLayout>check(view -> {
                assertEquals(10, view.getScrimVisibleHeightTrigger());
                assertEquals(20, view.getScrimAnimationDuration());
            }));
    }
}
