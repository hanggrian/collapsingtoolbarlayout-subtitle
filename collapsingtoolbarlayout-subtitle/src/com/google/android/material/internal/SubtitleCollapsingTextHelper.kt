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

package com.google.android.material.internal

import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import androidx.annotation.ColorInt
import androidx.appcompat.R.styleable
import androidx.appcompat.widget.TintTypedArray
import androidx.core.math.MathUtils
import androidx.core.text.TextDirectionHeuristicsCompat
import androidx.core.view.GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK
import androidx.core.view.GravityCompat.getAbsoluteGravity
import androidx.core.view.ViewCompat.LAYOUT_DIRECTION_LTR
import androidx.core.view.ViewCompat.LAYOUT_DIRECTION_RTL
import androidx.core.view.ViewCompat.getLayoutDirection
import androidx.core.view.ViewCompat.postInvalidateOnAnimation
import com.google.android.material.animation.AnimationUtils
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * An internal helper class for [com.google.android.material.appbar.SubtitleCollapsingToolbarLayout].
 *
 * @see CollapsingTextHelper
 */
@Suppress("unused")
internal class SubtitleCollapsingTextHelper(private val view: View) {

    private var drawTitle = false
    private var expandedFraction = 0f

    private val expandedBounds = Rect()
    private val collapsedBounds = Rect()
    private val currentBounds = RectF()
    private var _expandedTextGravity = CENTER_VERTICAL
    private var _collapsedTextGravity = CENTER_VERTICAL
    private var _expandedTitleSize = 15f
    private var _expandedSubtitleSize = 15f
    private var _collapsedTitleSize = 15f
    private var _collapsedSubtitleSize = 15f
    private var _expandedTitleColor: ColorStateList? = null
    private var _expandedSubtitleColor: ColorStateList? = null
    private var _collapsedTitleColor: ColorStateList? = null
    private var _collapsedSubtitleColor: ColorStateList? = null

    private var expandedTitleY = 0f
    private var expandedSubtitleY = 0f
    private var collapsedTitleY = 0f
    private var collapsedSubtitleY = 0f
    private var expandedTitleX = 0f
    private var expandedSubtitleX = 0f
    private var collapsedTitleX = 0f
    private var collapsedSubtitleX = 0f
    private var currentTitleX = 0f
    private var currentSubtitleX = 0f
    private var currentTitleY = 0f
    private var currentSubtitleY = 0f
    private var _collapsedTitleTypeface: Typeface? = null
    private var _collapsedSubtitleTypeface: Typeface? = null
    private var _expandedTitleTypeface: Typeface? = null
    private var _expandedSubtitleTypeface: Typeface? = null
    private var _currentTitleTypeface: Typeface? = null
    private var _currentSubtitleTypeface: Typeface? = null

    private var _title: CharSequence? = null
    private var _subtitle: CharSequence? = null
    private var titleToDraw: CharSequence? = null
    private var subtitleToDraw: CharSequence? = null
    private var isRtl = false

    private var useTexture = false
    private var expandedTitleTexture: Bitmap? = null
    private var expandedSubtitleTexture: Bitmap? = null
    private var titleTexturePaint: Paint? = null
    private var subtitleTexturePaint: Paint? = null
    private var titleTextureAscent = 0f
    private var subtitleTextureAscent = 0f
    private var titleTextureDescent = 0f
    private var subtitleTextureDescent = 0f

    private var titleScale = 0f
    private var subtitleScale = 0f
    private var currentTitleSize = 0f
    private var currentSubtitleSize = 0f

    private var state: IntArray? = null

    private var boundsChanged = false

    private val titlePaint = TextPaint(ANTI_ALIAS_FLAG or SUBPIXEL_TEXT_FLAG)
    private val subtitlePaint = TextPaint(ANTI_ALIAS_FLAG or SUBPIXEL_TEXT_FLAG)

    private var positionInterpolator: TimeInterpolator? = null
    private var textSizeInterpolator: TimeInterpolator? = null

    private var collapsedShadowRadius = 0f
    private var collapsedShadowDx = 0f
    private var collapsedShadowDy = 0f
    private var collapsedShadowColor = 0

    private var expandedShadowRadius = 0f
    private var expandedShadowDx = 0f
    private var expandedShadowDy = 0f
    private var expandedShadowColor = 0

    internal fun setTextSizeInterpolator(interpolator: TimeInterpolator) {
        textSizeInterpolator = interpolator
        recalculate()
    }

    internal fun setPositionInterpolator(interpolator: TimeInterpolator) {
        positionInterpolator = interpolator
        recalculate()
    }

    internal var expandedTitleSize: Float
        get() = _expandedTitleSize
        set(textSize) {
            if (_expandedTitleSize != textSize) {
                _expandedTitleSize = textSize
                recalculate()
            }
        }

    internal var expandedSubtitleSize: Float
        get() = _expandedSubtitleSize
        set(textSize) {
            if (_expandedSubtitleSize != textSize) {
                _expandedSubtitleSize = textSize
                recalculate()
            }
        }

    internal var collapsedTitleSize: Float
        get() = _collapsedTitleSize
        set(textSize) {
            if (_collapsedTitleSize != textSize) {
                _collapsedTitleSize = textSize
                recalculate()
            }
        }

    internal var collapsedSubtitleSize: Float
        get() = _collapsedSubtitleSize
        set(textSize) {
            if (_collapsedSubtitleSize != textSize) {
                _collapsedSubtitleSize = textSize
                recalculate()
            }
        }

    internal var expandedTitleColor: ColorStateList
        get() = _expandedTitleColor!!
        set(textColor) {
            if (_expandedTitleColor != textColor) {
                _expandedTitleColor = textColor
                recalculate()
            }
        }

    internal var expandedSubtitleColor: ColorStateList
        get() = _expandedSubtitleColor!!
        set(textColor) {
            if (_expandedSubtitleColor != textColor) {
                _expandedSubtitleColor = textColor
                recalculate()
            }
        }

    internal var collapsedTitleColor: ColorStateList
        get() = _collapsedTitleColor!!
        set(textColor) {
            if (_collapsedTitleColor != textColor) {
                _collapsedTitleColor = textColor
                recalculate()
            }
        }

    internal var collapsedSubtitleColor: ColorStateList
        get() = _collapsedSubtitleColor!!
        set(textColor) {
            if (_collapsedSubtitleColor != textColor) {
                _collapsedSubtitleColor = textColor
                recalculate()
            }
        }

    internal fun setExpandedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(expandedBounds, left, top, right, bottom)) {
            expandedBounds.set(left, top, right, bottom)
            boundsChanged = true
            onBoundsChanged()
        }
    }

    internal fun setCollapsedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(collapsedBounds, left, top, right, bottom)) {
            collapsedBounds.set(left, top, right, bottom)
            boundsChanged = true
            onBoundsChanged()
        }
    }

    internal fun onBoundsChanged() {
        drawTitle = collapsedBounds.width() > 0 &&
            collapsedBounds.height() > 0 &&
            expandedBounds.width() > 0 &&
            expandedBounds.height() > 0
    }

    internal var expandedTextGravity: Int
        get() = _expandedTextGravity
        set(gravity) {
            if (_expandedTextGravity != gravity) {
                _expandedTextGravity = gravity
                recalculate()
            }
        }

    internal var collapsedTextGravity: Int
        get() = _collapsedTextGravity
        set(gravity) {
            if (_collapsedTextGravity != gravity) {
                _collapsedTextGravity = gravity
                recalculate()
            }
        }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setCollapsedTitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(view.context, resId, styleable.TextAppearance)
        if (a.hasValue(styleable.TextAppearance_android_textColor)) {
            _collapsedTitleColor = a.getColorStateList(styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(styleable.TextAppearance_android_textSize)) {
            _collapsedTitleSize = a.getDimensionPixelSize(styleable.TextAppearance_android_textSize,
                _expandedSubtitleSize.toInt()).toFloat()
        }
        collapsedShadowColor = a.getInt(styleable.TextAppearance_android_shadowColor, 0)
        collapsedShadowDx = a.getFloat(styleable.TextAppearance_android_shadowDx, 0f)
        collapsedShadowDy = a.getFloat(styleable.TextAppearance_android_shadowDy, 0f)
        collapsedShadowRadius = a.getFloat(styleable.TextAppearance_android_shadowRadius, 0f)
        a.recycle()
        if (SDK_INT >= 16) {
            _collapsedTitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setExpandedTitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(view.context, resId, styleable.TextAppearance)
        if (a.hasValue(styleable.TextAppearance_android_textColor)) {
            _expandedTitleColor = a.getColorStateList(styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(styleable.TextAppearance_android_textSize)) {
            _expandedTitleSize = a.getDimensionPixelSize(styleable.TextAppearance_android_textSize,
                _expandedTitleSize.toInt()).toFloat()
        }
        expandedShadowColor = a.getInt(styleable.TextAppearance_android_shadowColor, 0)
        expandedShadowDx = a.getFloat(styleable.TextAppearance_android_shadowDx, 0f)
        expandedShadowDy = a.getFloat(styleable.TextAppearance_android_shadowDy, 0f)
        expandedShadowRadius = a.getFloat(styleable.TextAppearance_android_shadowRadius, 0f)
        a.recycle()
        if (SDK_INT >= 16) {
            _expandedTitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setCollapsedSubtitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(view.context, resId, styleable.TextAppearance)
        if (a.hasValue(styleable.TextAppearance_android_textColor)) {
            _collapsedSubtitleColor =
                a.getColorStateList(styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(styleable.TextAppearance_android_textSize)) {
            _collapsedSubtitleSize = a.getDimensionPixelSize(
                styleable.TextAppearance_android_textSize, _collapsedSubtitleSize.toInt()).toFloat()
        }
        a.recycle()
        if (SDK_INT >= 16) {
            _collapsedSubtitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    internal fun setExpandedSubtitleTextAppearance(resId: Int) {
        val a = TintTypedArray.obtainStyledAttributes(view.context, resId, styleable.TextAppearance)
        if (a.hasValue(styleable.TextAppearance_android_textColor)) {
            _expandedSubtitleColor = a.getColorStateList(styleable.TextAppearance_android_textColor)
        }
        if (a.hasValue(styleable.TextAppearance_android_textSize)) {
            _expandedSubtitleSize = a.getDimensionPixelSize(
                styleable.TextAppearance_android_textSize, _expandedSubtitleSize.toInt()).toFloat()
        }
        a.recycle()
        if (SDK_INT >= 16) {
            _expandedSubtitleTypeface = readFontFamilyTypeface(resId)
        }
        recalculate()
    }

    @SuppressLint("InlinedApi")
    private fun readFontFamilyTypeface(resId: Int): Typeface? {
        val a = view.context.obtainStyledAttributes(resId, intArrayOf(android.R.attr.fontFamily))
        try {
            val family = a.getString(0)
            if (family != null) return Typeface.create(family, Typeface.NORMAL)
        } finally {
            a.recycle()
        }
        return null
    }

    internal var collapsedTitleTypeface: Typeface
        get() = _collapsedTitleTypeface ?: Typeface.DEFAULT
        set(value) {
            if (_collapsedTitleTypeface isDifferentThan value) {
                _collapsedTitleTypeface = value
                recalculate()
            }
        }

    internal var expandedTitleTypeface: Typeface
        get() = _expandedTitleTypeface ?: Typeface.DEFAULT
        set(value) {
            if (_expandedTitleTypeface isDifferentThan value) {
                _expandedTitleTypeface = value
                recalculate()
            }
        }

    internal var collapsedSubtitleTypeface: Typeface
        get() = _collapsedSubtitleTypeface ?: Typeface.DEFAULT
        set(value) {
            if (_collapsedSubtitleTypeface isDifferentThan value) {
                _collapsedSubtitleTypeface = value
                recalculate()
            }
        }

    internal var expandedSubtitleTypeface: Typeface
        get() = _expandedSubtitleTypeface ?: Typeface.DEFAULT
        set(value) {
            if (_expandedSubtitleTypeface isDifferentThan value) {
                _expandedSubtitleTypeface = value
                recalculate()
            }
        }

    internal fun setTitleTypefaces(typeface: Typeface) {
        _expandedTitleTypeface = typeface
        _collapsedTitleTypeface = typeface
        recalculate()
    }

    internal fun setSubtitleTypefaces(typeface: Typeface) {
        _expandedSubtitleTypeface = typeface
        _collapsedSubtitleTypeface = typeface
        recalculate()
    }

    internal var expansionFraction
        get() = expandedFraction
        set(value) {
            val fraction = MathUtils.clamp(value, 0f, 1f)
            if (fraction != expandedFraction) {
                expandedFraction = fraction
                calculateCurrentOffsets()
            }
        }

    internal fun setState(state: IntArray): Boolean {
        this.state = state
        if (isStateful()) {
            recalculate()
            return true
        }
        return false
    }

    internal fun isStateful() = _collapsedTitleColor != null &&
        _collapsedTitleColor!!.isStateful ||
        _expandedTitleColor != null &&
        _expandedTitleColor!!.isStateful

    private fun calculateCurrentOffsets() = calculateOffsets(expandedFraction)

    private fun calculateOffsets(fraction: Float) {
        interpolateBounds(fraction)
        currentTitleX = lerp(expandedTitleX, collapsedTitleX, fraction, positionInterpolator)
        currentTitleY = lerp(expandedTitleY, collapsedTitleY, fraction, positionInterpolator)
        currentSubtitleX = lerp(
            expandedSubtitleX, collapsedSubtitleX, fraction, positionInterpolator)
        currentSubtitleY = lerp(
            expandedSubtitleY, collapsedSubtitleY, fraction, positionInterpolator)
        setInterpolatedTitleSize(lerp(
            _expandedTitleSize, _collapsedTitleSize, fraction, textSizeInterpolator))
        setInterpolatedSubtitleSize(lerp(
            _expandedSubtitleSize, _collapsedSubtitleSize, fraction, textSizeInterpolator))
        titlePaint.color = when {
            _collapsedTitleColor != _expandedTitleColor -> blendColors(
                currentExpandedTitleColor, currentCollapsedTitleColor, fraction)
            else -> currentCollapsedTitleColor
        }
        subtitlePaint.color = when {
            _collapsedSubtitleColor != _expandedSubtitleColor -> blendColors(
                currentExpandedSubtitleColor, currentCollapsedSubtitleColor, fraction)
            else -> currentCollapsedSubtitleColor
        }
        titlePaint.setShadowLayer(
            lerp(expandedShadowRadius, collapsedShadowRadius, fraction, null),
            lerp(expandedShadowDx, collapsedShadowDx, fraction, null),
            lerp(expandedShadowDy, collapsedShadowDy, fraction, null),
            blendColors(expandedShadowColor, collapsedShadowColor, fraction))
        postInvalidateOnAnimation(view)
    }

    private val currentExpandedTitleColor: Int
        @ColorInt get() = state
            ?.let { _expandedTitleColor!!.getColorForState(it, 0) }
            ?: _expandedTitleColor!!.defaultColor

    private val currentCollapsedTitleColor: Int
        @ColorInt get() = state
            ?.let { _collapsedTitleColor!!.getColorForState(it, 0) }
            ?: _collapsedTitleColor!!.defaultColor

    private val currentExpandedSubtitleColor: Int
        @ColorInt get() = state
            ?.let { _expandedSubtitleColor!!.getColorForState(it, 0) }
            ?: _expandedSubtitleColor!!.defaultColor

    private val currentCollapsedSubtitleColor: Int
        @ColorInt get() = state
            ?.let { _collapsedSubtitleColor!!.getColorForState(it, 0) }
            ?: _collapsedSubtitleColor!!.defaultColor

    @SuppressLint("RtlHardcoded")
    private fun calculateBaseOffsets() {
        val currentTitleSize = currentTitleSize
        val currentSubtitleSize = currentSubtitleSize
        val isTitleOnly = TextUtils.isEmpty(_subtitle)

        calculateUsingTitleSize(_collapsedTitleSize)
        calculateUsingSubtitleSize(_collapsedSubtitleSize)

        var titleWidth: Float = when {
            titleToDraw != null -> titlePaint.measureText(titleToDraw, 0, titleToDraw!!.length)
            else -> 0f
        }
        var subtitleWidth = when {
            subtitleToDraw != null -> subtitlePaint.measureText(
                subtitleToDraw, 0, subtitleToDraw!!.length)
            else -> 0f
        }
        var titleHeight = titlePaint.descent() - titlePaint.ascent()
        var titleOffset = titleHeight / 2 - titlePaint.descent()
        var subtitleHeight = subtitlePaint.descent() - subtitlePaint.ascent()
        var subtitleOffset = subtitleHeight / 2 - subtitlePaint.descent()
        val offset = (collapsedBounds.height() - (titleHeight + subtitleHeight)) / 3
        val collapsedAbsGravity = getAbsoluteGravity(_collapsedTextGravity, when {
            isRtl -> LAYOUT_DIRECTION_RTL
            else -> LAYOUT_DIRECTION_LTR
        })
        when {
            isTitleOnly -> when (collapsedAbsGravity and VERTICAL_GRAVITY_MASK) {
                BOTTOM -> collapsedTitleY = collapsedBounds.bottom.toFloat()
                TOP -> collapsedTitleY = collapsedBounds.top - titlePaint.ascent()
                CENTER_VERTICAL -> collapsedTitleY = collapsedBounds.centerY() + titleOffset
            }
            else -> {
                collapsedTitleY = collapsedBounds.top + offset - titlePaint.ascent()
                collapsedSubtitleY = collapsedBounds.top.toFloat() + offset * 2 + titleHeight -
                    subtitlePaint.ascent()
            }
        }
        when (collapsedAbsGravity and RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            CENTER_HORIZONTAL -> {
                collapsedTitleX = collapsedBounds.centerX() - titleWidth / 2
                collapsedSubtitleX = collapsedBounds.centerX() - subtitleWidth / 2
            }
            RIGHT -> {
                collapsedTitleX = collapsedBounds.right - titleWidth
                collapsedSubtitleX = collapsedBounds.right - subtitleWidth
            }
            LEFT -> {
                collapsedTitleX = collapsedBounds.left.toFloat()
                collapsedSubtitleX = collapsedBounds.left.toFloat()
            }
        }

        calculateUsingTitleSize(_expandedTitleSize)
        calculateUsingSubtitleSize(_expandedSubtitleSize)
        titleWidth = when {
            titleToDraw != null -> titlePaint.measureText(titleToDraw, 0, titleToDraw!!.length)
            else -> 0f
        }
        subtitleWidth = when {
            subtitleToDraw != null -> subtitlePaint.measureText(
                subtitleToDraw, 0, subtitleToDraw!!.length)
            else -> 0f
        }
        titleHeight = titlePaint.descent() - titlePaint.ascent()
        titleOffset = titleHeight / 2 - titlePaint.descent()
        subtitleHeight = subtitlePaint.descent() - subtitlePaint.ascent()
        subtitleOffset = subtitleHeight / 2 - subtitlePaint.descent()
        val expandedAbsGravity = getAbsoluteGravity(_expandedTextGravity, when {
            isRtl -> LAYOUT_DIRECTION_RTL
            else -> LAYOUT_DIRECTION_LTR
        })
        when {
            isTitleOnly -> when (expandedAbsGravity and VERTICAL_GRAVITY_MASK) {
                BOTTOM -> expandedTitleY = expandedBounds.bottom.toFloat()
                TOP -> expandedTitleY = expandedBounds.top - titlePaint.ascent()
                CENTER_VERTICAL -> expandedTitleY = expandedBounds.centerY() + titleOffset
            }
            else -> {
                when (expandedAbsGravity and VERTICAL_GRAVITY_MASK) {
                    BOTTOM -> expandedTitleY = expandedBounds.bottom - subtitleHeight -
                        titleOffset
                    TOP -> expandedTitleY = expandedBounds.top - titlePaint.ascent()
                    CENTER_VERTICAL -> expandedTitleY = expandedBounds.centerY() + titleOffset
                }
                when (expandedAbsGravity and VERTICAL_GRAVITY_MASK) {
                    BOTTOM -> expandedSubtitleY = expandedBounds.bottom.toFloat()
                    TOP -> expandedSubtitleY = expandedTitleY + subtitleHeight + titleOffset
                    CENTER_VERTICAL -> expandedSubtitleY = expandedTitleY + subtitleHeight +
                        titleOffset
                }
            }
        }
        when (expandedAbsGravity and RELATIVE_HORIZONTAL_GRAVITY_MASK) {
            CENTER_HORIZONTAL -> {
                expandedTitleX = expandedBounds.centerX() - titleWidth / 2
                expandedSubtitleX = expandedBounds.centerX() - subtitleWidth / 2
            }
            RIGHT -> {
                expandedTitleX = expandedBounds.right - titleWidth
                expandedSubtitleX = expandedBounds.right - subtitleWidth
            }
            LEFT -> {
                expandedTitleX = expandedBounds.left.toFloat()
                expandedSubtitleX = expandedBounds.left.toFloat()
            }
        }

        clearTexture()
        calculateUsingTitleSize(currentTitleSize)
        calculateUsingSubtitleSize(currentSubtitleSize)
    }

    private fun interpolateBounds(fraction: Float) {
        currentBounds.left = lerp(
            expandedBounds.left.toFloat(),
            collapsedBounds.left.toFloat(),
            fraction,
            positionInterpolator)
        currentBounds.top = lerp(
            expandedTitleY,
            collapsedTitleY,
            fraction,
            positionInterpolator)
        currentBounds.right = lerp(
            expandedBounds.right.toFloat(),
            collapsedBounds.right.toFloat(),
            fraction,
            positionInterpolator)
        currentBounds.bottom = lerp(
            expandedBounds.bottom.toFloat(),
            collapsedBounds.bottom.toFloat(),
            fraction,
            positionInterpolator)
    }

    fun draw(canvas: Canvas) {
        val saveCountTitle = canvas.save()
        if (titleToDraw != null && drawTitle) {
            val titleX = currentTitleX
            var titleY = currentTitleY
            val subtitleX = currentSubtitleX
            var subtitleY = currentSubtitleY
            val drawTexture = useTexture && expandedTitleTexture != null

            val titleAscent: Float
            val subtitleAscent: Float
            val titleDescent: Float
            when {
                drawTexture -> {
                    titleAscent = titleTextureAscent * titleScale
                    titleDescent = titleTextureDescent * titleScale
                    subtitleAscent = subtitleTextureAscent * subtitleScale
                }
                else -> {
                    titleAscent = titlePaint.ascent() * titleScale
                    titleDescent = titlePaint.descent() * titleScale
                    subtitleAscent = subtitlePaint.ascent() * subtitleScale
                }
            }

            if (DEBUG_DRAW) {
                canvas.drawRect(
                    currentBounds.left,
                    titleY + titleAscent,
                    currentBounds.right,
                    titleY + titleDescent, DEBUG_DRAW_PAINT!!)
            }

            if (drawTexture) {
                titleY += titleAscent
                subtitleY += subtitleAscent
            }

            // separate canvas save for subtitle
            val saveCountSubtitle = canvas.save()
            if (!TextUtils.isEmpty(_subtitle)) {
                if (subtitleScale != 1f) {
                    canvas.scale(subtitleScale, subtitleScale, subtitleX, subtitleY)
                }
                when {
                    drawTexture -> canvas.drawBitmap(
                        expandedSubtitleTexture!!,
                        subtitleX,
                        subtitleY,
                        subtitleTexturePaint)
                    else -> canvas.drawText(
                        subtitleToDraw!!,
                        0,
                        subtitleToDraw!!.length,
                        subtitleX,
                        subtitleY,
                        subtitlePaint)
                }
                canvas.restoreToCount(saveCountSubtitle)
            }

            if (titleScale != 1f) canvas.scale(titleScale, titleScale, titleX, titleY)

            when {
                drawTexture -> canvas.drawBitmap(
                    expandedTitleTexture!!,
                    titleX,
                    titleY,
                    titleTexturePaint)
                else -> canvas.drawText(
                    titleToDraw!!,
                    0,
                    titleToDraw!!.length,
                    titleX,
                    titleY,
                    titlePaint)
            }
        }
        canvas.restoreToCount(saveCountTitle)
    }

    private fun CharSequence.isRtl(): Boolean {
        val defaultIsRtl = getLayoutDirection(view) == LAYOUT_DIRECTION_RTL
        return (when {
            defaultIsRtl -> TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
            else -> TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR
        }).isRtl(this, 0, length)
    }

    private fun setInterpolatedTitleSize(textSize: Float) {
        calculateUsingTitleSize(textSize)
        useTexture = USE_SCALING_TEXTURE && titleScale != 1f
        if (useTexture) ensureExpandedTitleTexture()
        postInvalidateOnAnimation(view)
    }

    private infix fun Typeface?.isDifferentThan(other: Typeface?): Boolean =
        this != null && this != other || this == null && other != null

    private fun calculateUsingTitleSize(titleSize: Float) {
        if (title == null) {
            return
        }

        val collapsedWidth = collapsedBounds.width().toFloat()
        val expandedWidth = expandedBounds.width().toFloat()

        val availableWidth: Float
        val newTitleSize: Float
        var updateDrawText = false

        when {
            isClose(titleSize, _collapsedTitleSize) -> {
                newTitleSize = _collapsedTitleSize
                titleScale = 1f
                if (_currentTitleTypeface != _collapsedTitleTypeface) {
                    _currentTitleTypeface = _collapsedTitleTypeface
                    updateDrawText = true
                }
                availableWidth = collapsedWidth
            }
            else -> {
                newTitleSize = _expandedTitleSize
                if (_currentTitleTypeface != _expandedTitleTypeface) {
                    _currentTitleTypeface = _expandedTitleTypeface
                    updateDrawText = true
                }
                titleScale = when {
                    isClose(titleSize, _expandedTitleSize) -> 1f
                    else -> titleSize / _expandedTitleSize
                }

                val titleSizeRatio = _collapsedTitleSize / _expandedTitleSize
                val scaledDownWidth = expandedWidth * titleSizeRatio

                availableWidth = when {
                    scaledDownWidth > collapsedWidth ->
                        min(collapsedWidth / titleSizeRatio, expandedWidth)
                    else -> expandedWidth
                }
            }
        }

        if (availableWidth > 0) {
            updateDrawText = currentTitleSize != newTitleSize || boundsChanged || updateDrawText
            currentTitleSize = newTitleSize
            boundsChanged = false
        }

        if (titleToDraw == null || updateDrawText) {
            titlePaint.textSize = currentTitleSize
            titlePaint.typeface = _currentTitleTypeface
            titlePaint.isLinearText = titleScale != 1f
            val title = TextUtils.ellipsize(
                title, titlePaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(title, titleToDraw)) {
                titleToDraw = title
                isRtl = titleToDraw!!.isRtl()
            }
        }
    }

    private fun setInterpolatedSubtitleSize(textSize: Float) {
        calculateUsingSubtitleSize(textSize)
        useTexture = USE_SCALING_TEXTURE && subtitleScale != 1f
        if (useTexture) ensureExpandedSubtitleTexture()
        postInvalidateOnAnimation(view)
    }

    private fun calculateUsingSubtitleSize(subtitleSize: Float) {
        if (subtitle == null) {
            return
        }

        val collapsedWidth = collapsedBounds.width().toFloat()
        val expandedWidth = expandedBounds.width().toFloat()

        val availableWidth: Float
        val newSubtitleSize: Float
        var updateDrawText = false

        when {
            isClose(subtitleSize, _collapsedSubtitleSize) -> {
                newSubtitleSize = _collapsedSubtitleSize
                subtitleScale = 1f
                if (_currentSubtitleTypeface != _collapsedSubtitleTypeface) {
                    _currentSubtitleTypeface = _collapsedSubtitleTypeface
                    updateDrawText = true
                }
                availableWidth = collapsedWidth
            }
            else -> {
                newSubtitleSize = _expandedSubtitleSize
                if (_currentSubtitleTypeface != _expandedSubtitleTypeface) {
                    _currentSubtitleTypeface = _expandedSubtitleTypeface
                    updateDrawText = true
                }
                subtitleScale = when {
                    isClose(subtitleSize, _expandedSubtitleSize) -> 1f
                    else -> subtitleSize / _expandedSubtitleSize
                }

                val subtitleSizeRatio = _collapsedSubtitleSize / _expandedSubtitleSize
                val scaledDownWidth = expandedWidth * subtitleSizeRatio

                availableWidth = when {
                    scaledDownWidth > collapsedWidth ->
                        min(collapsedWidth / subtitleSizeRatio, expandedWidth)
                    else -> expandedWidth
                }
            }
        }

        if (availableWidth > 0) {
            updateDrawText = currentSubtitleSize != newSubtitleSize || boundsChanged ||
                updateDrawText
            currentSubtitleSize = newSubtitleSize
            boundsChanged = false
        }

        if (subtitleToDraw == null || updateDrawText) {
            subtitlePaint.textSize = currentSubtitleSize
            subtitlePaint.typeface = _currentSubtitleTypeface
            subtitlePaint.isLinearText = subtitleScale != 1f
            val subtitle = TextUtils.ellipsize(
                subtitle, subtitlePaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(subtitle, subtitleToDraw)) {
                subtitleToDraw = subtitle
                isRtl = subtitleToDraw!!.isRtl()
            }
        }
    }

    private fun ensureExpandedTitleTexture() {
        if (expandedTitleTexture != null || expandedBounds.isEmpty ||
            TextUtils.isEmpty(titleToDraw)) {
            return
        }
        calculateOffsets(0f)
        titleTextureAscent = titlePaint.ascent()
        titleTextureDescent = titlePaint.descent()
        val w = titlePaint.measureText(titleToDraw, 0, titleToDraw!!.length).roundToInt()
        val h = (titleTextureDescent - titleTextureAscent).roundToInt()
        if (w <= 0 || h <= 0) {
            return
        }
        expandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(expandedTitleTexture!!)
        c.drawText(
            titleToDraw!!,
            0,
            titleToDraw!!.length,
            0f,
            h - titlePaint.descent(),
            titlePaint)
        if (titleTexturePaint == null) {
            titleTexturePaint = Paint(ANTI_ALIAS_FLAG or FILTER_BITMAP_FLAG)
        }
    }

    private fun ensureExpandedSubtitleTexture() {
        if (expandedSubtitleTexture != null || expandedBounds.isEmpty ||
            TextUtils.isEmpty(subtitleToDraw)) {
            return
        }
        calculateOffsets(0f)
        subtitleTextureAscent = subtitlePaint.ascent()
        subtitleTextureDescent = subtitlePaint.descent()
        val w = subtitlePaint.measureText(subtitleToDraw, 0, subtitleToDraw!!.length)
            .roundToInt()
        val h = (subtitleTextureDescent - subtitleTextureAscent).roundToInt()
        if (w <= 0 || h <= 0) {
            return
        }
        expandedSubtitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(expandedSubtitleTexture!!)
        c.drawText(
            subtitleToDraw!!,
            0,
            subtitleToDraw!!.length,
            0f,
            h - subtitlePaint.descent(),
            subtitlePaint)
        if (subtitleTexturePaint == null) {
            subtitleTexturePaint = Paint(ANTI_ALIAS_FLAG or FILTER_BITMAP_FLAG)
        }
    }

    fun recalculate() {
        if (view.height > 0 && view.width > 0) {
            calculateBaseOffsets()
            calculateCurrentOffsets()
        }
    }

    internal var title: CharSequence?
        get() = _title
        set(title) {
            if (title == null || title != _title) {
                _title = title
                titleToDraw = null
                clearTexture()
                recalculate()
            }
        }

    internal var subtitle: CharSequence?
        get() = _subtitle
        set(subtitle) {
            if (subtitle == null || subtitle != _subtitle) {
                _subtitle = subtitle
                subtitleToDraw = null
                clearTexture()
                recalculate()
            }
        }

    private fun clearTexture() {
        if (expandedTitleTexture != null) {
            expandedTitleTexture!!.recycle()
            expandedTitleTexture = null
        }
        if (expandedSubtitleTexture != null) {
            expandedSubtitleTexture!!.recycle()
            expandedSubtitleTexture = null
        }
    }

    private companion object {
        val USE_SCALING_TEXTURE = SDK_INT < 18

        const val DEBUG_DRAW = false
        val DEBUG_DRAW_PAINT: Paint?

        init {
            DEBUG_DRAW_PAINT = if (DEBUG_DRAW) Paint() else null
            DEBUG_DRAW_PAINT?.let {
                it.isAntiAlias = true
                it.color = Color.MAGENTA
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
            interpolator: TimeInterpolator?
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