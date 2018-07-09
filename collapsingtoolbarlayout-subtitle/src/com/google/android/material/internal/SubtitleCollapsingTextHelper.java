/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.internal;

import android.animation.TimeInterpolator;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
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

import com.google.android.material.animation.AnimationUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.math.MathUtils;
import androidx.core.text.TextDirectionHeuristicsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @see CollapsingTextHelper
 */
@RestrictTo(LIBRARY_GROUP)
public final class SubtitleCollapsingTextHelper {

    // Pre-JB-MR2 doesn't support HW accelerated canvas scaled title so we will workaround it
    // by using our own texture
    private static final boolean USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18;

    private static final boolean DEBUG_DRAW = false;
    private static final Paint DEBUG_DRAW_PAINT;

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

    private final Rect expandedBounds;
    private final Rect collapsedBounds;
    private final RectF currentBounds;
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

    private float expandedSubtitleY;
    private float collapsedSubtitleY;
    private float expandedSubtitleX;
    private float collapsedSubtitleX;
    private float currentSubtitleX;
    private float currentSubtitleY;
    private Typeface collapsedSubtitleTypeface;
    private Typeface expandedSubtitleTypeface;
    private Typeface currentSubtitleTypeface;

    private CharSequence title;
    private CharSequence titleToDraw;
    private CharSequence subtitle;
    private CharSequence subtitleToDraw;
    private boolean isRtl;

    private boolean useTexture;
    private Bitmap expandedTitleTexture;
    private Paint titleTexturePaint;
    private float titleTextureAscent;
    private float titleTextureDescent;

    private Bitmap expandedSubtitleTexture;
    private Paint subtitleTexturePaint;
    private float subtitleTextureAscent;
    private float subtitleTextureDescent;

    private float titleScale;
    private float currentTitleSize;
    private float subtitleScale;
    private float currentSubtitleSize;

    private int[] state;

    private boolean boundsChanged;

    private final TextPaint titlePaint;
    private final TextPaint titleTmpPaint;
    private final TextPaint subtitlePaint;
    private final TextPaint subtitleTmpPaint;

    private TimeInterpolator positionInterpolator;
    private TimeInterpolator textSizeInterpolator;

    private float collapsedTitleShadowRadius;
    private float collapsedTitleShadowDx;
    private float collapsedTitleShadowDy;
    private int collapsedTitleShadowColor;

    private float expandedTitleShadowRadius;
    private float expandedTitleShadowDx;
    private float expandedTitleShadowDy;
    private int expandedTitleShadowColor;

    private float collapsedSubtitleShadowRadius;
    private float collapsedSubtitleShadowDx;
    private float collapsedSubtitleShadowDy;
    private int collapsedSubtitleShadowColor;

    private float expandedSubtitleShadowRadius;
    private float expandedSubtitleShadowDx;
    private float expandedSubtitleShadowDy;
    private int expandedSubtitleShadowColor;

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

    public void setExpandedTitleSize(float size) {
        if (expandedTitleSize != size) {
            expandedTitleSize = size;
            recalculate();
        }
    }

    public void setCollapsedTitleSize(float size) {
        if (collapsedTitleSize != size) {
            collapsedTitleSize = size;
            recalculate();
        }
    }

    public void setCollapsedTitleColor(ColorStateList color) {
        if (collapsedTitleColor != color) {
            collapsedTitleColor = color;
            recalculate();
        }
    }

    public void setExpandedTitleColor(ColorStateList color) {
        if (expandedTitleColor != color) {
            expandedTitleColor = color;
            recalculate();
        }
    }

    public void setExpandedSubtitleSize(float size) {
        if (expandedSubtitleSize != size) {
            expandedSubtitleSize = size;
            recalculate();
        }
    }

    public void setCollapsedSubtitleSize(float size) {
        if (collapsedSubtitleSize != size) {
            collapsedSubtitleSize = size;
            recalculate();
        }
    }

    public void setCollapsedSubtitleColor(ColorStateList color) {
        if (collapsedSubtitleColor != color) {
            collapsedSubtitleColor = color;
            recalculate();
        }
    }

    public void setExpandedSubtitleColor(ColorStateList color) {
        if (expandedSubtitleColor != color) {
            expandedSubtitleColor = color;
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

    public void setCollapsedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(collapsedBounds, left, top, right, bottom)) {
            collapsedBounds.set(left, top, right, bottom);
            boundsChanged = true;
            onBoundsChanged();
        }
    }

    public float calculateCollapsedTitleWidth() {
        if (title == null) {
            return 0;
        }
        getTitlePaintCollapsed(titleTmpPaint);
        return titleTmpPaint.measureText(title, 0, title.length());
    }

    public float getCollapsedTitleHeight() {
        getTitlePaintCollapsed(titleTmpPaint);
        // Return collapsed height measured from the baseline.
        return -titleTmpPaint.ascent();
    }

    public void getCollapsedTitleActualBounds(RectF bounds) {
        boolean isRtl = calculateIsRtl(title);

        bounds.left =
                !isRtl ? collapsedBounds.left : collapsedBounds.right - calculateCollapsedTitleWidth();
        bounds.top = collapsedBounds.top;
        bounds.right = !isRtl ? bounds.left + calculateCollapsedTitleWidth() : collapsedBounds.right;
        bounds.bottom = collapsedBounds.top + getCollapsedTitleHeight();
    }

    private void getTitlePaintCollapsed(TextPaint paint) {
        paint.setTextSize(collapsedTitleSize);
        paint.setTypeface(collapsedTitleTypeface);
    }

    public float calculateCollapsedSubtitleWidth() {
        if (subtitle == null) {
            return 0;
        }
        getSubtitlePaintCollapsed(subtitleTmpPaint);
        return subtitleTmpPaint.measureText(subtitle, 0, subtitle.length());
    }

    public float getCollapsedSubtitleHeight() {
        getSubtitlePaintCollapsed(subtitleTmpPaint);
        // Return collapsed height measured from the baseline.
        return -subtitleTmpPaint.ascent();
    }

    public void getCollapsedSubtitleActualBounds(RectF bounds) {
        boolean isRtl = calculateIsRtl(subtitle);

        bounds.left =
                !isRtl ? collapsedBounds.left : collapsedBounds.right - calculateCollapsedSubtitleWidth();
        bounds.top = collapsedBounds.top;
        bounds.right = !isRtl ? bounds.left + calculateCollapsedSubtitleWidth() : collapsedBounds.right;
        bounds.bottom = collapsedBounds.top + getCollapsedSubtitleHeight();
    }

    private void getSubtitlePaintCollapsed(TextPaint paint) {
        paint.setTextSize(collapsedSubtitleSize);
        paint.setTypeface(collapsedSubtitleTypeface);
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
        TintTypedArray a =
                TintTypedArray.obtainStyledAttributes(
                        view.getContext(), resId, androidx.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textColor)) {
            collapsedTitleColor =
                    a.getColorStateList(
                            androidx.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textSize)) {
            collapsedTitleSize =
                    a.getDimensionPixelSize(
                            androidx.appcompat.R.styleable.TextAppearance_android_textSize,
                            (int) collapsedTitleSize);
        }
        collapsedTitleShadowColor =
                a.getInt(androidx.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        collapsedTitleShadowDx =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        collapsedTitleShadowDy =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        collapsedTitleShadowRadius =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            collapsedTitleTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    public void setExpandedTitleAppearance(int resId) {
        TintTypedArray a =
                TintTypedArray.obtainStyledAttributes(
                        view.getContext(), resId, androidx.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textColor)) {
            expandedTitleColor =
                    a.getColorStateList(
                            androidx.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textSize)) {
            expandedTitleSize =
                    a.getDimensionPixelSize(
                            androidx.appcompat.R.styleable.TextAppearance_android_textSize,
                            (int) expandedTitleSize);
        }
        expandedTitleShadowColor =
                a.getInt(androidx.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        expandedTitleShadowDx =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        expandedTitleShadowDy =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        expandedTitleShadowRadius =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            expandedTitleTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    public void setCollapsedSubtitleAppearance(int resId) {
        TintTypedArray a =
                TintTypedArray.obtainStyledAttributes(
                        view.getContext(), resId, androidx.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textColor)) {
            collapsedSubtitleColor =
                    a.getColorStateList(
                            androidx.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textSize)) {
            collapsedSubtitleSize =
                    a.getDimensionPixelSize(
                            androidx.appcompat.R.styleable.TextAppearance_android_textSize,
                            (int) collapsedSubtitleSize);
        }
        collapsedSubtitleShadowColor =
                a.getInt(androidx.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        collapsedSubtitleShadowDx =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        collapsedSubtitleShadowDy =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        collapsedSubtitleShadowRadius =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            collapsedSubtitleTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    public void setExpandedSubtitleAppearance(int resId) {
        TintTypedArray a =
                TintTypedArray.obtainStyledAttributes(
                        view.getContext(), resId, androidx.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textColor)) {
            expandedSubtitleColor =
                    a.getColorStateList(
                            androidx.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(androidx.appcompat.R.styleable.TextAppearance_android_textSize)) {
            expandedSubtitleSize =
                    a.getDimensionPixelSize(
                            androidx.appcompat.R.styleable.TextAppearance_android_textSize,
                            (int) expandedSubtitleSize);
        }
        expandedSubtitleShadowColor =
                a.getInt(androidx.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        expandedSubtitleShadowDx =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        expandedSubtitleShadowDy =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        expandedSubtitleShadowRadius =
                a.getFloat(androidx.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            expandedSubtitleTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    private Typeface readFontFamilyTypeface(int resId) {
        final TypedArray a =
                view.getContext().obtainStyledAttributes(resId, new int[]{android.R.attr.fontFamily});
        try {
            final String family = a.getString(0);
            if (family != null) {
                return Typeface.create(family, Typeface.NORMAL);
            }
        } finally {
            a.recycle();
        }
        return null;
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    public void setCollapsedTitleTypeface(Typeface typeface) {
        if (collapsedTitleTypeface != typeface) {
            collapsedTitleTypeface = typeface;
            recalculate();
        }
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    public void setExpandedTitleTypeface(Typeface typeface) {
        if (expandedTitleTypeface != typeface) {
            expandedTitleTypeface = typeface;
            recalculate();
        }
    }

    public void setTitleTypefaces(Typeface typeface) {
        collapsedTitleTypeface = expandedTitleTypeface = typeface;
        recalculate();
    }

    public Typeface getCollapsedTitleTypeface() {
        return collapsedTitleTypeface != null ? collapsedTitleTypeface : Typeface.DEFAULT;
    }

    public Typeface getExpandedTitleTypeface() {
        return expandedTitleTypeface != null ? expandedTitleTypeface : Typeface.DEFAULT;
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    public void setCollapsedSubtitleTypeface(Typeface typeface) {
        if (collapsedSubtitleTypeface != typeface) {
            collapsedSubtitleTypeface = typeface;
            recalculate();
        }
    }

    @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
    public void setExpandedSubtitleTypeface(Typeface typeface) {
        if (expandedSubtitleTypeface != typeface) {
            expandedSubtitleTypeface = typeface;
            recalculate();
        }
    }

    public void setSubtitleTypefaces(Typeface typeface) {
        collapsedSubtitleTypeface = expandedSubtitleTypeface = typeface;
        recalculate();
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
        currentSubtitleX = lerp(expandedSubtitleX, collapsedSubtitleX, fraction, positionInterpolator);
        currentSubtitleY = lerp(expandedSubtitleY, collapsedSubtitleY, fraction, positionInterpolator);

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
                blendColors(expandedTitleShadowColor, collapsedTitleShadowColor, fraction));

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
                blendColors(expandedSubtitleShadowColor, collapsedSubtitleShadowColor, fraction));

        ViewCompat.postInvalidateOnAnimation(view);
    }

    @ColorInt
    private int getCurrentExpandedTitleColor() {
        if (state != null) {
            return expandedTitleColor.getColorForState(state, 0);
        } else {
            return expandedTitleColor.getDefaultColor();
        }
    }

    @ColorInt
    @VisibleForTesting
    public int getCurrentCollapsedTitleColor() {
        if (state != null) {
            return collapsedTitleColor.getColorForState(state, 0);
        } else {
            return collapsedTitleColor.getDefaultColor();
        }
    }

    @ColorInt
    private int getCurrentExpandedSubtitleColor() {
        if (state != null) {
            return expandedSubtitleColor.getColorForState(state, 0);
        } else {
            return expandedSubtitleColor.getDefaultColor();
        }
    }

    @ColorInt
    @VisibleForTesting
    public int getCurrentCollapsedSubtitleColor() {
        if (state != null) {
            return collapsedSubtitleColor.getColorForState(state, 0);
        } else {
            return collapsedSubtitleColor.getDefaultColor();
        }
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
            final float titleHeight = titlePaint.descent() - titlePaint.ascent();
            final float titleOffset = titleHeight / 2 - titlePaint.descent();
            final float subtitleHeight = subtitlePaint.descent() - subtitlePaint.ascent();
            final float subtitleOffset = subtitleHeight / 2 - subtitlePaint.descent();
            final float offset = (collapsedBounds.height() - (titleHeight + subtitleHeight)) / 3;
            collapsedTitleY = collapsedBounds.top + offset - titlePaint.ascent();
            collapsedSubtitleY = collapsedBounds.top + offset * 2 + titleHeight - subtitlePaint.ascent();
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
        titleWidth = titleToDraw != null ? titlePaint.measureText(titleToDraw, 0, titleToDraw.length()) : 0;
        subtitleWidth = subtitleToDraw != null ? subtitlePaint.measureText(subtitleToDraw, 0, subtitleToDraw.length()) : 0;
        final int expandedAbsGravity =
                GravityCompat.getAbsoluteGravity(
                        expandedTextGravity,
                        isRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
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
            switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    expandedSubtitleY = expandedBounds.bottom;
                    break;
                case Gravity.TOP:
                    expandedSubtitleY = expandedBounds.top - subtitlePaint.ascent();
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    float textHeight = subtitlePaint.descent() - subtitlePaint.ascent();
                    float textOffset = (textHeight / 2) - subtitlePaint.descent();
                    expandedSubtitleY = expandedBounds.centerY() + textOffset;
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
                    canvas.drawText(subtitleToDraw, 0, subtitleToDraw.length(), subtitleX, subtitleY, titlePaint);
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
                    TextUtils.ellipsize(this.title, titlePaint, availableWidth, TextUtils.TruncateAt.END);
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
            updateDrawText = (currentSubtitleSize != newTextSize) || boundsChanged || updateDrawText;
            currentSubtitleSize = newTextSize;
            boundsChanged = false;
        }

        if (subtitleToDraw == null || updateDrawText) {
            subtitlePaint.setTextSize(currentSubtitleSize);
            subtitlePaint.setTypeface(currentSubtitleTypeface);
            // Use linear title scaling if we're scaling the canvas
            subtitlePaint.setLinearText(subtitleScale != 1f);

            // If we don't currently have title to draw, or the title size has changed, ellipsize...
            final CharSequence text =
                    TextUtils.ellipsize(this.subtitle, subtitlePaint, availableWidth, TextUtils.TruncateAt.END);
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

        final int w = Math.round(subtitlePaint.measureText(subtitleToDraw, 0, subtitleToDraw.length()));
        final int h = Math.round(subtitleTextureDescent - subtitleTextureAscent);

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
    public void setTitle(CharSequence title) {
        if (title == null || !title.equals(this.title)) {
            this.title = title;
            titleToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    public CharSequence getTitle() {
        return title;
    }

    /**
     * Set the subtitle to display
     *
     * @param subtitle
     */
    public void setSubtitle(CharSequence subtitle) {
        if (subtitle == null || !subtitle.equals(this.subtitle)) {
            this.subtitle = subtitle;
            subtitleToDraw = null;
            clearTexture();
            recalculate();
        }
    }

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
            float startValue, float endValue, float fraction, TimeInterpolator interpolator) {
        if (interpolator != null) {
            fraction = interpolator.getInterpolation(fraction);
        }
        return AnimationUtils.lerp(startValue, endValue, fraction);
    }

    private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
        return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
    }
}