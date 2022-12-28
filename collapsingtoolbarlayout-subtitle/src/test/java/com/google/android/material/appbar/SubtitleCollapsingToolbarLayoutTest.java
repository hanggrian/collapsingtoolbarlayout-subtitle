package com.google.android.material.appbar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowInsetsCompat;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.ScrimTester;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.TestActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Tests for {@link SubtitleCollapsingToolbarLayout}, sorted by original class.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
public class SubtitleCollapsingToolbarLayoutTest {

  private AppCompatActivity activity;
  private SubtitleCollapsingToolbarLayout layout;

  @Before
  public void setup() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    layout = new SubtitleCollapsingToolbarLayout(activity);
  }

  @Test
  public void onWindowInsetChanged() {
    WindowInsetsCompat insets = layout.onWindowInsetChanged(WindowInsetsCompat.CONSUMED);
    assertEquals(WindowInsetsCompat.CONSUMED, insets);
  }

  @Test
  public void text() {
    assertNull(layout.getTitle());
    layout.setTitle("Title");
    assertEquals("Title", layout.getTitle());

    assertNull(layout.getSubtitle());
    layout.setSubtitle("Subtitle");
    assertEquals("Subtitle", layout.getSubtitle());

    assertTrue(layout.isTitleEnabled());
    layout.setTitleEnabled(false);
    assertFalse(layout.isTitleEnabled());
    assertNull(layout.getTitle());
    assertNull(layout.getSubtitle());
  }

  @Test
  public void titleCollapseMode() {
    assertEquals(CollapsingToolbarLayout.TITLE_COLLAPSE_MODE_SCALE, layout.getTitleCollapseMode());
    layout.setTitleCollapseMode(CollapsingToolbarLayout.TITLE_COLLAPSE_MODE_FADE);
    assertEquals(CollapsingToolbarLayout.TITLE_COLLAPSE_MODE_FADE, layout.getTitleCollapseMode());
  }

  @Test
  public void scrim() {
    contentScrim.test();
    statusBarScrim.test();

    assertEquals(0, layout.getScrimVisibleHeightTrigger());
    layout.setScrimVisibleHeightTrigger(10);
    assertEquals(10, layout.getScrimVisibleHeightTrigger());

    assertEquals(600, layout.getScrimAnimationDuration());
    layout.setScrimAnimationDuration(20);
    assertEquals(20, layout.getScrimAnimationDuration());

    assertEquals(255, layout.getScrimAlpha());
    layout.setScrimAlpha(0);
    assertEquals(0, layout.getScrimAlpha());
  }

  @Test
  public void visibility() {
    layout.setVisibility(View.VISIBLE);
    layout.setStatusBarScrimColor(Color.RED);
    assertTrue(layout.statusBarScrim.isVisible());
    layout.setVisibility(View.GONE);
    assertFalse(layout.statusBarScrim.isVisible());

    layout.setVisibility(View.VISIBLE);
    layout.setContentScrimColor(Color.GREEN);
    assertTrue(layout.getContentScrim().isVisible());
    layout.setVisibility(View.GONE);
    assertFalse(layout.getContentScrim().isVisible());
  }

  @Test
  public void gravity() {
    assertEquals(GravityCompat.START | Gravity.CENTER_VERTICAL, layout.getCollapsedTitleGravity());
    layout.setCollapsedTitleGravity(Gravity.TOP);
    assertEquals(Gravity.TOP, layout.getCollapsedTitleGravity());

    assertEquals(GravityCompat.START | Gravity.BOTTOM, layout.getExpandedTitleGravity());
    layout.setExpandedTitleGravity(Gravity.BOTTOM);
    assertEquals(Gravity.BOTTOM, layout.getExpandedTitleGravity());
  }

  @Test
  public void typeface() {
    final AssetManager assets = activity.getAssets();

    assertEquals(Typeface.DEFAULT, layout.getCollapsedTitleTypeface());
    final Typeface bold = Typeface.createFromAsset(assets, "OpenSans-Bold.ttf");
    layout.setCollapsedTitleTypeface(bold);
    assertEquals(bold, layout.getCollapsedTitleTypeface());

    assertEquals(Typeface.DEFAULT, layout.getCollapsedSubtitleTypeface());
    final Typeface extraBold = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf");
    layout.setCollapsedSubtitleTypeface(extraBold);
    assertEquals(extraBold, layout.getCollapsedSubtitleTypeface());

    assertEquals(Typeface.DEFAULT, layout.getExpandedTitleTypeface());
    final Typeface light = Typeface.createFromAsset(assets, "OpenSans-Light.ttf");
    layout.setExpandedTitleTypeface(light);
    assertEquals(light, layout.getExpandedTitleTypeface());

    assertEquals(Typeface.DEFAULT, layout.getExpandedSubtitleTypeface());
    final Typeface medium = Typeface.createFromAsset(assets, "OpenSans-Medium.ttf");
    layout.setExpandedSubtitleTypeface(medium);
    assertEquals(medium, layout.getExpandedSubtitleTypeface());
  }

  @Test
  public void margin() {
    float defaultMargin = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 32f, activity.getResources().getDisplayMetrics());
    assertEquals(defaultMargin, layout.getExpandedTitleMarginStart(), 0);
    assertEquals(defaultMargin, layout.getExpandedTitleMarginTop(), 0);
    assertEquals(defaultMargin, layout.getExpandedTitleMarginEnd(), 0);
    assertEquals(defaultMargin, layout.getExpandedTitleMarginBottom(), 0);

    layout.setExpandedTitleMargin(1, 2, 3, 4);
    assertEquals(1, layout.getExpandedTitleMarginStart());
    assertEquals(2, layout.getExpandedTitleMarginTop());
    assertEquals(3, layout.getExpandedTitleMarginEnd());
    assertEquals(4, layout.getExpandedTitleMarginBottom());

    layout.setExpandedTitleMarginStart(5);
    assertEquals(5, layout.getExpandedTitleMarginStart());
    layout.setExpandedTitleMarginTop(6);
    assertEquals(6, layout.getExpandedTitleMarginTop());
    layout.setExpandedTitleMarginEnd(7);
    assertEquals(7, layout.getExpandedTitleMarginEnd());
    layout.setExpandedTitleMarginBottom(8);
    assertEquals(8, layout.getExpandedTitleMarginBottom());
  }

  @Test
  public void maxLines() {
    assertEquals(1, layout.getTitleMaxLines());
    layout.setTitleMaxLines(2);
    assertEquals(2, layout.getTitleMaxLines());

    assertEquals(1, layout.getSubtitleMaxLines());
    layout.setSubtitleMaxLines(3);
    assertEquals(3, layout.getSubtitleMaxLines());
  }

  @Test
  public void rtlTextDirectionHeuristicsEnabled() {
    assertFalse(layout.isRtlTextDirectionHeuristicsEnabled());
    layout.setRtlTextDirectionHeuristicsEnabled(true);
    assertTrue(layout.isRtlTextDirectionHeuristicsEnabled());
  }

  @Test
  public void forceApplySystemWindowInsetTop() {
    assertFalse(layout.isForceApplySystemWindowInsetTop());
    layout.setForceApplySystemWindowInsetTop(true);
    assertTrue(layout.isForceApplySystemWindowInsetTop());
  }

  @Test
  public void extraMultilineHeightEnabled() {
    assertFalse(layout.isTitleExtraMultilineHeightEnabled());
    layout.setTitleExtraMultilineHeightEnabled(true);
    assertTrue(layout.isTitleExtraMultilineHeightEnabled());

    assertFalse(layout.isSubtitleExtraMultilineHeightEnabled());
    layout.setSubtitleExtraMultilineHeightEnabled(true);
    assertTrue(layout.isSubtitleExtraMultilineHeightEnabled());
  }

  @Test
  public void positionInterpolator() {
    assertNull(layout.getTitlePositionInterpolator());
    final TimeInterpolator interpolator = input -> 0;
    layout.setTitlePositionInterpolator(interpolator);
    assertEquals(interpolator, layout.getTitlePositionInterpolator());
  }

  private final ScrimTester contentScrim = new ScrimTester() {
    @NonNull
    @Override
    public Context getContext() {
      return activity;
    }

    @Override
    public void set(@Nullable Drawable drawable) {
      layout.setContentScrim(drawable);
    }

    @Override
    public void setColor(int color) {
      layout.setContentScrimColor(color);
    }

    @Override
    public void setResources(int res) {
      layout.setContentScrimResource(res);
    }

    @Nullable
    @Override
    public Drawable get() {
      return layout.getContentScrim();
    }
  };
  private final ScrimTester statusBarScrim = new ScrimTester() {
    @NonNull
    @Override
    public Context getContext() {
      return activity;
    }

    @Override
    public void set(@Nullable Drawable drawable) {
      layout.setStatusBarScrim(drawable);
    }

    @Override
    public void setColor(int color) {
      layout.setStatusBarScrimColor(color);
    }

    @Override
    public void setResources(int res) {
      layout.setStatusBarScrimResource(res);
    }

    @Nullable
    @Override
    public Drawable get() {
      return layout.getStatusBarScrim();
    }
  };
}
