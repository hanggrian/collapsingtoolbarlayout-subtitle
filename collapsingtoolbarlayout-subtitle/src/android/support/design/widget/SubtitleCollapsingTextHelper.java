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

package android.support.design.widget;

import android.annotation.SuppressLint;
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
import android.support.v4.view.GravityCompat;
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
 * @see CollapsingTextHelper
 */
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

    private float mExpandedTitleY, mExpandedSubtitleY;
    private float mCollapsedTitleY, mCollapsedSubtitleY;
    private float mExpandedTitleX, mExpandedSubtitleX;
    private float mCollapsedTitleX, mCollapsedSubtitleX;
    private float mCurrentTitleX, mCurrentSubtitleX;
    private float mCurrentTitleY, mCurrentSubtitleY;
    private Typeface mCollapsedTypeface;
    private Typeface mExpandedTypeface;
    private Typeface mCurrentTypeface;

    private CharSequence mTitle, mSubtitle;
    private CharSequence mTitleToDraw, mSubtitleToDraw;
    private boolean mIsRtl;

    private boolean mUseTexture;
    private Bitmap mExpandedTitleTexture, mExpandedSubtitleTexture;
    private Paint mTitleTexturePaint, mSubtitleTexturePaint;
    private float mTitleTextureAscent, mSubtitleTextureAscent;
    private float mTitleTextureDescent, mSubtitleTextureDescent;

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

    void setExpandedTitleSize(float textSize) {
        if (mExpandedTitleSize != textSize) {
            mExpandedTitleSize = textSize;
            recalculate();
        }
    }

    void setCollapsedTitleSize(float textSize) {
        if (mCollapsedTitleSize != textSize) {
            mCollapsedTitleSize = textSize;
            recalculate();
        }
    }

    void setCollapsedTitleColor(ColorStateList textColor) {
        if (mCollapsedTitleColor != textColor) {
            mCollapsedTitleColor = textColor;
            recalculate();
        }
    }

    void setExpandedTitleColor(ColorStateList textColor) {
        if (mExpandedTitleColor != textColor) {
            mExpandedTitleColor = textColor;
            recalculate();
        }
    }

    void setCollapsedSubtitleColor(ColorStateList textColor) {
        if (mCollapsedSubtitleColor != textColor) {
            mCollapsedSubtitleColor = textColor;
            recalculate();
        }
    }

    void setExpandedSubtitleColor(ColorStateList textColor) {
        if (mExpandedSubtitleColor != textColor) {
            mExpandedSubtitleColor = textColor;
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

    void onBoundsChanged() {
        mDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0 && mExpandedBounds.width() > 0 && mExpandedBounds.height() > 0;
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

    @SuppressWarnings("RestrictedApi")
    void setCollapsedTitleAppearance(int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), resId, android.support.v7.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            mCollapsedTitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            mCollapsedTitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, (int) mCollapsedTitleSize);
        mCollapsedShadowColor = a.getInt(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        mCollapsedShadowDx = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        mCollapsedShadowDy = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        mCollapsedShadowRadius = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();
        if (Build.VERSION.SDK_INT >= 16)
            mCollapsedTypeface = readFontFamilyTypeface(resId);
        recalculate();
    }

    @SuppressWarnings("RestrictedApi")
    void setExpandedTitleAppearance(int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), resId, android.support.v7.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            mExpandedTitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            mExpandedTitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, (int) mExpandedTitleSize);
        mExpandedShadowColor = a.getInt(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0);
        mExpandedShadowDx = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0);
        mExpandedShadowDy = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0);
        mExpandedShadowRadius = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0);
        a.recycle();
        if (Build.VERSION.SDK_INT >= 16)
            mExpandedTypeface = readFontFamilyTypeface(resId);
        recalculate();
    }

    @SuppressWarnings("RestrictedApi")
    void setCollapsedSubtitleAppearance(int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), resId, R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            mCollapsedSubtitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            mCollapsedSubtitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, (int) mCollapsedSubtitleSize);
        a.recycle();
        recalculate();
    }

    @SuppressWarnings("RestrictedApi")
    void setExpandedSubtitleAppearance(int resId) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), resId, android.support.v7.appcompat.R.styleable.TextAppearance);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            mExpandedSubtitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            mExpandedSubtitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, (int) mExpandedSubtitleSize);
        a.recycle();
        recalculate();
    }

    private Typeface readFontFamilyTypeface(int resId) {
        final TypedArray a = mView.getContext().obtainStyledAttributes(resId, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN
                ? new int[]{android.R.attr.fontFamily}
                : new int[0]);
        try {
            final String family = a.getString(0);
            if (family != null)
                return Typeface.create(family, Typeface.NORMAL);
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
        return (mCollapsedTitleColor != null && mCollapsedTitleColor.isStateful()) || (mExpandedTitleColor != null && mExpandedTitleColor.isStateful());
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
        mCurrentTitleX = lerp(mExpandedTitleX, mCollapsedTitleX, fraction, mPositionInterpolator);
        mCurrentTitleY = lerp(mExpandedTitleY, mCollapsedTitleY, fraction, mPositionInterpolator);
        mCurrentSubtitleX = lerp(mExpandedSubtitleX, mCollapsedSubtitleX, fraction, mPositionInterpolator);
        mCurrentSubtitleY = lerp(mExpandedSubtitleY, mCollapsedSubtitleY, fraction, mPositionInterpolator);

        setInterpolatedTitleSize(lerp(mExpandedTitleSize, mCollapsedTitleSize, fraction, mTextSizeInterpolator));
        setInterpolatedSubtitleSize(lerp(mExpandedSubtitleSize, mCollapsedSubtitleSize, fraction, mTextSizeInterpolator));

        if (mCollapsedTitleColor != mExpandedTitleColor)
            mTitlePaint.setColor(blendColors(getCurrentExpandedTitleColor(), getCurrentCollapsedTitleColor(), fraction));
        else
            mTitlePaint.setColor(getCurrentCollapsedTitleColor());

        if (mCollapsedSubtitleColor != mExpandedSubtitleColor)
            mSubtitlePaint.setColor(blendColors(getCurrentExpandedSubtitleColor(), getCurrentCollapsedSubtitleColor(), fraction));
        else
            mSubtitlePaint.setColor(getCurrentCollapsedSubtitleColor());

        mTitlePaint.setShadowLayer(
                lerp(mExpandedShadowRadius, mCollapsedShadowRadius, fraction, null),
                lerp(mExpandedShadowDx, mCollapsedShadowDx, fraction, null),
                lerp(mExpandedShadowDy, mCollapsedShadowDy, fraction, null),
                blendColors(mExpandedShadowColor, mCollapsedShadowColor, fraction));
        ViewCompat.postInvalidateOnAnimation(mView);
    }

    @ColorInt
    private int getCurrentExpandedTitleColor() {
        if (mState != null)
            return mExpandedTitleColor.getColorForState(mState, 0);
        else
            return mExpandedTitleColor.getDefaultColor();
    }

    @ColorInt
    private int getCurrentCollapsedTitleColor() {
        if (mState != null)
            return mCollapsedTitleColor.getColorForState(mState, 0);
        else
            return mCollapsedTitleColor.getDefaultColor();
    }

    @ColorInt
    private int getCurrentExpandedSubtitleColor() {
        if (mState != null)
            return mExpandedSubtitleColor.getColorForState(mState, 0);
        else
            return mExpandedSubtitleColor.getDefaultColor();
    }

    @ColorInt
    private int getCurrentCollapsedSubtitleColor() {
        if (mState != null)
            return mCollapsedSubtitleColor.getColorForState(mState, 0);
        else
            return mCollapsedSubtitleColor.getDefaultColor();
    }

    @SuppressLint("RtlHardcoded")
    private void calculateBaseOffsets() {
        final float currentTitleSize = mCurrentTitleSize;
        final float currentSubtitleSize = mCurrentSubtitleSize;
        final boolean titleOnly = TextUtils.isEmpty(mSubtitle);

        calculateUsingTitleSize(mCollapsedTitleSize);
        calculateUsingSubtitleSize(mCollapsedSubtitleSize);
        float titleWidth = mTitleToDraw != null ? mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw.length()) : 0;
        float subtitleWidth = mSubtitleToDraw != null ? mSubtitlePaint.measureText(mSubtitleToDraw, 0, mSubtitleToDraw.length()) : 0;
        float titleHeight = mTitlePaint.descent() - mTitlePaint.ascent();
        float titleOffset = (titleHeight / 2) - mTitlePaint.descent();
        float subtitleHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent();
        float subtitleOffset = (subtitleHeight / 2);
        final int collapsedAbsGravity = GravityCompat.getAbsoluteGravity(mCollapsedTextGravity, mIsRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
        switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                if (titleOnly) {
                    mCollapsedTitleY = mCollapsedBounds.bottom;
                } else {
                    float offset = ((mCollapsedBounds.height() - (titleHeight + subtitleHeight)) / 3);
                    mCollapsedTitleY = mCollapsedBounds.top + offset - mTitlePaint.ascent();
                    mCollapsedSubtitleY = mCollapsedBounds.top + (offset * 2) + titleHeight - mSubtitlePaint.ascent();
                }
                break;
            case Gravity.TOP:
                if (titleOnly) {
                    mCollapsedTitleY = mCollapsedBounds.top - mTitlePaint.ascent();
                } else {
                    float offset = ((mCollapsedBounds.height() - (titleHeight + subtitleHeight)) / 3);
                    mCollapsedTitleY = mCollapsedBounds.top + offset - mTitlePaint.ascent();
                    mCollapsedSubtitleY = mCollapsedBounds.top + (offset * 2) + titleHeight - mSubtitlePaint.ascent();
                }
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                if (titleOnly) {
                    mCollapsedTitleY = mCollapsedBounds.centerY() + titleOffset;
                } else {
                    float offset = ((mCollapsedBounds.height() - (titleHeight + subtitleHeight)) / 3);
                    mCollapsedTitleY = mCollapsedBounds.top + offset - mTitlePaint.ascent();
                    mCollapsedSubtitleY = mCollapsedBounds.top + (offset * 2) + titleHeight - mSubtitlePaint.ascent();
                }
                break;
        }
        switch (collapsedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                mCollapsedTitleX = mCollapsedBounds.centerX() - (titleWidth / 2);
                mCollapsedSubtitleX = mCollapsedBounds.centerX() - (subtitleWidth / 2);
                break;
            case Gravity.RIGHT:
                mCollapsedTitleX = mCollapsedBounds.right - titleWidth;
                mCollapsedSubtitleX = mCollapsedBounds.right - subtitleWidth;
                break;
            case Gravity.LEFT:
            default:
                mCollapsedTitleX = mCollapsedBounds.left;
                mCollapsedSubtitleX = mCollapsedBounds.left;
                break;
        }

        calculateUsingTitleSize(mExpandedTitleSize);
        calculateUsingSubtitleSize(mExpandedSubtitleSize);
        titleWidth = mTitleToDraw != null ? mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw.length()) : 0;
        subtitleWidth = mSubtitleToDraw != null ? mSubtitlePaint.measureText(mSubtitleToDraw, 0, mSubtitleToDraw.length()) : 0;
        titleHeight = mTitlePaint.descent() - mTitlePaint.ascent();
        titleOffset = (titleHeight / 2) - mTitlePaint.descent();
        subtitleHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent();
        subtitleOffset = (subtitleHeight / 2);
        final int expandedAbsGravity = GravityCompat.getAbsoluteGravity(mExpandedTextGravity, mIsRtl ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
        switch (expandedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                if (titleOnly) {
                    mExpandedTitleY = mExpandedBounds.bottom;
                } else {
                    mExpandedTitleY = mExpandedBounds.bottom + mSubtitlePaint.ascent();
                    mExpandedSubtitleY = mExpandedTitleY + subtitleOffset - mSubtitlePaint.ascent();
                }
                break;
            case Gravity.TOP:
                if (titleOnly) {
                    mExpandedTitleY = mExpandedBounds.top - mTitlePaint.ascent();
                } else {
                    mExpandedTitleY = mExpandedBounds.top - mTitlePaint.ascent();
                    mExpandedSubtitleY = mExpandedTitleY + subtitleOffset - mSubtitlePaint.ascent();
                }
                break;
            case Gravity.CENTER_VERTICAL:
            default:
                if (titleOnly) {
                    mExpandedTitleY = mExpandedBounds.centerY() + titleOffset;
                } else {
                    mExpandedTitleY = mExpandedBounds.centerY() + titleOffset + mSubtitlePaint.ascent();
                    mExpandedSubtitleY = mExpandedTitleY + subtitleOffset - mSubtitlePaint.ascent();
                }
                break;
        }
        switch (expandedAbsGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                mExpandedTitleX = mExpandedBounds.centerX() - (titleWidth / 2);
                mExpandedSubtitleX = mExpandedBounds.centerX() - (subtitleWidth / 2);
                break;
            case Gravity.RIGHT:
                mExpandedTitleX = mExpandedBounds.right - titleWidth;
                mExpandedSubtitleX = mExpandedBounds.right - subtitleWidth;
                break;
            case Gravity.LEFT:
            default:
                mExpandedTitleX = mExpandedBounds.left;
                mExpandedSubtitleX = mExpandedBounds.left;
                break;
        }

        clearTexture();
        calculateUsingTitleSize(currentTitleSize);
        calculateUsingSubtitleSize(currentSubtitleSize);
    }

    private void interpolateBounds(float fraction) {
        mCurrentBounds.left = lerp(mExpandedBounds.left, mCollapsedBounds.left, fraction, mPositionInterpolator);
        mCurrentBounds.top = lerp(mExpandedTitleY, mCollapsedTitleY, fraction, mPositionInterpolator);
        mCurrentBounds.right = lerp(mExpandedBounds.right, mCollapsedBounds.right, fraction, mPositionInterpolator);
        mCurrentBounds.bottom = lerp(mExpandedBounds.bottom, mCollapsedBounds.bottom, fraction, mPositionInterpolator);
    }

    public void draw(Canvas canvas) {
        final int saveCountTitle = canvas.save();
        if (mTitleToDraw != null && mDrawTitle) {
            float titleX = mCurrentTitleX;
            float titleY = mCurrentTitleY;
            float subtitleX = mCurrentSubtitleX;
            float subtitleY = mCurrentSubtitleY;
            final boolean drawTexture = mUseTexture && mExpandedTitleTexture != null;

            final float titleAscent;
            final float subtitleAscent;
            final float titleDescent;
            if (drawTexture) {
                titleAscent = mTitleTextureAscent * mTitleScale;
                titleDescent = mTitleTextureDescent * mTitleScale;
                subtitleAscent = mSubtitleTextureAscent * mSubtitleScale;
            } else {
                titleAscent = mTitlePaint.ascent() * mTitleScale;
                titleDescent = mTitlePaint.descent() * mTitleScale;
                subtitleAscent = mSubtitlePaint.ascent() * mSubtitleScale;
            }

            if (DEBUG_DRAW)
                canvas.drawRect(mCurrentBounds.left, titleY + titleAscent, mCurrentBounds.right, titleY + titleDescent, DEBUG_DRAW_PAINT);

            if (drawTexture) {
                titleY += titleAscent;
                subtitleY += subtitleAscent;
            }

            // separate canvas save for subtitle
            final int saveCountSubtitle = canvas.save();
            if (!TextUtils.isEmpty(mSubtitle)) {
                if (mSubtitleScale != 1f)
                    canvas.scale(mSubtitleScale, mSubtitleScale, subtitleX, subtitleY);
                if (drawTexture)
                    canvas.drawBitmap(mExpandedSubtitleTexture, subtitleX, subtitleY, mSubtitleTexturePaint);
                else
                    canvas.drawText(mSubtitleToDraw, 0, mSubtitleToDraw.length(), subtitleX, subtitleY, mSubtitlePaint);
                canvas.restoreToCount(saveCountSubtitle);
            }

            if (mTitleScale != 1f)
                canvas.scale(mTitleScale, mTitleScale, titleX, titleY);

            if (drawTexture)
                canvas.drawBitmap(mExpandedTitleTexture, titleX, titleY, mTitleTexturePaint);
            else
                canvas.drawText(mTitleToDraw, 0, mTitleToDraw.length(), titleX, titleY, mTitlePaint);
        }
        canvas.restoreToCount(saveCountTitle);
    }

    private boolean calculateIsRtl(CharSequence text) {
        final boolean defaultIsRtl = ViewCompat.getLayoutDirection(mView) == ViewCompat.LAYOUT_DIRECTION_RTL;
        return (defaultIsRtl
                ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
                : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length());
    }

    private void setInterpolatedTitleSize(float textSize) {
        calculateUsingTitleSize(textSize);
        mUseTexture = USE_SCALING_TEXTURE && mTitleScale != 1f;
        if (mUseTexture)
            ensureExpandedTitleTexture();
        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void calculateUsingTitleSize(final float titleSize) {
        if (mTitle == null) return;

        final float collapsedWidth = mCollapsedBounds.width();
        final float expandedWidth = mExpandedBounds.width();

        final float availableWidth;
        final float newTitleSize;
        boolean updateDrawText = false;

        if (isClose(titleSize, mCollapsedTitleSize)) {
            newTitleSize = mCollapsedTitleSize;
            mTitleScale = 1f;
            if (mCurrentTypeface != mCollapsedTypeface) {
                mCurrentTypeface = mCollapsedTypeface;
                updateDrawText = true;
            }
            availableWidth = collapsedWidth;
        } else {
            newTitleSize = mExpandedTitleSize;
            if (mCurrentTypeface != mExpandedTypeface) {
                mCurrentTypeface = mExpandedTypeface;
                updateDrawText = true;
            }
            if (isClose(titleSize, mExpandedTitleSize))
                mTitleScale = 1f;
            else
                mTitleScale = titleSize / mExpandedTitleSize;

            final float titleSizeRatio = mCollapsedTitleSize / mExpandedTitleSize;
            final float scaledDownWidth = expandedWidth * titleSizeRatio;

            if (scaledDownWidth > collapsedWidth)
                availableWidth = Math.min(collapsedWidth / titleSizeRatio, expandedWidth);
            else
                availableWidth = expandedWidth;
        }

        if (availableWidth > 0) {
            updateDrawText = (mCurrentTitleSize != newTitleSize) || mBoundsChanged || updateDrawText;
            mCurrentTitleSize = newTitleSize;
            mBoundsChanged = false;
        }

        if (mTitleToDraw == null || updateDrawText) {
            mTitlePaint.setTextSize(mCurrentTitleSize);
            mTitlePaint.setTypeface(mCurrentTypeface);
            mTitlePaint.setLinearText(mTitleScale != 1f);
            final CharSequence title = TextUtils.ellipsize(mTitle, mTitlePaint, availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(title, mTitleToDraw)) {
                mTitleToDraw = title;
                mIsRtl = calculateIsRtl(mTitleToDraw);
            }
        }
    }

    private void setInterpolatedSubtitleSize(float textSize) {
        calculateUsingSubtitleSize(textSize);
        mUseTexture = USE_SCALING_TEXTURE && mSubtitleScale != 1f;
        if (mUseTexture)
            ensureExpandedSubtitleTexture();
        ViewCompat.postInvalidateOnAnimation(mView);
    }

    private void calculateUsingSubtitleSize(final float subtitleSize) {
        if (mSubtitle == null) return;

        final float collapsedWidth = mCollapsedBounds.width();
        final float expandedWidth = mExpandedBounds.width();

        final float availableWidth;
        final float newSubtitleSize;
        boolean updateDrawText = false;

        if (isClose(subtitleSize, mCollapsedSubtitleSize)) {
            newSubtitleSize = mCollapsedSubtitleSize;
            mSubtitleScale = 1f;
            if (mCurrentTypeface != mCollapsedTypeface) {
                mCurrentTypeface = mCollapsedTypeface;
                updateDrawText = true;
            }
            availableWidth = collapsedWidth;
        } else {
            newSubtitleSize = mExpandedSubtitleSize;
            if (mCurrentTypeface != mExpandedTypeface) {
                mCurrentTypeface = mExpandedTypeface;
                updateDrawText = true;
            }
            if (isClose(subtitleSize, mExpandedSubtitleSize))
                mSubtitleScale = 1f;
            else
                mSubtitleScale = subtitleSize / mExpandedSubtitleSize;

            final float subtitleSizeRatio = mCollapsedSubtitleSize / mExpandedSubtitleSize;
            final float scaledDownWidth = expandedWidth * subtitleSizeRatio;

            if (scaledDownWidth > collapsedWidth)
                availableWidth = Math.min(collapsedWidth / subtitleSizeRatio, expandedWidth);
            else
                availableWidth = expandedWidth;
        }

        if (availableWidth > 0) {
            updateDrawText = (mCurrentSubtitleSize != newSubtitleSize) || mBoundsChanged || updateDrawText;
            mCurrentSubtitleSize = newSubtitleSize;
            mBoundsChanged = false;
        }

        if (mSubtitleToDraw == null || updateDrawText) {
            mSubtitlePaint.setTextSize(mCurrentSubtitleSize);
            mSubtitlePaint.setTypeface(mCurrentTypeface);
            mSubtitlePaint.setLinearText(mSubtitleScale != 1f);
            final CharSequence subtitle = TextUtils.ellipsize(mSubtitle, mSubtitlePaint, availableWidth, TextUtils.TruncateAt.END);
            if (!TextUtils.equals(subtitle, mSubtitleToDraw)) {
                mSubtitleToDraw = subtitle;
                mIsRtl = calculateIsRtl(mSubtitleToDraw);
            }
        }
    }

    private void ensureExpandedTitleTexture() {
        if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty() || TextUtils.isEmpty(mTitleToDraw))
            return;
        calculateOffsets(0f);
        mTitleTextureAscent = mTitlePaint.ascent();
        mTitleTextureDescent = mTitlePaint.descent();
        final int w = Math.round(mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw.length()));
        final int h = Math.round(mTitleTextureDescent - mTitleTextureAscent);
        if (w <= 0 || h <= 0)
            return;
        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mExpandedTitleTexture);
        c.drawText(mTitleToDraw, 0, mTitleToDraw.length(), 0, h - mTitlePaint.descent(), mTitlePaint);
        if (mTitleTexturePaint == null)
            mTitleTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    private void ensureExpandedSubtitleTexture() {
        if (mExpandedSubtitleTexture != null || mExpandedBounds.isEmpty() || TextUtils.isEmpty(mSubtitleToDraw))
            return;
        calculateOffsets(0f);
        mSubtitleTextureAscent = mSubtitlePaint.ascent();
        mSubtitleTextureDescent = mSubtitlePaint.descent();
        final int w = Math.round(mSubtitlePaint.measureText(mSubtitleToDraw, 0, mSubtitleToDraw.length()));
        final int h = Math.round(mSubtitleTextureDescent - mSubtitleTextureAscent);
        if (w <= 0 || h <= 0)
            return;
        mExpandedSubtitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mExpandedSubtitleTexture);
        c.drawText(mSubtitleToDraw, 0, mSubtitleToDraw.length(), 0, h - mSubtitlePaint.descent(), mSubtitlePaint);
        if (mSubtitleTexturePaint == null)
            mSubtitleTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    public void recalculate() {
        if (mView.getHeight() > 0 && mView.getWidth() > 0) {
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }
    }

    void setTitle(CharSequence title) {
        if (title == null || !title.equals(mTitle)) {
            mTitle = title;
            mTitleToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    CharSequence getTitle() {
        return mTitle;
    }

    void setSubtitle(CharSequence subtitle) {
        if (subtitle == null || !subtitle.equals(mSubtitle)) {
            mSubtitle = subtitle;
            mSubtitleToDraw = null;
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
        if (mExpandedSubtitleTexture != null) {
            mExpandedSubtitleTexture.recycle();
            mExpandedSubtitleTexture = null;
        }
    }

    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < 0.001f;
    }

    ColorStateList getExpandedTitleColor() {
        return mExpandedTitleColor;
    }

    ColorStateList getCollapsedTitleColor() {
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

    private static float lerp(float startValue, float endValue, float fraction, Interpolator interpolator) {
        if (interpolator != null)
            fraction = interpolator.getInterpolation(fraction);
        return AnimationUtils.lerp(startValue, endValue, fraction);
    }

    private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
        return !(r.left != left || r.top != top || r.right != right || r.bottom != bottom);
    }
}