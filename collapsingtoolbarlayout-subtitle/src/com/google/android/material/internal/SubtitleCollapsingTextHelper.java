package com.google.android.material.internal;

import android.animation.TimeInterpolator;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.core.text.TextDirectionHeuristicsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.resources.CancelableFontCallback;
import com.google.android.material.resources.TextAppearance;

/**
 * Helper class for {@link com.google.android.material.appbar.SubtitleCollapsingToolbarLayout}.
 *
 * @see CollapsingTextHelper
 */
public final class SubtitleCollapsingTextHelper {

    // Pre-JB-MR2 doesn't support HW accelerated canvas scaled title so we will workaround it
    // by using our own texture
    private static final boolean USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18;

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

    @NonNull private final Rect expandedBounds;
    @NonNull private final Rect collapsedBounds;
    @NonNull private final RectF currentBounds;
    private int expandedTextGravity = Gravity.CENTER_VERTICAL;
    private int collapsedTextGravity = Gravity.CENTER_VERTICAL;
    private float expandedTitleSize = 15;
    private float collapsedTitleSize = 15;
    private float expandedSubtitleSize = 15;
    private float collapsedSubtitleSize = 15;
    private ColorStateList expandedTitleColor;
    private ColorStateList collapsedTitleColor;
    private ColorStateList expandedSubtitleColor;
    private ColorStateList collapsedSubtitleColor;

    private float expandedTitleY;
    private float collapsedTitleY;
    private float expandedTitleX;
    private float collapsedTitleX;
    private float currentTitleX;
    private float currentTitleY;
    private Typeface collapsedTitleTypeface;
    private Typeface expandedTitleTypeface;
    private Typeface currentTitleTypeface;
    private CancelableFontCallback expandedTitleFontCallback;
    private CancelableFontCallback collapsedTitleFontCallback;

    private float expandedSubtitleY;
    private float collapsedSubtitleY;
    private float expandedSubtitleX;
    private float collapsedSubtitleX;
    private float currentSubtitleX;
    private float currentSubtitleY;
    private Typeface collapsedSubtitleTypeface;
    private Typeface expandedSubtitleTypeface;
    private Typeface currentSubtitleTypeface;
    private CancelableFontCallback expandedSubtitleFontCallback;
    private CancelableFontCallback collapsedSubtitleFontCallback;

    @Nullable private CharSequence title;
    @Nullable private CharSequence titleToDraw;
    @Nullable private CharSequence subtitle;
    @Nullable private CharSequence subtitleToDraw;
    private boolean isRtl;

    private boolean useTexture;
    @Nullable private Bitmap expandedTitleTexture;
    private Paint titleTexturePaint;
    private float titleTextureAscent;
    private float titleTextureDescent;
    @Nullable private Bitmap expandedSubtitleTexture;
    private Paint subtitleTexturePaint;
    private float subtitleTextureAscent;
    private float subtitleTextureDescent;

    private float titleScale;
    private float currentTitleSize;
    private float subtitleScale;
    private float currentSubtitleSize;

    private int[] state;

    private boolean boundsChanged;

    @NonNull private final TextPaint titlePaint;
    @NonNull private final TextPaint titleTmpPaint;
    @NonNull private final TextPaint subtitlePaint;
    @NonNull private final TextPaint subtitleTmpPaint;

    private TimeInterpolator positionInterpolator;
    private TimeInterpolator textSizeInterpolator;

    private float collapsedTitleShadowRadius;
    private float collapsedTitleShadowDx;
    private float collapsedTitleShadowDy;
    private ColorStateList collapsedTitleShadowColor;

    private float expandedTitleShadowRadius;
    private float expandedTitleShadowDx;
    private float expandedTitleShadowDy;
    private ColorStateList expandedTitleShadowColor;

    private float collapsedSubtitleShadowRadius;
    private float collapsedSubtitleShadowDx;
    private float collapsedSubtitleShadowDy;
    private ColorStateList collapsedSubtitleShadowColor;

    private float expandedSubtitleShadowRadius;
    private float expandedSubtitleShadowDx;
    private float expandedSubtitleShadowDy;
    private ColorStateList expandedSubtitleShadowColor;

    public SubtitleCollapsingTextHelper(View view) {
        this.view = view;

        titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        titleTmpPaint = new TextPaint(titlePaint);
        subtitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        subtitleTmpPaint = new TextPaint(subtitlePaint);

        collapsedBounds = new Rect();
        expandedBounds = new Rect();
        currentBounds = new RectF();
    }

    public void setTextSizeInterpolator(TimeInterpolator interpolator) {
        textSizeInterpolator = interpolator;
        recalculate();
    }

    public void setPositionInterpolator(TimeInterpolator interpolator) {
        positionInterpolator = interpolator;
        recalculate();
    }

    public void setExpandedTitleSize(float textSize) {
        if (expandedTitleSize != textSize) {
            expandedTitleSize = textSize;
            recalculate();
        }
    }

    public void setCollapsedTitleSize(float textSize) {
        if (collapsedTitleSize != textSize) {
            collapsedTitleSize = textSize;
            recalculate();
        }
    }

    public void setExpandedSubtitleSize(float textSize) {
        if (expandedSubtitleSize != textSize) {
            expandedSubtitleSize = textSize;
            recalculate();
        }
    }

    public void setCollapsedSubtitleSize(float textSize) {
        if (collapsedSubtitleSize != textSize) {
            collapsedSubtitleSize = textSize;
            recalculate();
        }
    }

    public void setCollapsedTitleColor(ColorStateList textColor) {
        if (collapsedTitleColor != textColor) {
            collapsedTitleColor = textColor;
            recalculate();
        }
    }

    public void setExpandedTitleColor(ColorStateList textColor) {
        if (expandedTitleColor != textColor) {
            expandedTitleColor = textColor;
            recalculate();
        }
    }

    public void setCollapsedSubtitleColor(ColorStateList textColor) {
        if (collapsedSubtitleColor != textColor) {
            collapsedSubtitleColor = textColor;
            recalculate();
        }
    }

    public void setExpandedSubtitleColor(ColorStateList textColor) {
        if (expandedSubtitleColor != textColor) {
            expandedSubtitleColor = textColor;
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

    public void getCollapsedTitleActualBounds(@NonNull RectF bounds) {
        boolean isRtl = calculateIsRtl(title);

        bounds.left = !isRtl
            ? collapsedBounds.left : collapsedBounds.right - calculateCollapsedTitleWidth();
        bounds.top = collapsedBounds.top;
        bounds.right = !isRtl ? bounds.left + calculateCollapsedTitleWidth() : collapsedBounds.right;
        bounds.bottom = collapsedBounds.top + getCollapsedTitleHeight();
    }

    public float calculateCollapsedTitleWidth() {
        if (title == null) {
            return 0;
        }
        getTitlePaintCollapsed(titleTmpPaint);
        return titleTmpPaint.measureText(title, 0, title.length());
    }

    public void getCollapsedSubtitleActualBounds(@NonNull RectF bounds) {
        boolean isRtl = calculateIsRtl(subtitle);

        bounds.left = !isRtl
            ? collapsedBounds.left : collapsedBounds.right - calculateCollapsedSubtitleWidth();
        bounds.top = collapsedBounds.top;
        bounds.right = !isRtl ? bounds.left + calculateCollapsedSubtitleWidth() : collapsedBounds.right;
        bounds.bottom = collapsedBounds.top + getCollapsedSubtitleHeight();
    }

    public float calculateCollapsedSubtitleWidth() {
        if (subtitle == null) {
            return 0;
        }
        getSubtitlePaintCollapsed(subtitleTmpPaint);
        return subtitleTmpPaint.measureText(subtitle, 0, subtitle.length());
    }

    public float getExpandedTitleHeight() {
        getTitlePaintExpanded(titleTmpPaint);
        // Return expanded height measured from the baseline.
        return -titleTmpPaint.ascent();
    }

    public float getCollapsedTitleHeight() {
        getTitlePaintCollapsed(titleTmpPaint);
        // Return collapsed height measured from the baseline.
        return -titleTmpPaint.ascent();
    }

    private void getTitlePaintExpanded(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(expandedTitleSize);
        textPaint.setTypeface(expandedTitleTypeface);
    }

    private void getTitlePaintCollapsed(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(collapsedTitleSize);
        textPaint.setTypeface(collapsedTitleTypeface);
    }

    public float getExpandedSubtitleHeight() {
        getSubtitlePaintExpanded(subtitleTmpPaint);
        // Return expanded height measured from the baseline.
        return -subtitleTmpPaint.ascent();
    }

    public float getCollapsedSubtitleHeight() {
        getSubtitlePaintCollapsed(subtitleTmpPaint);
        // Return collapsed height measured from the baseline.
        return -subtitleTmpPaint.ascent();
    }

    private void getSubtitlePaintExpanded(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(expandedSubtitleSize);
        textPaint.setTypeface(expandedSubtitleTypeface);
    }

    private void getSubtitlePaintCollapsed(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(collapsedSubtitleSize);
        textPaint.setTypeface(collapsedSubtitleTypeface);
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

    public void setCollapsedTitleAppearance(int resId) {
        TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);

        if (textAppearance.textColor != null) {
            collapsedTitleColor = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            collapsedTitleSize = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            collapsedTitleShadowColor = textAppearance.shadowColor;
        }
        collapsedTitleShadowDx = textAppearance.shadowDx;
        collapsedTitleShadowDy = textAppearance.shadowDy;
        collapsedTitleShadowRadius = textAppearance.shadowRadius;

        // Cancel pending async fetch, if any, and replace with a new one.
        if (collapsedTitleFontCallback != null) {
            collapsedTitleFontCallback.cancel();
        }
        collapsedTitleFontCallback =
            new CancelableFontCallback(
                new CancelableFontCallback.ApplyFont() {
                    @Override
                    public void apply(Typeface font) {
                        setCollapsedTitleTypeface(font);
                    }
                },
                textAppearance.getFallbackFont());
        textAppearance.getFontAsync(view.getContext(), collapsedTitleFontCallback);

        recalculate();
    }

    public void setExpandedTitleAppearance(int resId) {
        TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);
        if (textAppearance.textColor != null) {
            expandedTitleColor = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            expandedTitleSize = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            expandedTitleShadowColor = textAppearance.shadowColor;
        }
        expandedTitleShadowDx = textAppearance.shadowDx;
        expandedTitleShadowDy = textAppearance.shadowDy;
        expandedTitleShadowRadius = textAppearance.shadowRadius;

        // Cancel pending async fetch, if any, and replace with a new one.
        if (expandedTitleFontCallback != null) {
            expandedTitleFontCallback.cancel();
        }
        expandedTitleFontCallback =
            new CancelableFontCallback(
                new CancelableFontCallback.ApplyFont() {
                    @Override
                    public void apply(Typeface font) {
                        setExpandedTitleTypeface(font);
                    }
                },
                textAppearance.getFallbackFont());
        textAppearance.getFontAsync(view.getContext(), expandedTitleFontCallback);

        recalculate();
    }

    public void setCollapsedSubtitleAppearance(int resId) {
        TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);

        if (textAppearance.textColor != null) {
            collapsedSubtitleColor = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            collapsedSubtitleSize = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            collapsedSubtitleShadowColor = textAppearance.shadowColor;
        }
        collapsedSubtitleShadowDx = textAppearance.shadowDx;
        collapsedSubtitleShadowDy = textAppearance.shadowDy;
        collapsedSubtitleShadowRadius = textAppearance.shadowRadius;

        // Cancel pending async fetch, if any, and replace with a new one.
        if (collapsedSubtitleFontCallback != null) {
            collapsedSubtitleFontCallback.cancel();
        }
        collapsedSubtitleFontCallback =
            new CancelableFontCallback(
                new CancelableFontCallback.ApplyFont() {
                    @Override
                    public void apply(Typeface font) {
                        setCollapsedTitleTypeface(font);
                    }
                },
                textAppearance.getFallbackFont());
        textAppearance.getFontAsync(view.getContext(), collapsedSubtitleFontCallback);

        recalculate();
    }

    public void setExpandedSubtitleAppearance(int resId) {
        TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);
        if (textAppearance.textColor != null) {
            expandedSubtitleColor = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            expandedSubtitleSize = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            expandedSubtitleShadowColor = textAppearance.shadowColor;
        }
        expandedSubtitleShadowDx = textAppearance.shadowDx;
        expandedSubtitleShadowDy = textAppearance.shadowDy;
        expandedSubtitleShadowRadius = textAppearance.shadowRadius;

        // Cancel pending async fetch, if any, and replace with a new one.
        if (expandedSubtitleFontCallback != null) {
            expandedSubtitleFontCallback.cancel();
        }
        expandedSubtitleFontCallback =
            new CancelableFontCallback(
                new CancelableFontCallback.ApplyFont() {
                    @Override
                    public void apply(Typeface font) {
                        setExpandedTitleTypeface(font);
                    }
                },
                textAppearance.getFallbackFont());
        textAppearance.getFontAsync(view.getContext(), expandedSubtitleFontCallback);

        recalculate();
    }

    public void setCollapsedTitleTypeface(Typeface typeface) {
        if (setCollapsedTitleTypefaceInternal(typeface)) {
            recalculate();
        }
    }

    public void setExpandedTitleTypeface(Typeface typeface) {
        if (setExpandedTitleTypefaceInternal(typeface)) {
            recalculate();
        }
    }

    public void setCollapsedSubtitleTypeface(Typeface typeface) {
        if (setCollapsedSubtitleTypefaceInternal(typeface)) {
            recalculate();
        }
    }

    public void setExpandedSubtitleTypeface(Typeface typeface) {
        if (setExpandedSubtitleTypefaceInternal(typeface)) {
            recalculate();
        }
    }

    public void setTitleTypefaces(Typeface typeface) {
        boolean collapsedFontChanged = setCollapsedTitleTypefaceInternal(typeface);
        boolean expandedFontChanged = setExpandedTitleTypefaceInternal(typeface);
        if (collapsedFontChanged || expandedFontChanged) {
            recalculate();
        }
    }

    public void setSubtitleTypefaces(Typeface typeface) {
        boolean collapsedFontChanged = setCollapsedTitleTypefaceInternal(typeface);
        boolean expandedFontChanged = setExpandedTitleTypefaceInternal(typeface);
        if (collapsedFontChanged || expandedFontChanged) {
            recalculate();
        }
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    private boolean setCollapsedTitleTypefaceInternal(Typeface typeface) {
        // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
        // already updated one when async op comes back after a while.
        if (collapsedTitleFontCallback != null) {
            collapsedTitleFontCallback.cancel();
        }
        if (collapsedTitleTypeface != typeface) {
            collapsedTitleTypeface = typeface;
            return true;
        }
        return false;
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    private boolean setExpandedTitleTypefaceInternal(Typeface typeface) {
        // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
        // already updated one when async op comes back after a while.
        if (expandedTitleFontCallback != null) {
            expandedTitleFontCallback.cancel();
        }
        if (expandedTitleTypeface != typeface) {
            expandedTitleTypeface = typeface;
            return true;
        }
        return false;
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    private boolean setCollapsedSubtitleTypefaceInternal(Typeface typeface) {
        // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
        // already updated one when async op comes back after a while.
        if (collapsedSubtitleFontCallback != null) {
            collapsedSubtitleFontCallback.cancel();
        }
        if (collapsedSubtitleTypeface != typeface) {
            collapsedSubtitleTypeface = typeface;
            return true;
        }
        return false;
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    private boolean setExpandedSubtitleTypefaceInternal(Typeface typeface) {
        // Explicit Typeface setting cancels pending async fetch, if any, to avoid old font overriding
        // already updated one when async op comes back after a while.
        if (expandedSubtitleFontCallback != null) {
            expandedSubtitleFontCallback.cancel();
        }
        if (expandedSubtitleTypeface != typeface) {
            expandedSubtitleTypeface = typeface;
            return true;
        }
        return false;
    }

    public Typeface getCollapsedTitleTypeface() {
        return collapsedTitleTypeface != null ? collapsedTitleTypeface : Typeface.DEFAULT;
    }

    public Typeface getExpandedTitleTypeface() {
        return expandedTitleTypeface != null ? expandedTitleTypeface : Typeface.DEFAULT;
    }

    public Typeface getCollapsedSubtitleTypeface() {
        return collapsedSubtitleTypeface != null ? collapsedSubtitleTypeface : Typeface.DEFAULT;
    }

    public Typeface getExpandedSubtitleTypeface() {
        return expandedSubtitleTypeface != null ? expandedSubtitleTypeface : Typeface.DEFAULT;
    }

    /**
     * Set the value indicating the current scroll value. This decides how much of the background will
     * be displayed, as well as the title metrics/positioning.
     * <p>
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

    public final boolean setState(final int[] state) {
        this.state = state;

        if (isStateful()) {
            recalculate();
            return true;
        }

        return false;
    }

    public final boolean isStateful() {
        return (collapsedTitleColor != null && collapsedTitleColor.isStateful())
            || (expandedTitleColor != null && expandedTitleColor.isStateful());
    }

    public float getExpansionFraction() {
        return expandedFraction;
    }

    public float getCollapsedTitleSize() {
        return collapsedTitleSize;
    }

    public float getExpandedTitleSize() {
        return expandedTitleSize;
    }

    public float getCollapsedSubtitleSize() {
        return collapsedSubtitleSize;
    }

    public float getExpandedSubtitleSize() {
        return expandedSubtitleSize;
    }

    private void calculateCurrentOffsets() {
        calculateOffsets(expandedFraction);
    }

    private void calculateOffsets(final float fraction) {
        interpolateBounds(fraction);
        currentTitleX = lerp(expandedTitleX, collapsedTitleX, fraction, positionInterpolator);
        currentTitleY = lerp(expandedTitleY, collapsedTitleY, fraction, positionInterpolator);
        currentSubtitleX = lerp(
            expandedSubtitleX, collapsedSubtitleX, fraction, positionInterpolator);
        currentSubtitleY = lerp(
            expandedSubtitleY, collapsedSubtitleY, fraction, positionInterpolator);

        setInterpolatedTitleSize(
            lerp(expandedTitleSize, collapsedTitleSize, fraction, textSizeInterpolator));
        setInterpolatedSubtitleSize(
            lerp(expandedSubtitleSize, collapsedSubtitleSize, fraction, textSizeInterpolator));

        if (collapsedTitleColor != expandedTitleColor) {
            // If the collapsed and expanded title colors are different, blend them based on the
            // fraction
            titlePaint.setColor(
                blendColors(getCurrentExpandedTitleColor(), getCurrentCollapsedTitleColor(), fraction));
        } else {
            titlePaint.setColor(getCurrentCollapsedTitleColor());
        }

        titlePaint.setShadowLayer(
            lerp(expandedTitleShadowRadius, collapsedTitleShadowRadius, fraction, null),
            lerp(expandedTitleShadowDx, collapsedTitleShadowDx, fraction, null),
            lerp(expandedTitleShadowDy, collapsedTitleShadowDy, fraction, null),
            blendColors(
                getCurrentColor(expandedTitleShadowColor), getCurrentColor(collapsedTitleShadowColor), fraction));

        if (collapsedSubtitleColor != expandedSubtitleColor) {
            // If the collapsed and expanded title colors are different, blend them based on the
            // fraction
            subtitlePaint.setColor(
                blendColors(getCurrentExpandedSubtitleColor(), getCurrentCollapsedSubtitleColor(), fraction));
        } else {
            subtitlePaint.setColor(getCurrentCollapsedSubtitleColor());
        }

        subtitlePaint.setShadowLayer(
            lerp(expandedSubtitleShadowRadius, collapsedSubtitleShadowRadius, fraction, null),
            lerp(expandedSubtitleShadowDx, collapsedSubtitleShadowDx, fraction, null),
            lerp(expandedSubtitleShadowDy, collapsedSubtitleShadowDy, fraction, null),
            blendColors(
                getCurrentColor(expandedSubtitleShadowColor), getCurrentColor(collapsedSubtitleShadowColor), fraction));

        ViewCompat.postInvalidateOnAnimation(view);
    }

    @ColorInt
    private int getCurrentExpandedTitleColor() {
        return getCurrentColor(expandedTitleColor);
    }

    @ColorInt
    public int getCurrentCollapsedTitleColor() {
        return getCurrentColor(collapsedTitleColor);
    }

    @ColorInt
    private int getCurrentExpandedSubtitleColor() {
        return getCurrentColor(expandedSubtitleColor);
    }

    @ColorInt
    public int getCurrentCollapsedSubtitleColor() {
        return getCurrentColor(collapsedSubtitleColor);
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

    private void calculateBaseOffsets() {
        final float currentTitleSize = this.currentTitleSize;
        final float currentSubtitleSize = this.currentSubtitleSize;
        final boolean isTitleOnly = TextUtils.isEmpty(subtitle);

        // We then calculate the collapsed title size, using the same logic
        calculateUsingTitleSize(collapsedTitleSize);
        calculateUsingSubtitleSize(collapsedSubtitleSize);
        float titleWidth =
            titleToDraw != null ? titlePaint.measureText(titleToDraw, 0, titleToDraw.length()) : 0;
        float subtitleWidth =
            subtitleToDraw != null ? subtitlePaint.measureText(subtitleToDraw, 0, subtitleToDraw.length()) : 0;
        final int collapsedAbsGravity =
            GravityCompat.getAbsoluteGravity(
                collapsedTextGravity,
                isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
        //region reusable dimension
        float titleHeight = titlePaint.descent() - titlePaint.ascent();
        float titleOffset = titleHeight / 2 - titlePaint.descent();
        float subtitleHeight = subtitlePaint.descent() - subtitlePaint.ascent();
        float subtitleOffset = subtitleHeight / 2 - subtitlePaint.descent();
        //endregion
        if (isTitleOnly) {
            switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    collapsedTitleY = collapsedBounds.bottom;
                    break;
                case Gravity.TOP:
                    collapsedTitleY = collapsedBounds.top - titlePaint.ascent();
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    float textHeight = titlePaint.descent() - titlePaint.ascent();
                    float textOffset = (textHeight / 2) - titlePaint.descent();
                    collapsedTitleY = collapsedBounds.centerY() + textOffset;
                    break;
            }
        } else {
            final float offset = (collapsedBounds.height() - (titleHeight + subtitleHeight)) / 3;
            collapsedTitleY = collapsedBounds.top + offset - titlePaint.ascent();
            collapsedSubtitleY = collapsedBounds.top + offset * 2 + titleHeight -
                subtitlePaint.ascent();
        }
        switch (collapsedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                collapsedTitleX = collapsedBounds.centerX() - (titleWidth / 2);
                collapsedSubtitleX = collapsedBounds.centerX() - (subtitleWidth / 2);
                break;
            case Gravity.RIGHT:
                collapsedTitleX = collapsedBounds.right - titleWidth;
                collapsedSubtitleX = collapsedBounds.right - subtitleWidth;
                break;
            case Gravity.LEFT:
            default:
                collapsedTitleX = collapsedBounds.left;
                collapsedSubtitleX = collapsedBounds.left;
                break;
        }

        calculateUsingTitleSize(expandedTitleSize);
        calculateUsingSubtitleSize(expandedSubtitleSize);
        titleWidth =
            titleToDraw != null ? titlePaint.measureText(titleToDraw, 0, titleToDraw.length()) : 0;
        subtitleWidth =
            subtitleToDraw != null ? subtitlePaint.measureText(subtitleToDraw, 0, subtitleToDraw.length()) : 0;
        //region dimension modification
        titleHeight = titlePaint.descent() - titlePaint.ascent();
        titleOffset = titleHeight / 2 - titlePaint.descent();
        subtitleHeight = subtitlePaint.descent() - subtitlePaint.ascent();
        subtitleOffset = subtitleHeight / 2 - subtitlePaint.descent();
        //endregion
        final int expandedAbsGravity = GravityCompat.getAbsoluteGravity(
            expandedTextGravity,
            isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR
        );
        if (isTitleOnly) {
            switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    expandedTitleY = expandedBounds.bottom;
                    break;
                case Gravity.TOP:
                    expandedTitleY = expandedBounds.top - titlePaint.ascent();
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    float textHeight = titlePaint.descent() - titlePaint.ascent();
                    float textOffset = (textHeight / 2) - titlePaint.descent();
                    expandedTitleY = expandedBounds.centerY() + textOffset;
                    break;
            }
        } else {
            switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    expandedTitleY = expandedBounds.bottom - subtitleHeight - titleOffset;
                    expandedSubtitleY = expandedBounds.bottom;
                    break;
                case Gravity.TOP:
                    expandedTitleY = expandedBounds.top - titlePaint.ascent();
                    expandedSubtitleY = expandedTitleY + subtitleHeight + titleOffset;
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    expandedTitleY = expandedBounds.centerY() + titleOffset;
                    expandedSubtitleY = expandedTitleY + subtitleHeight + titleOffset;
                    break;
            }
        }
        switch (expandedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                expandedTitleX = expandedBounds.centerX() - (titleWidth / 2);
                expandedSubtitleX = expandedBounds.centerX() - (subtitleWidth / 2);
                break;
            case Gravity.RIGHT:
                expandedTitleX = expandedBounds.right - titleWidth;
                expandedSubtitleX = expandedBounds.right - subtitleWidth;
                break;
            case Gravity.LEFT:
            default:
                expandedTitleX = expandedBounds.left;
                expandedSubtitleX = expandedBounds.left;
                break;
        }

        // The bounds have changed so we need to clear the texture
        clearTexture();
        // Now reset the title size back to the original
        setInterpolatedTitleSize(currentTitleSize);
        setInterpolatedSubtitleSize(currentSubtitleSize);
    }

    private void interpolateBounds(float fraction) {
        currentBounds.left =
            lerp(expandedBounds.left, collapsedBounds.left, fraction, positionInterpolator);
        currentBounds.top = lerp(expandedTitleY, collapsedTitleY, fraction, positionInterpolator);
        currentBounds.right =
            lerp(expandedBounds.right, collapsedBounds.right, fraction, positionInterpolator);
        currentBounds.bottom =
            lerp(expandedBounds.bottom, collapsedBounds.bottom, fraction, positionInterpolator);
    }

    public void draw(Canvas canvas) {
        final int titleSaveCount = canvas.save();

        if (titleToDraw != null && drawTitle) {
            float titleX = currentTitleX;
            float titleY = currentTitleY;
            float subtitleX = currentSubtitleX;
            float subtitleY = currentSubtitleY;

            final boolean drawTexture = useTexture && expandedTitleTexture != null;

            final float titleAscent;
            final float titleDescent;
            final float subtitleAscent;
            final float subtitleDescent;
            if (drawTexture) {
                titleAscent = titleTextureAscent * titleScale;
                titleDescent = titleTextureDescent * titleScale;
                subtitleAscent = subtitleTextureAscent * subtitleScale;
                subtitleDescent = subtitleTextureDescent * subtitleScale;
            } else {
                titleAscent = subtitlePaint.ascent() * subtitleScale;
                titleDescent = subtitlePaint.descent() * subtitleScale;
                subtitleAscent = subtitlePaint.ascent() * subtitleScale;
                subtitleDescent = subtitlePaint.descent() * subtitleScale;
            }

            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a magenta rect in the title bounds
                canvas.drawRect(
                    currentBounds.left, titleY + titleAscent, currentBounds.right, titleY + titleDescent, DEBUG_DRAW_PAINT);
            }

            if (drawTexture) {
                titleY += titleAscent;
                subtitleY += subtitleAscent;
            }

            //region IMPORTANT: separate canvas save for subtitle
            final int subtitleSaveCount = canvas.save();
            if (!TextUtils.isEmpty(subtitle)) {
                if (subtitleScale != 1f) {
                    canvas.scale(subtitleScale, subtitleScale, subtitleX, subtitleY);
                }

                if (drawTexture) {
                    // If we should use a texture, draw it instead of title
                    canvas.drawBitmap(expandedSubtitleTexture, subtitleX, subtitleY, subtitleTexturePaint);
                } else {
                    canvas.drawText(subtitleToDraw, 0, subtitleToDraw.length(), subtitleX, subtitleY, subtitlePaint);
                }
                canvas.restoreToCount(subtitleSaveCount);
            }
            //endregion

            if (titleScale != 1f) {
                canvas.scale(titleScale, titleScale, titleX, titleY);
            }

            if (drawTexture) {
                // If we should use a texture, draw it instead of title
                canvas.drawBitmap(expandedTitleTexture, titleX, titleY, titleTexturePaint);
            } else {
                canvas.drawText(titleToDraw, 0, titleToDraw.length(), titleX, titleY, titlePaint);
            }
        }

        canvas.restoreToCount(titleSaveCount);
    }

    private boolean calculateIsRtl(CharSequence text) {
        final boolean defaultIsRtl =
            ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
            ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
            : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR)
            .isRtl(text, 0, text.length());
    }

    private void setInterpolatedTitleSize(float size) {
        calculateUsingTitleSize(size);

        // Use our texture if the scale isn't 1.0
        useTexture = USE_SCALING_TEXTURE && titleScale != 1f;

        if (useTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTitleTexture();
        }

        ViewCompat.postInvalidateOnAnimation(view);
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    private void calculateUsingTitleSize(final float size) {
        if (title == null) {
            return;
        }

        final float collapsedWidth = collapsedBounds.width();
        final float expandedWidth = expandedBounds.width();

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(size, collapsedTitleSize)) {
            newTextSize = collapsedTitleSize;
            titleScale = 1f;
            if (currentTitleTypeface != collapsedTitleTypeface) {
                currentTitleTypeface = collapsedTitleTypeface;
                updateDrawText = true;
            }
            availableWidth = collapsedWidth;
        } else {
            newTextSize = expandedTitleSize;
            if (currentTitleTypeface != expandedTitleTypeface) {
                currentTitleTypeface = expandedTitleTypeface;
                updateDrawText = true;
            }
            if (isClose(size, expandedTitleSize)) {
                // If we're close to the expanded title size, snap to it and use a scale of 1
                titleScale = 1f;
            } else {
                // Else, we'll scale down from the expanded title size
                titleScale = size / expandedTitleSize;
            }

            final float textSizeRatio = collapsedTitleSize / expandedTitleSize;
            // This is the size of the expanded bounds when it is scaled to match the
            // collapsed title size
            final float scaledDownWidth = expandedWidth * textSizeRatio;

            if (scaledDownWidth > collapsedWidth) {
                // If the scaled down size is larger than the actual collapsed width, we need to
                // cap the available width so that when the expanded title scales down, it matches
                // the collapsed width
                availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth);
            } else {
                // Otherwise we'll just use the expanded width
                availableWidth = expandedWidth;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (currentTitleSize != newTextSize) || boundsChanged || updateDrawText;
            currentTitleSize = newTextSize;
            boundsChanged = false;
        }

        if (titleToDraw == null || updateDrawText) {
            titlePaint.setTextSize(currentTitleSize);
            titlePaint.setTypeface(currentTitleTypeface);
            // Use linear title scaling if we're scaling the canvas
            titlePaint.setLinearText(titleScale != 1f);

            // If we don't currently have title to draw, or the title size has changed, ellipsize...
            final CharSequence text =
                TextUtils
                    .ellipsize(this.title, titlePaint, availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(text, titleToDraw)) {
                titleToDraw = text;
                isRtl = calculateIsRtl(titleToDraw);
            }
        }
    }

    private void ensureExpandedTitleTexture() {
        if (expandedTitleTexture != null || expandedBounds.isEmpty() || TextUtils.isEmpty(titleToDraw)) {
            return;
        }

        calculateOffsets(0f);
        titleTextureAscent = titlePaint.ascent();
        titleTextureDescent = titlePaint.descent();

        final int w = Math.round(titlePaint.measureText(titleToDraw, 0, titleToDraw.length()));
        final int h = Math.round(titleTextureDescent - titleTextureAscent);

        if (w <= 0 || h <= 0) {
            return; // If the width or height are 0, return
        }

        expandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(expandedTitleTexture);
        c.drawText(titleToDraw, 0, titleToDraw.length(), 0, h - titlePaint.descent(), titlePaint);

        if (titleTexturePaint == null) {
            // Make sure we have a paint
            titleTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    private void setInterpolatedSubtitleSize(float size) {
        calculateUsingSubtitleSize(size);

        // Use our texture if the scale isn't 1.0
        useTexture = USE_SCALING_TEXTURE && subtitleScale != 1f;

        if (useTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedSubtitleTexture();
        }

        ViewCompat.postInvalidateOnAnimation(view);
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    private void calculateUsingSubtitleSize(final float size) {
        if (subtitle == null) {
            return;
        }

        final float collapsedWidth = collapsedBounds.width();
        final float expandedWidth = expandedBounds.width();

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(size, collapsedSubtitleSize)) {
            newTextSize = collapsedSubtitleSize;
            subtitleScale = 1f;
            if (currentSubtitleTypeface != collapsedSubtitleTypeface) {
                currentSubtitleTypeface = collapsedSubtitleTypeface;
                updateDrawText = true;
            }
            availableWidth = collapsedWidth;
        } else {
            newTextSize = expandedSubtitleSize;
            if (currentSubtitleTypeface != expandedSubtitleTypeface) {
                currentSubtitleTypeface = expandedSubtitleTypeface;
                updateDrawText = true;
            }
            if (isClose(size, expandedSubtitleSize)) {
                // If we're close to the expanded title size, snap to it and use a scale of 1
                subtitleScale = 1f;
            } else {
                // Else, we'll scale down from the expanded title size
                subtitleScale = size / expandedSubtitleSize;
            }

            final float textSizeRatio = collapsedSubtitleSize / expandedSubtitleSize;
            // This is the size of the expanded bounds when it is scaled to match the
            // collapsed title size
            final float scaledDownWidth = expandedWidth * textSizeRatio;

            if (scaledDownWidth > collapsedWidth) {
                // If the scaled down size is larger than the actual collapsed width, we need to
                // cap the available width so that when the expanded title scales down, it matches
                // the collapsed width
                availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth);
            } else {
                // Otherwise we'll just use the expanded width
                availableWidth = expandedWidth;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (currentSubtitleSize != newTextSize) || boundsChanged ||
                updateDrawText;
            currentSubtitleSize = newTextSize;
            boundsChanged = false;
        }

        if (subtitleToDraw == null || updateDrawText) {
            subtitlePaint.setTextSize(currentSubtitleSize);
            subtitlePaint.setTypeface(currentSubtitleTypeface);
            // Use linear title scaling if we're scaling the canvas
            subtitlePaint.setLinearText(subtitleScale != 1f);

            // If we don't currently have title to draw, or the title size has changed, ellipsize...
            final CharSequence text = TextUtils.ellipsize(
                this.subtitle,
                subtitlePaint,
                availableWidth,
                TextUtils.TruncateAt.END
            );
            if (!TextUtils.equals(text, subtitleToDraw)) {
                subtitleToDraw = text;
                isRtl = calculateIsRtl(subtitleToDraw);
            }
        }
    }

    private void ensureExpandedSubtitleTexture() {
        if (expandedSubtitleTexture != null || expandedBounds.isEmpty() || TextUtils.isEmpty(subtitleToDraw)) {
            return;
        }

        calculateOffsets(0f);
        subtitleTextureAscent = subtitlePaint.ascent();
        subtitleTextureDescent = subtitlePaint.descent();

        final int w = Math.round(
            subtitlePaint.measureText(subtitleToDraw, 0, subtitleToDraw.length()));
        final int h = Math.round(
            subtitleTextureDescent - subtitleTextureAscent);

        if (w <= 0 || h <= 0) {
            return; // If the width or height are 0, return
        }

        expandedSubtitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(expandedSubtitleTexture);
        c.drawText(subtitleToDraw, 0, subtitleToDraw.length(), 0, h - subtitlePaint.descent(), subtitlePaint);

        if (subtitleTexturePaint == null) {
            // Make sure we have a paint
            subtitleTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    public void recalculate() {
        if (view.getHeight() > 0 && view.getWidth() > 0) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    /**
     * Set the title to display
     *
     * @param title
     */
    public void setTitle(@Nullable CharSequence title) {
        if (title == null || !title.equals(this.title)) {
            this.title = title;
            titleToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    @Nullable
    public CharSequence getTitle() {
        return title;
    }

    /**
     * Set the subtitle to display
     *
     * @param subtitle
     */
    public void setSubtitle(@Nullable CharSequence subtitle) {
        if (subtitle == null || !subtitle.equals(this.subtitle)) {
            this.subtitle = subtitle;
            subtitleToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    @Nullable
    public CharSequence getSubtitle() {
        return subtitle;
    }

    private void clearTexture() {
        if (expandedTitleTexture != null) {
            expandedTitleTexture.recycle();
            expandedTitleTexture = null;
        }
        if (expandedSubtitleTexture != null) {
            expandedSubtitleTexture.recycle();
            expandedSubtitleTexture = null;
        }
    }

    /**
     * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
     * defined as it's difference being < 0.001.
     */
    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
    }

    public ColorStateList getExpandedTitleColor() {
        return expandedTitleColor;
    }

    public ColorStateList getCollapsedTitleColor() {
        return collapsedTitleColor;
    }

    public ColorStateList getExpandedSubtitleColor() {
        return expandedSubtitleColor;
    }

    public ColorStateList getCollapsedSubtitleColor() {
        return collapsedSubtitleColor;
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 0.0 will return {@code color1}, 0.5 will give an even blend,
     *              1.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
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