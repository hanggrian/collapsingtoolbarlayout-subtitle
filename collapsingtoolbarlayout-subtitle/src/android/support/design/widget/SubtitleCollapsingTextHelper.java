/*
 * Copyright (C) 2015 The Android Open Source Project
 * Modified 2016 by Ahmad Muzakki (modifications are marked with comments)
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

package android.support.design.widget;

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
import android.support.annotation.ColorInt;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.TintTypedArray;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;

import com.hendraanggrian.collapsingtoolbarlayout.subtitle.R;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
@SuppressWarnings("RestrictedApi")
final class SubtitleCollapsingTextHelper {

    // Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
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

    private final View mView;

    private boolean mDrawTitle;
    private float mExpandedFraction;

    private final Rect mExpandedBounds;
    private final Rect mCollapsedBounds;
    private final RectF mCurrentBounds;
    private int mExpandedTextGravity = Gravity.CENTER_VERTICAL;
    private int mCollapsedTextGravity = Gravity.CENTER_VERTICAL;
    private float mExpandedTitleSize = 15;
    private float mExpandedSubtitleSize = 15;
    private float mCollapsedTitleSize = 15;
    private float mCollapsedSubtitleSize = 15;
    private ColorStateList mExpandedTitleColor, mExpandedSubtitleColor;
    private ColorStateList mCollapsedTitleColor, mCollapsedSubtitleColor;

    private float mExpandedDrawY, mExpandedDrawSubY;
    private float mCollapsedDrawY, mCollapsedDrawSubY;
    private float mExpandedDrawX;
    private float mCollapsedDrawX;
    private float mCurrentDrawX;
    private float mCurrentDrawY, mCurrentDrawSubY;
    private Typeface mCollapsedTypeface;
    private Typeface mExpandedTypeface;
    private Typeface mCurrentTypeface;

    private CharSequence mTitle, mSubtitle;
    private CharSequence mTextToDraw;
    private boolean mIsRtl;

    private boolean mUseTexture;
    private Bitmap mExpandedTitleTexture;
    private Paint mTexturePaint;
    private float mTextureAscent;
    private float mTextureDescent;

    private float mTitleScale, mSubtitleScale;
    private float mCurrentTitleSize, mCurrentSubtitleSize;

    private int[] mState;

    private boolean mBoundsChanged;

    private final TextPaint mTitlePaint, mSubtitlePaint;

    private Interpolator mPositionInterpolator;
    private Interpolator mTextSizeInterpolator;

    private float mCollapsedShadowRadius, mCollapsedShadowDx, mCollapsedShadowDy;
    private int mCollapsedShadowColor;

    private float mExpandedShadowRadius, mExpandedShadowDx, mExpandedShadowDy;
    private int mExpandedShadowColor;

    SubtitleCollapsingTextHelper(View view) {
        mView = view;

        mTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mSubtitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);

        mCollapsedBounds = new Rect();
        mExpandedBounds = new Rect();
        mCurrentBounds = new RectF();
    }

    void setTextSizeInterpolator(Interpolator interpolator) {
        mTextSizeInterpolator = interpolator;
        recalculate();
    }

    void setPositionInterpolator(Interpolator interpolator) {
        mPositionInterpolator = interpolator;
        recalculate();
    }

    void setExpandedTextSize(float textSize) {
        if (mExpandedTitleSize != textSize) {
            mExpandedTitleSize = textSize;
            recalculate();
        }
    }

    void setCollapsedTextSize(float textSize) {
        if (mCollapsedTitleSize != textSize) {
            mCollapsedTitleSize = textSize;
            recalculate();
        }
    }

    void setCollapsedTextColor(ColorStateList textColor) {
        if (mCollapsedTitleColor != textColor) {
            mCollapsedTitleColor = textColor;
            recalculate();
        }
    }

    void setExpandedTextColor(ColorStateList textColor) {
        if (mExpandedTitleColor != textColor) {
            mExpandedTitleColor = textColor;
            recalculate();
        }
    }

    void setExpandedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mExpandedBounds, left, top, right, bottom)) {
            mExpandedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    void setCollapsedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mCollapsedBounds, left, top, right, bottom)) {
            mCollapsedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    private void onBoundsChanged() {
        mDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0
                && mExpandedBounds.width() > 0 && mExpandedBounds.height() > 0;
    }

    void setExpandedTextGravity(int gravity) {
        if (mExpandedTextGravity != gravity) {
            mExpandedTextGravity = gravity;
            recalculate();
        }
    }

    int getExpandedTextGravity() {
        return mExpandedTextGravity;
    }

    void setCollapsedTextGravity(int gravity) {
        if (mCollapsedTextGravity != gravity) {
            mCollapsedTextGravity = gravity;
            recalculate();
        }
    }

    int getCollapsedTextGravity() {
        return mCollapsedTextGravity;
    }

    void setCollapsedTitleAppearance(int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), resId,
                android.support.v7.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mCollapsedTitleColor = a.getColorStateList(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mCollapsedTitleSize = a.getDimensionPixelSize(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                    (int) mCollapsedTitleSize);
        }
        mCollapsedShadowColor = a.getInt(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        mCollapsedShadowDx = a.getFloat(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        mCollapsedShadowDy = a.getFloat(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        mCollapsedShadowRadius = a.getFloat(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            mCollapsedTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    void setExpandedTitleAppearance(int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), resId,
                android.support.v7.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mExpandedTitleColor = a.getColorStateList(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mExpandedTitleSize = a.getDimensionPixelSize(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                    (int) mExpandedTitleSize);
        }
        mExpandedShadowColor = a.getInt(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        mExpandedShadowDx = a.getFloat(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        mExpandedShadowDy = a.getFloat(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        mExpandedShadowRadius = a.getFloat(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            mExpandedTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    void setCollapsedSubtitleAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mCollapsedSubtitleColor = a.getColorStateList(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mCollapsedSubtitleSize = a.getDimensionPixelSize(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                    (int) mCollapsedSubtitleSize);
        }
        a.recycle();

        recalculate();
    }

    void setExpandedSubtitleAppearance(int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), resId,
                android.support.v7.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mExpandedSubtitleColor = a.getColorStateList(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mExpandedSubtitleSize = a.getDimensionPixelSize(
                    android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                    (int) mExpandedSubtitleSize);
        }
        a.recycle();

        recalculate();
    }

    private Typeface readFontFamilyTypeface(int resId) {
        final TypedArray a = mView.getContext().obtainStyledAttributes(resId, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN
                ? new int[]{android.R.attr.fontFamily}
                : new int[0]);
        try {
            final String family = a.getString(0);
            if (family != null) {
                return Typeface.create(family, Typeface.NORMAL);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to read font family typeface: " + resId);
        } finally {
            a.recycle();
        }
        return null;
    }

    void setCollapsedTypeface(Typeface typeface) {
        if (mCollapsedTypeface != typeface) {
            mCollapsedTypeface = typeface;
            recalculate();
        }
    }

    void setExpandedTypeface(Typeface typeface) {
        if (mExpandedTypeface != typeface) {
            mExpandedTypeface = typeface;
            recalculate();
        }
    }

    void setTypefaces(Typeface typeface) {
        mCollapsedTypeface = mExpandedTypeface = typeface;
        recalculate();
    }

    Typeface getCollapsedTypeface() {
        return mCollapsedTypeface != null ? mCollapsedTypeface : Typeface.DEFAULT;
    }

    Typeface getExpandedTypeface() {
        return mExpandedTypeface != null ? mExpandedTypeface : Typeface.DEFAULT;
    }

    void setExpansionFraction(float fraction) {
        fraction = MathUtils.constrain(fraction, 0f, 1f);

        if (fraction != mExpandedFraction) {
            mExpandedFraction = fraction;
            calculateCurrentOffsets();
        }
    }

    final boolean setState(final int[] state) {
        mState = state;

        if (isStateful()) {
            recalculate();
            return true;
        }

        return false;
    }

    final boolean isStateful() {
        return (mCollapsedTitleColor != null && mCollapsedTitleColor.isStateful())
                || (mExpandedTitleColor != null && mExpandedTitleColor.isStateful());
    }

    float getExpansionFraction() {
        return mExpandedFraction;
    }

    float getCollapsedTextSize() {
        return mCollapsedTitleSize;
    }

    float getExpandedTextSize() {
        return mExpandedTitleSize;
    }

    private void calculateCurrentOffsets() {
        calculateOffsets(mExpandedFraction);
    }

    private void calculateOffsets(final float fraction) {
        interpolateBounds(fraction);
        mCurrentDrawX = lerp(mExpandedDrawX, mCollapsedDrawX, fraction,
                mPositionInterpolator);
        mCurrentDrawY = lerp(mExpandedDrawY, mCollapsedDrawY, fraction,
                mPositionInterpolator);
        //region modification
        mCurrentDrawSubY = lerp(mExpandedDrawSubY, mCollapsedDrawSubY, fraction,
                mPositionInterpolator);
        //endregion

        setInterpolatedTitleSize(lerp(mExpandedTitleSize, mCollapsedTitleSize,
                fraction, mTextSizeInterpolator));

        //region modification
        setInterpolatedSubtitleSize(lerp(mExpandedSubtitleSize, mCollapsedSubtitleSize,
                fraction, mTextSizeInterpolator));
        //endregion

        if (mCollapsedTitleColor != mExpandedTitleColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            mTitlePaint.setColor(blendColors(
                    getCurrentExpandedTitleColor(), getCurrentCollapsedTitleColor(), fraction));
        } else {
            mTitlePaint.setColor(getCurrentCollapsedTitleColor());
        }

        //region modification
        if (mCollapsedSubtitleColor != mExpandedSubtitleColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            mSubtitlePaint.setColor(blendColors(
                    getCurrentExpandedSubtitleColor(), getCurrentCollapsedSubtitleColor(), fraction));
        } else {
            mSubtitlePaint.setColor(getCurrentCollapsedSubtitleColor());
        }
        //endregion

        mTitlePaint.setShadowLayer(
                lerp(mExpandedShadowRadius, mCollapsedShadowRadius, fraction, null),
                lerp(mExpandedShadowDx, mCollapsedShadowDx, fraction, null),
                lerp(mExpandedShadowDy, mCollapsedShadowDy, fraction, null),
                blendColors(mExpandedShadowColor, mCollapsedShadowColor, fraction));

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    @ColorInt
    private int getCurrentExpandedTitleColor() {
        if (mState != null) {
            return mExpandedTitleColor.getColorForState(mState, 0);
        } else {
            return mExpandedTitleColor.getDefaultColor();
        }
    }

    @ColorInt
    private int getCurrentCollapsedTitleColor() {
        if (mState != null) {
            return mCollapsedTitleColor.getColorForState(mState, 0);
        } else {
            return mCollapsedTitleColor.getDefaultColor();
        }
    }

    @ColorInt
    private int getCurrentExpandedSubtitleColor() {
        if (mState != null) {
            return mExpandedSubtitleColor.getColorForState(mState, 0);
        } else {
            return mExpandedSubtitleColor.getDefaultColor();
        }
    }

    @ColorInt
    private int getCurrentCollapsedSubtitleColor() {
        if (mState != null) {
            return mCollapsedSubtitleColor.getColorForState(mState, 0);
        } else {
            return mCollapsedSubtitleColor.getDefaultColor();
        }
    }

    private void calculateBaseOffsets() {
        final float currentTextSize = mCurrentTitleSize;

        // We then calculate the collapsed text size, using the same logic
        calculateUsingTitleSize(mCollapsedTitleSize);
        calculateUsingSubtitleSize(mCollapsedSubtitleSize);

        float textHeight = mTitlePaint.descent() - mTitlePaint.ascent();
        if (!TextUtils.isEmpty(mSubtitle)) {
            float subHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent();
            float subOffset = (subHeight / 2) - mSubtitlePaint.descent();
            float offset = ((mCollapsedBounds.height() - (textHeight + subHeight)) / 3);

            mCollapsedDrawY = mCollapsedBounds.top + offset - mTitlePaint.ascent();
            mCollapsedDrawSubY = mCollapsedBounds.top + (offset * 2) + textHeight - mSubtitlePaint.ascent();
        } else { // title only
            textHeight = mTitlePaint.descent() - mTitlePaint.ascent();
            float textOffset = (textHeight / 2) - mTitlePaint.descent();
            mCollapsedDrawY = mCollapsedBounds.centerY() + textOffset;
        }
        mCollapsedDrawX = mCollapsedBounds.left;

        calculateUsingTitleSize(mExpandedTitleSize);
        calculateUsingSubtitleSize(mExpandedSubtitleSize);

        if (!TextUtils.isEmpty(mSubtitle)) {
            float subHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent();
            float subOffset = (subHeight / 2);

            mExpandedDrawY = mExpandedBounds.bottom + mSubtitlePaint.ascent();
            mExpandedDrawSubY = mExpandedDrawY + subOffset - mSubtitlePaint.ascent();
        } else { // title only
            mExpandedDrawY = mExpandedBounds.bottom;
        }
        mExpandedDrawX = mExpandedBounds.left;

        // The bounds have changed so we need to clear the texture
        clearTexture();
        // Now reset the text size back to the original
        setInterpolatedTitleSize(currentTextSize);
    }

    private void interpolateBounds(float fraction) {
        mCurrentBounds.left = lerp(mExpandedBounds.left, mCollapsedBounds.left,
                fraction, mPositionInterpolator);
        mCurrentBounds.top = lerp(mExpandedDrawY, mCollapsedDrawY,
                fraction, mPositionInterpolator);
        mCurrentBounds.right = lerp(mExpandedBounds.right, mCollapsedBounds.right,
                fraction, mPositionInterpolator);
        mCurrentBounds.bottom = lerp(mExpandedBounds.bottom, mCollapsedBounds.bottom,
                fraction, mPositionInterpolator);
    }

    public void draw(Canvas canvas) {
        final int saveCount = canvas.save();

        if (mTextToDraw != null && mDrawTitle) {
            float x = mCurrentDrawX;
            float y = mCurrentDrawY;
            float subY = mCurrentDrawSubY;
            final boolean drawTexture = mUseTexture && mExpandedTitleTexture != null;

            final float ascent;
            final float descent;
            if (drawTexture) {
                ascent = mTextureAscent * mTitleScale;
                descent = mTextureDescent * mTitleScale;
            } else {
                ascent = mTitlePaint.ascent() * mTitleScale;
                descent = mTitlePaint.descent() * mTitleScale;
            }

            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a magenta rect in the text bounds
                canvas.drawRect(mCurrentBounds.left, y + ascent, mCurrentBounds.right, y + descent,
                        DEBUG_DRAW_PAINT);
            }

            if (drawTexture) {
                y += ascent;
            }

            //region modification
            final int saveCountSub = canvas.save();
            if (mSubtitle != null) {
                if (mSubtitleScale != 1f) {
                    canvas.scale(mSubtitleScale, mSubtitleScale, x, subY);
                }
                canvas.drawText(mSubtitle, 0, mSubtitle.length(), x, subY, mSubtitlePaint);
                canvas.restoreToCount(saveCountSub);
            }
            //endregion

            if (mTitleScale != 1f) {
                canvas.scale(mTitleScale, mTitleScale, x, y);
            }

            if (drawTexture) {
                // If we should use a texture, draw it instead of text
                canvas.drawBitmap(mExpandedTitleTexture, x, y, mTexturePaint);
            } else {
                canvas.drawText(mTextToDraw, 0, mTextToDraw.length(), x, y, mTitlePaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private boolean calculateIsRtl(CharSequence text) {
        final boolean defaultIsRtl = ViewCompat.getLayoutDirection(mView)
                == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
                ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
                : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
    }

    private void setInterpolatedTitleSize(float textSize) {
        calculateUsingTitleSize(textSize);

        // Use our texture if the scale isn't 1.0
        mUseTexture = USE_SCALING_TEXTURE && mTitleScale != 1f;

        if (mUseTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTexture();
        }

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void calculateUsingTitleSize(final float textSize) {
        if (mTitle == null) return;

        final float collapsedWidth = mCollapsedBounds.width();
        final float expandedWidth = mExpandedBounds.width();

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(textSize, mCollapsedTitleSize)) {
            newTextSize = mCollapsedTitleSize;
            mTitleScale = 1f;
            if (mCurrentTypeface != mCollapsedTypeface) {
                mCurrentTypeface = mCollapsedTypeface;
                updateDrawText = true;
            }
            availableWidth = collapsedWidth;
        } else {
            newTextSize = mExpandedTitleSize;
            if (mCurrentTypeface != mExpandedTypeface) {
                mCurrentTypeface = mExpandedTypeface;
                updateDrawText = true;
            }
            if (isClose(textSize, mExpandedTitleSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                mTitleScale = 1f;
            } else {
                // Else, we'll scale down from the expanded text size
                mTitleScale = textSize / mExpandedTitleSize;
            }

            final float textSizeRatio = mCollapsedTitleSize / mExpandedTitleSize;
            // This is the size of the expanded bounds when it is scaled to match the
            // collapsed text size
            final float scaledDownWidth = expandedWidth * textSizeRatio;

            if (scaledDownWidth > collapsedWidth) {
                // If the scaled down size is larger than the actual collapsed width, we need to
                // cap the available width so that when the expanded text scales down, it matches
                // the collapsed width
                availableWidth = Math.min(collapsedWidth / textSizeRatio, expandedWidth);
            } else {
                // Otherwise we'll just use the expanded width
                availableWidth = expandedWidth;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (mCurrentTitleSize != newTextSize) || mBoundsChanged || updateDrawText;
            mCurrentTitleSize = newTextSize;
            mBoundsChanged = false;
        }

        if (mTextToDraw == null || updateDrawText) {
            mTitlePaint.setTextSize(mCurrentTitleSize);
            mTitlePaint.setTypeface(mCurrentTypeface);
            // Use linear text scaling if we're scaling the canvas
            mTitlePaint.setLinearText(mTitleScale != 1f);

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            final CharSequence title = TextUtils.ellipsize(mTitle, mTitlePaint,
                    availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(title, mTextToDraw)) {
                mTextToDraw = title;
                mIsRtl = calculateIsRtl(mTextToDraw);
            }
        }
    }

    private void setInterpolatedSubtitleSize(float textSize) {
        calculateUsingSubtitleSize(textSize);

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void calculateUsingSubtitleSize(final float subSize) {
        if (mSubtitle == null) return;

        final float collapsedWidth = mCollapsedBounds.width();
        final float expandedWidth = mExpandedBounds.width();

        final float availableWidth;
        final float newSubSize;
        boolean updateDrawText = false;

        if (isClose(subSize, mCollapsedSubtitleSize)) {
            newSubSize = mCollapsedSubtitleSize;
            mSubtitleScale = 1f;
            availableWidth = collapsedWidth;
        } else {
            newSubSize = mExpandedSubtitleSize;
            if (isClose(subSize, mExpandedSubtitleSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                mSubtitleScale = 1f;
            } else {
                // Else, we'll scale down from the expanded text size
                mSubtitleScale = subSize / mExpandedSubtitleSize;
            }

            final float subSizeRatio = mCollapsedSubtitleSize / mExpandedSubtitleSize;
            // This is the size of the expanded bounds when it is scaled to match the
            // collapsed text size
            final float scaledDownWidth = expandedWidth * subSizeRatio;

            if (scaledDownWidth > collapsedWidth) {
                // If the scaled down size is larger than the actual collapsed width, we need to
                // cap the available width so that when the expanded text scales down, it matches
                // the collapsed width
                availableWidth = Math.min(collapsedWidth / subSizeRatio, expandedWidth);
            } else {
                // Otherwise we'll just use the expanded width
                availableWidth = expandedWidth;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (mCurrentSubtitleSize != newSubSize) || mBoundsChanged || updateDrawText;
            mCurrentSubtitleSize = newSubSize;
            mBoundsChanged = false;
        }

        if (updateDrawText) {
            mSubtitlePaint.setTextSize(mCurrentSubtitleSize);
            mSubtitlePaint.setTypeface(mCurrentTypeface);
            // Use linear text scaling if we're scaling the canvas
            mSubtitlePaint.setLinearText(mSubtitleScale != 1f);
        }
    }

    private void ensureExpandedTexture() {
        if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty()
                || TextUtils.isEmpty(mTextToDraw)) {
            return;
        }

        calculateOffsets(0f);
        mTextureAscent = mTitlePaint.ascent();
        mTextureDescent = mTitlePaint.descent();

        final int w = Math.round(mTitlePaint.measureText(mTextToDraw, 0, mTextToDraw.length()));
        final int h = Math.round(mTextureDescent - mTextureAscent);

        if (w <= 0 || h <= 0) {
            return; // If the width or height are 0, return
        }

        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(mExpandedTitleTexture);
        c.drawText(mTextToDraw, 0, mTextToDraw.length(), 0, h - mTitlePaint.descent(), mTitlePaint);

        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    void recalculate() {
        if (mView.getHeight() > 0 && mView.getWidth() > 0) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    void setTitle(CharSequence text) {
        if (text == null || !text.equals(mTitle)) {
            mTitle = text;
            mTextToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    CharSequence getTitle() {
        return mTitle;
    }

    void setSubtitle(CharSequence text) {
        if (text == null || !text.equals(mSubtitle)) {
            mSubtitle = text;
            clearTexture();
            recalculate();
        }
    }

    CharSequence getSubtitle() {
        return mSubtitle;
    }

    private void clearTexture() {
        if (mExpandedTitleTexture != null) {
            mExpandedTitleTexture.recycle();
            mExpandedTitleTexture = null;
        }
    }

    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
    }

    ColorStateList getExpandedTextColor() {
        return mExpandedTitleColor;
    }

    ColorStateList getCollapsedTextColor() {
        return mCollapsedTitleColor;
    }

    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    private static float lerp(float startValue, float endValue, float fraction,
                              Interpolator interpolator) {
        if (interpolator != null) {
            fraction = interpolator.getInterpolation(fraction);
        }
        return AnimationUtils.lerp(startValue, endValue, fraction);
    }

    private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
        return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
    }
}