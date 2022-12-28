package com.google.android.material.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.animation.TimeInterpolator;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.TestActivity;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Tests for {@link CollapsingTextHelper2}, sorted by original class.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
public class CollapsingTextHelper2Test {

  private AppCompatActivity activity;
  private CollapsingTextHelper2 helper;

  @Before
  public void setup() {
    activity = Robolectric.buildActivity(TestActivity.class).setup().get();
    helper = new CollapsingTextHelper2(new View(activity));
  }

  @Test
  public void positionInterpolator() {
    assertNull(helper.getPositionInterpolator());
    final TimeInterpolator interpolator = input -> 0;
    helper.setPositionInterpolator(interpolator);
    assertEquals(interpolator, helper.getPositionInterpolator());
  }

  @Test
  public void textSize() {
    assertEquals(15f, helper.getExpandedTextSize(), 0);
    helper.setExpandedTextSize(1f);
    assertEquals(1f, helper.getExpandedTextSize(), 0);

    assertEquals(15f, helper.getExpandedTextSize2(), 0);
    helper.setExpandedTextSize2(2f);
    assertEquals(2f, helper.getExpandedTextSize2(), 0);

    assertEquals(15f, helper.getCollapsedTextSize(), 0);
    helper.setCollapsedTextSize(3f);
    assertEquals(3f, helper.getCollapsedTextSize(), 0);

    assertEquals(15f, helper.getCollapsedTextSize2(), 0);
    helper.setCollapsedTextSize2(4f);
    assertEquals(4f, helper.getCollapsedTextSize2(), 0);
  }

  @Test
  public void textColor() {
    assertNull(helper.getExpandedTextColor());
    helper.setExpandedTextColor(ColorStateList.valueOf(Color.RED));
    assertEquals(Color.RED, helper.getExpandedTextColor().getDefaultColor());

    assertNull(helper.getExpandedTextColor2());
    helper.setExpandedTextColor2(ColorStateList.valueOf(Color.GREEN));
    assertEquals(Color.GREEN, helper.getExpandedTextColor2().getDefaultColor());

    assertNull(helper.getCollapsedTextColor());
    helper.setCollapsedTextColor(ColorStateList.valueOf(Color.BLUE));
    assertEquals(Color.BLUE, helper.getCollapsedTextColor().getDefaultColor());

    assertNull(helper.getCollapsedTextColor2());
    helper.setCollapsedTextColor2(ColorStateList.valueOf(Color.CYAN));
    assertEquals(Color.CYAN, helper.getCollapsedTextColor2().getDefaultColor());
  }

  @Test
  public void textWidth() {
    assertEquals(0, helper.calculateCollapsedTextWidth(), 0);
    helper.setText("Title");
    assertTrue(helper.calculateCollapsedTextWidth() > 0);

    assertEquals(0, helper.calculateCollapsedTextWidth2(), 0);
    helper.setText2("Subtitle");
    assertTrue(helper.calculateCollapsedTextWidth2() > 0);
  }

  @Test
  public void textHeight() {
    assertEquals(0, helper.getExpandedTextHeight(), 0);
    assertEquals(0, helper.getExpandedTextHeight2(), 0);
    assertEquals(0, helper.getExpandedTextFullHeight(), 0);
    assertEquals(0, helper.getExpandedTextFullHeight2(), 0);
    assertEquals(0, helper.getCollapsedTextHeight(), 0);
    assertEquals(0, helper.getCollapsedTextHeight2(), 0);
  }

  @Test
  public void fadeModeThresholdFraction() {
    assertEquals(0.5, helper.getFadeModeThresholdFraction(), 0);
    helper.setFadeModeStartFraction(1);
    assertEquals(1, helper.getFadeModeThresholdFraction(), 0);
  }

  @Test
  public void gravity() {
    assertEquals(Gravity.CENTER_VERTICAL, helper.getCollapsedTextGravity());
    helper.setCollapsedTextGravity(Gravity.TOP);
    assertEquals(Gravity.TOP, helper.getCollapsedTextGravity());

    assertEquals(Gravity.CENTER_VERTICAL, helper.getExpandedTextGravity());
    helper.setExpandedTextGravity(Gravity.BOTTOM);
    assertEquals(Gravity.BOTTOM, helper.getExpandedTextGravity());
  }

  @Test
  public void textAppearance() {
    helper.setCollapsedTextAppearance(R.style.TextAppearance_Collapsed_Text);
    assertEquals(Color.RED, helper.getCollapsedTextColor().getDefaultColor());
    assertEquals(1, helper.getCollapsedTextSize(), 0);

    helper.setCollapsedTextAppearance2(R.style.TextAppearance_Collapsed_Text2);
    assertEquals(Color.GREEN, helper.getCollapsedTextColor2().getDefaultColor());
    assertEquals(2, helper.getCollapsedTextSize2(), 0);

    helper.setExpandedTextAppearance(R.style.TextAppearance_Expanded_Text);
    assertEquals(Color.BLUE, helper.getExpandedTextColor().getDefaultColor());
    assertEquals(3, helper.getExpandedTextSize(), 0);

    helper.setExpandedTextAppearance2(R.style.TextAppearance_Expanded_Text2);
    assertEquals(Color.YELLOW, helper.getExpandedTextColor2().getDefaultColor());
    assertEquals(4, helper.getExpandedTextSize2(), 0);
  }

  @Test
  public void typeface() {
    final AssetManager assets = activity.getAssets();

    assertEquals(Typeface.DEFAULT, helper.getCollapsedTypeface());
    final Typeface bold = Typeface.createFromAsset(assets, "OpenSans-Bold.ttf");
    helper.setCollapsedTypeface(bold);
    assertEquals(bold, helper.getCollapsedTypeface());

    assertEquals(Typeface.DEFAULT, helper.getCollapsedTypeface2());
    final Typeface extraBold = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf");
    helper.setCollapsedTypeface2(extraBold);
    assertEquals(extraBold, helper.getCollapsedTypeface2());

    assertEquals(Typeface.DEFAULT, helper.getExpandedTypeface());
    final Typeface light = Typeface.createFromAsset(assets, "OpenSans-Light.ttf");
    helper.setExpandedTypeface(light);
    assertEquals(light, helper.getExpandedTypeface());

    assertEquals(Typeface.DEFAULT, helper.getExpandedTypeface2());
    final Typeface medium = Typeface.createFromAsset(assets, "OpenSans-Medium.ttf");
    helper.setExpandedTypeface2(medium);
    assertEquals(medium, helper.getExpandedTypeface2());

    final Typeface regular = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf");
    helper.setTypefaces(regular);
    assertEquals(regular, helper.getCollapsedTypeface());
    assertEquals(regular, helper.getExpandedTypeface());

    final Typeface semiBold = Typeface.createFromAsset(assets, "OpenSans-SemiBold.ttf");
    helper.setTypefaces2(semiBold);
    assertEquals(semiBold, helper.getCollapsedTypeface2());
    assertEquals(semiBold, helper.getExpandedTypeface2());
  }

  @Test
  public void expansionFraction() {
    assertEquals(0f, helper.getExpansionFraction(), 0);
    helper.setExpansionFraction(0.5f);
    assertEquals(0.5f, helper.getExpansionFraction(), 0);
  }

  @Test
  public void isStateful() {
    assertFalse(helper.isStateful());
    helper.setCollapsedTextColor(new ColorStateList(
        new int[][]{
            new int[]{android.R.attr.state_enabled},
            new int[]{-android.R.attr.state_enabled}
        },
        new int[]{Color.RED, Color.GREEN}));
    assertTrue(helper.isStateful());
  }

  @Test
  public void rtlTextDirectionHeuristicsEnabled() {
    assertTrue(helper.isRtlTextDirectionHeuristicsEnabled());
    helper.setRtlTextDirectionHeuristicsEnabled(false);
    assertFalse(helper.isRtlTextDirectionHeuristicsEnabled());
  }

  @Test
  public void currentTextColor() {
    assertEquals(0, helper.getCurrentCollapsedTextColor());
    helper.setCollapsedTextColor(ColorStateList.valueOf(Color.RED));
    assertEquals(Color.RED, helper.getCurrentCollapsedTextColor());

    assertEquals(0, helper.getCurrentCollapsedTextColor2());
    helper.setCollapsedTextColor2(ColorStateList.valueOf(Color.GREEN));
    assertEquals(Color.GREEN, helper.getCurrentCollapsedTextColor2());
  }

  @Test
  public void text() {
    assertNull(helper.getText());
    helper.setText("Title");
    assertEquals("Title", helper.getText());

    assertNull(helper.getText2());
    helper.setText2("Subtitle");
    assertEquals("Subtitle", helper.getText2());
  }

  @Test
  public void maxLines() {
    assertEquals(1, helper.getMaxLines());
    helper.setMaxLines(2);
    assertEquals(2, helper.getMaxLines());

    assertEquals(1, helper.getMaxLines2());
    helper.setMaxLines2(3);
    assertEquals(3, helper.getMaxLines2());
  }
}
