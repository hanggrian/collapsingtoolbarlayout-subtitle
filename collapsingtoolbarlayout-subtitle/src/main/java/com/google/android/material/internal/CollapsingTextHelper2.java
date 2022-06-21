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
public final class CollapsingTextHelper2 {

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
    private float expandedTextSize, expandedTextSize2 = 15;
    private float collapsedTextSize, collapsedTextSize2 = 15;
    private ColorStateList expandedTextColor, expandedTextColor2;
    private ColorStateList collapsedTextColor, collapsedTextColor2;

    private float expandedDrawY, expandedDrawY2;
    private float collapsedDrawY, collapsedDrawY2;
    private float expandedDrawX, expandedDrawX2;
    private float collapsedDrawX, collapsedDrawX2;
    private float currentDrawX, currentDrawX2;
    private float currentDrawY, currentDrawY2;
    private Typeface collapsedTypeface, collapsedTypeface2;
    private Typeface expandedTypeface, expandedTypeface2;
    private Typeface currentTypeface, currentTypeface2;
    private CancelableFontCallback expandedFontCallback, expandedFontCallback2;
    private CancelableFontCallback collapsedFontCallback, collapsedFontCallback2;

    @Nullable private CharSequence text, text2;
    @Nullable private CharSequence textToDraw, textToDraw2;
    private boolean isRtl;

    private boolean useTexture;
    @Nullable private Bitmap expandedTitleTexture, expandedTitleTexture2;
    private Paint texturePaint, texturePaint2;
    private float textureAscent, textureAscent2;
    private float textureDescent, textureDescent2;

    private float scale, scale2;
    private float currentTextSize, currentTextSize2;

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

    public CollapsingTextHelper2(View view) {
        this.view = view;

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        textPaint2 = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        tmpPaint = new TextPaint(textPaint);
        tmpPaint2 = new TextPaint(textPaint2);

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

    public void getCollapsedTextActualBounds(@NonNull RectF bounds) {
        boolean isRtl = calculateIsRtl(text);

        bounds.left = !isRtl ? collapsedBounds.left : collapsedBounds.right - calculateCollapsedTextWidth();
        bounds.top = collapsedBounds.top;
        bounds.right = !isRtl ? bounds.left + calculateCollapsedTextWidth() : collapsedBounds.right;
        bounds.bottom = collapsedBounds.top + getCollapsedTextHeight();
    }

    public void getCollapsedTextActualBounds2(@NonNull RectF bounds) {
        boolean isRtl = calculateIsRtl(text2);

        bounds.left = !isRtl ? collapsedBounds.left : collapsedBounds.right - calculateCollapsedTextWidth2();
        bounds.top = collapsedBounds.top;
        bounds.right = !isRtl ? bounds.left + calculateCollapsedTextWidth2() : collapsedBounds.right;
        bounds.bottom = collapsedBounds.top + getCollapsedTextHeight2();
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
        return -tmpPaint.ascent();
    }

    public float getExpandedTextHeight2() {
        getTextPaintExpanded2(tmpPaint2);
        return -tmpPaint2.ascent();
    }

    public float getCollapsedTextHeight() {
        getTextPaintCollapsed(tmpPaint);
        return -tmpPaint.ascent();
    }

    public float getCollapsedTextHeight2() {
        getTextPaintCollapsed2(tmpPaint2);
        return -tmpPaint2.ascent();
    }

    private void getTextPaintExpanded(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(expandedTextSize);
        textPaint.setTypeface(expandedTypeface);
    }

    private void getTextPaintExpanded2(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(expandedTextSize2);
        textPaint.setTypeface(expandedTypeface2);
    }

    private void getTextPaintCollapsed(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(collapsedTextSize);
        textPaint.setTypeface(collapsedTypeface);
    }

    private void getTextPaintCollapsed2(@NonNull TextPaint textPaint) {
        textPaint.setTextSize(collapsedTextSize2);
        textPaint.setTypeface(collapsedTypeface2);
    }

    void onBoundsChanged() {
        drawTitle = collapsedBounds.width() > 0
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

        if (textAppearance.textColor != null) {
            collapsedTextColor = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            collapsedTextSize = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            collapsedShadowColor = textAppearance.shadowColor;
        }
        collapsedShadowDx = textAppearance.shadowDx;
        collapsedShadowDy = textAppearance.shadowDy;
        collapsedShadowRadius = textAppearance.shadowRadius;

        if (collapsedFontCallback != null) {
            collapsedFontCallback.cancel();
        }
        collapsedFontCallback = new CancelableFontCallback(new CancelableFontCallback.ApplyFont() {
            @Override
            public void apply(Typeface font) {
                setCollapsedTypeface(font);
            }
        }, textAppearance.getFallbackFont());
        textAppearance.getFontAsync(view.getContext(), collapsedFontCallback);

        recalculate();
    }

    public void setCollapsedTextAppearance2(int resId) {
        TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);

        if (textAppearance.textColor != null) {
            collapsedTextColor2 = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            collapsedTextSize2 = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            collapsedShadowColor2 = textAppearance.shadowColor;
        }
        collapsedShadowDx2 = textAppearance.shadowDx;
        collapsedShadowDy2 = textAppearance.shadowDy;
        collapsedShadowRadius2 = textAppearance.shadowRadius;

        if (collapsedFontCallback2 != null) {
            collapsedFontCallback2.cancel();
        }
        collapsedFontCallback2 = new CancelableFontCallback(new CancelableFontCallback.ApplyFont() {
            @Override
            public void apply(Typeface font) {
                setCollapsedTypeface2(font);
            }
        }, textAppearance.getFallbackFont());
        textAppearance.getFontAsync(view.getContext(), collapsedFontCallback2);

        recalculate();
    }

    public void setExpandedTextAppearance(int resId) {
        TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);
        if (textAppearance.textColor != null) {
            expandedTextColor = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            expandedTextSize = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            expandedShadowColor = textAppearance.shadowColor;
        }
        expandedShadowDx = textAppearance.shadowDx;
        expandedShadowDy = textAppearance.shadowDy;
        expandedShadowRadius = textAppearance.shadowRadius;

        if (expandedFontCallback != null) {
            expandedFontCallback.cancel();
        }
        expandedFontCallback = new CancelableFontCallback(new CancelableFontCallback.ApplyFont() {
            @Override
            public void apply(Typeface font) {
                setExpandedTypeface(font);
            }
        }, textAppearance.getFallbackFont());
        textAppearance.getFontAsync(view.getContext(), expandedFontCallback);

        recalculate();
    }

    public void setExpandedTextAppearance2(int resId) {
        TextAppearance textAppearance = new TextAppearance(view.getContext(), resId);
        if (textAppearance.textColor != null) {
            expandedTextColor2 = textAppearance.textColor;
        }
        if (textAppearance.textSize != 0) {
            expandedTextSize2 = textAppearance.textSize;
        }
        if (textAppearance.shadowColor != null) {
            expandedShadowColor2 = textAppearance.shadowColor;
        }
        expandedShadowDx2 = textAppearance.shadowDx;
        expandedShadowDy2 = textAppearance.shadowDy;
        expandedShadowRadius2 = textAppearance.shadowRadius;

        if (expandedFontCallback2 != null) {
            expandedFontCallback2.cancel();
        }
        expandedFontCallback2 = new CancelableFontCallback(new CancelableFontCallback.ApplyFont() {
            @Override
            public void apply(Typeface font) {
                setExpandedTypeface2(font);
            }
        }, textAppearance.getFallbackFont());
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

    @SuppressWarnings("ReferenceEquality")
    private boolean setCollapsedTypefaceInternal(Typeface typeface) {
        if (collapsedFontCallback != null) {
            collapsedFontCallback.cancel();
        }
        if (collapsedTypeface != typeface) {
            collapsedTypeface = typeface;
            return true;
        }
        return false;
    }

    @SuppressWarnings("ReferenceEquality")
    private boolean setCollapsedTypefaceInternal2(Typeface typeface) {
        if (collapsedFontCallback2 != null) {
            collapsedFontCallback2.cancel();
        }
        if (collapsedTypeface2 != typeface) {
            collapsedTypeface2 = typeface;
            return true;
        }
        return false;
    }

    @SuppressWarnings("ReferenceEquality")
    private boolean setExpandedTypefaceInternal(Typeface typeface) {
        if (expandedFontCallback != null) {
            expandedFontCallback.cancel();
        }
        if (expandedTypeface != typeface) {
            expandedTypeface = typeface;
            return true;
        }
        return false;
    }

    @SuppressWarnings("ReferenceEquality")
    private boolean setExpandedTypefaceInternal2(Typeface typeface) {
        if (expandedFontCallback2 != null) {
            expandedFontCallback2.cancel();
        }
        if (expandedTypeface2 != typeface) {
            expandedTypeface2 = typeface;
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
        return (collapsedTextColor != null && collapsedTextColor.isStateful())
            || (collapsedTextColor2 != null && collapsedTextColor2.isStateful())
            || (expandedTextColor != null && expandedTextColor.isStateful())
            || (expandedTextColor2 != null && expandedTextColor2.isStateful());
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

    private void calculateCurrentOffsets() {
        calculateOffsets(expandedFraction);
    }

    private void calculateOffsets(final float fraction) {
        interpolateBounds(fraction);
        currentDrawX = lerp(expandedDrawX, collapsedDrawX, fraction, positionInterpolator);
        currentDrawX2 = lerp(expandedDrawX2, collapsedDrawX2, fraction, positionInterpolator);
        currentDrawY = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);
        currentDrawY2 = lerp(expandedDrawY2, collapsedDrawY2, fraction, positionInterpolator);

        setInterpolatedTextSize(lerp(expandedTextSize, collapsedTextSize, fraction, textSizeInterpolator));
        setInterpolatedTextSize2(lerp(expandedTextSize2, collapsedTextSize2, fraction, textSizeInterpolator));

        if (collapsedTextColor != expandedTextColor) {
            textPaint.setColor(
                blendColors(getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), fraction));
        } else {
            textPaint.setColor(getCurrentCollapsedTextColor());
        }
        if (collapsedTextColor2 != expandedTextColor2) {
            textPaint2.setColor(blendColors(getCurrentExpandedTextColor2(), getCurrentCollapsedTextColor2(), fraction));
        } else {
            textPaint2.setColor(getCurrentCollapsedTextColor2());
        }

        textPaint.setShadowLayer(
            lerp(expandedShadowRadius, collapsedShadowRadius, fraction, null),
            lerp(expandedShadowDx, collapsedShadowDx, fraction, null),
            lerp(expandedShadowDy, collapsedShadowDy, fraction, null),
            blendColors(getCurrentColor(expandedShadowColor), getCurrentColor(collapsedShadowColor), fraction));
        textPaint2.setShadowLayer(
            lerp(expandedShadowRadius2, collapsedShadowRadius2, fraction, null),
            lerp(expandedShadowDx2, collapsedShadowDx2, fraction, null),
            lerp(expandedShadowDy2, collapsedShadowDy2, fraction, null),
            blendColors(getCurrentColor(expandedShadowColor2), getCurrentColor(collapsedShadowColor2), fraction));

        ViewCompat.postInvalidateOnAnimation(view);
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

    private void calculateBaseOffsets() {
        final float currentTitleSize = this.currentTextSize;
        final float currentSubtitleSize = this.currentTextSize2;

        final boolean isTitleOnly = TextUtils.isEmpty(text2);

        calculateUsingTextSize(collapsedTextSize);
        calculateUsingTextSize2(collapsedTextSize2);
        float width = textToDraw != null ? textPaint.measureText(textToDraw, 0, textToDraw.length()) : 0;
        float width2 = textToDraw2 != null ? textPaint2.measureText(textToDraw2, 0, textToDraw2.length()) : 0;
        final int collapsedAbsGravity =
            GravityCompat.getAbsoluteGravity(
                collapsedTextGravity,
                isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);

        // reusable dimension
        float height = textPaint.descent() - textPaint.ascent();
        float height2 = textPaint2.descent() - textPaint2.ascent();
        float offset = height / 2 - textPaint.descent();
        float offset2 = height2 / 2 - textPaint2.descent();

        if (isTitleOnly) {
            switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    collapsedDrawY = collapsedBounds.bottom;
                    break;
                case Gravity.TOP:
                    collapsedDrawY = collapsedBounds.top - textPaint.ascent();
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    float textHeight = textPaint.descent() - textPaint.ascent();
                    float textOffset = (textHeight / 2) - textPaint.descent();
                    collapsedDrawY = collapsedBounds.centerY() + textOffset;
                    break;
            }
        } else {
            final float allOffset = (collapsedBounds.height() - (height + height2)) / 3;
            collapsedDrawY = collapsedBounds.top + allOffset - textPaint.ascent();
            collapsedDrawY2 = collapsedBounds.top + allOffset * 2 + height - textPaint2.ascent();
        }
        switch (collapsedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                collapsedDrawX = collapsedBounds.centerX() - (width / 2);
                collapsedDrawX2 = collapsedBounds.centerX() - (width2 / 2);
                break;
            case Gravity.RIGHT:
                collapsedDrawX = collapsedBounds.right - width;
                collapsedDrawX2 = collapsedBounds.right - width2;
                break;
            case Gravity.LEFT:
            default:
                collapsedDrawX = collapsedBounds.left;
                collapsedDrawX2 = collapsedBounds.left;
                break;
        }

        calculateUsingTextSize(expandedTextSize);
        calculateUsingTextSize2(expandedTextSize2);
        width = textToDraw != null ? textPaint.measureText(textToDraw, 0, textToDraw.length()) : 0;
        width2 = textToDraw2 != null ? textPaint2.measureText(textToDraw2, 0, textToDraw2.length()) : 0;

        // dimension modification
        height = textPaint.descent() - textPaint.ascent();
        offset = height / 2 - textPaint.descent();
        height2 = textPaint2.descent() - textPaint2.ascent();
        offset2 = height2 / 2 - textPaint2.descent();

        final int expandedAbsGravity = GravityCompat.getAbsoluteGravity(
            expandedTextGravity,
            isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR
        );
        if (isTitleOnly) {
            switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    expandedDrawY = expandedBounds.bottom;
                    break;
                case Gravity.TOP:
                    expandedDrawY = expandedBounds.top - textPaint.ascent();
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    float textHeight = textPaint.descent() - textPaint.ascent();
                    float textOffset = (textHeight / 2) - textPaint.descent();
                    expandedDrawY = expandedBounds.centerY() + textOffset;
                    break;
            }
        } else {
            switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    expandedDrawY = expandedBounds.bottom - height2 - offset;
                    expandedDrawY2 = expandedBounds.bottom;
                    break;
                case Gravity.TOP:
                    expandedDrawY = expandedBounds.top - textPaint.ascent();
                    expandedDrawY2 = expandedDrawY + height2 + offset;
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    expandedDrawY = expandedBounds.centerY() + offset;
                    expandedDrawY2 = expandedDrawY + height2 + offset;
                    break;
            }
        }
        switch (expandedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                expandedDrawX = expandedBounds.centerX() - (width / 2);
                expandedDrawX2 = expandedBounds.centerX() - (width2 / 2);
                break;
            case Gravity.RIGHT:
                expandedDrawX = expandedBounds.right - width;
                expandedDrawX2 = expandedBounds.right - width2;
                break;
            case Gravity.LEFT:
            default:
                expandedDrawX = expandedBounds.left;
                expandedDrawX2 = expandedBounds.left;
                break;
        }

        clearTexture();
        setInterpolatedTextSize(currentTitleSize);
        setInterpolatedTextSize2(currentSubtitleSize);
    }

    private void interpolateBounds(float fraction) {
        currentBounds.left = lerp(expandedBounds.left, collapsedBounds.left, fraction, positionInterpolator);
        currentBounds.top = lerp(expandedDrawY, collapsedDrawY, fraction, positionInterpolator);
        currentBounds.right = lerp(expandedBounds.right, collapsedBounds.right, fraction, positionInterpolator);
        currentBounds.bottom = lerp(expandedBounds.bottom, collapsedBounds.bottom, fraction, positionInterpolator);
    }

    public void draw(@NonNull Canvas canvas) {
        final int saveCount = canvas.save();

        if (drawTitle && textToDraw != null) {
            float titleX = currentDrawX;
            float titleY = currentDrawY;

            final boolean drawTitleTexture = useTexture && expandedTitleTexture != null;

            final float titleAscent;
            final float titleDescent;
            if (drawTitleTexture) {
                titleAscent = textureAscent * scale;
                titleDescent = textureDescent * scale;
            } else {
                titleAscent = textPaint.ascent() * scale;
                titleDescent = textPaint.descent() * scale;
            }

            if (DEBUG_DRAW) {
                canvas.drawRect(currentBounds.left,
                    titleY + titleAscent, currentBounds.right, titleY + titleDescent, DEBUG_DRAW_PAINT);
            }

            if (drawTitleTexture) {
                titleY += titleAscent;
            }

            // additional canvas save for subtitle
            if (textToDraw2 != null) {
                final int subtitleSaveCount = canvas.save();

                float subtitleX = currentDrawX2;
                float subtitleY = currentDrawY2;

                final boolean drawSubtitleTexture = useTexture && expandedTitleTexture2 != null;

                final float subtitleAscent;
                final float subtitleDescent;
                if (drawSubtitleTexture) {
                    subtitleAscent = textureAscent2 * scale2;
                    subtitleDescent = textureDescent2 * scale2;
                } else {
                    subtitleAscent = textPaint2.ascent() * scale2;
                    subtitleDescent = textPaint2.descent() * scale2;
                }

                if (DEBUG_DRAW) {
                    canvas.drawRect(currentBounds.left,
                        subtitleY + subtitleAscent, currentBounds.right, subtitleY + subtitleDescent, DEBUG_DRAW_PAINT);
                }

                if (drawSubtitleTexture) {
                    subtitleY += subtitleAscent;
                }

                if (scale2 != 1f) {
                    canvas.scale(scale2, scale2, subtitleX, subtitleY);
                }

                if (drawSubtitleTexture) {
                    canvas.drawBitmap(expandedTitleTexture2, subtitleX, subtitleY, texturePaint2);
                } else {
                    canvas.drawText(textToDraw2, 0, textToDraw2.length(), subtitleX, subtitleY, textPaint2);
                }
                canvas.restoreToCount(subtitleSaveCount);
            }

            if (scale != 1f) {
                canvas.scale(scale, scale, titleX, titleY);
            }

            if (drawTitleTexture) {
                canvas.drawBitmap(expandedTitleTexture, titleX, titleY, texturePaint);
            } else {
                canvas.drawText(textToDraw, 0, textToDraw.length(), titleX, titleY, textPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private boolean calculateIsRtl(@NonNull CharSequence text) {
        final boolean defaultIsRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
            ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
            : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
    }

    private void setInterpolatedTextSize(float textSize) {
        calculateUsingTextSize(textSize);

        useTexture = USE_SCALING_TEXTURE && scale != 1f;

        if (useTexture) {
            ensureExpandedTexture();
        }

        ViewCompat.postInvalidateOnAnimation(view);
    }

    private void setInterpolatedTextSize2(float textSize) {
        calculateUsingTextSize2(textSize);

        useTexture = USE_SCALING_TEXTURE && scale2 != 1f;

        if (useTexture) {
            ensureExpandedTexture2();
        }

        ViewCompat.postInvalidateOnAnimation(view);
    }

    @SuppressWarnings("ReferenceEquality")
    private void calculateUsingTextSize(final float size) {
        if (text == null) {
            return;
        }

        final float collapsedWidth = collapsedBounds.width();
        final float expandedWidth = expandedBounds.width();

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(size, collapsedTextSize)) {
            newTextSize = collapsedTextSize;
            scale = 1f;
            if (currentTypeface != collapsedTypeface) {
                currentTypeface = collapsedTypeface;
                updateDrawText = true;
            }
            availableWidth = collapsedWidth;
        } else {
            newTextSize = expandedTextSize;
            if (currentTypeface != expandedTypeface) {
                currentTypeface = expandedTypeface;
                updateDrawText = true;
            }
            if (isClose(size, expandedTextSize)) {
                scale = 1f;
            } else {
                scale = size / expandedTextSize;
            }

            final float textSizeRatio = collapsedTextSize / expandedTextSize;
            final float scaledDownWidth = expandedWidth * textSizeRatio;

            if (scaledDownWidth > collapsedWidth) {
                availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth);
            } else {
                availableWidth = expandedWidth;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (currentTextSize != newTextSize) || boundsChanged || updateDrawText;
            currentTextSize = newTextSize;
            boundsChanged = false;
        }

        if (textToDraw == null || updateDrawText) {
            textPaint.setTextSize(currentTextSize);
            textPaint.setTypeface(currentTypeface);
            textPaint.setLinearText(scale != 1f);

            final CharSequence text =
                TextUtils
                    .ellipsize(this.text, textPaint, availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(text, textToDraw)) {
                textToDraw = text;
                isRtl = calculateIsRtl(textToDraw);
            }
        }
    }

    @SuppressWarnings("ReferenceEquality")
    private void calculateUsingTextSize2(final float size) {
        if (text2 == null) {
            return;
        }

        final float collapsedWidth = collapsedBounds.width();
        final float expandedWidth = expandedBounds.width();

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(size, collapsedTextSize2)) {
            newTextSize = collapsedTextSize2;
            scale2 = 1f;
            if (currentTypeface2 != collapsedTypeface2) {
                currentTypeface2 = collapsedTypeface2;
                updateDrawText = true;
            }
            availableWidth = collapsedWidth;
        } else {
            newTextSize = expandedTextSize2;
            if (currentTypeface2 != expandedTypeface2) {
                currentTypeface2 = expandedTypeface2;
                updateDrawText = true;
            }
            if (isClose(size, expandedTextSize2)) {
                scale2 = 1f;
            } else {
                scale2 = size / expandedTextSize2;
            }

            final float textSizeRatio = collapsedTextSize2 / expandedTextSize2;
            final float scaledDownWidth = expandedWidth * textSizeRatio;

            if (scaledDownWidth > collapsedWidth) {
                availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth);
            } else {
                availableWidth = expandedWidth;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (currentTextSize2 != newTextSize) || boundsChanged || updateDrawText;
            currentTextSize2 = newTextSize;
            boundsChanged = false;
        }

        if (textToDraw2 == null || updateDrawText) {
            textPaint2.setTextSize(currentTextSize2);
            textPaint2.setTypeface(currentTypeface2);
            textPaint2.setLinearText(scale2 != 1f);

            final CharSequence text =
                TextUtils.ellipsize(this.text2, textPaint2, availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(text, textToDraw2)) {
                textToDraw2 = text;
                isRtl = calculateIsRtl(textToDraw2);
            }
        }
    }

    private void ensureExpandedTexture() {
        if (expandedTitleTexture != null || expandedBounds.isEmpty() || TextUtils.isEmpty(textToDraw)) {
            return;
        }

        calculateOffsets(0f);
        textureAscent = textPaint.ascent();
        textureDescent = textPaint.descent();

        final int w = Math.round(textPaint.measureText(textToDraw, 0, textToDraw.length()));
        final int h = Math.round(textureDescent - textureAscent);

        if (w <= 0 || h <= 0) {
            return;
        }

        expandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(expandedTitleTexture);
        c.drawText(textToDraw, 0, textToDraw.length(), 0, h - textPaint.descent(), textPaint);

        if (texturePaint == null) {
            texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    private void ensureExpandedTexture2() {
        if (expandedTitleTexture2 != null || expandedBounds.isEmpty() || TextUtils.isEmpty(textToDraw2)) {
            return;
        }

        calculateOffsets(0f);
        textureAscent2 = textPaint2.ascent();
        textureDescent2 = textPaint2.descent();

        final int w = Math.round(textPaint2.measureText(textToDraw2, 0, textToDraw2.length()));
        final int h = Math.round(textureDescent2 - textureAscent2);

        if (w <= 0 || h <= 0) {
            return;
        }

        expandedTitleTexture2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(expandedTitleTexture2);
        c.drawText(textToDraw2, 0, textToDraw2.length(), 0, h - textPaint2.descent(), textPaint2);

        if (texturePaint2 == null) {
            texturePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    public void recalculate() {
        if (view.getHeight() > 0 && view.getWidth() > 0) {
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    /**
     * Set the title to display
     *
     * @param title
     */
    public void setText(@Nullable CharSequence title) {
        if (title == null || !title.equals(this.text)) {
            this.text = title;
            textToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    /**
     * Set the subtitle to display
     *
     * @param subtitle
     */
    public void setText2(@Nullable CharSequence subtitle) {
        if (subtitle == null || !subtitle.equals(this.text2)) {
            this.text2 = subtitle;
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

    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
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
