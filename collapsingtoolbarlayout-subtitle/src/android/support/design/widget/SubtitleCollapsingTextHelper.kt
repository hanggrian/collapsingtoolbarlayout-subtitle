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
import android.graphics.*
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.text.TextDirectionHeuristicsCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.TintTypedArray
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import com.hendraanggrian.collapsingtoolbarlayout.subtitle.R

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 * @see CollapsingTextHelper
 */
@SuppressLint("RestrictedApi")
internal class SubtitleCollapsingTextHelper(private val mView: View) {

    private var mDrawTitle = false

    private val mCurrentBounds = RectF()

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

    private val mTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
    private val mSubtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)

    private var mCollapsedShadowRadius = 0f
    private var mCollapsedShadowDx = 0f
    private var mCollapsedShadowDy = 0f
    private var mCollapsedShadowColor = 0

    private var mExpandedShadowRadius = 0f
    private var mExpandedShadowDx = 0f
    private var mExpandedShadowDy = 0f
    private var mExpandedShadowColor = 0

    internal var textSizeInterpolator: Interpolator? = null
        set(value) {
            field = value
            recalculate()
        }

    internal var positionInterpolator: Interpolator? = null
        set(value) {
            field = value
            recalculate()
        }

    internal var expandedTitleSize: Float = 15f
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var expandedSubtitleSize: Float = 15f
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var collapsedTitleSize: Float = 15f
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var collapsedSubtitleSize: Float = 15f
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var expandedTitleColor: ColorStateList? = null
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var expandedSubtitleColor: ColorStateList? = null
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var collapsedTitleColor: ColorStateList? = null
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var collapsedSubtitleColor: ColorStateList? = null
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var expandedBounds = Rect()
        set(value) {
            if (!rectEquals(field, value.left, value.top, value.right, value.bottom)) {
                field.set(value.left, value.top, value.right, value.bottom)
                mBoundsChanged = true
                onBoundsChanged()
            }
        }

    internal var collapsedBounds = Rect()
        set(value) {
            if (!rectEquals(field, value.left, value.top, value.right, value.bottom)) {
                field.set(value.left, value.top, value.right, value.bottom)
                mBoundsChanged = true
                onBoundsChanged()
            }
        }

    internal fun onBoundsChanged() {
        mDrawTitle = collapsedBounds.width() > 0 && collapsedBounds.height() > 0 && expandedBounds.width() > 0 && expandedBounds.height() > 0
    }

    internal var expandedTextGravity = Gravity.CENTER_VERTICAL
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal var collapsedTextGravity = Gravity.CENTER_VERTICAL
        set(value) {
            if (field != value) {
                field = value
                recalculate()
            }
        }

    internal fun setCollapsedTitleAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(mView.context, resId, android.support.v7.appcompat.R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            collapsedTitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            collapsedTitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, collapsedTitleSize.toInt()).toFloat()
        mCollapsedShadowColor = a.getInt(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0)
        mCollapsedShadowDx = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0f)
        mCollapsedShadowDy = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0f)
        mCollapsedShadowRadius = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0f)
        a.recycle()
        if (Build.VERSION.SDK_INT >= 16)
            mCollapsedTitleTypeface = readFontFamilyTypeface(resId)
        recalculate()
    }

    internal fun setExpandedTitleAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(mView.context, resId, android.support.v7.appcompat.R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            expandedTitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            expandedTitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, expandedTitleSize.toInt()).toFloat()
        mExpandedShadowColor = a.getInt(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowColor, 0)
        mExpandedShadowDx = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDx, 0f)
        mExpandedShadowDy = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowDy, 0f)
        mExpandedShadowRadius = a.getFloat(android.support.v7.appcompat.R.styleable.TextAppearance_android_shadowRadius, 0f)
        a.recycle()
        if (Build.VERSION.SDK_INT >= 16)
            mExpandedTitleTypeface = readFontFamilyTypeface(resId)
        recalculate()
    }

    internal fun setCollapsedSubtitleAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(mView.context, resId, R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            collapsedSubtitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            collapsedSubtitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, collapsedSubtitleSize.toInt()).toFloat()
        a.recycle()
        if (Build.VERSION.SDK_INT >= 16)
            mCollapsedSubtitleTypeface = readFontFamilyTypeface(resId)
        recalculate()
    }

    internal fun setExpandedSubtitleAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(mView.context, resId, android.support.v7.appcompat.R.styleable.TextAppearance)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor))
            expandedSubtitleColor = a.getColorStateList(android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)
        if (a.hasValue(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize))
            expandedSubtitleSize = a.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, expandedSubtitleSize.toInt()).toFloat()
        a.recycle()
        if (Build.VERSION.SDK_INT >= 16)
            mExpandedSubtitleTypeface = readFontFamilyTypeface(resId)
        recalculate()
    }

    private fun readFontFamilyTypeface(resId: Int): Typeface? = mView.context.obtainStyledAttributes(resId, intArrayOf(android.R.attr.fontFamily)).let {
        try {
            val family = it.getString(0)
            if (family != null) Typeface.create(family, Typeface.NORMAL)
        } finally {
            it.recycle()
        }
        null
    }

    internal var collapsedTitleTypeface: Typeface
        get() = if (mCollapsedTitleTypeface != null) mCollapsedTitleTypeface!! else Typeface.DEFAULT
        set(value) {
            if (mCollapsedTitleTypeface != value) {
                mCollapsedTitleTypeface = value
                recalculate()
            }
        }

    internal var expandedTitleTypeface: Typeface
        get() = if (mExpandedTitleTypeface != null) mExpandedTitleTypeface!! else Typeface.DEFAULT
        set(value) {
            if (mExpandedTitleTypeface != value) {
                mExpandedTitleTypeface = value
                recalculate()
            }
        }

    internal var collapsedSubtitleTypeface: Typeface
        get() = if (mCollapsedSubtitleTypeface != null) mCollapsedSubtitleTypeface!! else Typeface.DEFAULT
        set(value) {
            if (mCollapsedSubtitleTypeface != value) {
                mCollapsedSubtitleTypeface = value
                recalculate()
            }
        }

    internal var expandedSubtitleTypeface: Typeface
        get() = if (mExpandedSubtitleTypeface != null) mExpandedSubtitleTypeface!! else Typeface.DEFAULT
        set(value) {
            if (mExpandedSubtitleTypeface != value) {
                mExpandedSubtitleTypeface = value
                recalculate()
            }
        }

    internal fun setTitleTypefaces(typeface: Typeface) {
        mExpandedTitleTypeface = typeface
        mCollapsedTitleTypeface = mExpandedTitleTypeface
        recalculate()
    }

    internal fun setSubtitleTypefaces(typeface: Typeface) {
        mExpandedSubtitleTypeface = typeface
        mCollapsedSubtitleTypeface = mExpandedSubtitleTypeface
        recalculate()
    }

    internal var expansionFraction = 0f
        set(value) {
            val fraction = MathUtils.constrain(value, 0f, 1f)
            if (fraction != expansionFraction) {
                field = fraction
                calculateCurrentOffsets()
            }
        }

    internal fun setState(state: IntArray): Boolean {
        mState = state
        if (isStateful) {
            recalculate()
            return true
        }
        return false
    }

    internal val isStateful = collapsedTitleColor != null && collapsedTitleColor!!.isStateful || expandedTitleColor != null && expandedTitleColor!!.isStateful

    private fun calculateCurrentOffsets() = calculateOffsets(expansionFraction)

    private fun calculateOffsets(fraction: Float) {
        interpolateBounds(fraction)
        mCurrentTitleX = lerp(mExpandedTitleX, mCollapsedTitleX, fraction, positionInterpolator)
        mCurrentTitleY = lerp(mExpandedTitleY, mCollapsedTitleY, fraction, positionInterpolator)
        mCurrentSubtitleX = lerp(mExpandedSubtitleX, mCollapsedSubtitleX, fraction, positionInterpolator)
        mCurrentSubtitleY = lerp(mExpandedSubtitleY, mCollapsedSubtitleY, fraction, positionInterpolator)

        setInterpolatedTitleSize(lerp(expandedTitleSize, collapsedTitleSize, fraction, textSizeInterpolator))
        setInterpolatedSubtitleSize(lerp(expandedSubtitleSize, collapsedSubtitleSize, fraction, textSizeInterpolator))

        if (collapsedTitleColor != expandedTitleColor) mTitlePaint.color = blendColors(currentExpandedTitleColor, currentCollapsedTitleColor, fraction)
        else mTitlePaint.color = currentCollapsedTitleColor

        if (collapsedSubtitleColor != expandedSubtitleColor) mSubtitlePaint.color = blendColors(currentExpandedSubtitleColor, currentCollapsedSubtitleColor, fraction)
        else mSubtitlePaint.color = currentCollapsedSubtitleColor

        mTitlePaint.setShadowLayer(
                lerp(mExpandedShadowRadius, mCollapsedShadowRadius, fraction, null),
                lerp(mExpandedShadowDx, mCollapsedShadowDx, fraction, null),
                lerp(mExpandedShadowDy, mCollapsedShadowDy, fraction, null),
                blendColors(mExpandedShadowColor, mCollapsedShadowColor, fraction))
        ViewCompat.postInvalidateOnAnimation(mView)
    }

    private val currentExpandedTitleColor: Int
        @ColorInt get() =
            if (mState != null) expandedTitleColor!!.getColorForState(mState, 0)
            else expandedTitleColor!!.defaultColor

    private val currentCollapsedTitleColor: Int
        @ColorInt get() =
            if (mState != null) collapsedTitleColor!!.getColorForState(mState, 0)
            else collapsedTitleColor!!.defaultColor

    private val currentExpandedSubtitleColor: Int
        @ColorInt get() =
            if (mState != null) expandedSubtitleColor!!.getColorForState(mState, 0)
            else expandedSubtitleColor!!.defaultColor

    private val currentCollapsedSubtitleColor: Int
        @ColorInt get() =
            if (mState != null) collapsedSubtitleColor!!.getColorForState(mState, 0)
            else collapsedSubtitleColor!!.defaultColor

    private fun calculateBaseOffsets() {
        val currentTitleSize = mCurrentTitleSize
        val currentSubtitleSize = mCurrentSubtitleSize
        val titleOnly = TextUtils.isEmpty(subtitle)

        calculateUsingTitleSize(collapsedTitleSize)
        calculateUsingSubtitleSize(collapsedSubtitleSize)
        var titleWidth = if (mTitleToDraw != null) mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw!!.length) else 0f
        var subtitleWidth = if (mSubtitleToDraw != null) mSubtitlePaint.measureText(mSubtitleToDraw, 0, mSubtitleToDraw!!.length) else 0f
        var titleHeight = mTitlePaint.descent() - mTitlePaint.ascent()
        var titleOffset = titleHeight / 2 - mTitlePaint.descent()
        var subtitleHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent()
        var subtitleOffset = subtitleHeight / 2
        val collapsedAbsGravity = GravityCompat.getAbsoluteGravity(collapsedTextGravity, if (mIsRtl) ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR)
        when (collapsedAbsGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> if (titleOnly) {
                mCollapsedTitleY = collapsedBounds.bottom.toFloat()
            } else {
                val offset = (collapsedBounds.height() - (titleHeight + subtitleHeight)) / 3
                mCollapsedTitleY = collapsedBounds.top + offset - mTitlePaint.ascent()
                mCollapsedSubtitleY = collapsedBounds.top.toFloat() + offset * 2 + titleHeight - mSubtitlePaint.ascent()
            }
            Gravity.TOP -> if (titleOnly) {
                mCollapsedTitleY = collapsedBounds.top - mTitlePaint.ascent()
            } else {
                val offset = (collapsedBounds.height() - (titleHeight + subtitleHeight)) / 3
                mCollapsedTitleY = collapsedBounds.top + offset - mTitlePaint.ascent()
                mCollapsedSubtitleY = collapsedBounds.top.toFloat() + offset * 2 + titleHeight - mSubtitlePaint.ascent()
            }
        // Gravity.CENTER_VERTICAL,
            else -> if (titleOnly) {
                mCollapsedTitleY = collapsedBounds.centerY() + titleOffset
            } else {
                val offset = (collapsedBounds.height() - (titleHeight + subtitleHeight)) / 3
                mCollapsedTitleY = collapsedBounds.top + offset - mTitlePaint.ascent()
                mCollapsedSubtitleY = collapsedBounds.top.toFloat() + offset * 2 + titleHeight - mSubtitlePaint.ascent()
            }
        }
        when (collapsedAbsGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> {
                mCollapsedTitleX = collapsedBounds.centerX() - titleWidth / 2
                mCollapsedSubtitleX = collapsedBounds.centerX() - subtitleWidth / 2
            }
            Gravity.RIGHT -> {
                mCollapsedTitleX = collapsedBounds.right - titleWidth
                mCollapsedSubtitleX = collapsedBounds.right - subtitleWidth
            }
        // Gravity.LEFT,
            else -> {
                mCollapsedTitleX = collapsedBounds.left.toFloat()
                mCollapsedSubtitleX = collapsedBounds.left.toFloat()
            }
        }

        calculateUsingTitleSize(expandedTitleSize)
        calculateUsingSubtitleSize(expandedSubtitleSize)
        titleWidth = if (mTitleToDraw != null) mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw!!.length) else 0f
        subtitleWidth = if (mSubtitleToDraw != null) mSubtitlePaint.measureText(mSubtitleToDraw, 0, mSubtitleToDraw!!.length) else 0f
        titleHeight = mTitlePaint.descent() - mTitlePaint.ascent()
        titleOffset = titleHeight / 2 - mTitlePaint.descent()
        subtitleHeight = mSubtitlePaint.descent() - mSubtitlePaint.ascent()
        subtitleOffset = subtitleHeight / 2
        val expandedAbsGravity = GravityCompat.getAbsoluteGravity(expandedTextGravity, if (mIsRtl) ViewCompat.LAYOUT_DIRECTION_RTL else ViewCompat.LAYOUT_DIRECTION_LTR)
        when (expandedAbsGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> if (titleOnly) {
                mExpandedTitleY = expandedBounds.bottom.toFloat()
            } else {
                mExpandedTitleY = expandedBounds.bottom + mSubtitlePaint.ascent()
                mExpandedSubtitleY = mExpandedTitleY + subtitleOffset - mSubtitlePaint.ascent()
            }
            Gravity.TOP -> if (titleOnly) {
                mExpandedTitleY = expandedBounds.top - mTitlePaint.ascent()
            } else {
                mExpandedTitleY = expandedBounds.top - mTitlePaint.ascent()
                mExpandedSubtitleY = mExpandedTitleY + subtitleOffset - mSubtitlePaint.ascent()
            }
        // Gravity.CENTER_VERTICAL,
            else -> if (titleOnly) {
                mExpandedTitleY = expandedBounds.centerY() + titleOffset
            } else {
                mExpandedTitleY = expandedBounds.centerY().toFloat() + titleOffset + mSubtitlePaint.ascent()
                mExpandedSubtitleY = mExpandedTitleY + subtitleOffset - mSubtitlePaint.ascent()
            }
        }
        when (expandedAbsGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> {
                mExpandedTitleX = expandedBounds.centerX() - titleWidth / 2
                mExpandedSubtitleX = expandedBounds.centerX() - subtitleWidth / 2
            }
            Gravity.RIGHT -> {
                mExpandedTitleX = expandedBounds.right - titleWidth
                mExpandedSubtitleX = expandedBounds.right - subtitleWidth
            }
        // Gravity.LEFT,
            else -> {
                mExpandedTitleX = expandedBounds.left.toFloat()
                mExpandedSubtitleX = expandedBounds.left.toFloat()
            }
        }

        clearTexture()
        calculateUsingTitleSize(currentTitleSize)
        calculateUsingSubtitleSize(currentSubtitleSize)
    }

    private fun interpolateBounds(fraction: Float) {
        mCurrentBounds.left = lerp(expandedBounds.left.toFloat(), collapsedBounds.left.toFloat(), fraction, positionInterpolator)
        mCurrentBounds.top = lerp(mExpandedTitleY, mCollapsedTitleY, fraction, positionInterpolator)
        mCurrentBounds.right = lerp(expandedBounds.right.toFloat(), collapsedBounds.right.toFloat(), fraction, positionInterpolator)
        mCurrentBounds.bottom = lerp(expandedBounds.bottom.toFloat(), collapsedBounds.bottom.toFloat(), fraction, positionInterpolator)
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
            if (drawTexture) {
                titleAscent = mTitleTextureAscent * mTitleScale
                titleDescent = mTitleTextureDescent * mTitleScale
                subtitleAscent = mSubtitleTextureAscent * mSubtitleScale
            } else {
                titleAscent = mTitlePaint.ascent() * mTitleScale
                titleDescent = mTitlePaint.descent() * mTitleScale
                subtitleAscent = mSubtitlePaint.ascent() * mSubtitleScale
            }

            if (DEBUG_DRAW)
                canvas.drawRect(mCurrentBounds.left, titleY + titleAscent, mCurrentBounds.right, titleY + titleDescent, DEBUG_DRAW_PAINT!!)

            if (drawTexture) {
                titleY += titleAscent
                subtitleY += subtitleAscent
            }

            // separate canvas save for subtitle
            val saveCountSubtitle = canvas.save()
            if (!TextUtils.isEmpty(subtitle)) {
                if (mSubtitleScale != 1f) canvas.scale(mSubtitleScale, mSubtitleScale, subtitleX, subtitleY)
                if (drawTexture) canvas.drawBitmap(mExpandedSubtitleTexture!!, subtitleX, subtitleY, mSubtitleTexturePaint)
                else canvas.drawText(mSubtitleToDraw!!, 0, mSubtitleToDraw!!.length, subtitleX, subtitleY, mSubtitlePaint)
                canvas.restoreToCount(saveCountSubtitle)
            }

            if (mTitleScale != 1f) canvas.scale(mTitleScale, mTitleScale, titleX, titleY)

            if (drawTexture) canvas.drawBitmap(mExpandedTitleTexture!!, titleX, titleY, mTitleTexturePaint)
            else canvas.drawText(mTitleToDraw!!, 0, mTitleToDraw!!.length, titleX, titleY, mTitlePaint)
        }
        canvas.restoreToCount(saveCountTitle)
    }

    private fun calculateIsRtl(text: CharSequence): Boolean {
        val defaultIsRtl = ViewCompat.getLayoutDirection(mView) == ViewCompat.LAYOUT_DIRECTION_RTL
        return (if (defaultIsRtl) TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
        else TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(text, 0, text.length)
    }

    private fun setInterpolatedTitleSize(textSize: Float) {
        calculateUsingTitleSize(textSize)
        mUseTexture = USE_SCALING_TEXTURE && mTitleScale != 1f
        if (mUseTexture) ensureExpandedTitleTexture()
        ViewCompat.postInvalidateOnAnimation(mView)
    }

    private fun calculateUsingTitleSize(titleSize: Float) {
        if (title == null) return

        val collapsedWidth = collapsedBounds.width().toFloat()
        val expandedWidth = expandedBounds.width().toFloat()

        val availableWidth: Float
        val newTitleSize: Float
        var updateDrawText = false

        if (isClose(titleSize, collapsedTitleSize)) {
            newTitleSize = collapsedTitleSize
            mTitleScale = 1f
            if (mCurrentTitleTypeface != mCollapsedTitleTypeface) {
                mCurrentTitleTypeface = mCollapsedTitleTypeface
                updateDrawText = true
            }
            availableWidth = collapsedWidth
        } else {
            newTitleSize = expandedTitleSize
            if (mCurrentTitleTypeface != mExpandedTitleTypeface) {
                mCurrentTitleTypeface = mExpandedTitleTypeface
                updateDrawText = true
            }
            mTitleScale =
                    if (isClose(titleSize, expandedTitleSize)) 1f
                    else titleSize / expandedTitleSize

            val titleSizeRatio = collapsedTitleSize / expandedTitleSize
            val scaledDownWidth = expandedWidth * titleSizeRatio

            availableWidth =
                    if (scaledDownWidth > collapsedWidth) Math.min(collapsedWidth / titleSizeRatio, expandedWidth)
                    else expandedWidth
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
            val title = TextUtils.ellipsize(this.title, mTitlePaint, availableWidth, TextUtils.TruncateAt.END)
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
        ViewCompat.postInvalidateOnAnimation(mView)
    }

    private fun calculateUsingSubtitleSize(subtitleSize: Float) {
        if (subtitle == null) return

        val collapsedWidth = collapsedBounds.width().toFloat()
        val expandedWidth = expandedBounds.width().toFloat()

        val availableWidth: Float
        val newSubtitleSize: Float
        var updateDrawText = false

        if (isClose(subtitleSize, collapsedSubtitleSize)) {
            newSubtitleSize = collapsedSubtitleSize
            mSubtitleScale = 1f
            if (mCurrentSubtitleTypeface != mCollapsedSubtitleTypeface) {
                mCurrentSubtitleTypeface = mCollapsedSubtitleTypeface
                updateDrawText = true
            }
            availableWidth = collapsedWidth
        } else {
            newSubtitleSize = expandedSubtitleSize
            if (mCurrentSubtitleTypeface != mExpandedSubtitleTypeface) {
                mCurrentSubtitleTypeface = mExpandedSubtitleTypeface
                updateDrawText = true
            }
            mSubtitleScale =
                    if (isClose(subtitleSize, expandedSubtitleSize)) 1f
                    else subtitleSize / expandedSubtitleSize

            val subtitleSizeRatio = collapsedSubtitleSize / expandedSubtitleSize
            val scaledDownWidth = expandedWidth * subtitleSizeRatio

            availableWidth =
                    if (scaledDownWidth > collapsedWidth) Math.min(collapsedWidth / subtitleSizeRatio, expandedWidth)
                    else expandedWidth
        }

        if (availableWidth > 0) {
            updateDrawText = mCurrentSubtitleSize != newSubtitleSize || mBoundsChanged || updateDrawText
            mCurrentSubtitleSize = newSubtitleSize
            mBoundsChanged = false
        }

        if (mSubtitleToDraw == null || updateDrawText) {
            mSubtitlePaint.textSize = mCurrentSubtitleSize
            mSubtitlePaint.typeface = mCurrentSubtitleTypeface
            mSubtitlePaint.isLinearText = mSubtitleScale != 1f
            val subtitle = TextUtils.ellipsize(this.subtitle, mSubtitlePaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(subtitle, mSubtitleToDraw)) {
                mSubtitleToDraw = subtitle
                mIsRtl = calculateIsRtl(mSubtitleToDraw!!)
            }
        }
    }

    private fun ensureExpandedTitleTexture() {
        if (mExpandedTitleTexture != null || expandedBounds.isEmpty || TextUtils.isEmpty(mTitleToDraw)) return
        calculateOffsets(0f)
        mTitleTextureAscent = mTitlePaint.ascent()
        mTitleTextureDescent = mTitlePaint.descent()
        val w = Math.round(mTitlePaint.measureText(mTitleToDraw, 0, mTitleToDraw!!.length))
        val h = Math.round(mTitleTextureDescent - mTitleTextureAscent)
        if (w <= 0 || h <= 0) return
        mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(mExpandedTitleTexture!!)
        c.drawText(mTitleToDraw!!, 0, mTitleToDraw!!.length, 0f, h - mTitlePaint.descent(), mTitlePaint)
        if (mTitleTexturePaint == null)
            mTitleTexturePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }

    private fun ensureExpandedSubtitleTexture() {
        if (mExpandedSubtitleTexture != null || expandedBounds.isEmpty || TextUtils.isEmpty(mSubtitleToDraw)) return
        calculateOffsets(0f)
        mSubtitleTextureAscent = mSubtitlePaint.ascent()
        mSubtitleTextureDescent = mSubtitlePaint.descent()
        val w = Math.round(mSubtitlePaint.measureText(mSubtitleToDraw, 0, mSubtitleToDraw!!.length))
        val h = Math.round(mSubtitleTextureDescent - mSubtitleTextureAscent)
        if (w <= 0 || h <= 0) return
        mExpandedSubtitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(mExpandedSubtitleTexture!!)
        c.drawText(mSubtitleToDraw!!, 0, mSubtitleToDraw!!.length, 0f, h - mSubtitlePaint.descent(), mSubtitlePaint)
        if (mSubtitleTexturePaint == null)
            mSubtitleTexturePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }

    fun recalculate() {
        if (mView.height > 0 && mView.width > 0) {
            calculateBaseOffsets()
            calculateCurrentOffsets()
        }
    }

    internal var title: CharSequence? = null
        set(title) {
            if (title == null || title != this.title) {
                field = title
                mTitleToDraw = null
                clearTexture()
                recalculate()
            }
        }

    internal var subtitle: CharSequence? = null
        set(subtitle) {
            if (subtitle == null || subtitle != this.subtitle) {
                field = subtitle
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

    companion object {
        private val USE_SCALING_TEXTURE = Build.VERSION.SDK_INT < 18

        private val DEBUG_DRAW = false
        private val DEBUG_DRAW_PAINT: Paint?

        init {
            DEBUG_DRAW_PAINT = if (DEBUG_DRAW) Paint() else null
            if (DEBUG_DRAW_PAINT != null) {
                DEBUG_DRAW_PAINT.isAntiAlias = true
                DEBUG_DRAW_PAINT.color = Color.MAGENTA
            }
        }

        private fun isClose(value: Float, targetValue: Float) = Math.abs(value - targetValue) < 0.001f

        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val a = Color.alpha(color1) * inverseRatio + Color.alpha(color2) * ratio
            val r = Color.red(color1) * inverseRatio + Color.red(color2) * ratio
            val g = Color.green(color1) * inverseRatio + Color.green(color2) * ratio
            val b = Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio
            return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
        }

        private fun lerp(startValue: Float, endValue: Float, fraction: Float, interpolator: Interpolator?): Float {
            var fraction = fraction
            if (interpolator != null)
                fraction = interpolator.getInterpolation(fraction)
            return AnimationUtils.lerp(startValue, endValue, fraction)
        }

        private fun rectEquals(r: Rect, left: Int, top: Int, right: Int, bottom: Int) = !(r.left != left || r.top != top || r.right != right || r.bottom != bottom)
    }
}