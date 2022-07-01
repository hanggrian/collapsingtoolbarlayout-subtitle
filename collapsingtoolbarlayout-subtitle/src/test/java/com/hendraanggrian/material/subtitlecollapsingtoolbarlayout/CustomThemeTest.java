package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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
public class CustomThemeTest {

    private AppCompatActivity activity;
    private SubtitleCollapsingToolbarLayout toolbarLayout;

    @Before
    public void setUpActivityAndResources() {
        activity = Robolectric.buildActivity(CustomThemeActivity.class).setup().get();
        toolbarLayout = (SubtitleCollapsingToolbarLayout) activity.getLayoutInflater()
            .inflate(com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.
                R.layout.test_subtitlecollapsingtoolbarlayout, null);
    }

    @Test
    public void text() {
        assertEquals("Title", toolbarLayout.getTitle());
        assertEquals("Subtitle", toolbarLayout.getSubtitle());
    }

    @Test
    public void collapseMode() {
        assertEquals(CollapsingToolbarLayout.TITLE_COLLAPSE_MODE_FADE, toolbarLayout.getTitleCollapseMode());
    }

    @Test
    public void scrim() {
        final ColorDrawable contentDrawable = new ColorDrawable(Color.RED);
        contentDrawable.setAlpha(0);
        final ColorDrawable statusBarDrawable = new ColorDrawable(Color.GREEN);
        statusBarDrawable.setAlpha(0);
        assertEquals(contentDrawable.getColor(), ((ColorDrawable) toolbarLayout.getContentScrim()).getColor());
        assertEquals(statusBarDrawable.getColor(), ((ColorDrawable) toolbarLayout.getStatusBarScrim()).getColor());
    }

    @Test
    public void gravity() {
        assertEquals(GravityCompat.END, toolbarLayout.getCollapsedTitleGravity());
        assertEquals(GravityCompat.START, toolbarLayout.getExpandedTitleGravity());
    }

    @Test
    public void margin() {
        float margin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f, activity.getResources().getDisplayMetrics());
        assertEquals(margin, toolbarLayout.getExpandedTitleMarginStart(), 0);
        assertEquals(margin, toolbarLayout.getExpandedTitleMarginTop(), 0);
        assertEquals(margin, toolbarLayout.getExpandedTitleMarginEnd(), 0);
        assertEquals(margin, toolbarLayout.getExpandedTitleMarginBottom(), 0);
    }

    @Test
    public void maxLines() {
        assertEquals(2, toolbarLayout.getTitleMaxLines());
        assertEquals(3, toolbarLayout.getSubtitleMaxLines());
    }

    @Test
    public void forceApplySystemWindowInsetTop() {
        assertTrue(toolbarLayout.isForceApplySystemWindowInsetTop());
    }

    @Test
    public void extraMultilineHeightEnabled() {
        assertTrue(toolbarLayout.isTitleExtraMultilineHeightEnabled());
        assertTrue(toolbarLayout.isSubtitleExtraMultilineHeightEnabled());
    }
}
