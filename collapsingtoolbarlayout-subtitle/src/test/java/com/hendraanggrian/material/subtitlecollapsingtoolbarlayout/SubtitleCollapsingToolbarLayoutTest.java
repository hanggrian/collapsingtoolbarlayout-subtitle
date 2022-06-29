package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
public class SubtitleCollapsingToolbarLayoutTest {

    private AppCompatActivity activity;
    private SubtitleCollapsingToolbarLayout toolbarLayout;

    @Before
    public void setUpActivityAndResources() {
        activity = Robolectric.buildActivity(TestActivity.class).setup().get();
        toolbarLayout = (SubtitleCollapsingToolbarLayout) activity.getLayoutInflater()
            .inflate(com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.
                R.layout.test_subtitlecollapsingtoolbarlayout, null);
    }

    @Test
    public void title() {
        assertNull(toolbarLayout.getTitle());
        assertNull(toolbarLayout.getSubtitle());

        toolbarLayout.setTitle("Title");
        toolbarLayout.setSubtitle("Subtitle");
        assertTrue(toolbarLayout.isTitleEnabled());
        assertEquals("Title", toolbarLayout.getTitle());
        assertEquals("Subtitle", toolbarLayout.getSubtitle());

        toolbarLayout.setTitleEnabled(false);
        assertFalse(toolbarLayout.isTitleEnabled());
        assertNull(toolbarLayout.getTitle());
        assertNull(toolbarLayout.getSubtitle());
    }

    @Test
    public void scrim() {
        contentScrim.test();
        statusBarScrim.test();

        assertEquals(0, toolbarLayout.getScrimVisibleHeightTrigger());
        assertEquals(600, toolbarLayout.getScrimAnimationDuration());

        toolbarLayout.setScrimVisibleHeightTrigger(10);
        toolbarLayout.setScrimAnimationDuration(20);
        assertEquals(10, toolbarLayout.getScrimVisibleHeightTrigger());
        assertEquals(20, toolbarLayout.getScrimAnimationDuration());
    }

    @Test
    public void gravity() {
        assertEquals(GravityCompat.START | Gravity.CENTER_VERTICAL, toolbarLayout.getCollapsedTitleGravity());
        assertEquals(GravityCompat.START | Gravity.BOTTOM, toolbarLayout.getExpandedTitleGravity());

        toolbarLayout.setCollapsedTitleGravity(Gravity.TOP);
        toolbarLayout.setExpandedTitleGravity(Gravity.BOTTOM);
        assertEquals(Gravity.TOP, toolbarLayout.getCollapsedTitleGravity());
        assertEquals(Gravity.BOTTOM, toolbarLayout.getExpandedTitleGravity());
    }

    @Test
    public void typeface() {
        assertEquals(Typeface.DEFAULT, toolbarLayout.getCollapsedTitleTypeface());
        assertEquals(Typeface.DEFAULT, toolbarLayout.getCollapsedSubtitleTypeface());
        assertEquals(Typeface.DEFAULT, toolbarLayout.getExpandedTitleTypeface());
        assertEquals(Typeface.DEFAULT, toolbarLayout.getExpandedSubtitleTypeface());

        AssetManager assets = activity.getAssets();
        Typeface bold1 = Typeface.createFromAsset(assets, "OpenSans-Bold.ttf");
        Typeface regular1 = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf");
        Typeface bold2 = Typeface.createFromAsset(assets, "Lato-Bold.ttf");
        Typeface regular2 = Typeface.createFromAsset(assets, "Lato-Regular.ttf");
        toolbarLayout.setCollapsedTitleTypeface(bold1);
        toolbarLayout.setCollapsedSubtitleTypeface(regular1);
        toolbarLayout.setExpandedTitleTypeface(bold2);
        toolbarLayout.setExpandedSubtitleTypeface(regular2);
        assertEquals(bold1, toolbarLayout.getCollapsedTitleTypeface());
        assertEquals(regular1, toolbarLayout.getCollapsedSubtitleTypeface());
        assertEquals(bold2, toolbarLayout.getExpandedTitleTypeface());
        assertEquals(regular2, toolbarLayout.getExpandedSubtitleTypeface());
    }

    @Test
    public void margin() {
        float defaultMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 32f, activity.getResources().getDisplayMetrics());
        assertEquals(defaultMargin, toolbarLayout.getExpandedTitleMarginStart(), 0);
        assertEquals(defaultMargin, toolbarLayout.getExpandedTitleMarginTop(), 0);
        assertEquals(defaultMargin, toolbarLayout.getExpandedTitleMarginEnd(), 0);
        assertEquals(defaultMargin, toolbarLayout.getExpandedTitleMarginBottom(), 0);

        toolbarLayout.setExpandedTitleMargin(1, 2, 3, 4);
        assertEquals(1, toolbarLayout.getExpandedTitleMarginStart());
        assertEquals(2, toolbarLayout.getExpandedTitleMarginTop());
        assertEquals(3, toolbarLayout.getExpandedTitleMarginEnd());
        assertEquals(4, toolbarLayout.getExpandedTitleMarginBottom());

        toolbarLayout.setExpandedTitleMarginStart(5);
        toolbarLayout.setExpandedTitleMarginTop(6);
        toolbarLayout.setExpandedTitleMarginEnd(7);
        toolbarLayout.setExpandedTitleMarginBottom(8);
        assertEquals(5, toolbarLayout.getExpandedTitleMarginStart());
        assertEquals(6, toolbarLayout.getExpandedTitleMarginTop());
        assertEquals(7, toolbarLayout.getExpandedTitleMarginEnd());
        assertEquals(8, toolbarLayout.getExpandedTitleMarginBottom());
    }

    @Test
    public void maxLines() {
        assertEquals(1, toolbarLayout.getMaxLines());

        toolbarLayout.setMaxLines(2);
        assertEquals(2, toolbarLayout.getMaxLines());
    }

    private final ScrimTester contentScrim = new ScrimTester() {
        @NonNull
        @Override
        Context getContext() {
            return activity;
        }

        @Override
        void set(@Nullable Drawable drawable) {
            toolbarLayout.setContentScrim(drawable);
        }

        @Override
        void setColor(int color) {
            toolbarLayout.setContentScrimColor(color);
        }

        @Override
        void setResources(int res) {
            toolbarLayout.setContentScrimResource(res);
        }

        @Nullable
        @Override
        Drawable get() {
            return toolbarLayout.getContentScrim();
        }
    };
    private final ScrimTester statusBarScrim = new ScrimTester() {
        @NonNull
        @Override
        Context getContext() {
            return activity;
        }

        @Override
        void set(@Nullable Drawable drawable) {
            toolbarLayout.setStatusBarScrim(drawable);
        }

        @Override
        void setColor(int color) {
            toolbarLayout.setStatusBarScrimColor(color);
        }

        @Override
        void setResources(int res) {
            toolbarLayout.setStatusBarScrimResource(res);
        }

        @Nullable
        @Override
        Drawable get() {
            return toolbarLayout.getStatusBarScrim();
        }
    };
}
