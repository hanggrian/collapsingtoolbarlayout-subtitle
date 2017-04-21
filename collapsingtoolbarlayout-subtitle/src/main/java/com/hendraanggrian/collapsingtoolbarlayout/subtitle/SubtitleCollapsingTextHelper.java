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

package com.hendraanggrian.collapsingtoolbarlayout.subtitle;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;

public final class SubtitleCollapsingTextHelper {

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
    private float mExpandedTextSize = 15;
    private float mCollapsedTextSize = 15;
    private int mExpandedTextColor;
    private int mCollapsedTitleTextColor;

    //region Text sizes
    private float mTitleExpandedDrawY;
    private float mTitleCollapsedDrawY;
    private float mTitleExpandedDrawX;
    private float mTitleCollapsedDrawX;
    private float mTitleDrawX;
    private float mTitleDrawY;
    private float mSubtitleExpandedDrawY;
    private float mSubtitleCollapsedDrawY;
    private float mSubtitleExpandedDrawX;
    private float mSubtitleCollapsedDrawX;
    private float mSubtitleDrawX;
    private float mSubtitleDrawY;
    //endregion

    private Typeface mCollapsedTypeface;
    private Typeface mExpandedTypeface;
    private Typeface mCurrentTypeface;

    // begin modification
    private float mExpandedSubSize = 15;
    private int mExpandedSubColor;

    private float mCollapsedSubSize = 15;
    private int mCollapsedSubColor;
    private float mCurrentSubSize;
    private float mCurrentSubScale;
    private float mSubScale;
// end modification

    //region Text
    private CharSequence mTitleText;
    private CharSequence mTitleTextToDraw;
    private CharSequence mSubtitleText;
    private CharSequence mSubtitleTextToDraw;
    //endregion
    private boolean mIsRtl;

    private boolean mUseTexture;
    private Bitmap mExpandedTitleTexture;
    private Paint mTexturePaint;
    private float mTextureAscent;
    private float mTextureDescent;

    private float mScale;
    private float mCurrentTextSize;

    private boolean mBoundsChanged;

    //region Title Text Paint
    private final TextPaint mTitleTextPaint;
    private final TextPaint mSubtitleTextPaint;
    //endregion

    private Interpolator mPositionInterpolator;
    private Interpolator mTextSizeInterpolator;

    private float mCollapsedShadowRadius, mCollapsedShadowDx, mCollapsedShadowDy;
    private int mCollapsedShadowColor;

    private float mExpandedShadowRadius, mExpandedShadowDx, mExpandedShadowDy;
    private int mExpandedShadowColor;

    public SubtitleCollapsingTextHelper(View view) {
        mView = view;

        mTitleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mSubtitleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG); // modification

        mCollapsedBounds = new Rect();
        mExpandedBounds = new Rect();
        mCurrentBounds = new RectF();
    }

    public void setTextSizeInterpolator(Interpolator interpolator) {
        mTextSizeInterpolator = interpolator;
        recalculate();
    }

    public void setPositionInterpolator(Interpolator interpolator) {
        mPositionInterpolator = interpolator;
        recalculate();
    }

    public void setExpandedTextSize(float textSize) {
        if (mExpandedTextSize != textSize) {
            mExpandedTextSize = textSize;
            recalculate();
        }
    }

    public void setCollapsedTextSize(float textSize) {
        if (mCollapsedTextSize != textSize) {
            mCollapsedTextSize = textSize;
            recalculate();
        }
    }

    // begin modification
    public void setExpandedSubtitleTextColor(int mExpandedSubColor) {
        this.mExpandedSubColor = mExpandedSubColor;
    }

    public void setExpandedSubtitleTextSize(int mExpandedSubSize) {
        this.mExpandedSubSize = mExpandedSubSize;
    }
// end

    public void setCollapsedTitleTextColor(int textColor) {
        if (mCollapsedTitleTextColor != textColor) {
            mCollapsedTitleTextColor = textColor;
            recalculate();
        }
    }

    public void setExpandedTitleTextColor(int textColor) {
        if (mExpandedTextColor != textColor) {
            mExpandedTextColor = textColor;
            recalculate();
        }
    }

    public void setExpandedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mExpandedBounds, left, top, right, bottom)) {
            mExpandedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    public void setCollapsedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(mCollapsedBounds, left, top, right, bottom)) {
            mCollapsedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    public void onBoundsChanged() {
        mDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0 && mExpandedBounds.width() > 0 && mExpandedBounds.height() > 0;
    }

    public void setExpandedTextGravity(int gravity) {
        if (mExpandedTextGravity != gravity) {
            mExpandedTextGravity = gravity;
            recalculate();
        }
    }

    public int getExpandedTextGravity() {
        return mExpandedTextGravity;
    }

    public void setCollapsedTextGravity(int gravity) {
        if (mCollapsedTextGravity != gravity) {
            mCollapsedTextGravity = gravity;
            recalculate();
        }
    }

    public int getCollapsedTextGravity() {
        return mCollapsedTextGravity;
    }

    public void setCollapsedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mCollapsedTitleTextColor = a.getColor(R.styleable.TextAppearance_android_textColor, mCollapsedTitleTextColor);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mCollapsedTextSize = a.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, (int) mCollapsedTextSize);
        }
        mCollapsedShadowColor = a.getInt(R.styleable.TextAppearance_android_shadowColor, 0);
        mCollapsedShadowDx = a.getFloat(R.styleable.TextAppearance_android_shadowDx, 0);
        mCollapsedShadowDy = a.getFloat(R.styleable.TextAppearance_android_shadowDy, 0);
        mCollapsedShadowRadius = a.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            mCollapsedTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    public void setExpandedTextAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mExpandedTextColor = a.getColor(R.styleable.TextAppearance_android_textColor, mExpandedTextColor);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mExpandedTextSize = a.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, (int) mExpandedTextSize);
        }
        mExpandedShadowColor = a.getInt(R.styleable.TextAppearance_android_shadowColor, 0);
        mExpandedShadowDx = a.getFloat(R.styleable.TextAppearance_android_shadowDx, 0);
        mExpandedShadowDy = a.getFloat(R.styleable.TextAppearance_android_shadowDy, 0);
        mExpandedShadowRadius = a.getFloat(R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();

        if (Build.VERSION.SDK_INT >= 16) {
            mExpandedTypeface = readFontFamilyTypeface(resId);
        }

        recalculate();
    }

    //region subtitle appearance setters
    public void setCollapsedSubtitleAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mCollapsedSubColor = a.getColor(R.styleable.TextAppearance_android_textColor, mCollapsedSubColor);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mCollapsedSubSize = a.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, (int) mCollapsedSubSize);
        }
        a.recycle();
    }

    public void setExpandedSubtitleAppearance(int resId) {
        TypedArray a = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (a.hasValue(R.styleable.TextAppearance_android_textColor)) {
            mExpandedSubColor = a.getColor(R.styleable.TextAppearance_android_textColor, mExpandedSubColor);
        }
        if (a.hasValue(R.styleable.TextAppearance_android_textSize)) {
            mExpandedSubSize = a.getDimensionPixelSize(R.styleable.TextAppearance_android_textSize, (int) mExpandedSubSize);
        }
        a.recycle();
    }
    //endregion

    private Typeface readFontFamilyTypeface(int resId) {
        final TypedArray a = mView.getContext().obtainStyledAttributes(resId, new int[]{android.R.attr.fontFamily});
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

    public void setCollapsedTypeface(Typeface typeface) {
        if (mCollapsedTypeface != typeface) {
            mCollapsedTypeface = typeface;
            recalculate();
        }
    }

    public void setExpandedTypeface(Typeface typeface) {
        if (mExpandedTypeface != typeface) {
            mExpandedTypeface = typeface;
            recalculate();
        }
    }

    public void setTypefaces(Typeface typeface) {
        mCollapsedTypeface = mExpandedTypeface = typeface;
        recalculate();
    }

    public Typeface getCollapsedTypeface() {
        return mCollapsedTypeface != null ? mCollapsedTypeface : Typeface.DEFAULT;
    }

    public Typeface getExpandedTypeface() {
        return mExpandedTypeface != null ? mExpandedTypeface : Typeface.DEFAULT;
    }

    /**
     * Set the value indicating the current scroll value. This decides how much of the
     * background will be displayed, as well as the title metrics/positioning.
     * <p>
     * A value of {@code 0.0} indicates that the layout is fully expanded.
     * A value of {@code 1.0} indicates that the layout is fully collapsed.
     */
    public void setExpansionFraction(float fraction) {
        fraction = MathUtils.constrain(fraction, 0f, 1f);

        if (fraction != mExpandedFraction) {
            mExpandedFraction = fraction;
            calculateCurrentOffsets();
        }
    }

    public float getExpansionFraction() {
        return mExpandedFraction;
    }

    public float getCollapsedTextSize() {
        return mCollapsedTextSize;
    }

    public float getExpandedTextSize() {
        return mExpandedTextSize;
    }

    private void calculateCurrentOffsets() {
        calculateOffsets(mExpandedFraction);
    }

    private void calculateOffsets(final float fraction) {
        interpolateBounds(fraction);
        mTitleDrawX = lerp(mTitleExpandedDrawX, mTitleCollapsedDrawX, fraction, mPositionInterpolator);
        mTitleDrawY = lerp(mTitleExpandedDrawY, mTitleCollapsedDrawY, fraction, mPositionInterpolator);
        //mSubtitleDrawX = lerp(mSubtitleExpandedSubX, mSubtitleCollapsedDrawY, fraction, mPositionInterpolator);
        mSubtitleDrawY = lerp(mSubtitleExpandedDrawY, mSubtitleCollapsedDrawY, fraction, mPositionInterpolator);

        setInterpolatedTitleTextSize(lerp(mExpandedTextSize, mCollapsedTextSize, fraction, mTextSizeInterpolator));
        setInterpolatedSubtitleTextSize(lerp(mExpandedSubSize, mCollapsedSubSize, fraction, mTextSizeInterpolator));

        if (mCollapsedTitleTextColor != mExpandedTextColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            mTitleTextPaint.setColor(blendColors(mExpandedTextColor, mCollapsedTitleTextColor, fraction));
        } else {
            mTitleTextPaint.setColor(mCollapsedTitleTextColor);
        }

// begin modification
        if (mCollapsedSubColor != mExpandedSubColor) {
            // If the collapsed and expanded text colors are different, blend them based on the
            // fraction
            mSubtitleTextPaint.setColor(blendColors(mExpandedSubColor, mCollapsedSubColor, fraction));
        } else {
            mSubtitleTextPaint.setColor(mCollapsedSubColor);
        }
// end

        mTitleTextPaint.setShadowLayer(
                lerp(mExpandedShadowRadius, mCollapsedShadowRadius, fraction, null),
                lerp(mExpandedShadowDx, mCollapsedShadowDx, fraction, null),
                lerp(mExpandedShadowDy, mCollapsedShadowDy, fraction, null),
                blendColors(mExpandedShadowColor, mCollapsedShadowColor, fraction));

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    // begin modification
// WE DONT SUPPORT GRAVITY OF THE TITLE
    private void calculateBaseOffsets() {
        final float currentTextSize = mCurrentTextSize;

        // We then calculate the collapsed text size, using the same logic
        calculateUsingTitleTextSize(mCollapsedTextSize);
        calculateUsingSubtitleTextSize(mCollapsedSubSize);

        float textHeight = mTitleTextPaint.descent() - mTitleTextPaint.ascent();
        float textOffset = (textHeight / 2);
        if (mSubtitleText != null) {
            float subHeight = mSubtitleTextPaint.descent() - mSubtitleTextPaint.ascent();
            float subOffset = (subHeight / 2) - mSubtitleTextPaint.descent();
            float offset = ((mCollapsedBounds.height() - (textHeight + subHeight)) / 3);

            mTitleCollapsedDrawY = mCollapsedBounds.top + offset - mTitleTextPaint.ascent();
            mSubtitleCollapsedDrawY = mCollapsedBounds.top + (offset * 2) + textHeight - mSubtitleTextPaint.ascent();
        } else { // title only
            mTitleCollapsedDrawY = mCollapsedBounds.centerY() + textOffset;
        }
        mTitleCollapsedDrawX = mCollapsedBounds.left;


        calculateUsingTitleTextSize(mExpandedTextSize);
        calculateUsingSubtitleTextSize(mExpandedSubSize);

        textHeight = mTitleTextPaint.descent() - mTitleTextPaint.ascent();
        textOffset = (textHeight / 2);
        if (mSubtitleText != null) {
            float subHeight = mSubtitleTextPaint.descent() - mSubtitleTextPaint.ascent();
            float subOffset = (subHeight / 2);

            mTitleExpandedDrawY = mExpandedBounds.centerY() + textOffset;
            mSubtitleExpandedDrawY = mTitleExpandedDrawY + subOffset - mSubtitleTextPaint.ascent();
        } else { // title only
            mTitleExpandedDrawY = mExpandedBounds.centerY() + textOffset;
        }
        mTitleExpandedDrawX = mExpandedBounds.left;

        // The bounds have changed so we need to clear the texture
        clearTexture();
        // Now reset the text size back to the original
        setInterpolatedTitleTextSize(currentTextSize);
    }
//end modification

    private void interpolateBounds(float fraction) {
        mCurrentBounds.left = lerp(mExpandedBounds.left, mCollapsedBounds.left, fraction, mPositionInterpolator);
        mCurrentBounds.top = lerp(mTitleExpandedDrawY, mTitleCollapsedDrawY, fraction, mPositionInterpolator);
        mCurrentBounds.right = lerp(mExpandedBounds.right, mCollapsedBounds.right, fraction, mPositionInterpolator);
        mCurrentBounds.bottom = lerp(mExpandedBounds.bottom, mCollapsedBounds.bottom, fraction, mPositionInterpolator);
    }

    public void draw(Canvas canvas) {
        final int saveCount = canvas.save();

        if (mTitleTextToDraw != null && mDrawTitle) {
            float x = mTitleDrawX;
            float y = mTitleDrawY;
            float subY = mSubtitleDrawY;
            final boolean drawTexture = mUseTexture && mExpandedTitleTexture != null;

            final float ascent;
            final float descent;
            if (drawTexture) {
                ascent = mTextureAscent * mScale;
                descent = mTextureDescent * mScale;
            } else {
                ascent = mTitleTextPaint.ascent() * mScale;
                descent = mTitleTextPaint.descent() * mScale;
            }

            if (DEBUG_DRAW) {
                // Just a debug tool, which drawn a Magneta rect in the text bounds
                canvas.drawRect(mCurrentBounds.left, y + ascent, mCurrentBounds.right, y + descent, DEBUG_DRAW_PAINT);
            }

            if (drawTexture) {
                y += ascent;
            }

            //region draw subtitle
            if (mSubtitleTextToDraw != null) {
                if (mSubScale != 1f) {
                    canvas.scale(mSubScale, mSubScale, x, subY);
                }
                canvas.drawText(mSubtitleTextToDraw, 0, mSubtitleTextToDraw.length(), x, subY, mSubtitleTextPaint);
                canvas.restoreToCount(saveCount);
            }
            //endregion

            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }

            if (drawTexture) {
                // If we should use a texture, draw it instead of text
                canvas.drawBitmap(mExpandedTitleTexture, x, y, mTexturePaint);
            } else {
                canvas.drawText(mTitleTextToDraw, 0, mTitleTextToDraw.length(), x, y, mTitleTextPaint);
            }
        }

        canvas.restoreToCount(saveCount);
    }

    private boolean calculateIsRtl(CharSequence text) {
        final boolean defaultIsRtl = ViewCompat.getLayoutDirection(mView) == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
                ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
                : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
    }

    //region set interpolated text size
    private void setInterpolatedTitleTextSize(float textSize) {
        calculateUsingTitleTextSize(textSize);

        // Use our texture if the scale isn't 1.0
        mUseTexture = USE_SCALING_TEXTURE && mScale != 1f;

        if (mUseTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTexture();
        }

        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void setInterpolatedSubtitleTextSize(float textSize) {
        calculateUsingSubtitleTextSize(textSize);

        ViewCompat.postInvalidateOnAnimation(mView);
    }
    //endregion

    //region calculate using text size
    private void calculateUsingTitleTextSize(final float textSize) {
        if (mTitleText == null) return;

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(textSize, mCollapsedTextSize)) {
            availableWidth = mCollapsedBounds.width();
            newTextSize = mCollapsedTextSize;
            mScale = 1f;
            if (mCurrentTypeface != mCollapsedTypeface) {
                mCurrentTypeface = mCollapsedTypeface;
                updateDrawText = true;
            }
        } else {
            availableWidth = mExpandedBounds.width();
            newTextSize = mExpandedTextSize;
            if (mCurrentTypeface != mExpandedTypeface) {
                mCurrentTypeface = mExpandedTypeface;
                updateDrawText = true;
            }

            if (isClose(textSize, mExpandedTextSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                mScale = 1f;
            } else {
                // Else, we'll scale down from the expanded text size
                mScale = textSize / mExpandedTextSize;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (mCurrentTextSize != newTextSize) || mBoundsChanged || updateDrawText;
            mCurrentTextSize = newTextSize;
            mBoundsChanged = false;
        }

        if (mTitleTextToDraw == null || updateDrawText) {
            mTitleTextPaint.setTextSize(mCurrentTextSize);
            mTitleTextPaint.setTypeface(mCurrentTypeface);
            // Use linear text scaling if we're scaling the canvas
            mTitleTextPaint.setLinearText(mScale != 1f);

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            final CharSequence title = TextUtils.ellipsize(mTitleText, mTitleTextPaint,
                    availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(title, mTitleTextToDraw)) {
                mTitleTextToDraw = title;
                mIsRtl = calculateIsRtl(mTitleTextToDraw);
            }
        }
    }

    private void calculateUsingSubtitleTextSize(final float textSize) {
        if (mTitleText == null || mSubtitleText == null) return;

        final float availableWidth;
        final float newTextSize;
        boolean updateDrawText = false;

        if (isClose(textSize, mCollapsedSubSize)) {
            availableWidth = mCollapsedBounds.width();
            newTextSize = mCollapsedSubSize;
            mSubScale = 1f;
            if (mCurrentTypeface != mCollapsedTypeface) {
                mCurrentTypeface = mCollapsedTypeface;
                updateDrawText = true;
            }
        } else {
            availableWidth = mExpandedBounds.width();
            newTextSize = mExpandedSubSize;
            if (mCurrentTypeface != mExpandedTypeface) {
                mCurrentTypeface = mExpandedTypeface;
                updateDrawText = true;
            }

            if (isClose(textSize, mExpandedSubSize)) {
                // If we're close to the expanded text size, snap to it and use a scale of 1
                mSubScale = 1f;
            } else {
                // Else, we'll scale down from the expanded text size
                mSubScale = textSize / mExpandedSubSize;
            }
        }

        if (availableWidth > 0) {
            updateDrawText = (mCurrentSubSize != newTextSize) || mBoundsChanged || updateDrawText;
            mCurrentSubSize = newTextSize;
            mBoundsChanged = false;
        }


        if (updateDrawText) {
            mSubtitleTextPaint.setTypeface(mCurrentTypeface);
            mSubtitleTextPaint.setTextSize(mCurrentSubSize);

            // Use linear text scaling if we're scaling the canvas
            mSubtitleTextPaint.setLinearText(mSubScale != 1f);

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            final CharSequence title = TextUtils.ellipsize(mSubtitleText, mSubtitleTextPaint, availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(title, mSubtitleTextToDraw)) {
                mSubtitleTextToDraw = title;
                mIsRtl = calculateIsRtl(mSubtitleTextToDraw);
            }
        }
    }
    //endregion

    private void ensureExpandedTexture() {
        if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty() || TextUtils.isEmpty(mTitleTextToDraw)) {
            return;
        }

        calculateOffsets(0f);
        mTextureAscent = mTitleTextPaint.ascent();
        mTextureDescent = mTitleTextPaint.descent();

        final int w = Math.round(mTitleTextPaint.measureText(mTitleTextToDraw, 0, mTitleTextToDraw.length()));
        final int h = Math.round(mTextureDescent - mTextureAscent);

        if (w <= 0 || h <= 0) {
            return; // If the width or height are 0, return
        }

        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(mExpandedTitleTexture);
        c.drawText(mTitleTextToDraw, 0, mTitleTextToDraw.length(), 0, h - mTitleTextPaint.descent(), mTitleTextPaint);

        if (mTexturePaint == null) {
            // Make sure we have a paint
            mTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        }
    }

    public void recalculate() {
        if (mView.getHeight() > 0 && mView.getWidth() > 0) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    //region Text setter
    public void setTitle(CharSequence text) {
        if (text == null || !text.equals(mTitleText)) {
            mTitleText = text;
            mTitleTextToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    public void setSubtitle(CharSequence text) {
        if (text == null || !text.equals(mSubtitleText)) {
            mSubtitleText = text;
            mSubtitleTextToDraw = null;
            clearTexture();
            recalculate();
        }
    }
    //endregion

    public CharSequence getText() {
        return mTitleText;
    }

    private void clearTexture() {
        if (mExpandedTitleTexture != null) {
            mExpandedTitleTexture.recycle();
            mExpandedTitleTexture = null;
        }
    }

    /**
     * Returns true if {@code value} is 'close' to it's closest decimal value. Close is currently
     * defined as it's difference being < 0.001.
     */
    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
    }

    public int getExpandedTextColor() {
        return mExpandedTextColor;
    }

    public int getCollapsedTextColor() {
        return mCollapsedTitleTextColor;
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