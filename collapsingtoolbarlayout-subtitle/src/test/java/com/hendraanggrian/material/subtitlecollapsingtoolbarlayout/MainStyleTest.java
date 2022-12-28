package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
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

/**
 * Tests for {@link SubtitleCollapsingToolbarLayout} with custom styling, sorted by original class.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
public class MainStyleTest {

  private AppCompatActivity activity;
  private SubtitleCollapsingToolbarLayout toolbarLayout;

  @Before
  public void setup() {
    activity = Robolectric.buildActivity(StyleTestActivity.class).setup().get();
    toolbarLayout = (SubtitleCollapsingToolbarLayout) activity.getLayoutInflater()
        .inflate(R.layout.test_subtitlecollapsingtoolbarlayout, null);
  }

  @Test
  public void margin() {
    assertEquals(5, toolbarLayout.getExpandedTitleMarginStart(), 0);
    assertEquals(5, toolbarLayout.getExpandedTitleMarginTop(), 0);
    assertEquals(5, toolbarLayout.getExpandedTitleMarginEnd(), 0);
    assertEquals(5, toolbarLayout.getExpandedTitleMarginBottom(), 0);
  }

  @Test
  public void scrim() {
    final ColorDrawable contentDrawable = new ColorDrawable(Color.RED);
    contentDrawable.setAlpha(0);
    final ColorDrawable statusBarDrawable = new ColorDrawable(Color.GREEN);
    statusBarDrawable.setAlpha(0);
    assertEquals(contentDrawable.getColor(),
        ((ColorDrawable) toolbarLayout.getContentScrim()).getColor());
    assertEquals(statusBarDrawable.getColor(),
        ((ColorDrawable) toolbarLayout.getStatusBarScrim()).getColor());

    assertEquals(10, toolbarLayout.getScrimVisibleHeightTrigger());
    assertEquals(20, toolbarLayout.getScrimAnimationDuration());
  }

  @Test
  public void gravity() {
    assertEquals(GravityCompat.END, toolbarLayout.getCollapsedTitleGravity());
    assertEquals(GravityCompat.START, toolbarLayout.getExpandedTitleGravity());
  }

  @Test
  public void text() {
    assertEquals("Title", toolbarLayout.getTitle());
    assertEquals("Subtitle", toolbarLayout.getSubtitle());
  }

  @Test
  public void collapseMode() {
    assertEquals(
        CollapsingToolbarLayout.TITLE_COLLAPSE_MODE_FADE, toolbarLayout.getTitleCollapseMode());
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

  @Test
  public void positionInterpolator() {
    assertNotNull(toolbarLayout.getTitlePositionInterpolator());
  }

  private static class StyleTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R.style.Theme_Main);
    }
  }
}
