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

package android.support.design.widget

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.MAGENTA
import android.graphics.Color.alpha
import android.graphics.Color.argb
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.graphics.Paint.SUBPIXEL_TEXT_FLAG
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build.VERSION.SDK_INT
import android.support.annotation.ColorInt
import android.support.v4.math.MathUtils
import android.support.v4.text.TextDirectionHeuristicsCompat
import android.support.v4.view.GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK
import android.support.v4.view.GravityCompat.getAbsoluteGravity
import android.support.v4.view.ViewCompat.LAYOUT_DIRECTION_LTR
import android.support.v4.view.ViewCompat.LAYOUT_DIRECTION_RTL
import android.support.v4.view.ViewCompat.getLayoutDirection
import android.support.v4.view.ViewCompat.postInvalidateOnAnimation
import android.support.v7.widget.TintTypedArray
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity.BOTTOM
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.LEFT
import android.view.Gravity.RIGHT
import android.view.Gravity.TOP
import android.view.Gravity.VERTICAL_GRAVITY_MASK
import android.view.View
import android.view.animation.Interpolator
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * An internal helper class for [SubtitleCollapsingToolbarLayout].
 *
 * @see CollapsingTextHelper
 */
internal class SubtitleCollapsingTextHelper(private val mView: View) {

    private var mDrawTitle = false
    private var mExpandedFraction = 0f

    private val mExpandedBounds = Rect()
    private val mCollapsedBounds = Rect()
    private val mCurrentBounds = RectF()
    private var mExpandedTextGravity = CENTER_VERTICAL
    private var mCollapsedTextGravity = CENTER_VERTICAL
    private var mExpandedTitleSize = 15f
    private var mExpandedSubtitleSize = 15f
    private var mCollapsedTitleSize = 15f
    private var mCollapsedSubtitleSize = 15f
    private var mExpandedTitleColor: ColorStateList? = null
    private var mExpandedSubtitleColor: ColorStateList? = null
    private var mCollapsedTitleColor: ColorStateList? = null
    private var mCollapsedSubtitleColor: ColorStateList? = null

    private var mExpandedTitleY = 0f
    private var mExpandedSubtitleY = 0f
    private var mCollapsedTitleY = 0f
    private var mCollapsedSubtitleY = 0f
    private var mExpandedTitleX = 0f
    private var mExpandedSubtitleX = 0f
    private var mCollapsedTitleX = 0f
    private var mCollapsedSubtitleX = 0f
    private var mCurrentTitleX = 0f
    private var mCurrentSubtitleX = 0f
    private var mCurrentTitleY = 0f
    private var mCurrentSubtitleY = 0f
    private var mCollapsedTitleTypeface: Typeface? = null
    private var mCollapsedSubtitleTypeface: Typeface? = null
    private var mExpandedTitleTypeface: Typeface? = null
    private var mExpandedSubtitleTypeface: Typeface? = null
    private var mCurrentTitleTypeface: Typeface? = null
    private var mCurrentSubtitleTypeface: Typeface? = null

    private var mTitle: CharSequence? = null
    private var mSubtitle: CharSequence? = null
    private var mTitleToDraw: CharSequence? = null
    private var mSubtitleToDraw: CharSequence? = null
    private var mIsRtl = false

    private var mUseTexture = false
    private var mExpandedTitleTexture: Bitmap? = null
    private var mExpandedSubtitleTexture: Bitmap? = null
    private var mTitleTexturePaint: Paint? = null
    private var mSubtitleTexturePaint: Paint? = null
    private var mTitleTextureAscent = 0f
    private var mSubtitleTextureAscent = 0f
    private var mTitleTextureDescent = 0f
    private var mSubtitleTextureDescent = 0f

    private var mTitleScale = 0f
    private var mSubtitleScale = 0f
    private var mCurrentTitleSize = 0f
    private var mCurrentSubtitleSize = 0f

    private var mState: IntArray? = null

    private var mBoundsChanged = false

    private val mTitlePaint = TextPaint(ANTI_ALIAS_FLAG or SUBPIXEL_TEXT_FLAG)
    private val mSubtitlePaint = TextPaint(ANTI_ALIAS_FLAG or SUBPIXEL_TEXT_FLAG)

    private var mPositionInterpolator: Interpolator? = null
    private var mTextSizeInterpolator: Interpolator? = null

    private var mCollapsedShadowRadius = 0f
    private var mCollapsedShadowDx = 0f
    private var mCollapsedShadowDy = 0f
    private var mCollapsedShadowColor = 0

    private var mExpandedShadowRadius = 0f
    private var mExpandedShadowDx = 0f
    private var mExpandedShadowDy = 0f
    private var mExpandedShadowColor = 0

    internal fun setTextSizeInterpolator(interpolator: Interpolator) {
        mTextSizeInterpolator = interpolator
        recalculate()
    }

    internal fun setPositionInterpolator(interpolator: Interpolator) {
        mPositionInterpolator = interpolator
        recalculate()
    }

    internal var expandedTitleSize: Float
        get() = mExpandedTitleSize
        set(textSize) {
            if (mExpandedTitleSize != textSize) {
                mExpandedTitleSize = textSize
                recalculate()
            }
        }

    internal var expandedSubtitleSize: Float
        get() = mExpandedSubtitleSize
        set(textSize) {
            if (mExpandedSubtitleSize != textSize) {
                mExpandedSubtitleSize = textSize
                recalculate()
            }
        }

    internal var collapsedTitleSize: Float
        get() = mCollapsedTitleSize
        set(textSize) {
            if (mCollapsedTitleSize != textSize) {
                mCollapsedTitleSize = textSize
                recalculate()
            }
        }

    internal var collapsedSubtitleSize: Float
        get() = mCollapsedSubtitleSize
        set(textSize) {
            if (mCollapsedSubtitleSize != textSize) {
                mCollapsedSubtitleSize = textSize
                recalculate()
            }
        }

    internal var expandedTitleColor: ColorStateList
        get() = mExpandedTitleColor!!
        set(textColor) {
            if (mExpandedTitleColor != textColor) {
                mExpandedTitleColor = textColor
                recalculate()
            }
        }

    internal var expandedSubtitleColor: ColorStateList
        get() = mExpandedSubtitleColor!!
        set(textColor) {
            if (mExpandedSubtitleColor != textColor) {
                mExpandedSubtitleColor = textColor
                recalculate()
            }
        }

    internal var collapsedTitleColor: ColorStateList
        get() = mCollapsedTitleColor!!
        set(textColor) {
            if (mCollapsedTitleColor != textColor) {
                mCollapsedTitleColor = textColor
                recalculate()
            }
        }

    internal var collapsedSubtitleColor: ColorStateList
        get() = mCollapsedSubtitleColor!!
        set(textColor) {
            if (mCollapsedSubtitleColor != textColor) {
                mCollapsedSubtitleColor = textColor
                recalculate()
            }
        }

    internal fun setExpandedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(mExpandedBounds, left, top, right, bottom)) {
            mExpandedBounds.set(left, top, right, bottom)
            mBoundsChanged = true
            onBoundsChanged()
        }
    }

    internal fun setCollapsedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(mCollapsedBounds, left, top, right, bottom)) {
            mCollapsedBounds.set(left, top, right, bottom)
            mBoundsChanged = true
            onBoundsChanged()
        }
    }

    internal fun onBoundsChanged() {
        mDrawTitle = mCollapsedBounds.width() > 0 &&
            mCollapsedBounds.height() > 0 &&
            mExpandedBounds.width() > 0 &&
            mExpandedBounds.height() > 0
    }

    internal var expandedTextGravity: Int
        get() = mExpandedTextGravity
        set(gravity) {
            if (mExpandedTextGravity != gravity) {
                mExpandedTextGravity = gravity
                recalculate()
            }
        }

    internal var collapsedTextGravity: Int
        get() = mCollapsedTextGravity
        set(gravity) {
            if (mCollapsedTextGravity != gravity) {
                mCollapsedTextGravity = gravity
                recalculate()
            }
        }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setCollapsedTitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(
            mView.context, resId, android.support.v7.appcompat.R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mCollapsedTitleColor = a.getColorStateList(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mCollapsedTitleSize = a.getDimensionPixelSize(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                mExpandedSubtitleSize.toInt()).toFloat()
        }
        mCollapsedShadowColor = a.getInt(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0)
        mCollapsedShadowDx = a.getFloat(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0f)
        mCollapsedShadowDy = a.getFloat(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0f)
        mCollapsedShadowRadius = a.getFloat(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0f)
        a.recycle()
        if (SDK_INT >= 16) {
            mCollapsedTitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setExpandedTitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(
            mView.context, resId, android.support.v7.appcompat.R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mExpandedTitleColor = a.getColorStateList(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mExpandedTitleSize = a.getDimensionPixelSize(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                mExpandedTitleSize.toInt()).toFloat()
        }
        mExpandedShadowColor = a.getInt(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0)
        mExpandedShadowDx = a.getFloat(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0f)
        mExpandedShadowDy = a.getFloat(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0f)
        mExpandedShadowRadius = a.getFloat(
            android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0f)
        a.recycle()
        if (SDK_INT >= 16) {
            mExpandedTitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setCollapsedSubtitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(
            mView.context, resId, android.support.v7.appcompat.R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mCollapsedSubtitleColor = a.getColorStateList(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mCollapsedSubtitleSize = a.getDimensionPixelSize(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                mCollapsedSubtitleSize.toInt()).toFloat()
        }
        a.recycle()
        if (SDK_INT >= 16) {
            mCollapsedSubtitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setExpandedSubtitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(
            mView.context, resId, android.support.v7.appcompat.R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)) {
            mExpandedSubtitleColor = a.getColorStateList(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize)) {
            mExpandedSubtitleSize = a.getDimensionPixelSize(
                android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize,
                mExpandedSubtitleSize.toInt()).toFloat()
        }
        a.recycle()
        if (SDK_INT >= 16) {
            mExpandedSubtitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("InlinedApi")
    private fun readFontFamilyTypeface(resId: Int): Typeface? {
        val a = mView.context.obtainStyledAttributes(resId, intArrayOf(android.R.attr.fontFamily))
        try {
            val family = a.getString(0)
            if (family != null) return Typeface.create(family, Typeface.NORMAL)
        } finally {
            a.recycle()
        }
        return null
    }

    internal var collapsedTitleTypeface: Typeface
        get() = mCollapsedTitleTypeface ?: Typeface.DEFAULT
        set(typeface) {
            if (mCollapsedTitleTypeface != typeface) {
                mCollapsedTitleTypeface = typeface
                recalculate()
            }
        }

    internal var expandedTitleTypeface: Typeface
        get() = mExpandedTitleTypeface ?: Typeface.DEFAULT
        set(typeface) {
            if (mExpandedTitleTypeface != typeface) {
                mExpandedTitleTypeface = typeface
                recalculate()
            }
        }

    internal var collapsedSubtitleTypeface: Typeface
        get() = mCollapsedSubtitleTypeface ?: Typeface.DEFAULT
        set(typeface) {
            if (mCollapsedSubtitleTypeface != typeface) {
                mCollapsedSubtitleTypeface = typeface
                recalculate()
            }
        }

    internal var expandedSubtitleTypeface: Typeface
        get() = mExpandedSubtitleTypeface ?: Typeface.DEFAULT
        set(typeface) {
            if (mExpandedSubtitleTypeface != typeface) {
                mExpandedSubtitleTypeface = typeface
                recalculate()
            }
        }

    internal fun setTitleTypefaces(typeface: Typeface) {
        mExpandedTitleTypeface = typeface
        mCollapsedTitleTypeface = typeface
        recalculate()
    }

    internal fun setSubtitleTypefaces(typeface: Typeface) {
        mExpandedSubtitleTypeface = typeface
        mCollapsedSubtitleTypeface = typeface
        recalculate()
    }

    internal var expansionFraction
        get() = mExpandedFraction
        set(fraction) {
            val fraction2 = MathUtils.clamp(fraction, 0f, 1f)
            if (fraction2 != mExpandedFraction) {
                mExpandedFraction = fraction2
                calculateCurrentOffsets()
            }
        }

    internal fun setState(state: IntArray): Boolean {
        mState = state
        if (isStateful()) {
            recalculate()
            return true
        }
        return false
    }

    internal fun isStateful() = mCollapsedTitleColor != null &&
        mCollapsedTitleColor!!.isStateful ||
        mExpandedTitleColor != null &&
        mExpandedTitleColor!!.isStateful

    private fun calculateCurrentOffsets() = calculateOffsets(mExpandedFraction)

    private fun calculateOffsets(fraction: Float) {
        interpolateBounds(fraction)
        mCurrentTitleX = lerp(mExpandedTitleX, mCollapsedTitleX, fraction, mPositionInterpolator)
        mCurrentTitleY = lerp(mExpandedTitleY, mCollapsedTitleY, fraction, mPositionInterpolator)
        mCurrentSubtitleX = lerp(
            mExpandedSubtitleX, mCollapsedSubtitleX, fraction, mPositionInterpolator)
        mCurrentSubtitleY = lerp(
            mExpandedSubtitleY, mCollapsedSubtitleY, fraction, mPositionInterpolator)
        setInterpolatedTitleSize(lerp(
            mExpandedTitleSize, mCollapsedTitleSize, fraction, mTextSizeInterpolator))
        setInterpolatedSubtitleSize(lerp(
            mExpandedSubtitleSize, mCollapsedSubtitleSize, fraction, mTextSizeInterpolator))
        mTitlePaint.color = when {
            mCollapsedTitleColor != mExpandedTitleColor -> blendColors(
                currentExpandedTitleColor, currentCollapsedTitleColor, fraction)
            else -> currentCollapsedTitleColor
        }
        mSubtitlePaint.color = when {
            mCollapsedSubtitleColor != mExpandedSubtitleColor -> blendColors(
                currentExpandedSubtitleColor, currentCollapsedSubtitleColor, fraction)
            else -> currentCollapsedSubtitleColor
        }
        mTitlePaint.setShadowLayer(
            lerp(mExpandedShadowRadius, mCollapsedShadowRadius, fraction, null),
            lerp(mExpandedShadowDx, mCollapsedShadowDx, fraction, null),
            lerp(mExpandedShadowDy, mCollapsedShadowDy, fraction, null),
            blendColors(mExpandedShadowColor, mCollapsedShadowColor, fraction))
        postInvalidateOnAnimation(mView)
    }

    private val currentExpandedTitleColor: Int
        @ColorInt get() = mState?.let { mExpandedTitleColor!!.getColorForState(mState, 0) }
            ?: mExpandedTitleColor!!.defaultColor

    private val currentCollapsedTitleColor: Int
        @ColorInt get() = mState?.let { mCollapsedTitleColor!!.getColorForState(mState, 0) }
            ?: mCollapsedTitleColor!!.defaultColor

    private val currentExpandedSubtitleColor: Int
        @ColorInt get() = mState?.let { mExpandedSubtitleColor!!.getColorForState(mState, 0) }
            ?: mExpandedSubtitleColor!!.defaultColor

    private val currentCollapsedSubtitleColor: Int
        @ColorInt get() = mState?.let { mCollapsedSubtitleColor!!.getColorForState(mState, 0) }
            ?: mCollapsedSubtitleColor!!.defaultColor

    @SuppressLint("RtlHardcoded")
    private fun calculateBaseOffsets() {
        val currentTitleSize = mCurrentTitleSize
        val currentSubtitleSize = mCurrentSubtitleSize
        val isTitleOnly = TextUtils.isEmpty(mSubtitle)

        calculateUsingTitleSize(mCollapsedTitleSize)
        calculateUsingSubtitleSize(mCollapsedSubtitleSize)

        var titleWidth: Float = when {
            mTitleToDraw != null -> mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw!!.length)
            else -> 0f
        }
        var subtitleWidth = when {
            mSubtitleToDraw != null -> mSubtitlePaint.measureText(
                mSubtitleToDraw, 0, mSubtitleToDraw!!.length)
            else -> 0f
        }
        var titleHeight = mTitlePaint.descent() - mTitlePaint.ascent()
        var titleOffset = titleHeight / 2 - mTitlePaint.descent()
        var subtitleHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent()
        var subtitleOffset = subtitleHeight / 2 - mSubtitlePaint.descent()
        val offset = (mCollapsedBounds.height() - (titleHeight + subtitleHeight)) / 3
        val collapsedAbsGravity = getAbsoluteGravity(mCollapsedTextGravity, when {
            mIsRtl -> LAYOUT_DIRECTION_RTL
            else -> LAYOUT_DIRECTION_LTR
        })
        when {
            isTitleOnly -> when (collapsedAbsGravity and VERTICAL_GRAVITY_MASK) {
                BOTTOM -> mCollapsedTitleY = mCollapsedBounds.bottom.toFloat()
                TOP -> mCollapsedTitleY = mCollapsedBounds.top - mTitlePaint.ascent()
                CENTER_VERTICAL -> mCollapsedTitleY = mCollapsedBounds.centerY() + titleOffset
            }
            else -> {
                mCollapsedTitleY = mCollapsedBounds.top + offset - mTitlePaint.ascent()
                mCollapsedSubtitleY = mCollapsedBounds.top.toFloat() + offset * 2 + titleHeight -
                    mSubtitlePaint.ascent()
            }
        }
        when (collapsedAbsGravity and RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            CENTER_HORIZONTAL -> {
                mCollapsedTitleX = mCollapsedBounds.centerX() - titleWidth / 2
                mCollapsedSubtitleX = mCollapsedBounds.centerX() - subtitleWidth / 2
            }
            RIGHT -> {
                mCollapsedTitleX = mCollapsedBounds.right - titleWidth
                mCollapsedSubtitleX = mCollapsedBounds.right - subtitleWidth
            }
            LEFT -> {
                mCollapsedTitleX = mCollapsedBounds.left.toFloat()
                mCollapsedSubtitleX = mCollapsedBounds.left.toFloat()
            }
        }

        calculateUsingTitleSize(mExpandedTitleSize)
        calculateUsingSubtitleSize(mExpandedSubtitleSize)
        titleWidth = when {
            mTitleToDraw != null -> mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw!!.length)
            else -> 0f
        }
        subtitleWidth = when {
            mSubtitleToDraw != null -> mSubtitlePaint.measureText(
                mSubtitleToDraw, 0, mSubtitleToDraw!!.length)
            else -> 0f
        }
        titleHeight = mTitlePaint.descent() - mTitlePaint.ascent()
        titleOffset = titleHeight / 2 - mTitlePaint.descent()
        subtitleHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent()
        subtitleOffset = subtitleHeight / 2 - mSubtitlePaint.descent()
        val expandedAbsGravity = getAbsoluteGravity(mExpandedTextGravity, when {
            mIsRtl -> LAYOUT_DIRECTION_RTL
            else -> LAYOUT_DIRECTION_LTR
        })
        when {
            isTitleOnly -> when (expandedAbsGravity and VERTICAL_GRAVITY_MASK) {
                BOTTOM -> mExpandedTitleY = mExpandedBounds.bottom.toFloat()
                TOP -> mExpandedTitleY = mExpandedBounds.top - mTitlePaint.ascent()
                CENTER_VERTICAL -> mExpandedTitleY = mExpandedBounds.centerY() + titleOffset
            }
            else -> {
                when (expandedAbsGravity and VERTICAL_GRAVITY_MASK) {
                    BOTTOM -> mExpandedTitleY = mExpandedBounds.bottom - subtitleHeight -
                        titleOffset
                    TOP -> mExpandedTitleY = mExpandedBounds.top - mTitlePaint.ascent()
                    CENTER_VERTICAL -> mExpandedTitleY = mExpandedBounds.centerY() + titleOffset
                }
                when (expandedAbsGravity and VERTICAL_GRAVITY_MASK) {
                    BOTTOM -> mExpandedSubtitleY = mExpandedBounds.bottom.toFloat()
                    TOP -> mExpandedSubtitleY = mExpandedTitleY + subtitleHeight + titleOffset
                    CENTER_VERTICAL -> mExpandedSubtitleY = mExpandedTitleY + subtitleHeight +
                        titleOffset
                }
            }
        }
        when (expandedAbsGravity and RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            CENTER_HORIZONTAL -> {
                mExpandedTitleX = mExpandedBounds.centerX() - titleWidth / 2
                mExpandedSubtitleX = mExpandedBounds.centerX() - subtitleWidth / 2
            }
            RIGHT -> {
                mExpandedTitleX = mExpandedBounds.right - titleWidth
                mExpandedSubtitleX = mExpandedBounds.right - subtitleWidth
            }
            LEFT -> {
                mExpandedTitleX = mExpandedBounds.left.toFloat()
                mExpandedSubtitleX = mExpandedBounds.left.toFloat()
            }
        }

        clearTexture()
        calculateUsingTitleSize(currentTitleSize)
        calculateUsingSubtitleSize(currentSubtitleSize)
    }

    private fun interpolateBounds(fraction: Float) {
        mCurrentBounds.left = lerp(
            mExpandedBounds.left.toFloat(),
            mCollapsedBounds.left.toFloat(),
            fraction,
            mPositionInterpolator)
        mCurrentBounds.top = lerp(
            mExpandedTitleY,
            mCollapsedTitleY,
            fraction,
            mPositionInterpolator)
        mCurrentBounds.right = lerp(
            mExpandedBounds.right.toFloat(),
            mCollapsedBounds.right.toFloat(),
            fraction,
            mPositionInterpolator)
        mCurrentBounds.bottom = lerp(
            mExpandedBounds.bottom.toFloat(),
            mCollapsedBounds.bottom.toFloat(),
            fraction,
            mPositionInterpolator)
    }

    fun draw(canvas: Canvas) {
        val saveCountTitle = canvas.save()
        if (mTitleToDraw != null && mDrawTitle) {
            val titleX = mCurrentTitleX
            var titleY = mCurrentTitleY
            val subtitleX = mCurrentSubtitleX
            var subtitleY = mCurrentSubtitleY
            val drawTexture = mUseTexture && mExpandedTitleTexture != null

            val titleAscent: Float
            val subtitleAscent: Float
            val titleDescent: Float
            when {
                drawTexture -> {
                    titleAscent = mTitleTextureAscent * mTitleScale
                    titleDescent = mTitleTextureDescent * mTitleScale
                    subtitleAscent = mSubtitleTextureAscent * mSubtitleScale
                }
                else -> {
                    titleAscent = mTitlePaint.ascent() * mTitleScale
                    titleDescent = mTitlePaint.descent() * mTitleScale
                    subtitleAscent = mSubtitlePaint.ascent() * mSubtitleScale
                }
            }

            if (DEBUG_DRAW) {
                canvas.drawRect(
                    mCurrentBounds.left,
                    titleY + titleAscent,
                    mCurrentBounds.right,
                    titleY + titleDescent, DEBUG_DRAW_PAINT!!)
            }

            if (drawTexture) {
                titleY += titleAscent
                subtitleY += subtitleAscent
            }

            // separate canvas save for subtitle
            val saveCountSubtitle = canvas.save()
            if (!TextUtils.isEmpty(mSubtitle)) {
                if (mSubtitleScale != 1f) {
                    canvas.scale(mSubtitleScale, mSubtitleScale, subtitleX, subtitleY)
                }
                when {
                    drawTexture -> canvas.drawBitmap(
                        mExpandedSubtitleTexture!!,
                        subtitleX,
                        subtitleY,
                        mSubtitleTexturePaint)
                    else -> canvas.drawText(
                        mSubtitleToDraw!!,
                        0,
                        mSubtitleToDraw!!.length,
                        subtitleX,
                        subtitleY,
                        mSubtitlePaint)
                }
                canvas.restoreToCount(saveCountSubtitle)
            }

            if (mTitleScale != 1f) canvas.scale(mTitleScale, mTitleScale, titleX, titleY)

            when {
                drawTexture -> canvas.drawBitmap(
                    mExpandedTitleTexture!!,
                    titleX,
                    titleY,
                    mTitleTexturePaint)
                else -> canvas.drawText(
                    mTitleToDraw!!,
                    0,
                    mTitleToDraw!!.length,
                    titleX,
                    titleY,
                    mTitlePaint)
            }
        }
        canvas.restoreToCount(saveCountTitle)
    }

    private fun calculateIsRtl(text: CharSequence): Boolean {
        val defaultIsRtl = getLayoutDirection(mView) == LAYOUT_DIRECTION_RTL
        return (when {
            defaultIsRtl -> TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
            else -> TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR
        }).isRtl(text, 0, text.length)
    }

    private fun setInterpolatedTitleSize(textSize: Float) {
        calculateUsingTitleSize(textSize)
        mUseTexture = USE_SCALING_TEXTURE && mTitleScale != 1f
        if (mUseTexture) ensureExpandedTitleTexture()
        postInvalidateOnAnimation(mView)
    }

    private fun areTypefacesDifferent(first: Typeface?, second: Typeface?) = first != null &&
        first != second ||
        first == null &&
        second != null

    private fun calculateUsingTitleSize(titleSize: Float) {
        if (title == null) return

        val collapsedWidth = mCollapsedBounds.width().toFloat()
        val expandedWidth = mExpandedBounds.width().toFloat()

        val availableWidth: Float
        val newTitleSize: Float
        var updateDrawText = false

        when {
            isClose(titleSize, mCollapsedTitleSize) -> {
                newTitleSize = mCollapsedTitleSize
                mTitleScale = 1f
                if (mCurrentTitleTypeface != mCollapsedTitleTypeface) {
                    mCurrentTitleTypeface = mCollapsedTitleTypeface
                    updateDrawText = true
                }
                availableWidth = collapsedWidth
            }
            else -> {
                newTitleSize = mExpandedTitleSize
                if (mCurrentTitleTypeface != mExpandedTitleTypeface) {
                    mCurrentTitleTypeface = mExpandedTitleTypeface
                    updateDrawText = true
                }
                mTitleScale = when {
                    isClose(titleSize, mExpandedTitleSize) -> 1f
                    else -> titleSize / mExpandedTitleSize
                }

                val titleSizeRatio = mCollapsedTitleSize / mExpandedTitleSize
                val scaledDownWidth = expandedWidth * titleSizeRatio

                availableWidth = when {
                    scaledDownWidth > collapsedWidth ->
                        min(collapsedWidth / titleSizeRatio, expandedWidth)
                    else -> expandedWidth
                }
            }
        }

        if (availableWidth > 0) {
            updateDrawText = mCurrentTitleSize != newTitleSize || mBoundsChanged || updateDrawText
            mCurrentTitleSize = newTitleSize
            mBoundsChanged = false
        }

        if (mTitleToDraw == null || updateDrawText) {
            mTitlePaint.textSize = mCurrentTitleSize
            mTitlePaint.typeface = mCurrentTitleTypeface
            mTitlePaint.isLinearText = mTitleScale != 1f
            val title = TextUtils.ellipsize(
                title, mTitlePaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(title, mTitleToDraw)) {
                mTitleToDraw = title
                mIsRtl = calculateIsRtl(mTitleToDraw!!)
            }
        }
    }

    private fun setInterpolatedSubtitleSize(textSize: Float) {
        calculateUsingSubtitleSize(textSize)
        mUseTexture = USE_SCALING_TEXTURE && mSubtitleScale != 1f
        if (mUseTexture) ensureExpandedSubtitleTexture()
        postInvalidateOnAnimation(mView)
    }

    private fun calculateUsingSubtitleSize(subtitleSize: Float) {
        if (subtitle == null) return

        val collapsedWidth = mCollapsedBounds.width().toFloat()
        val expandedWidth = mExpandedBounds.width().toFloat()

        val availableWidth: Float
        val newSubtitleSize: Float
        var updateDrawText = false

        when {
            isClose(subtitleSize, mCollapsedSubtitleSize) -> {
                newSubtitleSize = mCollapsedSubtitleSize
                mSubtitleScale = 1f
                if (mCurrentSubtitleTypeface != mCollapsedSubtitleTypeface) {
                    mCurrentSubtitleTypeface = mCollapsedSubtitleTypeface
                    updateDrawText = true
                }
                availableWidth = collapsedWidth
            }
            else -> {
                newSubtitleSize = mExpandedSubtitleSize
                if (mCurrentSubtitleTypeface != mExpandedSubtitleTypeface) {
                    mCurrentSubtitleTypeface = mExpandedSubtitleTypeface
                    updateDrawText = true
                }
                mSubtitleScale = when {
                    isClose(subtitleSize, mExpandedSubtitleSize) -> 1f
                    else -> subtitleSize / mExpandedSubtitleSize
                }

                val subtitleSizeRatio = mCollapsedSubtitleSize / mExpandedSubtitleSize
                val scaledDownWidth = expandedWidth * subtitleSizeRatio

                availableWidth = when {
                    scaledDownWidth > collapsedWidth ->
                        min(collapsedWidth / subtitleSizeRatio, expandedWidth)
                    else -> expandedWidth
                }
            }
        }

        if (availableWidth > 0) {
            updateDrawText = mCurrentSubtitleSize != newSubtitleSize || mBoundsChanged ||
                updateDrawText
            mCurrentSubtitleSize = newSubtitleSize
            mBoundsChanged = false
        }

        if (mSubtitleToDraw == null || updateDrawText) {
            mSubtitlePaint.textSize = mCurrentSubtitleSize
            mSubtitlePaint.typeface = mCurrentSubtitleTypeface
            mSubtitlePaint.isLinearText = mSubtitleScale != 1f
            val subtitle = TextUtils.ellipsize(
                subtitle, mSubtitlePaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(subtitle, mSubtitleToDraw)) {
                mSubtitleToDraw = subtitle
                mIsRtl = calculateIsRtl(mSubtitleToDraw!!)
            }
        }
    }

    private fun ensureExpandedTitleTexture() {
        if (mExpandedTitleTexture != null || mExpandedBounds.isEmpty ||
            TextUtils.isEmpty(mTitleToDraw)) return
        calculateOffsets(0f)
        mTitleTextureAscent = mTitlePaint.ascent()
        mTitleTextureDescent = mTitlePaint.descent()
        val w = mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw!!.length).roundToInt()
        val h = (mTitleTextureDescent - mTitleTextureAscent).roundToInt()
        if (w <= 0 || h <= 0) return
        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(mExpandedTitleTexture!!)
        c.drawText(
            mTitleToDraw!!,
            0,
            mTitleToDraw!!.length,
            0f,
            h - mTitlePaint.descent(),
            mTitlePaint)
        if (mTitleTexturePaint == null) {
            mTitleTexturePaint = Paint(ANTI_ALIAS_FLAG or FILTER_BITMAP_FLAG)
        }
    }

    private fun ensureExpandedSubtitleTexture() {
        if (mExpandedSubtitleTexture != null || mExpandedBounds.isEmpty ||
            TextUtils.isEmpty(mSubtitleToDraw)) return
        calculateOffsets(0f)
        mSubtitleTextureAscent = mSubtitlePaint.ascent()
        mSubtitleTextureDescent = mSubtitlePaint.descent()
        val w = mSubtitlePaint.measureText(mSubtitleToDraw, 0, mSubtitleToDraw!!.length)
            .roundToInt()
        val h = (mSubtitleTextureDescent - mSubtitleTextureAscent).roundToInt()
        if (w <= 0 || h <= 0) return
        mExpandedSubtitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(mExpandedSubtitleTexture!!)
        c.drawText(
            mSubtitleToDraw!!,
            0,
            mSubtitleToDraw!!.length,
            0f,
            h - mSubtitlePaint.descent(),
            mSubtitlePaint)
        if (mSubtitleTexturePaint == null) {
            mSubtitleTexturePaint = Paint(ANTI_ALIAS_FLAG or FILTER_BITMAP_FLAG)
        }
    }

    fun recalculate() {
        if (mView.height > 0 && mView.width > 0) {
            calculateBaseOffsets()
            calculateCurrentOffsets()
        }
    }

    internal var title: CharSequence?
        get() = mTitle
        set(title) {
            if (title == null || title != mTitle) {
                mTitle = title
                mTitleToDraw = null
                clearTexture()
                recalculate()
            }
        }

    internal var subtitle: CharSequence?
        get() = mSubtitle
        set(subtitle) {
            if (subtitle == null || subtitle != mSubtitle) {
                mSubtitle = subtitle
                mSubtitleToDraw = null
                clearTexture()
                recalculate()
            }
        }

    private fun clearTexture() {
        if (mExpandedTitleTexture != null) {
            mExpandedTitleTexture!!.recycle()
            mExpandedTitleTexture = null
        }
        if (mExpandedSubtitleTexture != null) {
            mExpandedSubtitleTexture!!.recycle()
            mExpandedSubtitleTexture = null
        }
    }

    private companion object {
        val USE_SCALING_TEXTURE = SDK_INT < 18

        const val DEBUG_DRAW = false
        val DEBUG_DRAW_PAINT: Paint?

        init {
            DEBUG_DRAW_PAINT = if (DEBUG_DRAW) Paint() else null
            if (DEBUG_DRAW_PAINT != null) {
                DEBUG_DRAW_PAINT.isAntiAlias = true
                DEBUG_DRAW_PAINT.color = MAGENTA
            }
        }

        fun isClose(value: Float, targetValue: Float) = abs(value - targetValue) < 0.001f

        fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val a = alpha(color1) * inverseRatio + alpha(color2) * ratio
            val r = red(color1) * inverseRatio + red(color2) * ratio
            val g = green(color1) * inverseRatio + green(color2) * ratio
            val b = blue(color1) * inverseRatio + blue(color2) * ratio
            return argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
        }

        fun lerp(
            startValue: Float,
            endValue: Float,
            fraction: Float,
            interpolator: Interpolator?
        ): Float = AnimationUtils.lerp(
            startValue,
            endValue,
            interpolator?.let { interpolator.getInterpolation(fraction) } ?: fraction)

        fun rectEquals(
            r: Rect,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int
        ) = !(r.left != left || r.top != top || r.right != right || r.bottom != bottom)
    }
}