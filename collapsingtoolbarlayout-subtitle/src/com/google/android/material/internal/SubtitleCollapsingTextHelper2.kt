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
import androidx.annotation.VisibleForTesting
import androidx.appcompat.R.styleable
import androidx.appcompat.widget.TintTypedArray
import androidx.core.math.MathUtils
import androidx.core.text.TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR
import androidx.core.text.TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL
import androidx.core.view.GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK
import androidx.core.view.GravityCompat.getAbsoluteGravity
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.LAYOUT_DIRECTION_LTR
import androidx.core.view.ViewCompat.LAYOUT_DIRECTION_RTL
import androidx.core.view.ViewCompat.getLayoutDirection
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
class SubtitleCollapsingTextHelper2(private val view: View) {

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
    private val titleTempPaint = TextPaint(titlePaint)
    private val subtitlePaint = TextPaint(ANTI_ALIAS_FLAG or SUBPIXEL_TEXT_FLAG)
    private val subtitleTempPaint = TextPaint(subtitlePaint)

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

    fun setTextSizeInterpolator(interpolator: TimeInterpolator) {
        textSizeInterpolator = interpolator
        recalculate()
    }

    fun setPositionInterpolator(interpolator: TimeInterpolator) {
        positionInterpolator = interpolator
        recalculate()
    }

    var expandedTitleSize: Float
        get() = _expandedTitleSize
        set(textSize) {
            if (_expandedTitleSize != textSize) {
                _expandedTitleSize = textSize
                recalculate()
            }
        }

    var expandedSubtitleSize: Float
        get() = _expandedSubtitleSize
        set(textSize) {
            if (_expandedSubtitleSize != textSize) {
                _expandedSubtitleSize = textSize
                recalculate()
            }
        }

    var collapsedTitleSize: Float
        get() = _collapsedTitleSize
        set(textSize) {
            if (_collapsedTitleSize != textSize) {
                _collapsedTitleSize = textSize
                recalculate()
            }
        }

    var collapsedSubtitleSize: Float
        get() = _collapsedSubtitleSize
        set(textSize) {
            if (_collapsedSubtitleSize != textSize) {
                _collapsedSubtitleSize = textSize
                recalculate()
            }
        }

    var expandedTitleColor: ColorStateList
        get() = _expandedTitleColor!!
        set(textColor) {
            if (_expandedTitleColor != textColor) {
                _expandedTitleColor = textColor
                recalculate()
            }
        }

    var expandedSubtitleColor: ColorStateList
        get() = _expandedSubtitleColor!!
        set(textColor) {
            if (_expandedSubtitleColor != textColor) {
                _expandedSubtitleColor = textColor
                recalculate()
            }
        }

    var collapsedTitleColor: ColorStateList
        get() = _collapsedTitleColor!!
        set(textColor) {
            if (_collapsedTitleColor != textColor) {
                _collapsedTitleColor = textColor
                recalculate()
            }
        }

    var collapsedSubtitleColor: ColorStateList
        get() = _collapsedSubtitleColor!!
        set(textColor) {
            if (_collapsedSubtitleColor != textColor) {
                _collapsedSubtitleColor = textColor
                recalculate()
            }
        }

    fun setExpandedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(expandedBounds, left, top, right, bottom)) {
            expandedBounds.set(left, top, right, bottom)
            boundsChanged = true
            onBoundsChanged()
        }
    }

    fun setCollapsedBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!rectEquals(collapsedBounds, left, top, right, bottom)) {
            collapsedBounds.set(left, top, right, bottom)
            boundsChanged = true
            onBoundsChanged()
        }
    }

    val collapsedTitleWidth: Float
        get() = title?.let {
            titleTempPaint.getTextPaintCollapsed()
            titleTempPaint.measureText(it, 0, it.length)
        } ?: 0f

    val collapsedTitleHeight: Float
        get() {
            titleTempPaint.getTextPaintCollapsed()
            return -titleTempPaint.ascent()
        }

    val collapsedSubtitleWidth: Float
        get() = subtitle?.let {
            subtitleTempPaint.getTextPaintCollapsed()
            subtitleTempPaint.measureText(it, 0, it.length)
        } ?: 0f

    val collapsedSubtitleHeight: Float
        get() {
            subtitleTempPaint.getTextPaintCollapsed()
            return -subtitleTempPaint.ascent()
        }

    fun RectF.getCollapsedTitleActualBounds() {
        val isRtl = title!!.isRtl()
        left = when {
            !isRtl -> collapsedBounds.left.toFloat()
            else -> collapsedBounds.right.toFloat() - collapsedTitleWidth
        }
        top = collapsedBounds.top.toFloat()
        right = when {
            !isRtl -> left + collapsedTitleWidth
            else -> collapsedBounds.right.toFloat()
        }
        bottom = collapsedBounds.top.toFloat() + collapsedTitleHeight
    }

    private fun TextPaint.getTextPaintCollapsed() {
        textSize = collapsedTitleSize
        typeface = collapsedTitleTypeface
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
        ViewCompat.postInvalidateOnAnimation(view)
    }

    private val currentExpandedTitleColor: Int
        @ColorInt
        get() = state?.let { _expandedTitleColor!!.getColorForState(it, 0) }
            ?: _expandedTitleColor!!.defaultColor

    private val currentCollapsedTitleColor: Int
        @ColorInt
        @VisibleForTesting
        get() = state?.let { _collapsedTitleColor!!.getColorForState(it, 0) }
            ?: _collapsedTitleColor!!.defaultColor

    private val currentExpandedSubtitleColor: Int
        @ColorInt
        get() = state?.let { _expandedSubtitleColor!!.getColorForState(it, 0) }
            ?: _expandedSubtitleColor!!.defaultColor

    private val currentCollapsedSubtitleColor: Int
        @ColorInt
        @VisibleForTesting
        get() = state?.let { _collapsedSubtitleColor!!.getColorForState(it, 0) }
            ?: _collapsedSubtitleColor!!.defaultColor

    @SuppressLint("RtlHardcoded")
    private fun calculateBaseOffsets() {
        val currentTitleSize = currentTitleSize
        val currentSubtitleSize = currentSubtitleSize
        val isTitleOnly = _subtitle.isNullOrBlank()

        calculateUsingTitleSize(_collapsedTitleSize)
        calculateUsingSubtitleSize(_collapsedSubtitleSize)

        var titleWidth = titleToDraw?.let { titlePaint.measureText(it, 0, it.length) } ?: 0f
        var subtitleWidth = subtitleToDraw?.let { subtitlePaint.measureText(it, 0, it.length) }
            ?: 0f
        val collapsedAbsGravity = getAbsoluteGravity(_collapsedTextGravity, when {
            isRtl -> LAYOUT_DIRECTION_RTL
            else -> LAYOUT_DIRECTION_LTR
        })

        var titleHeight = titlePaint.descent() - titlePaint.ascent()
        var titleOffset = titleHeight / 2 - titlePaint.descent()
        var subtitleHeight = subtitlePaint.descent() - subtitlePaint.ascent()
        var subtitleOffset = subtitleHeight / 2 - subtitlePaint.descent()
        val offset = (collapsedBounds.height() - (titleHeight + subtitleHeight)) / 3
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

        // The bounds have changed so we need to clear the texture
        clearTexture()
        // Now reset the text size back to the original
        calculateUsingTitleSize(currentTitleSize)
        calculateUsingSubtitleSize(currentSubtitleSize)
    }

    private fun interpolateBounds(fraction: Float) {
        currentBounds.left = lerp(expandedBounds.left.toFloat(), collapsedBounds.left.toFloat(),
            fraction, positionInterpolator)
        currentBounds.top = lerp(expandedTitleY, collapsedTitleY, fraction, positionInterpolator)
        currentBounds.right = lerp(expandedBounds.right.toFloat(), collapsedBounds.right.toFloat(),
            fraction, positionInterpolator)
        currentBounds.bottom = lerp(expandedBounds.bottom.toFloat(),
            collapsedBounds.bottom.toFloat(), fraction, positionInterpolator)
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
                // Just a debug tool, which drawn a magenta rect in the text bounds
                canvas.drawRect(currentBounds.left, titleY + titleAscent, currentBounds.right,
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
                    drawTexture -> canvas.drawBitmap(expandedSubtitleTexture!!, subtitleX,
                        subtitleY, subtitleTexturePaint)
                    else -> canvas.drawText(subtitleToDraw!!, 0, subtitleToDraw!!.length,
                        subtitleX, subtitleY, subtitlePaint)
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

    private fun CharSequence.isRtl(): Boolean = when (LAYOUT_DIRECTION_RTL) {
        getLayoutDirection(view) -> FIRSTSTRONG_RTL
        else -> FIRSTSTRONG_LTR
    }.isRtl(this, 0, length)

    private fun setInterpolatedTitleSize(titleSize: Float) {
        calculateUsingTitleSize(titleSize)

        // Use our texture if the scale isn't 1.0
        useTexture = USE_SCALING_TEXTURE && titleScale != 1f

        if (useTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedTitleTexture()
        }

        ViewCompat.postInvalidateOnAnimation(view)
    }

    private fun setInterpolatedSubtitleSize(textSize: Float) {
        calculateUsingSubtitleSize(textSize)

        // Use our texture if the scale isn't 1.0
        useTexture = USE_SCALING_TEXTURE && subtitleScale != 1f

        if (useTexture) {
            // Make sure we have an expanded texture if needed
            ensureExpandedSubtitleTexture()
        }

        ViewCompat.postInvalidateOnAnimation(view)
    }

    // TODO: wtf is this for
    private infix fun Typeface?.isDifferentThan(other: Typeface?): Boolean =
        this != null && this != other || this == null && other != null

    // @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
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
            titleSize isClose _collapsedTitleSize -> {
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
                    titleSize isClose _expandedTitleSize -> 1f
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
            // Use linear text scaling if we're scaling the canvas
            titlePaint.isLinearText = titleScale != 1f

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            val title = TextUtils.ellipsize(
                title, titlePaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(title, titleToDraw)) {
                titleToDraw = title
                isRtl = titleToDraw!!.isRtl()
            }
        }
    }

    private fun ensureExpandedTitleTexture() {
        if (expandedTitleTexture != null || expandedBounds.isEmpty || titleToDraw.isNullOrBlank()) {
            return
        }

        calculateOffsets(0f)
        titleTextureAscent = titlePaint.ascent()
        titleTextureDescent = titlePaint.descent()

        val w = titlePaint.measureText(titleToDraw, 0, titleToDraw!!.length).roundToInt()
        val h = (titleTextureDescent - titleTextureAscent).roundToInt()

        if (w <= 0 || h <= 0) {
            return // If the width or height are 0, return
        }

        expandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val c = Canvas(expandedTitleTexture!!)
        c.drawText(titleToDraw!!, 0, titleToDraw!!.length, 0f, h - titlePaint.descent(), titlePaint)

        if (titleTexturePaint == null) {
            // Make sure we have a paint
            titleTexturePaint = Paint(ANTI_ALIAS_FLAG or FILTER_BITMAP_FLAG)
        }
    }

    // @SuppressWarnings("ReferenceEquality") // Matches the Typeface comparison in TextView
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
            subtitleSize isClose _collapsedSubtitleSize -> {
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
                    subtitleSize isClose _expandedSubtitleSize -> 1f
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
            // Use linear text scaling if we're scaling the canvas
            subtitlePaint.isLinearText = subtitleScale != 1f

            // If we don't currently have text to draw, or the text size has changed, ellipsize...
            val subtitle = TextUtils.ellipsize(
                subtitle, subtitlePaint, availableWidth, TextUtils.TruncateAt.END)
            if (!TextUtils.equals(subtitle, subtitleToDraw)) {
                subtitleToDraw = subtitle
                isRtl = subtitleToDraw!!.isRtl()
            }
        }
    }

    private fun ensureExpandedSubtitleTexture() {
        if (expandedSubtitleTexture != null || expandedBounds.isEmpty ||
            subtitleToDraw.isNullOrBlank()) {
            return
        }

        calculateOffsets(0f)
        subtitleTextureAscent = subtitlePaint.ascent()
        subtitleTextureDescent = subtitlePaint.descent()

        val w = subtitlePaint
            .measureText(subtitleToDraw, 0, subtitleToDraw!!.length).roundToInt()
        val h = (subtitleTextureDescent - subtitleTextureAscent).roundToInt()

        if (w <= 0 || h <= 0) {
            return // If the width or height are 0, return
        }

        expandedSubtitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val c = Canvas(expandedSubtitleTexture!!)
        c.drawText(subtitleToDraw!!, 0, subtitleToDraw!!.length, 0f, h - subtitlePaint.descent(),
            subtitlePaint)

        if (subtitleTexturePaint == null) {
            // Make sure we have a paint
            subtitleTexturePaint = Paint(ANTI_ALIAS_FLAG or FILTER_BITMAP_FLAG)
        }
    }

    fun recalculate() {
        if (view.height > 0 && view.width > 0) {
            // If we've already been laid out, calculate everything now otherwise we'll wait
            // until a layout
            calculateBaseOffsets()
            calculateCurrentOffsets()
        }
    }

    /** Set the title to display. */
    var title: CharSequence?
        get() = _title
        set(title) {
            if (title == null || title != _title) {
                _title = title
                titleToDraw = null
                clearTexture()
                recalculate()
            }
        }

    /** Set the subtitle to display. */
    var subtitle: CharSequence?
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
        expandedTitleTexture?.run {
            recycle()
            expandedTitleTexture = null
        }
        expandedSubtitleTexture?.run {
            recycle()
            expandedSubtitleTexture = null
        }
    }

    private companion object {

        // Pre-JB-MR2 doesn't support HW accelerated canvas scaled text so we will workaround it
        // by using our own texture
        val USE_SCALING_TEXTURE = SDK_INT < 18

        const val DEBUG_DRAW = false
        val DEBUG_DRAW_PAINT: Paint?

        init {
            DEBUG_DRAW_PAINT = if (DEBUG_DRAW) Paint() else null
            DEBUG_DRAW_PAINT?.run {
                isAntiAlias = true
                color = Color.MAGENTA
            }
        }

        /**
         * Returns true if [value] is 'close' to it's closest decimal value. Close is currently
         * defined as it's difference being < 0.001.
         */
        infix fun Float.isClose(value: Float) = abs(this - value) < 0.001f

        /**
         * Blend [color1] and [color2] using the given ratio.
         *
         * @param ratio of which to blend. 0.0 will return [color1], 0.5 will give an even blend,
         *     1.0 will return [color2.
         */
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
        ): Float = AnimationUtils.lerp(startValue, endValue,
            interpolator?.getInterpolation(fraction) ?: fraction)

        fun rectEquals(
            r: Rect,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int
        ): Boolean = !(r.left != left || r.top != top || r.right != right || r.bottom != bottom)
    }
}