package com.google.android.material.internal;

import android.animation.TimeInterpolator;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.math.MathUtils;
import androidx.core.text.TextDirectionHeuristicsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.StaticLayoutBuilderCompat.StaticLayoutBuilderCompatException;
import com.google.android.material.resources.CancelableFontCallback;
import com.google.android.material.resources.CancelableFontCallback.ApplyFont;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.resources.TypefaceUtils;

import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.Layout.Alignment.ALIGN_NORMAL;
import static android.text.Layout.Alignment.ALIGN_OPPOSITE;
import static androidx.core.util.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Helper class for rendering and animating collapsed title and subtitle.
 *
 * <p>Fields and methods for subtitle is suffixed with `2` for better readability with compare tool.
 *
 * @see CollapsingTextHelper
 */
public final class CollapsingTextHelper2 {
  // Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
  // by using our own texture
  private static final boolean USE_SCALING_TEXTURE = VERSION.SDK_INT < 18;
  private static final String TAG = "CollapsingTextHelper2";
  private static final String ELLIPSIS_NORMAL = "\u2026"; // HORIZONTAL ELLIPSIS (…)

  private static final float FADE_MODE_THRESHOLD_FRACTION_RELATIVE = 0.5f;

  private static final boolean DEBUG_DRAW = false;
  @NonNull private static final Paint DEBUG_DRAW_PAINT;

  static {
    DEBUG_DRAW_PAINT = DEBUG_DRAW ? new Paint() : null;
    if (DEBUG_DRAW_PAINT != null) {
      DEBUG_DRAW_PAINT.setAntiAlias(true);
      DEBUG_DRAW_PAINT.setColor(Color.MAGENTA);
    }
  }

  private final View view;

  private boolean drawTitle;
  private float expandedFraction;
  private boolean fadeModeEnabled;
  private float fadeModeStartFraction;
  private float fadeModeThresholdFraction;
  private int currentOffsetY;

  @NonNull private final Rect expandedBounds;
  @NonNull private final Rect collapsedBounds;
  @NonNull private final RectF currentBounds;
  private int expandedTextGravity = Gravity.CENTER_VERTICAL;
  private int collapsedTextGravity = Gravity.CENTER_VERTICAL;
  private float expandedTextSize = 15, expandedTextSize2 = 15;
  private float collapsedTextSize = 15, collapsedTextSize2 = 15;
  private ColorStateList expandedTextColor, expandedTextColor2;
  private ColorStateList collapsedTextColor, collapsedTextColor2;
  private int expandedLineCount, expandedLineCount2;
  private float expandedDrawY, expandedDrawY2;
  private float collapsedDrawY, collapsedDrawY2;
  private float expandedDrawX, expandedDrawX2;
  private float collapsedDrawX, collapsedDrawX2;
  private float currentDrawX, currentDrawX2;
  private float currentDrawY, currentDrawY2;
  private Typeface collapsedTypeface, collapsedTypeface2;
  private Typeface collapsedTypefaceBold, collapsedTypefaceBold2;
  private Typeface collapsedTypefaceDefault, collapsedTypefaceDefault2;
  private Typeface expandedTypeface, expandedTypeface2;
  private Typeface expandedTypefaceBold, expandedTypefaceBold2;
  private Typeface expandedTypefaceDefault, expandedTypefaceDefault2;
  private Typeface currentTypeface, currentTypeface2;
  private CancelableFontCallback expandedFontCallback, expandedFontCallback2;
  private CancelableFontCallback collapsedFontCallback, collapsedFontCallback2;
  @Nullable private CharSequence text, text2;
  @Nullable private CharSequence textToDraw, textToDraw2;
  private boolean isRtl;
  private boolean isRtlTextDirectionHeuristicsEnabled = true;

  private boolean useTexture;
  @Nullable private Bitmap expandedTitleTexture, expandedTitleTexture2;
  private Paint texturePaint, texturePaint2;

  // USING THE SAME SCALE WILL RESULT IN JITTERING
  private float scale, scale2;
  private float currentTextSize, currentTextSize2;
  private float currentShadowRadius, currentShadowRadius2;
  private float currentShadowDx, currentShadowDx2;
  private float currentShadowDy, currentShadowDy2;
  private int currentShadowColor, currentShadowColor2;

  private int[] state;

  private boolean boundsChanged;

  @NonNull private final TextPaint textPaint, textPaint2;
  @NonNull private final TextPaint tmpPaint, tmpPaint2;

  private TimeInterpolator positionInterpolator;
  private TimeInterpolator textSizeInterpolator;

  private float collapsedShadowRadius, collapsedShadowRadius2;
  private float collapsedShadowDx, collapsedShadowDx2;
  private float collapsedShadowDy, collapsedShadowDy2;
  private ColorStateList collapsedShadowColor, collapsedShadowColor2;
  private float expandedShadowRadius, expandedShadowRadius2;
  private float expandedShadowDx, expandedShadowDx2;
  private float expandedShadowDy, expandedShadowDy2;
  private ColorStateList expandedShadowColor, expandedShadowColor2;
  private float collapsedLetterSpacing, collapsedLetterSpacing2;
  private float expandedLetterSpacing, expandedLetterSpacing2;
  private float currentLetterSpacing, currentLetterSpacing2;
  private StaticLayout textLayout, textLayout2;
  private float collapsedTextWidth, collapsedTextWidth2;
  private float collapsedTextBlend, collapsedTextBlend2;
  private float expandedTextBlend, expandedTextBlend2;
  private CharSequence textToDrawCollapsed, textToDrawCollapsed2;
  private int maxLines = 1, maxLines2 = 1;
  private float lineSpacingAdd = StaticLayoutBuilderCompat.DEFAULT_LINE_SPACING_ADD,
      lineSpacingAdd2 = StaticLayoutBuilderCompat.DEFAULT_LINE_SPACING_ADD;
  private float lineSpacingMultiplier = StaticLayoutBuilderCompat.DEFAULT_LINE_SPACING_MULTIPLIER,
      lineSpacingMultiplier2 = StaticLayoutBuilderCompat.DEFAULT_LINE_SPACING_MULTIPLIER;
  private int hyphenationFrequency = StaticLayoutBuilderCompat.DEFAULT_HYPHENATION_FREQUENCY,
      hyphenationFrequency2 = StaticLayoutBuilderCompat.DEFAULT_HYPHENATION_FREQUENCY;

  public CollapsingTextHelper2(View view) {
    this.view = view;

    textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    textPaint2 = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    tmpPaint = new TextPaint(textPaint);
    tmpPaint2 = new TextPaint(textPaint2);

    collapsedBounds = new Rect();
    expandedBounds = new Rect();
    currentBounds = new RectF();

    fadeModeThresholdFraction = calculateFadeModeThresholdFraction();
    maybeUpdateFontWeightAdjustment(view.getContext().getResources().getConfiguration());
    maybeUpdateFontWeightAdjustment2(view.getContext().getResources().getConfiguration());
  }

  public void setTextSizeInterpolator(TimeInterpolator interpolator) {
    textSizeInterpolator = interpolator;
    recalculate();
  }

  public void setPositionInterpolator(TimeInterpolator interpolator) {
    positionInterpolator = interpolator;
    recalculate();
  }

  @Nullable
  public TimeInterpolator getPositionInterpolator() {
    return positionInterpolator;
  }

  public void setExpandedTextSize(float textSize) {
    if (expandedTextSize != textSize) {
      expandedTextSize = textSize;
      recalculate();
    }
  }

  public void setExpandedTextSize2(float textSize) {
    if (expandedTextSize2 != textSize) {
      expandedTextSize2 = textSize;
      recalculate();
    }
  }

  public void setCollapsedTextSize(float textSize) {
    if (collapsedTextSize != textSize) {
      collapsedTextSize = textSize;
      recalculate();
    }
  }

  public void setCollapsedTextSize2(float textSize) {
    if (collapsedTextSize2 != textSize) {
      collapsedTextSize2 = textSize;
      recalculate();
    }
  }

  public void setCollapsedTextColor(ColorStateList textColor) {
    if (collapsedTextColor != textColor) {
      collapsedTextColor = textColor;
      recalculate();
    }
  }

  public void setCollapsedTextColor2(ColorStateList textColor) {
    if (collapsedTextColor2 != textColor) {
      collapsedTextColor2 = textColor;
      recalculate();
    }
  }

  public void setExpandedTextColor(ColorStateList textColor) {
    if (expandedTextColor != textColor) {
      expandedTextColor = textColor;
      recalculate();
    }
  }

  public void setExpandedTextColor2(ColorStateList textColor) {
    if (expandedTextColor2 != textColor) {
      expandedTextColor2 = textColor;
      recalculate();
    }
  }

  public void setExpandedLetterSpacing(float letterSpacing) {
    if (expandedLetterSpacing != letterSpacing) {
      expandedLetterSpacing = letterSpacing;
      recalculate();
    }
  }

  public void setExpandedLetterSpacing2(float letterSpacing) {
    if (expandedLetterSpacing2 != letterSpacing) {
      expandedLetterSpacing2 = letterSpacing;
      recalculate();
    }
  }

  public void setExpandedBounds(int left, int top, int right, int bottom) {
    if (!rectEquals(expandedBounds, left, top, right, bottom)) {
      expandedBounds.set(left, top, right, bottom);
      boundsChanged = true;
      onBoundsChanged();
    }
  }

  public void setExpandedBounds(@NonNull Rect bounds) {
    setExpandedBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
  }

  public void setCollapsedBounds(int left, int top, int right, int bottom) {
    if (!rectEquals(collapsedBounds, left, top, right, bottom)) {
      collapsedBounds.set(left, top, right, bottom);
      boundsChanged = true;
      onBoundsChanged();
    }
  }

  public void setCollapsedBounds(@NonNull Rect bounds) {
    setCollapsedBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
  }

  public void getCollapsedTextActualBounds(@NonNull RectF bounds, int labelWidth, int textGravity) {
    isRtl = calculateIsRtl(text);
    bounds.left = getCollapsedTextLeftBound(labelWidth, textGravity);
    bounds.top = collapsedBounds.top;
    bounds.right = getCollapsedTextRightBound(bounds, labelWidth, textGravity);
    bounds.bottom = collapsedBounds.top + getCollapsedTextHeight();
  }

  public void getCollapsedTextActualBounds2(
      @NonNull RectF bounds, int labelWidth, int textGravity) {
    isRtl = calculateIsRtl(text2);
    bounds.left = getCollapsedTextLeftBound2(labelWidth, textGravity);
    bounds.top = collapsedBounds.top;
    bounds.right = getCollapsedTextRightBound2(bounds, labelWidth, textGravity);
    bounds.bottom = collapsedBounds.top + getCollapsedTextHeight2();
  }

  private float getCollapsedTextLeftBound(int width, int gravity) {
    if (gravity == Gravity.CENTER
        || (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
      return width / 2f - collapsedTextWidth / 2;
    } else if ((gravity & Gravity.END) == Gravity.END
        || (gravity & Gravity.RIGHT) == Gravity.RIGHT) {
      return isRtl ? collapsedBounds.left : (collapsedBounds.right - collapsedTextWidth);
    } else {
      return isRtl ? (collapsedBounds.right - collapsedTextWidth) : collapsedBounds.left;
    }
  }

  private float getCollapsedTextLeftBound2(int width, int gravity) {
    if (gravity == Gravity.CENTER
        || (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
      return width / 2f - collapsedTextWidth2 / 2;
    } else if ((gravity & Gravity.END) == Gravity.END
        || (gravity & Gravity.RIGHT) == Gravity.RIGHT) {
      return isRtl ? collapsedBounds.left : (collapsedBounds.right - collapsedTextWidth2);
    } else {
      return isRtl ? (collapsedBounds.right - collapsedTextWidth2) : collapsedBounds.left;
    }
  }

  private float getCollapsedTextRightBound(@NonNull RectF bounds, int width, int gravity) {
    if (gravity == Gravity.CENTER
        || (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
      return width / 2f + collapsedTextWidth / 2;
    } else if ((gravity & Gravity.END) == Gravity.END
        || (gravity & Gravity.RIGHT) == Gravity.RIGHT) {
      return isRtl ? (bounds.left + collapsedTextWidth) : collapsedBounds.right;
    } else {
      return isRtl ? collapsedBounds.right : (bounds.left + collapsedTextWidth);
    }
  }

  private float getCollapsedTextRightBound2(@NonNull RectF bounds, int width, int gravity) {
    if (gravity == Gravity.CENTER
        || (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
      return width / 2f + collapsedTextWidth2 / 2;
    } else if ((gravity & Gravity.END) == Gravity.END
        || (gravity & Gravity.RIGHT) == Gravity.RIGHT) {
      return isRtl ? (bounds.left + collapsedTextWidth2) : collapsedBounds.right;
    } else {
      return isRtl ? collapsedBounds.right : (bounds.left + collapsedTextWidth2);
    }
  }

  public float calculateCollapsedTextWidth() {
    if (text == null) {
      return 0;
    }
    getTextPaintCollapsed(tmpPaint);
    return tmpPaint.measureText(text, 0, text.length());
  }

  public float calculateCollapsedTextWidth2() {
    if (text2 == null) {
      return 0;
    }
    getTextPaintCollapsed2(tmpPaint2);
    return tmpPaint2.measureText(text2, 0, text2.length());
  }

  public float getExpandedTextHeight() {
    getTextPaintExpanded(tmpPaint);
    // Return expanded height measured from the baseline.
    return -tmpPaint.ascent();
  }

  public float getExpandedTextHeight2() {
    getTextPaintExpanded2(tmpPaint2);
    // Return expanded height measured from the baseline.
    return -tmpPaint2.ascent();
  }

  public float getExpandedTextFullHeight() {
    getTextPaintExpanded(tmpPaint);
    // Return expanded height measured from the baseline.
    return -tmpPaint.ascent() + tmpPaint.descent();
  }

  public float getExpandedTextFullHeight2() {
    getTextPaintExpanded2(tmpPaint2);
    // Return expanded height measured from the baseline.
    return -tmpPaint2.ascent() + tmpPaint2.descent();
  }

  public float getCollapsedTextHeight() {
    getTextPaintCollapsed(tmpPaint);
    // Return collapsed height measured from the baseline.
    return -tmpPaint.ascent();
  }

  public float getCollapsedTextHeight2() {
    getTextPaintCollapsed2(tmpPaint2);
    // Return collapsed height measured from the baseline.
    return -tmpPaint2.ascent();
  }

  public void setCurrentOffsetY(int currentOffsetY) {
    this.currentOffsetY = currentOffsetY;
  }

  public void setFadeModeStartFraction(float fadeModeStartFraction) {
    this.fadeModeStartFraction = fadeModeStartFraction;
    fadeModeThresholdFraction = calculateFadeModeThresholdFraction();
  }

  private float calculateFadeModeThresholdFraction() {
    return fadeModeStartFraction
        + (1 - fadeModeStartFraction) * FADE_MODE_THRESHOLD_FRACTION_RELATIVE;
  }

  public void setFadeModeEnabled(boolean fadeModeEnabled) {
    this.fadeModeEnabled = fadeModeEnabled;
  }

  private void getTextPaintExpanded(@NonNull TextPaint textPaint) {
    textPaint.setTextSize(expandedTextSize);
    textPaint.setTypeface(expandedTypeface);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      textPaint.setLetterSpacing(expandedLetterSpacing);
    }
  }

  private void getTextPaintExpanded2(@NonNull TextPaint textPaint) {
    textPaint.setTextSize(expandedTextSize2);
    textPaint.setTypeface(expandedTypeface2);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      textPaint.setLetterSpacing(expandedLetterSpacing2);
    }
  }

  private void getTextPaintCollapsed(@NonNull TextPaint textPaint) {
    textPaint.setTextSize(collapsedTextSize);
    textPaint.setTypeface(collapsedTypeface);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      textPaint.setLetterSpacing(collapsedLetterSpacing);
    }
  }

  private void getTextPaintCollapsed2(@NonNull TextPaint textPaint) {
    textPaint.setTextSize(collapsedTextSize2);
    textPaint.setTypeface(collapsedTypeface2);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      textPaint.setLetterSpacing(collapsedLetterSpacing2);
    }
  }

  void onBoundsChanged() {
    drawTitle =
        collapsedBounds.width() > 0
            && collapsedBounds.height() > 0
            && expandedBounds.width() > 0
            && expandedBounds.height() > 0;
  }

  public void setExpandedTextGravity(int gravity) {
    if (expandedTextGravity != gravity) {
      expandedTextGravity = gravity;
      recalculate();
    }
  }

  public int getExpandedTextGravity() {
    return expandedTextGravity;
  }

  public void setCollapsedTextGravity(int gravity) {
    if (collapsedTextGravity != gravity) {
      collapsedTextGravity = gravity;
      recalculate();
    }
  }

  public int getCollapsedTextGravity() {
    return collapsedTextGravity;
  }

  public void setCollapsedTextAppearance(int resId) {
    TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);

    if (textAppearance.getTextColor() != null) {
      collapsedTextColor = textAppearance.getTextColor();
    }
    if (textAppearance.getTextSize() != 0) {
      collapsedTextSize = textAppearance.getTextSize();
    }
    if (textAppearance.shadowColor != null) {
      collapsedShadowColor = textAppearance.shadowColor;
    }
    collapsedShadowDx = textAppearance.shadowDx;
    collapsedShadowDy = textAppearance.shadowDy;
    collapsedShadowRadius = textAppearance.shadowRadius;
    collapsedLetterSpacing = textAppearance.letterSpacing;

    // Cancel pending async fetch, if any, and replace with a new one.
    if (collapsedFontCallback != null) {
      collapsedFontCallback.cancel();
    }
    collapsedFontCallback =
        new CancelableFontCallback(
            new ApplyFont() {
              @Override
              public void apply(Typeface font) {
                setCollapsedTypeface(font);
              }
            },
            textAppearance.getFallbackFont());
    textAppearance.getFontAsync(view.getContext(), collapsedFontCallback);

    recalculate();
  }

  public void setCollapsedTextAppearance2(int resId) {
    TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);

    if (textAppearance.getTextColor() != null) {
      collapsedTextColor2 = textAppearance.getTextColor();
    }
    if (textAppearance.getTextSize() != 0) {
      collapsedTextSize2 = textAppearance.getTextSize();
    }
    if (textAppearance.shadowColor != null) {
      collapsedShadowColor2 = textAppearance.shadowColor;
    }
    collapsedShadowDx2 = textAppearance.shadowDx;
    collapsedShadowDy2 = textAppearance.shadowDy;
    collapsedShadowRadius2 = textAppearance.shadowRadius;
    collapsedLetterSpacing2 = textAppearance.letterSpacing;

    // Cancel pending async fetch, if any, and replace with a new one.
    if (collapsedFontCallback2 != null) {
      collapsedFontCallback2.cancel();
    }
    collapsedFontCallback2 =
        new CancelableFontCallback(
            new ApplyFont() {
              @Override
              public void apply(Typeface font) {
                setCollapsedTypeface2(font);
              }
            },
            textAppearance.getFallbackFont());
    textAppearance.getFontAsync(view.getContext(), collapsedFontCallback2);

    recalculate();
  }

  public void setExpandedTextAppearance(int resId) {
    TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);
    if (textAppearance.getTextColor() != null) {
      expandedTextColor = textAppearance.getTextColor();
    }
    if (textAppearance.getTextSize() != 0) {
      expandedTextSize = textAppearance.getTextSize();
    }
    if (textAppearance.shadowColor != null) {
      expandedShadowColor = textAppearance.shadowColor;
    }
    expandedShadowDx = textAppearance.shadowDx;
    expandedShadowDy = textAppearance.shadowDy;
    expandedShadowRadius = textAppearance.shadowRadius;
    expandedLetterSpacing = textAppearance.letterSpacing;

    // Cancel pending async fetch, if any, and replace with a new one.
    if (expandedFontCallback != null) {
      expandedFontCallback.cancel();
    }
    expandedFontCallback =
        new CancelableFontCallback(
            new ApplyFont() {
              @Override
              public void apply(Typeface font) {
                setExpandedTypeface(font);
              }
            },
            textAppearance.getFallbackFont());
    textAppearance.getFontAsync(view.getContext(), expandedFontCallback);

    recalculate();
  }

  public void setExpandedTextAppearance2(int resId) {
    TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);
    if (textAppearance.getTextColor() != null) {
      expandedTextColor2 = textAppearance.getTextColor();
    }
    if (textAppearance.getTextSize() != 0) {
      expandedTextSize2 = textAppearance.getTextSize();
    }
    if (textAppearance.shadowColor != null) {
      expandedShadowColor2 = textAppearance.shadowColor;
    }
    expandedShadowDx2 = textAppearance.shadowDx;
    expandedShadowDy2 = textAppearance.shadowDy;
    expandedShadowRadius2 = textAppearance.shadowRadius;
    expandedLetterSpacing2 = textAppearance.letterSpacing;

    // Cancel pending async fetch, if any, and replace with a new one.
    if (expandedFontCallback2 != null) {
      expandedFontCallback2.cancel();
    }
    expandedFontCallback2 =
        new CancelableFontCallback(
            new ApplyFont() {
              @Override
              public void apply(Typeface font) {
                setExpandedTypeface2(font);
              }
            },
            textAppearance.getFallbackFont());
    textAppearance.getFontAsync(view.getContext(), expandedFontCallback2);

    recalculate();
  }

  public void setCollapsedTypeface(Typeface typeface) {
    if (setCollapsedTypefaceInternal(typeface)) {
      recalculate();
    }
  }

  public void setCollapsedTypeface2(Typeface typeface) {
    if (setCollapsedTypefaceInternal2(typeface)) {
      recalculate();
    }
  }

  public void setExpandedTypeface(Typeface typeface) {
    if (setExpandedTypefaceInternal(typeface)) {
      recalculate();
    }
  }

  public void setExpandedTypeface2(Typeface typeface) {
    if (setExpandedTypefaceInternal2(typeface)) {
      recalculate();
    }
  }

  public void setTypefaces(Typeface typeface) {
    boolean collapsedFontChanged = setCollapsedTypefaceInternal(typeface);
    boolean expandedFontChanged = setExpandedTypefaceInternal(typeface);
    if (collapsedFontChanged || expandedFontChanged) {
      recalculate();
    }
  }

  public void setTypefaces2(Typeface typeface) {
    boolean collapsedFontChanged = setCollapsedTypefaceInternal2(typeface);
    boolean expandedFontChanged = setExpandedTypefaceInternal2(typeface);
    if (collapsedFontChanged || expandedFontChanged) {
      recalculate();
    }
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private boolean setCollapsedTypefaceInternal(Typeface typeface) {
    // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
    // already updated one when async op comes back after a while.
    if (collapsedFontCallback != null) {
      collapsedFontCallback.cancel();
    }
    if (collapsedTypefaceDefault != typeface) {
      collapsedTypefaceDefault = typeface;
      collapsedTypefaceBold =
          TypefaceUtils.maybeCopyWithFontWeightAdjustment(
              view.getContext().getResources().getConfiguration(), typeface);
      collapsedTypeface =
          collapsedTypefaceBold == null ? collapsedTypefaceDefault : collapsedTypefaceBold;
      return true;
    }
    return false;
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private boolean setCollapsedTypefaceInternal2(Typeface typeface) {
    // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
    // already updated one when async op comes back after a while.
    if (collapsedFontCallback2 != null) {
      collapsedFontCallback2.cancel();
    }
    if (collapsedTypefaceDefault2 != typeface) {
      collapsedTypefaceDefault2 = typeface;
      collapsedTypefaceBold2 =
          TypefaceUtils.maybeCopyWithFontWeightAdjustment(
              view.getContext().getResources().getConfiguration(), typeface);
      collapsedTypeface2 =
          collapsedTypefaceBold2 == null ? collapsedTypefaceDefault2 : collapsedTypefaceBold2;
      return true;
    }
    return false;
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private boolean setExpandedTypefaceInternal(Typeface typeface) {
    // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
    // already updated one when async op comes back after a while.
    if (expandedFontCallback != null) {
      expandedFontCallback.cancel();
    }
    if (expandedTypefaceDefault != typeface) {
      expandedTypefaceDefault = typeface;
      expandedTypefaceBold =
          TypefaceUtils.maybeCopyWithFontWeightAdjustment(
              view.getContext().getResources().getConfiguration(), typeface);
      expandedTypeface =
          expandedTypefaceBold == null ? expandedTypefaceDefault : expandedTypefaceBold;
      return true;
    }
    return false;
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private boolean setExpandedTypefaceInternal2(Typeface typeface) {
    // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
    // already updated one when async op comes back after a while.
    if (expandedFontCallback2 != null) {
      expandedFontCallback2.cancel();
    }
    if (expandedTypefaceDefault2 != typeface) {
      expandedTypefaceDefault2 = typeface;
      expandedTypefaceBold2 =
          TypefaceUtils.maybeCopyWithFontWeightAdjustment(
              view.getContext().getResources().getConfiguration(), typeface);
      expandedTypeface2 =
          expandedTypefaceBold2 == null ? expandedTypefaceDefault2 : expandedTypefaceBold2;
      return true;
    }
    return false;
  }

  public Typeface getCollapsedTypeface() {
    return collapsedTypeface != null ? collapsedTypeface : Typeface.DEFAULT;
  }

  public Typeface getCollapsedTypeface2() {
    return collapsedTypeface2 != null ? collapsedTypeface2 : Typeface.DEFAULT;
  }

  public Typeface getExpandedTypeface() {
    return expandedTypeface != null ? expandedTypeface : Typeface.DEFAULT;
  }

  public Typeface getExpandedTypeface2() {
    return expandedTypeface2 != null ? expandedTypeface2 : Typeface.DEFAULT;
  }

  public void maybeUpdateFontWeightAdjustment(@NonNull Configuration configuration) {
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      if (collapsedTypefaceDefault != null) {
        collapsedTypefaceBold =
            TypefaceUtils.maybeCopyWithFontWeightAdjustment(
                configuration, collapsedTypefaceDefault);
      }
      if (expandedTypefaceDefault != null) {
        expandedTypefaceBold =
            TypefaceUtils.maybeCopyWithFontWeightAdjustment(configuration, expandedTypefaceDefault);
      }
      collapsedTypeface =
          collapsedTypefaceBold != null ? collapsedTypefaceBold : collapsedTypefaceDefault;
      expandedTypeface =
          expandedTypefaceBold != null ? expandedTypefaceBold : expandedTypefaceDefault;
      recalculate(/* forceRecalculate= */ true);
    }
  }

  public void maybeUpdateFontWeightAdjustment2(@NonNull Configuration configuration) {
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      if (collapsedTypefaceDefault2 != null) {
        collapsedTypefaceBold2 =
            TypefaceUtils.maybeCopyWithFontWeightAdjustment(
                configuration, collapsedTypefaceDefault2);
      }
      if (expandedTypefaceDefault2 != null) {
        expandedTypefaceBold2 =
            TypefaceUtils.maybeCopyWithFontWeightAdjustment(
                configuration, expandedTypefaceDefault2);
      }
      collapsedTypeface2 =
          collapsedTypefaceBold2 != null ? collapsedTypefaceBold2 : collapsedTypefaceDefault2;
      expandedTypeface2 =
          expandedTypefaceBold2 != null ? expandedTypefaceBold2 : expandedTypefaceDefault2;
      recalculate(/* forceRecalculate= */ true);
    }
  }

  /**
   * Set the value indicating the current scroll value. This decides how much of the background will
   * be displayed, as well as the title metrics/positioning.
   *
   * <p>A value of {@code 0.0} indicates that the layout is fully expanded. A value of {@code 1.0}
   * indicates that the layout is fully collapsed.
   */
  public void setExpansionFraction(float fraction) {
    fraction = MathUtils.clamp(fraction, 0f, 1f);

    if (fraction != expandedFraction) {
      expandedFraction = fraction;
      calculateCurrentOffsets();
    }
  }

  public boolean setState(final int[] state) {
    this.state = state;

    if (isStateful()) {
      recalculate();
      return true;
    }

    return false;
  }

  public boolean isStateful() {
    return (collapsedTextColor != null && collapsedTextColor.isStateful())
        || (collapsedTextColor2 != null && collapsedTextColor2.isStateful())
        || (expandedTextColor != null && expandedTextColor.isStateful())
        || (expandedTextColor2 != null && expandedTextColor2.isStateful());
  }

  public float getFadeModeThresholdFraction() {
    return fadeModeThresholdFraction;
  }

  public float getExpansionFraction() {
    return expandedFraction;
  }

  public float getCollapsedTextSize() {
    return collapsedTextSize;
  }

  public float getCollapsedTextSize2() {
    return collapsedTextSize2;
  }

  public float getExpandedTextSize() {
    return expandedTextSize;
  }

  public float getExpandedTextSize2() {
    return expandedTextSize2;
  }

  public void setRtlTextDirectionHeuristicsEnabled(boolean rtlTextDirectionHeuristicsEnabled) {
    isRtlTextDirectionHeuristicsEnabled = rtlTextDirectionHeuristicsEnabled;
  }

  public boolean isRtlTextDirectionHeuristicsEnabled() {
    return isRtlTextDirectionHeuristicsEnabled;
  }

  private void calculateCurrentOffsets() {
    calculateOffsets(expandedFraction);
  }

  private void calculateOffsets(final float fraction) {
    interpolateBounds(fraction);
    float textBlendFraction;
    if (fadeModeEnabled) {
      if (fraction < fadeModeThresholdFraction) {
        textBlendFraction = 0F;
        currentDrawX = expandedDrawX;
        currentDrawX2 = expandedDrawX2;
        currentDrawY = expandedDrawY;
        currentDrawY2 = expandedDrawY2;

        setInterpolatedTextSize(/* fraction= */ 0);
        setInterpolatedTextSize2(/* fraction= */ 0);
      } else {
        textBlendFraction = 1F;
        currentDrawX = collapsedDrawX;
        currentDrawX2 = collapsedDrawX2;
        currentDrawY = collapsedDrawY - max(0, currentOffsetY);
        currentDrawY2 = collapsedDrawY2 - max(0, currentOffsetY);

        setInterpolatedTextSize(/* fraction= */ 1);
        setInterpolatedTextSize2(/* fraction= */ 1);
      }
    } else {
      textBlendFraction = fraction;
      currentDrawX = lerp(expandedDrawX, collapsedDrawX, fraction, positionInterpolator);
      currentDrawX2 = lerp(expandedDrawX2, collapsedDrawX2, fraction, positionInterpolator);
      currentDrawY = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);
      currentDrawY2 = lerp(expandedDrawY2, collapsedDrawY2, fraction, positionInterpolator);

      setInterpolatedTextSize(fraction);
      setInterpolatedTextSize2(fraction);
    }

    setCollapsedTextBlend(
        1 - lerp(0, 1, 1 - fraction, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    setCollapsedTextBlend2(
        1 - lerp(0, 1, 1 - fraction, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    setExpandedTextBlend(lerp(1, 0, fraction, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
    setExpandedTextBlend2(lerp(1, 0, fraction, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));

    if (collapsedTextColor != expandedTextColor) {
      // If the collapsed and expanded text colors are different, blend them based on the
      // fraction
      textPaint.setColor(
          blendArgb(
              getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), textBlendFraction));
    } else {
      textPaint.setColor(getCurrentCollapsedTextColor());
    }
    if (collapsedTextColor2 != expandedTextColor2) {
      // If the collapsed and expanded text colors are different, blend them based on the
      // fraction
      textPaint2.setColor(
          blendArgb(
              getCurrentExpandedTextColor2(), getCurrentCollapsedTextColor2(), textBlendFraction));
    } else {
      textPaint2.setColor(getCurrentCollapsedTextColor2());
    }

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      if (collapsedLetterSpacing != expandedLetterSpacing) {
        textPaint.setLetterSpacing(
            lerp(
                expandedLetterSpacing,
                collapsedLetterSpacing,
                fraction,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
      } else {
        textPaint.setLetterSpacing(collapsedLetterSpacing);
      }
    }
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      if (collapsedLetterSpacing2 != expandedLetterSpacing2) {
        textPaint2.setLetterSpacing(
            lerp(
                expandedLetterSpacing2,
                collapsedLetterSpacing2,
                fraction,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
      } else {
        textPaint2.setLetterSpacing(collapsedLetterSpacing2);
      }
    }

    // Calculates paint parameters for shadow layer.
    currentShadowRadius = lerp(expandedShadowRadius, collapsedShadowRadius, fraction, null);
    currentShadowRadius2 = lerp(expandedShadowRadius2, collapsedShadowRadius2, fraction, null);
    currentShadowDx = lerp(expandedShadowDx, collapsedShadowDx, fraction, null);
    currentShadowDx2 = lerp(expandedShadowDx2, collapsedShadowDx2, fraction, null);
    currentShadowDy = lerp(expandedShadowDy, collapsedShadowDy, fraction, null);
    currentShadowDy2 = lerp(expandedShadowDy2, collapsedShadowDy2, fraction, null);
    currentShadowColor =
        blendArgb(
            getCurrentColor(expandedShadowColor), getCurrentColor(collapsedShadowColor), fraction);
    currentShadowColor2 =
        blendArgb(
            getCurrentColor(expandedShadowColor2),
            getCurrentColor(collapsedShadowColor2),
            fraction);
    textPaint.setShadowLayer(
        currentShadowRadius, currentShadowDx, currentShadowDy, currentShadowColor);
    textPaint2.setShadowLayer(
        currentShadowRadius2, currentShadowDx2, currentShadowDy2, currentShadowColor2);

    if (fadeModeEnabled) {
      int originalAlpha = textPaint.getAlpha();
      int originalAlpha2 = textPaint2.getAlpha();

      // Calculates new alpha as a ratio of original alpha based on position.
      int textAlpha = (int) (calculateFadeModeTextAlpha(fraction) * originalAlpha);
      int textAlpha2 = (int) (calculateFadeModeTextAlpha(fraction) * originalAlpha2);

      textPaint.setAlpha(textAlpha);
      textPaint2.setAlpha(textAlpha2);
    }

    ViewCompat.postInvalidateOnAnimation(view);
  }

  private float calculateFadeModeTextAlpha(@FloatRange(from = 0.0, to = 1.0) float fraction) {
    if (fraction <= fadeModeThresholdFraction) {
      return AnimationUtils.lerp(
          /* startValue= */ 1,
          /* endValue= */ 0,
          /* startFraction= */ fadeModeStartFraction,
          /* endFraction= */ fadeModeThresholdFraction,
          fraction);
    } else {
      return AnimationUtils.lerp(
          /* startValue= */ 0,
          /* endValue= */ 1,
          /* startFraction= */ fadeModeThresholdFraction,
          /* endFraction= */ 1,
          fraction);
    }
  }

  @ColorInt
  private int getCurrentExpandedTextColor() {
    return getCurrentColor(expandedTextColor);
  }

  @ColorInt
  private int getCurrentExpandedTextColor2() {
    return getCurrentColor(expandedTextColor2);
  }

  @ColorInt
  public int getCurrentCollapsedTextColor() {
    return getCurrentColor(collapsedTextColor);
  }

  @ColorInt
  public int getCurrentCollapsedTextColor2() {
    return getCurrentColor(collapsedTextColor2);
  }

  @ColorInt
  private int getCurrentColor(@Nullable ColorStateList colorStateList) {
    if (colorStateList == null) {
      return 0;
    }
    if (state != null) {
      return colorStateList.getColorForState(state, 0);
    }
    return colorStateList.getDefaultColor();
  }

  // Y CONDITION:
  // - TOP: CLIP TITLE AT TOPMOST, THEN ADJUST THE SUBTITLE BELOW IT.
  // - BOTTOM: CLIP SUBTITLE AT BOTTOMMOST, THEN ADJUST THE SUBTITLE ABOVE IT.
  // - CENTER: CLIP TITLE AT CENTER MINUS DISTANCE, THEN ADJUST THE SUBTITLE BELOW IT.
  private void calculateBaseOffsets(boolean forceRecalculate) {
    final boolean isTitleOnly = TextUtils.isEmpty(text2);

    // We then calculate the collapsed text size, using the same logic
    calculateUsingTextSize(/* fraction= */ 1, forceRecalculate);
    calculateUsingTextSize2(/* fraction= */ 1, forceRecalculate);
    if (textToDraw != null && textLayout != null) {
      textToDrawCollapsed =
          TextUtils.ellipsize(textToDraw, textPaint, textLayout.getWidth(), TruncateAt.END);
    }
    if (textToDraw2 != null && textLayout2 != null) {
      textToDrawCollapsed2 =
          TextUtils.ellipsize(textToDraw2, textPaint2, textLayout2.getWidth(), TruncateAt.END);
    }
    if (textToDrawCollapsed != null) {
      collapsedTextWidth = measureTextWidth(textPaint, textToDrawCollapsed);
    } else {
      collapsedTextWidth = 0;
    }
    if (textToDrawCollapsed2 != null) {
      collapsedTextWidth2 = measureTextWidth(textPaint2, textToDrawCollapsed2);
    } else {
      collapsedTextWidth2 = 0;
    }
    final int collapsedAbsGravity =
        GravityCompat.getAbsoluteGravity(
            collapsedTextGravity,
            isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);

    // reusable dimension
    float textOffset = (textPaint.descent() - textPaint.ascent()) / 2;
    float textOffset2 = (textPaint2.descent() - textPaint2.ascent()) / 2;

    if (isTitleOnly) {
      switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
        case Gravity.BOTTOM:
          collapsedDrawY = collapsedBounds.bottom + textPaint.ascent();
          break;
        case Gravity.TOP:
          collapsedDrawY = collapsedBounds.top;
          break;
        case Gravity.CENTER_VERTICAL:
        default:
          collapsedDrawY = collapsedBounds.centerY() - textOffset;
          break;
      }
    } else {
      switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
        case Gravity.BOTTOM:
          collapsedDrawY =
              collapsedBounds.bottom
                  + textPaint2.ascent()
                  - textOffset
                  - textOffset2
                  - textOffset2 / 2;
          collapsedDrawY2 = collapsedBounds.bottom + textPaint2.ascent();
          break;
        case Gravity.TOP:
          collapsedDrawY = collapsedBounds.top;
          collapsedDrawY2 = collapsedBounds.top + textOffset + textOffset2 + textOffset2 / 2;
          break;
        case Gravity.CENTER_VERTICAL:
        default:
          collapsedDrawY = collapsedBounds.centerY() - textOffset - textOffset2;
          collapsedDrawY2 = collapsedBounds.centerY() + textOffset2 / 2;
          break;
      }
    }

    switch (collapsedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        collapsedDrawX = collapsedBounds.centerX() - (collapsedTextWidth / 2);
        collapsedDrawX2 = collapsedBounds.centerX() - (collapsedTextWidth2 / 2);
        break;
      case Gravity.RIGHT:
        collapsedDrawX = collapsedBounds.right - collapsedTextWidth;
        collapsedDrawX2 = collapsedBounds.right - collapsedTextWidth2;
        break;
      case Gravity.LEFT:
      default:
        collapsedDrawX = collapsedBounds.left;
        collapsedDrawX2 = collapsedBounds.left;
        break;
    }

    calculateUsingTextSize(/* fraction= */ 0, forceRecalculate);
    calculateUsingTextSize2(/* fraction= */ 0, forceRecalculate);
    float expandedTextHeight = textLayout != null ? textLayout.getHeight() : 0;
    float expandedTextHeight2 = textLayout2 != null ? textLayout2.getHeight() : 0;
    float expandedTextWidth = 0;
    float expandedTextWidth2 = 0;
    if (textLayout != null && maxLines > 1) {
      expandedTextWidth = textLayout.getWidth();
    } else if (textToDraw != null) {
      expandedTextWidth = measureTextWidth(textPaint, textToDraw);
    }
    if (textLayout2 != null && maxLines2 > 1) {
      expandedTextWidth2 = textLayout2.getWidth();
    } else if (textToDraw2 != null) {
      expandedTextWidth2 = measureTextWidth(textPaint2, textToDraw2);
    }
    expandedLineCount = textLayout != null ? textLayout.getLineCount() : 0;
    expandedLineCount2 = textLayout2 != null ? textLayout2.getLineCount() : 0;

    final int expandedAbsGravity =
        GravityCompat.getAbsoluteGravity(
            expandedTextGravity,
            isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);

    // reload
    textOffset = expandedTextHeight / 2;
    textOffset2 = expandedTextHeight2 / 2;

    if (isTitleOnly) {
      switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
        case Gravity.BOTTOM:
          expandedDrawY = expandedBounds.bottom - expandedTextHeight + textPaint.descent();
          break;
        case Gravity.TOP:
          expandedDrawY = expandedBounds.top;
          break;
        case Gravity.CENTER_VERTICAL:
        default:
          expandedDrawY = expandedBounds.centerY() - textOffset;
          break;
      }
    } else {
      switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
        case Gravity.BOTTOM:
          expandedDrawY = expandedBounds.bottom - expandedTextHeight - expandedTextHeight2;
          expandedDrawY2 = expandedBounds.bottom - expandedTextHeight2 + textPaint2.descent();
          break;
        case Gravity.TOP:
          expandedDrawY = expandedBounds.top;
          expandedDrawY2 = expandedBounds.top + expandedTextHeight + textPaint2.descent();
          break;
        case Gravity.CENTER_VERTICAL:
        default:
          expandedDrawY = expandedBounds.centerY() - expandedTextHeight - textPaint2.descent();
          expandedDrawY2 = expandedBounds.centerY();
          break;
      }
    }
    switch (expandedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        expandedDrawX = expandedBounds.centerX() - (expandedTextWidth / 2);
        expandedDrawX2 = expandedBounds.centerX() - (expandedTextWidth2 / 2);
        break;
      case Gravity.RIGHT:
        expandedDrawX = expandedBounds.right - expandedTextWidth;
        expandedDrawX2 = expandedBounds.right - expandedTextWidth2;
        break;
      case Gravity.LEFT:
      default:
        expandedDrawX = expandedBounds.left;
        expandedDrawX2 = expandedBounds.left;
        break;
    }

    // The bounds have changed so we need to clear the texture
    clearTexture();
    // Now reset the text size back to the original
    setInterpolatedTextSize(expandedFraction);
    setInterpolatedTextSize2(expandedFraction);
  }

  private float measureTextWidth(TextPaint textPaint, CharSequence textToDraw) {
    return textPaint.measureText(textToDraw, 0, textToDraw.length());
  }

  private void interpolateBounds(float fraction) {
    if (fadeModeEnabled) {
      currentBounds.set(fraction < fadeModeThresholdFraction ? expandedBounds : collapsedBounds);
    } else {
      currentBounds.left =
          lerp(expandedBounds.left, collapsedBounds.left, fraction, positionInterpolator);
      currentBounds.top = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);
      currentBounds.right =
          lerp(expandedBounds.right, collapsedBounds.right, fraction, positionInterpolator);
      currentBounds.bottom =
          lerp(expandedBounds.bottom, collapsedBounds.bottom, fraction, positionInterpolator);
    }
  }

  private void setCollapsedTextBlend(float blend) {
    collapsedTextBlend = blend;
    ViewCompat.postInvalidateOnAnimation(view);
  }

  private void setCollapsedTextBlend2(float blend) {
    collapsedTextBlend2 = blend;
    ViewCompat.postInvalidateOnAnimation(view);
  }

  private void setExpandedTextBlend(float blend) {
    expandedTextBlend = blend;
    ViewCompat.postInvalidateOnAnimation(view);
  }

  private void setExpandedTextBlend2(float blend) {
    expandedTextBlend2 = blend;
    ViewCompat.postInvalidateOnAnimation(view);
  }

  public void draw(@NonNull Canvas canvas) {
    final int saveCount = canvas.save();
    // Compute where to draw textLayout for this frame
    if (textToDraw != null && drawTitle) {
      textPaint.setTextSize(currentTextSize);
      float x = currentDrawX;
      float y = currentDrawY;
      final boolean drawTexture = useTexture && expandedTitleTexture != null;

      if (DEBUG_DRAW) {
        // Just a debug tool, which drawn a magenta rect in the text bounds
        canvas.drawRect(
            x,
            y,
            x + textLayout.getWidth() * scale,
            y + textLayout.getHeight() * scale,
            DEBUG_DRAW_PAINT);
      }

      if (scale != 1f && !fadeModeEnabled) {
        canvas.scale(scale, scale, x, y);
      }

      if (drawTexture) {
        // If we should use a texture, draw it instead of text
        canvas.drawBitmap(expandedTitleTexture, x, y, texturePaint);
        canvas.restoreToCount(saveCount);
        return;
      }

      if (shouldDrawMultiline()
          && (!fadeModeEnabled || expandedFraction > fadeModeThresholdFraction)) {
        drawMultilineTransition(canvas, currentDrawX - textLayout.getLineStart(0), y);
      } else {
        canvas.translate(x, y);
        textLayout.draw(canvas);
      }

      canvas.restoreToCount(saveCount);
    }
  }

  public void draw2(@NonNull Canvas canvas) {
    final int saveCount = canvas.save();
    // Compute where to draw textLayout for this frame
    if (textToDraw2 != null && drawTitle) {
      textPaint2.setTextSize(currentTextSize2);
      float x = currentDrawX2;
      float y = currentDrawY2;
      final boolean drawTexture = useTexture && expandedTitleTexture2 != null;

      if (DEBUG_DRAW) {
        // Just a debug tool, which drawn a magenta rect in the text bounds
        canvas.drawRect(
            x,
            y,
            x + textLayout2.getWidth() * scale2,
            y + textLayout2.getHeight() * scale2,
            DEBUG_DRAW_PAINT);
      }

      if (scale2 != 1f && !fadeModeEnabled) {
        canvas.scale(scale2, scale2, x, y);
      }

      if (drawTexture) {
        // If we should use a texture, draw it instead of text
        canvas.drawBitmap(expandedTitleTexture2, x, y, texturePaint2);
        canvas.restoreToCount(saveCount);
        return;
      }

      if (shouldDrawMultiline2()
          && (!fadeModeEnabled || expandedFraction > fadeModeThresholdFraction)) {
        drawMultilineTransition2(canvas, currentDrawX2 - textLayout2.getLineStart(0), y);
      } else {
        canvas.translate(x, y);
        textLayout2.draw(canvas);
      }

      canvas.restoreToCount(saveCount);
    }
  }

  private boolean shouldDrawMultiline() {
    return maxLines > 1 && (!isRtl || fadeModeEnabled) && !useTexture;
  }

  private boolean shouldDrawMultiline2() {
    return maxLines2 > 1 && (!isRtl || fadeModeEnabled) && !useTexture;
  }

  private void drawMultilineTransition(@NonNull Canvas canvas, float currentExpandedX, float y) {
    int originalAlpha = textPaint.getAlpha();
    // position expanded text appropriately
    canvas.translate(currentExpandedX, y);
    // Expanded text
    textPaint.setAlpha((int) (expandedTextBlend * originalAlpha));
    // Workaround for API 31(+). Paint applies an inverse alpha of Paint object on the shadow layer
    // when collapsing mode is scale and shadow color is opaque. The workaround is to set the shadow
    // not opaque. Then Paint will respect to the color's alpha. Applying the shadow color for
    // expanded text.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint.setShadowLayer(
          currentShadowRadius,
          currentShadowDx,
          currentShadowDy,
          MaterialColors.compositeARGBWithAlpha(currentShadowColor, textPaint.getAlpha()));
    }
    textLayout.draw(canvas);

    // Collapsed text
    textPaint.setAlpha((int) (collapsedTextBlend * originalAlpha));
    // Workaround for API 31(+). Applying the shadow color for collapsed texct.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint.setShadowLayer(
          currentShadowRadius,
          currentShadowDx,
          currentShadowDy,
          MaterialColors.compositeARGBWithAlpha(currentShadowColor, textPaint.getAlpha()));
    }
    int lineBaseline = textLayout.getLineBaseline(0);
    canvas.drawText(
        textToDrawCollapsed,
        /* start = */ 0,
        textToDrawCollapsed.length(),
        /* x = */ 0,
        lineBaseline,
        textPaint);
    // Reverse workaround for API 31(+). Applying opaque shadow color after the expanded text and
    // the collapsed text are drawn.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint.setShadowLayer(
          currentShadowRadius, currentShadowDx, currentShadowDy, currentShadowColor);
    }

    if (!fadeModeEnabled) {
      // Remove ellipsis for Cross-section animation
      String tmp = textToDrawCollapsed.toString().trim();
      if (tmp.endsWith(ELLIPSIS_NORMAL)) {
        tmp = tmp.substring(0, tmp.length() - 1);
      }
      // Cross-section between both texts (should stay at original alpha)
      textPaint.setAlpha(originalAlpha);
      canvas.drawText(
          tmp,
          /* start = */ 0,
          min(textLayout.getLineEnd(0), tmp.length()),
          /* x = */ 0,
          lineBaseline,
          textPaint);
    }
  }

  private void drawMultilineTransition2(@NonNull Canvas canvas, float currentExpandedX, float y) {
    int originalAlpha = textPaint2.getAlpha();
    // position expanded text appropriately
    canvas.translate(currentExpandedX, y);
    // Expanded text
    textPaint2.setAlpha((int) (expandedTextBlend2 * originalAlpha));
    // Workaround for API 31(+). Paint applies an inverse alpha of Paint object on the shadow layer
    // when collapsing mode is scale and shadow color is opaque. The workaround is to set the shadow
    // not opaque. Then Paint will respect to the color's alpha. Applying the shadow color for
    // expanded text.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint2.setShadowLayer(
          currentShadowRadius2,
          currentShadowDx2,
          currentShadowDy2,
          MaterialColors.compositeARGBWithAlpha(currentShadowColor2, textPaint2.getAlpha()));
    }
    textLayout2.draw(canvas);

    // Collapsed text
    textPaint2.setAlpha((int) (collapsedTextBlend2 * originalAlpha));
    // Workaround for API 31(+). Applying the shadow color for collapsed texct.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint2.setShadowLayer(
          currentShadowRadius2,
          currentShadowDx2,
          currentShadowDy2,
          MaterialColors.compositeARGBWithAlpha(currentShadowColor2, textPaint2.getAlpha()));
    }
    int lineBaseline = textLayout2.getLineBaseline(0);
    canvas.drawText(
        textToDrawCollapsed2,
        /* start = */ 0,
        textToDrawCollapsed2.length(),
        /* x = */ 0,
        lineBaseline,
        textPaint2);
    // Reverse workaround for API 31(+). Applying opaque shadow color after the expanded text and
    // the collapsed text are drawn.
    if (VERSION.SDK_INT >= VERSION_CODES.S) {
      textPaint2.setShadowLayer(
          currentShadowRadius2, currentShadowDx2, currentShadowDy2, currentShadowColor2);
    }

    if (!fadeModeEnabled) {
      // Remove ellipsis for Cross-section animation
      String tmp = textToDrawCollapsed2.toString().trim();
      if (tmp.endsWith(ELLIPSIS_NORMAL)) {
        tmp = tmp.substring(0, tmp.length() - 1);
      }
      // Cross-section between both texts (should stay at original alpha)
      textPaint2.setAlpha(originalAlpha);
      canvas.drawText(
          tmp,
          /* start = */ 0,
          min(textLayout2.getLineEnd(0), tmp.length()),
          /* x = */ 0,
          lineBaseline,
          textPaint2);
    }
  }

  private boolean calculateIsRtl(@NonNull CharSequence text) {
    final boolean defaultIsRtl = isDefaultIsRtl();
    return isRtlTextDirectionHeuristicsEnabled
        ? isTextDirectionHeuristicsIsRtl(text, defaultIsRtl)
        : defaultIsRtl;
  }

  private boolean isDefaultIsRtl() {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }

  private boolean isTextDirectionHeuristicsIsRtl(@NonNull CharSequence text, boolean defaultIsRtl) {
    return (defaultIsRtl
            ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
            : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR)
        .isRtl(text, 0, text.length());
  }

  private void setInterpolatedTextSize(float fraction) {
    calculateUsingTextSize(fraction);

    // Use our texture if the scale isn't 1.0
    useTexture = USE_SCALING_TEXTURE && scale != 1f;

    if (useTexture) {
      // Make sure we have an expanded texture if needed
      ensureExpandedTexture();
    }

    ViewCompat.postInvalidateOnAnimation(view);
  }

  private void setInterpolatedTextSize2(float fraction) {
    calculateUsingTextSize2(fraction);

    useTexture = USE_SCALING_TEXTURE && scale2 != 1f;

    if (useTexture) {
      ensureExpandedTexture2();
    }

    ViewCompat.postInvalidateOnAnimation(view);
  }

  private void calculateUsingTextSize(final float fraction) {
    calculateUsingTextSize(fraction, /* forceRecalculate= */ false);
  }

  private void calculateUsingTextSize2(final float fraction) {
    calculateUsingTextSize2(fraction, /* forceRecalculate= */ false);
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private void calculateUsingTextSize(final float fraction, boolean forceRecalculate) {
    if (text == null) {
      return;
    }

    float collapsedWidth = collapsedBounds.width();
    float expandedWidth = expandedBounds.width();

    float availableWidth;
    float newTextSize;
    float newLetterSpacing;
    boolean updateDrawText = false;

    if (isClose(fraction, /* targetValue= */ 1)) {
      newTextSize = collapsedTextSize;
      newLetterSpacing = collapsedLetterSpacing;
      scale = 1f;
      if (currentTypeface != collapsedTypeface) {
        currentTypeface = collapsedTypeface;
        updateDrawText = true;
      }
      availableWidth = collapsedWidth;
    } else {
      newTextSize = expandedTextSize;
      newLetterSpacing = expandedLetterSpacing;
      if (currentTypeface != expandedTypeface) {
        currentTypeface = expandedTypeface;
        updateDrawText = true;
      }
      if (isClose(fraction, /* targetValue= */ 0)) {
        // If we're close to the expanded text size, snap to it and use a scale of 1
        scale = 1f;
      } else {
        // Else, we'll scale down from the expanded text size
        scale =
            lerp(expandedTextSize, collapsedTextSize, fraction, textSizeInterpolator)
                / expandedTextSize;
      }

      float textSizeRatio = collapsedTextSize / expandedTextSize;
      // This is the size of the expanded bounds when it is scaled to match the
      // collapsed text size
      float scaledDownWidth = expandedWidth * textSizeRatio;

      if (forceRecalculate) {
        // If we're forcing a recalculate during a measure pass, use the expanded width since the
        // collapsed width might not be ready yet
        availableWidth = expandedWidth;
      } else {
        // If the scaled down size is larger than the actual collapsed width, we need to
        // cap the available width so that when the expanded text scales down, it matches
        // the collapsed width
        // Otherwise we'll just use the expanded width

        availableWidth =
            scaledDownWidth > collapsedWidth
                ? min(collapsedWidth / textSizeRatio, expandedWidth)
                : expandedWidth;
      }
    }

    if (availableWidth > 0) {
      boolean textSizeChanged = currentTextSize != newTextSize;
      boolean letterSpacingChanged = currentLetterSpacing != newLetterSpacing;
      updateDrawText = textSizeChanged || letterSpacingChanged || boundsChanged || updateDrawText;
      currentTextSize = newTextSize;
      currentLetterSpacing = newLetterSpacing;
      boundsChanged = false;
    }

    if (textToDraw == null || updateDrawText) {
      textPaint.setTextSize(currentTextSize);
      textPaint.setTypeface(currentTypeface);
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        textPaint.setLetterSpacing(currentLetterSpacing);
      }
      // Use linear text scaling if we're scaling the canvas
      textPaint.setLinearText(scale != 1f);

      isRtl = calculateIsRtl(text);
      textLayout = createStaticLayout(shouldDrawMultiline() ? maxLines : 1, availableWidth, isRtl);
      textToDraw = textLayout.getText();
    }
  }

  @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
  private void calculateUsingTextSize2(final float fraction, boolean forceRecalculate) {
    if (text2 == null) {
      return;
    }

    float collapsedWidth = collapsedBounds.width();
    float expandedWidth = expandedBounds.width();

    float availableWidth;
    float newTextSize;
    float newLetterSpacing;
    boolean updateDrawText = false;

    if (isClose(fraction, /* targetValue= */ 1)) {
      newTextSize = collapsedTextSize2;
      newLetterSpacing = collapsedLetterSpacing2;
      scale2 = 1f;
      if (currentTypeface2 != collapsedTypeface2) {
        currentTypeface2 = collapsedTypeface2;
        updateDrawText = true;
      }
      availableWidth = collapsedWidth;
    } else {
      newTextSize = expandedTextSize2;
      newLetterSpacing = expandedLetterSpacing2;
      if (currentTypeface2 != expandedTypeface2) {
        currentTypeface2 = expandedTypeface2;
        updateDrawText = true;
      }
      if (isClose(fraction, /* targetValue= */ 0)) {
        // If we're close to the expanded text size, snap to it and use a scale of 1
        scale2 = 1f;
      } else {
        // Else, we'll scale down from the expanded text size
        scale2 =
            lerp(expandedTextSize2, collapsedTextSize2, fraction, textSizeInterpolator)
                / expandedTextSize2;
      }

      float textSizeRatio = collapsedTextSize2 / expandedTextSize2;
      // This is the size of the expanded bounds when it is scaled to match the
      // collapsed text size
      float scaledDownWidth = expandedWidth * textSizeRatio;

      if (forceRecalculate) {
        // If we're forcing a recalculate during a measure pass, use the expanded width since the
        // collapsed width might not be ready yet
        availableWidth = expandedWidth;
      } else {
        // If the scaled down size is larger than the actual collapsed width, we need to
        // cap the available width so that when the expanded text scales down, it matches
        // the collapsed width
        // Otherwise we'll just use the expanded width

        availableWidth =
            scaledDownWidth > collapsedWidth
                ? min(collapsedWidth / textSizeRatio, expandedWidth)
                : expandedWidth;
      }
    }

    if (availableWidth > 0) {
      boolean textSizeChanged = currentTextSize2 != newTextSize;
      boolean letterSpacingChanged = currentLetterSpacing2 != newLetterSpacing;
      updateDrawText = textSizeChanged || letterSpacingChanged || boundsChanged || updateDrawText;
      currentTextSize2 = newTextSize;
      currentLetterSpacing2 = newLetterSpacing;
      boundsChanged = false;
    }

    if (textToDraw2 == null || updateDrawText) {
      textPaint2.setTextSize(currentTextSize2);
      textPaint2.setTypeface(currentTypeface2);
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        textPaint2.setLetterSpacing(currentLetterSpacing2);
      }
      // Use linear text scaling if we're scaling the canvas
      textPaint2.setLinearText(scale2 != 1f);

      isRtl = calculateIsRtl(text2);
      textLayout2 =
          createStaticLayout2(shouldDrawMultiline2() ? maxLines2 : 1, availableWidth, isRtl);
      textToDraw2 = textLayout2.getText();
    }
  }

  private StaticLayout createStaticLayout(int maxLines, float availableWidth, boolean isRtl) {
    StaticLayout textLayout = null;
    try {
      // In multiline mode, the text alignment should be controlled by the static layout.
      Alignment textAlignment = maxLines == 1 ? ALIGN_NORMAL : getMultilineTextLayoutAlignment();
      textLayout =
          StaticLayoutBuilderCompat.obtain(text, textPaint, (int) availableWidth)
              .setEllipsize(TruncateAt.END)
              .setIsRtl(isRtl)
              .setAlignment(textAlignment)
              .setIncludePad(false)
              .setMaxLines(maxLines)
              .setLineSpacing(lineSpacingAdd, lineSpacingMultiplier)
              .setHyphenationFrequency(hyphenationFrequency)
              .build();
    } catch (StaticLayoutBuilderCompatException e) {
      Log.e(TAG, e.getCause().getMessage(), e);
    }

    return checkNotNull(textLayout);
  }

  private StaticLayout createStaticLayout2(int maxLines, float availableWidth, boolean isRtl) {
    StaticLayout textLayout = null;
    try {
      // In multiline mode, the text alignment should be controlled by the static layout.
      Alignment textAlignment = maxLines == 1 ? ALIGN_NORMAL : getMultilineTextLayoutAlignment();
      textLayout =
          StaticLayoutBuilderCompat.obtain(text2, textPaint2, (int) availableWidth)
              .setEllipsize(TruncateAt.END)
              .setIsRtl(isRtl)
              .setAlignment(textAlignment)
              .setIncludePad(false)
              .setMaxLines(maxLines)
              .setLineSpacing(lineSpacingAdd2, lineSpacingMultiplier2)
              .setHyphenationFrequency(hyphenationFrequency2)
              .build();
    } catch (StaticLayoutBuilderCompatException e) {
      Log.e(TAG, e.getCause().getMessage(), e);
    }

    return checkNotNull(textLayout);
  }

  private Alignment getMultilineTextLayoutAlignment() {
    int absoluteGravity =
        GravityCompat.getAbsoluteGravity(
            expandedTextGravity,
            isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
      case Gravity.CENTER_HORIZONTAL:
        return ALIGN_CENTER;
      case Gravity.RIGHT:
        return isRtl ? ALIGN_NORMAL : ALIGN_OPPOSITE;
      default:
        return isRtl ? ALIGN_OPPOSITE : ALIGN_NORMAL;
    }
  }

  private void ensureExpandedTexture() {
    if (expandedTitleTexture != null || expandedBounds.isEmpty() || TextUtils.isEmpty(textToDraw)) {
      return;
    }

    calculateOffsets(0f);
    int width = textLayout.getWidth();
    int height = textLayout.getHeight();

    if (width <= 0 || height <= 0) {
      return;
    }

    expandedTitleTexture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(expandedTitleTexture);
    textLayout.draw(c);

    if (texturePaint == null) {
      // Make sure we have a paint
      texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }
  }

  private void ensureExpandedTexture2() {
    if (expandedTitleTexture2 != null
        || expandedBounds.isEmpty()
        || TextUtils.isEmpty(textToDraw2)) {
      return;
    }

    calculateOffsets(0f);
    int width = textLayout2.getWidth();
    int height = textLayout2.getHeight();

    if (width <= 0 || height <= 0) {
      return;
    }

    expandedTitleTexture2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(expandedTitleTexture2);
    textLayout2.draw(c);

    if (texturePaint2 == null) {
      // Make sure we have a paint
      texturePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }
  }

  public void recalculate() {
    recalculate(/* forceRecalculate= */ false);
  }

  public void recalculate(boolean forceRecalculate) {
    if ((view.getHeight() > 0 && view.getWidth() > 0) || forceRecalculate) {
      // If we've already been laid out, calculate everything now otherwise we'll wait
      // until a layout
      calculateBaseOffsets(forceRecalculate);
      calculateCurrentOffsets();
    }
  }

  /**
   * Set the title to display.
   *
   * @param text
   */
  public void setText(@Nullable CharSequence text) {
    if (text == null || !TextUtils.equals(this.text, text)) {
      this.text = text;
      textToDraw = null;
      clearTexture();
      recalculate();
    }
  }

  /**
   * Set the subtitle to display.
   *
   * @param text
   */
  public void setText2(@Nullable CharSequence text) {
    if (text == null || !TextUtils.equals(this.text2, text)) {
      this.text2 = text;
      textToDraw2 = null;
      clearTexture();
      recalculate();
    }
  }

  @Nullable
  public CharSequence getText() {
    return text;
  }

  @Nullable
  public CharSequence getText2() {
    return text2;
  }

  private void clearTexture() {
    if (expandedTitleTexture != null) {
      expandedTitleTexture.recycle();
      expandedTitleTexture = null;
    }
    if (expandedTitleTexture2 != null) {
      expandedTitleTexture2.recycle();
      expandedTitleTexture2 = null;
    }
  }

  public void setMaxLines(int maxLines) {
    if (maxLines != this.maxLines) {
      this.maxLines = maxLines;
      clearTexture();
      recalculate();
    }
  }

  public void setMaxLines2(int maxLines) {
    if (maxLines != this.maxLines2) {
      this.maxLines2 = maxLines;
      clearTexture();
      recalculate();
    }
  }

  public int getMaxLines() {
    return maxLines;
  }

  public int getMaxLines2() {
    return maxLines2;
  }

  /**
   * Returns the current title text line count.
   *
   * @return The current title text line count.
   */
  public int getLineCount() {
    return textLayout != null ? textLayout.getLineCount() : 0;
  }

  /**
   * Returns the current subtitle text line count.
   *
   * @return The current subtitle text line count.
   */
  public int getLineCount2() {
    return textLayout2 != null ? textLayout2.getLineCount() : 0;
  }

  /**
   * Returns the expanded title text line count.
   *
   * @return The expanded title text line count.
   */
  public int getExpandedLineCount() {
    return expandedLineCount;
  }

  /**
   * Returns the expanded title text line count.
   *
   * @return The expanded title text line count.
   */
  public int getExpandedLineCount2() {
    return expandedLineCount2;
  }

  @RequiresApi(VERSION_CODES.M)
  public void setLineSpacingAdd(float spacingAdd) {
    this.lineSpacingAdd = spacingAdd;
  }

  @RequiresApi(VERSION_CODES.M)
  public void setLineSpacingAdd2(float spacingAdd) {
    this.lineSpacingAdd2 = spacingAdd;
  }

  @RequiresApi(VERSION_CODES.M)
  public float getLineSpacingAdd() {
    return textLayout.getSpacingAdd();
  }

  @RequiresApi(VERSION_CODES.M)
  public float getLineSpacingAdd2() {
    return textLayout2.getSpacingAdd();
  }

  @RequiresApi(VERSION_CODES.M)
  public void setLineSpacingMultiplier(@FloatRange(from = 0.0) float spacingMultiplier) {
    this.lineSpacingMultiplier = spacingMultiplier;
  }

  @RequiresApi(VERSION_CODES.M)
  public void setLineSpacingMultiplier2(@FloatRange(from = 0.0) float spacingMultiplier) {
    this.lineSpacingMultiplier2 = spacingMultiplier;
  }

  @RequiresApi(VERSION_CODES.M)
  public float getLineSpacingMultiplier() {
    return textLayout.getSpacingMultiplier();
  }

  @RequiresApi(VERSION_CODES.M)
  public float getLineSpacingMultiplier2() {
    return textLayout2.getSpacingMultiplier();
  }

  @RequiresApi(VERSION_CODES.M)
  public void setHyphenationFrequency(int hyphenationFrequency) {
    this.hyphenationFrequency = hyphenationFrequency;
  }

  @RequiresApi(VERSION_CODES.M)
  public void setHyphenationFrequency2(int hyphenationFrequency) {
    this.hyphenationFrequency2 = hyphenationFrequency;
  }

  @RequiresApi(VERSION_CODES.M)
  public int getHyphenationFrequency() {
    return hyphenationFrequency;
  }

  @RequiresApi(VERSION_CODES.M)
  public int getHyphenationFrequency2() {
    return hyphenationFrequency2;
  }

  /**
   * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
   * defined as it's difference being < 0.00001.
   */
  private static boolean isClose(float value, float targetValue) {
    return Math.abs(value - targetValue) < 0.00001f;
  }

  public ColorStateList getExpandedTextColor() {
    return expandedTextColor;
  }

  public ColorStateList getExpandedTextColor2() {
    return expandedTextColor2;
  }

  public ColorStateList getCollapsedTextColor() {
    return collapsedTextColor;
  }

  public ColorStateList getCollapsedTextColor2() {
    return collapsedTextColor2;
  }

  /**
   * Blend between two ARGB colors using the given ratio.
   *
   * <p>A blend ratio of 0.0 will result in {@code color1}, 0.5 will give an even blend, 1.0 will
   * result in {@code color2}.
   *
   * <p>This is different from the AndroidX implementation by rounding the blended channel values
   * with {@link Math#round(float)}.
   *
   * @param color1 the first ARGB color.
   * @param color2 the second ARGB color.
   * @param ratio the blend ratio of {@code color1} to {@code color2}.
   */
  @ColorInt
  private static int blendArgb(
      @ColorInt int color1, @ColorInt int color2, @FloatRange(from = 0.0, to = 1.0) float ratio) {
    final float inverseRatio = 1 - ratio;
    float a = Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio;
    float r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio;
    float g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio;
    float b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio;
    return Color.argb(Math.round(a), Math.round(r), Math.round(g), Math.round(b));
  }

  private static float lerp(
      float startValue, float endValue, float fraction, @Nullable TimeInterpolator interpolator) {
    if (interpolator != null) {
      fraction = interpolator.getInterpolation(fraction);
    }
    return AnimationUtils.lerp(startValue, endValue, fraction);
  }

  private static boolean rectEquals(@NonNull Rect r, int left, int top, int right, int bottom) {
    return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
  }
}
