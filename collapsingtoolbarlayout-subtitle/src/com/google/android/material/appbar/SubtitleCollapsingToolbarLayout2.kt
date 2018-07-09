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

package com.google.android.material.appbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import android.content.res.Configuration.SCREENLAYOUT_SIZE_UNDEFINED
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.ViewGroupUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.math.MathUtils
import androidx.core.util.ObjectsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.animation.AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
import com.google.android.material.animation.AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
import com.google.android.material.appbar.CollapsingToolbarLayout.getViewOffsetHelper
import com.google.android.material.internal.SubtitleCollapsingTextHelper2
import com.google.android.material.internal.ThemeEnforcement
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.R
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A [Toolbar] wrapper with subtitle support.
 * It is identical to [CollapsingToolbarLayout] and shares the same behavior and attributes, in
 * addition to its own attributes defined in [R.styleable.SubtitleCollapsingToolbarLayout].
 *
 * @see CollapsingToolbarLayout
 */
@Suppress("LeakingThis", "unused")
open class SubtitleCollapsingToolbarLayout2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var refreshToolbar = true
    private var toolbarId: Int
    private var toolbar: Toolbar? = null
    private var toolbarDirectChild: View? = null
    private var dummyView: View? = null

    private var expandedMarginStart: Int
    private var expandedMarginTop: Int
    private var expandedMarginEnd: Int
    private var expandedMarginBottom: Int

    private val tmpRect = Rect()
    internal val collapsingTextHelper = SubtitleCollapsingTextHelper2(this)
    private var collapsingTitleEnabled: Boolean
    private var drawCollapsingTitle = false

    private var _contentScrim: Drawable?
    internal var _statusBarScrim: Drawable?
    private var _scrimAlpha = 0
    private var scrimsAreShown = false
    private var scrimAnimator: ValueAnimator? = null
    private var _scrimAnimationDuration: Long
    private var _scrimVisibleHeightTrigger = -1

    private var onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener? = null

    internal var currentOffset = 0

    internal var lastInsets: WindowInsetsCompat? = null

    init {
        collapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR)

        val a = ThemeEnforcement.obtainStyledAttributes(context, attrs,
            R.styleable.SubtitleCollapsingToolbarLayout, defStyleAttr,
            R.style.Widget_Design_CollapsingToolbar_Subtitle)

        collapsingTextHelper.expandedTextGravity = a.getInt(
            R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleGravity,
            GravityCompat.START or Gravity.BOTTOM)
        collapsingTextHelper.collapsedTextGravity = a.getInt(
            R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleGravity,
            GravityCompat.START or Gravity.CENTER_VERTICAL)

        expandedMarginBottom = a.getDimensionPixelSize(
            R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMargin, 0)
        expandedMarginEnd = expandedMarginBottom
        expandedMarginTop = expandedMarginEnd
        expandedMarginStart = expandedMarginTop

        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart)) {
            expandedMarginStart = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd)) {
            expandedMarginEnd = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop)) {
            expandedMarginTop = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom)) {
            expandedMarginBottom = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom, 0)
        }

        collapsingTitleEnabled = a.getBoolean(
            R.styleable.SubtitleCollapsingToolbarLayout_titleEnabled, true)
        collapsingTextHelper.title = a.getText(R.styleable.SubtitleCollapsingToolbarLayout_title)
        collapsingTextHelper.subtitle = a.getText(
            R.styleable.SubtitleCollapsingToolbarLayout_subtitle)

        // First load the default title appearances
        collapsingTextHelper.setExpandedTitleTextAppearance(
            R.style.TextAppearance_Design_CollapsingToolbar_Expanded)
        collapsingTextHelper.setCollapsedTitleTextAppearance(
            androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title)

        // Now overlay any custom subtitle appearances
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance)) {
            collapsingTextHelper.setExpandedTitleTextAppearance(a.getResourceId(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance, 0))
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance)) {
            collapsingTextHelper.setCollapsedTitleTextAppearance(a.getResourceId(
                R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance, 0))
        }

        // First load the default title appearances
        collapsingTextHelper.setExpandedSubtitleTextAppearance(a.getResourceId(
            R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance, 0))
        collapsingTextHelper.setCollapsedSubtitleTextAppearance(a.getResourceId(
            R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance, 0))

        // Now overlay any custom subtitle appearances
        if (a.hasValue(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance)) {
            collapsingTextHelper.setExpandedSubtitleTextAppearance(a.getResourceId(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance, 0))
        }
        if (a.hasValue(
                R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance)) {
            collapsingTextHelper.setCollapsedSubtitleTextAppearance(a.getResourceId(
                R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance, 0))
        }

        _scrimVisibleHeightTrigger = a.getDimensionPixelSize(
            R.styleable.SubtitleCollapsingToolbarLayout_scrimVisibleHeightTrigger, -1)
        _scrimAnimationDuration = a.getInt(
            R.styleable.SubtitleCollapsingToolbarLayout_scrimAnimationDuration,
            DEFAULT_SCRIM_ANIMATION_DURATION).toLong()

        _contentScrim = a.getDrawable(R.styleable.SubtitleCollapsingToolbarLayout_contentScrim)
        _statusBarScrim = a.getDrawable(R.styleable.SubtitleCollapsingToolbarLayout_statusBarScrim)

        toolbarId = a.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_toolbarId, -1)

        a.recycle()
        setWillNotDraw(false)
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            onWindowInsetChanged(insets)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val parent = parent
        if (parent is AppBarLayout) {
            @Suppress("DEPRECATION") ViewCompat.setFitsSystemWindows(this,
                ViewCompat.getFitsSystemWindows(parent as View))
            if (onOffsetChangedListener == null) {
                onOffsetChangedListener = OffsetUpdateListener()
            }
            parent.addOnOffsetChangedListener(onOffsetChangedListener)
            ViewCompat.requestApplyInsets(this)
        }
    }

    override fun onDetachedFromWindow() {
        val parent = parent
        if (onOffsetChangedListener != null && parent is AppBarLayout) {
            parent.removeOnOffsetChangedListener(onOffsetChangedListener)
        }
        super.onDetachedFromWindow()
    }

    internal open fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null
        if (ViewCompat.getFitsSystemWindows(this)) {
            newInsets = insets
        }
        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets
            requestLayout()
        }
        return insets.consumeSystemWindowInsets()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        ensureToolbar()
        if (toolbar == null && _contentScrim != null && _scrimAlpha > 0) {
            _contentScrim!!.mutate().alpha = _scrimAlpha
            _contentScrim!!.draw(canvas)
        }
        if (collapsingTitleEnabled && drawCollapsingTitle) {
            collapsingTextHelper.draw(canvas)
        }
        if (_statusBarScrim != null && _scrimAlpha > 0) {
            val topInset = lastInsets?.systemWindowInsetTop ?: 0
            if (topInset > 0) {
                _statusBarScrim!!.setBounds(0, -currentOffset, width, topInset - currentOffset)
                _statusBarScrim!!.mutate().alpha = _scrimAlpha
                _statusBarScrim!!.draw(canvas)
            }
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        var invalidated = false
        if (_contentScrim != null && _scrimAlpha > 0 && child.isToolbarChild()) {
            _contentScrim!!.mutate().alpha = _scrimAlpha
            _contentScrim!!.draw(canvas)
            invalidated = true
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _contentScrim?.setBounds(0, 0, w, h)
    }

    private fun ensureToolbar() {
        if (!refreshToolbar) {
            return
        }
        toolbar = null
        toolbarDirectChild = null
        if (toolbarId != -1) {
            toolbar = findViewById(toolbarId)
            if (toolbar != null) {
                toolbarDirectChild = toolbar!!.findDirectChild()
            }
        }
        if (toolbar == null) {
            var toolbar: Toolbar? = null
            var i = 0
            val count = childCount
            while (i < count) {
                val child = getChildAt(i)
                if (child is Toolbar) {
                    toolbar = child
                    break
                }
                i++
            }
            this.toolbar = toolbar
        }
        updateDummyView()
        refreshToolbar = false
    }

    private fun View.isToolbarChild(): Boolean = this == when (toolbarDirectChild) {
        null, this@SubtitleCollapsingToolbarLayout2 -> toolbar
        else -> toolbarDirectChild
    }

    private fun View.findDirectChild(): View {
        var directChild = this
        var p: ViewParent? = parent
        while (p != this@SubtitleCollapsingToolbarLayout2 && p != null) {
            if (p is View) {
                directChild = p
            }
            p = p.parent
        }
        return directChild
    }

    private fun updateDummyView() {
        if (!collapsingTitleEnabled && dummyView != null) {
            val parent = dummyView!!.parent
            (parent as? ViewGroup)?.removeView(dummyView)
        }
        if (collapsingTitleEnabled && toolbar != null) {
            if (dummyView == null) {
                dummyView = View(context)
            }
            if (dummyView!!.parent == null) {
                toolbar!!.addView(dummyView, MATCH_PARENT, MATCH_PARENT)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        ensureToolbar()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val topInset = lastInsets?.systemWindowInsetTop ?: 0
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
            super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(measuredHeight + topInset, MeasureSpec.EXACTLY))
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (lastInsets != null) {
            val insetTop = lastInsets!!.systemWindowInsetTop
            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.top < insetTop) {
                        ViewCompat.offsetTopAndBottom(child, insetTop)
                    }
                }
                i++
            }
        }

        if (collapsingTitleEnabled && dummyView != null) {
            drawCollapsingTitle = ViewCompat.isAttachedToWindow(dummyView!!) &&
                dummyView!!.visibility == VISIBLE
            if (drawCollapsingTitle) {
                val isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
                val maxOffset = (toolbarDirectChild ?: toolbar)!!.maxOffsetForPinChild
                ViewGroupUtils.getDescendantRect(this, dummyView, tmpRect)
                collapsingTextHelper.setCollapsedBounds(
                    tmpRect.left + when {
                        isRtl -> toolbar!!.titleMarginEnd
                        else -> toolbar!!.titleMarginStart
                    },
                    tmpRect.top + maxOffset + toolbar!!.titleMarginTop,
                    tmpRect.right + when {
                        isRtl -> toolbar!!.titleMarginStart
                        else -> toolbar!!.titleMarginEnd
                    },
                    tmpRect.bottom + maxOffset - toolbar!!.titleMarginBottom)
                collapsingTextHelper.setExpandedBounds(
                    when {
                        isRtl -> expandedMarginEnd
                        else -> expandedMarginStart
                    },
                    tmpRect.top + expandedMarginTop,
                    right - left - when {
                        isRtl -> expandedMarginStart
                        else -> expandedMarginEnd
                    },
                    bottom - top - expandedMarginBottom)
                collapsingTextHelper.recalculate()
            }
        }

        var i = 0
        val z = childCount
        while (i < z) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout()
            i++
        }
        if (toolbar != null) {
            if (collapsingTitleEnabled && TextUtils.isEmpty(collapsingTextHelper.title)) {
                collapsingTextHelper.title = toolbar!!.title
            }
            minimumHeight = when (toolbarDirectChild) {
                null, this -> toolbar!!.heightWithMargins
                else -> toolbarDirectChild!!.heightWithMargins
            }
        }
        updateScrimVisibility()
    }

    /**
     * Title of this toolbar layout.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_title
     */
    open var title: CharSequence?
        get() = if (collapsingTitleEnabled) collapsingTextHelper.title else null
        set(title) {
            collapsingTextHelper.title = title
        }

    /**
     * Subtitle of this toolbar layout.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_subtitle
     */
    open var subtitle: CharSequence?
        get() = if (collapsingTitleEnabled) collapsingTextHelper.subtitle else null
        set(subtitle) {
            collapsingTextHelper.subtitle = subtitle
        }

    /**
     * Checks whether this view should display its own title, also affects subtitle.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_titleEnabled
     */
    open var isTitleEnabled: Boolean
        get() = collapsingTitleEnabled
        set(enabled) {
            if (enabled != collapsingTitleEnabled) {
                collapsingTitleEnabled = enabled
                updateDummyView()
                requestLayout()
            }
        }

    /** Sets whether the content scrim should be shown. */
    @JvmOverloads open fun setScrimsShown(
        shown: Boolean,
        animate: Boolean = ViewCompat.isLaidOut(this) && !isInEditMode
    ) {
        if (scrimsAreShown != shown) {
            when {
                animate -> animateScrim(if (shown) 0xFF else 0x0)
                else -> scrimAlpha = if (shown) 0xFF else 0x0
            }
            scrimsAreShown = shown
        }
    }

    private fun animateScrim(targetAlpha: Int) {
        ensureToolbar()
        when {
            scrimAnimator == null -> {
                scrimAnimator = ValueAnimator()
                scrimAnimator!!.duration = _scrimAnimationDuration
                scrimAnimator!!.interpolator = when {
                    targetAlpha > scrimAlpha -> FAST_OUT_LINEAR_IN_INTERPOLATOR
                    else -> LINEAR_OUT_SLOW_IN_INTERPOLATOR
                }
                scrimAnimator!!.addUpdateListener { animator ->
                    scrimAlpha = animator.animatedValue as Int
                }
            }
            scrimAnimator!!.isRunning -> scrimAnimator!!.cancel()
        }
        scrimAnimator!!.setIntValues(_scrimAlpha, targetAlpha)
        scrimAnimator!!.start()
    }

    internal open var scrimAlpha: Int
        get() = _scrimAlpha
        set(alpha) {
            if (alpha != _scrimAlpha) {
                val contentScrim = _contentScrim
                if (contentScrim != null && toolbar != null) {
                    ViewCompat.postInvalidateOnAnimation(toolbar!!)
                }
                _scrimAlpha = alpha
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

    /**
     * Drawable to use for the content scrim from resources
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_contentScrim
     */
    open var contentScrim: Drawable?
        get() = _contentScrim
        set(drawable) {
            if (_contentScrim != drawable) {
                _contentScrim?.callback = null
                _contentScrim = drawable?.mutate()
                _contentScrim?.let {
                    it.setBounds(0, 0, width, height)
                    it.callback = this
                    it.alpha = _scrimAlpha
                }
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

    /**
     * Set color to use as content scrim.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_contentScrim
     */
    open fun setContentScrimColor(@ColorInt color: Int) {
        contentScrim = ColorDrawable(color)
    }

    /**
     * Set resource to use as content scrim.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_contentScrim
     */
    open fun setContentScrimResource(@DrawableRes resId: Int) {
        contentScrim = ContextCompat.getDrawable(context, resId)
    }

    /**
     * Drawable to use for the status bar scrim from resources
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_statusBarScrim
     */
    open var statusBarScrim: Drawable?
        get() = _statusBarScrim
        set(drawable) {
            if (_statusBarScrim != drawable) {
                _statusBarScrim?.callback = null
                _statusBarScrim = drawable?.mutate()
                _statusBarScrim?.let {
                    if (it.isStateful) {
                        it.state = drawableState
                    }
                    DrawableCompat.setLayoutDirection(it, ViewCompat.getLayoutDirection(this))
                    it.setVisible(visibility == VISIBLE, false)
                    it.callback = this
                    it.alpha = _scrimAlpha
                }
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }

    /**
     * Set resource to use as status bar scrim.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_statusBarScrim
     */
    open fun setStatusBarScrimColor(@ColorInt color: Int) {
        statusBarScrim = ColorDrawable(color)
    }

    /**
     * Set resource to use as status bar scrim.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_statusBarScrim
     */
    open fun setStatusBarScrimResource(@DrawableRes resId: Int) {
        statusBarScrim = ContextCompat.getDrawable(context, resId)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val state = drawableState
        var changed = false
        var d = _statusBarScrim
        if (d != null && d.isStateful) {
            changed = d.setState(state)
        }
        d = _contentScrim
        if (d != null && d.isStateful) {
            changed = changed or d.setState(state)
        }
        @Suppress("SENSELESS_COMPARISON")
        if (collapsingTextHelper != null) {
            changed = changed or collapsingTextHelper.setState(state)
        }
        if (changed) {
            invalidate()
        }
    }

    override fun verifyDrawable(who: Drawable) = super.verifyDrawable(who) ||
        who == _contentScrim ||
        who == _statusBarScrim

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        val visible = visibility == VISIBLE
        if (_statusBarScrim != null && _statusBarScrim!!.isVisible != visible) {
            _statusBarScrim!!.setVisible(visible, false)
        }
        if (_contentScrim != null && _contentScrim!!.isVisible != visible) {
            _contentScrim!!.setVisible(visible, false)
        }
    }

    /**
     * Sets text color and size for the collapsed title.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance
     */
    open fun setCollapsedTitleTextAppearance(@StyleRes resId: Int) =
        collapsingTextHelper.setCollapsedTitleTextAppearance(resId)

    /**
     * Sets text color and size for the collapsed subtitle.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance
     */
    open fun setCollapsedSubtitleTextAppearance(@StyleRes resId: Int) =
        collapsingTextHelper.setCollapsedSubtitleTextAppearance(resId)

    /** Sets text color of the collapsed title from color state list. */
    open fun setCollapsedTitleTextColor(colors: ColorStateList) {
        collapsingTextHelper.collapsedTitleColor = colors
    }

    /** Sets text color of the collapsed title from color resource. */
    open fun setCollapsedTitleTextColor(@ColorInt color: Int) =
        setCollapsedTitleTextColor(ColorStateList.valueOf(color))

    /** Sets text color of the collapsed subtitle from color state list. */
    open fun setCollapsedSubtitleTextColor(colors: ColorStateList) {
        collapsingTextHelper.collapsedSubtitleColor = colors
    }

    /** Sets text color of the collapsed subtitle from color resource. */
    open fun setCollapsedSubtitleTextColor(@ColorInt color: Int) =
        setCollapsedSubtitleTextColor(ColorStateList.valueOf(color))

    /**
     * Horizontal and vertical alignment for title when collapsed.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleGravity
     */
    open var collapsedTitleGravity: Int
        get() = collapsingTextHelper.collapsedTextGravity
        set(gravity) {
            collapsingTextHelper.collapsedTextGravity = gravity
        }

    /**
     * Sets text color and size for the expanded title.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance
     */
    open fun setExpandedTitleTextAppearance(@StyleRes resId: Int) =
        collapsingTextHelper.setExpandedTitleTextAppearance(resId)

    /**
     * Sets text color and size for the expanded subtitle.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance
     */
    open fun setExpandedSubtitleTextAppearance(@StyleRes resId: Int) =
        collapsingTextHelper.setExpandedSubtitleTextAppearance(resId)

    /** Sets text color of the expanded title from color state list. */
    open fun setExpandedTitleTextColor(colors: ColorStateList) {
        collapsingTextHelper.expandedTitleColor = colors
    }

    /** Sets text color of the expanded title from color resource. */
    open fun setExpandedTitleTextColor(@ColorInt color: Int) =
        setExpandedTitleTextColor(ColorStateList.valueOf(color))

    /** Sets text color of the expanded subtitle from color state list. */
    open fun setExpandedSubtitleTextColor(colors: ColorStateList) {
        collapsingTextHelper.expandedSubtitleColor = colors
    }

    /** Sets text color of the expanded subtitle from color resource. */
    open fun setExpandedSubtitleTextColor(@ColorInt color: Int) =
        setExpandedSubtitleTextColor(ColorStateList.valueOf(color))

    /**
     * Horizontal and vertical alignment for title when expanded.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleGravity
     */
    open var expandedTitleGravity: Int
        get() = collapsingTextHelper.expandedTextGravity
        set(gravity) {
            collapsingTextHelper.expandedTextGravity = gravity
        }

    /** Typeface used for the collapsed title. */
    open var collapsedTitleTypeface: Typeface
        get() = collapsingTextHelper.collapsedTitleTypeface
        set(typeface) {
            collapsingTextHelper.collapsedTitleTypeface = typeface
        }

    /** Typeface used for the collapsed subtitle. */
    open var collapsedSubtitleTypeface: Typeface
        get() = collapsingTextHelper.collapsedSubtitleTypeface
        set(typeface) {
            collapsingTextHelper.collapsedSubtitleTypeface = typeface
        }

    /** Typeface used for the expanded title. */
    open var expandedTitleTypeface: Typeface
        get() = collapsingTextHelper.expandedTitleTypeface
        set(typeface) {
            collapsingTextHelper.expandedTitleTypeface = typeface
        }

    /** Typeface used for the expanded subtitle. */
    open var expandedSubtitleTypeface: Typeface
        get() = collapsingTextHelper.expandedSubtitleTypeface
        set(typeface) {
            collapsingTextHelper.expandedSubtitleTypeface = typeface
        }

    /** Sets the expanded title margins. */
    open fun setExpandedTitleMargin(start: Int, top: Int, end: Int, bottom: Int) {
        expandedMarginStart = start
        expandedMarginTop = top
        expandedMarginEnd = end
        expandedMarginBottom = bottom
        requestLayout()
    }

    /**
     * Starting expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart
     */
    open var expandedTitleMarginStart: Int
        get() = expandedMarginStart
        set(margin) {
            expandedMarginStart = margin
            requestLayout()
        }

    /**
     * Top expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop
     */
    open var expandedTitleMarginTop: Int
        get() = expandedMarginTop
        set(margin) {
            expandedMarginTop = margin
            requestLayout()
        }

    /**
     * Ending expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd
     */
    open var expandedTitleMarginEnd: Int
        get() = expandedMarginEnd
        set(margin) {
            expandedMarginEnd = margin
            requestLayout()
        }

    /**
     * Bottom expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom
     */
    open var expandedTitleMarginBottom: Int
        get() = expandedMarginBottom
        set(margin) {
            expandedMarginBottom = margin
            requestLayout()
        }

    /**
     * Set the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_scrimVisibleHeightTrigger
     */
    open var scrimVisibleHeightTrigger: Int
        get() {
            if (_scrimVisibleHeightTrigger >= 0) {
                return _scrimVisibleHeightTrigger
            }
            val insetTop = lastInsets?.systemWindowInsetTop ?: 0
            val minHeight = ViewCompat.getMinimumHeight(this)
            if (minHeight > 0) return min(minHeight * 2 + insetTop, height)
            return height / 3
        }
        set(@IntRange(from = 0) height) {
            if (_scrimVisibleHeightTrigger != height) {
                _scrimVisibleHeightTrigger = height
                updateScrimVisibility()
            }
        }

    /**
     * Set the duration used for scrim visibility animations.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_scrimAnimationDuration
     */
    open var scrimAnimationDuration: Long
        get() = _scrimAnimationDuration
        set(@IntRange(from = 0) duration) {
            _scrimAnimationDuration = duration
        }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean = p is LayoutParams

    override fun generateDefaultLayoutParams(): LayoutParams =
        LayoutParams(MATCH_PARENT, MATCH_PARENT)

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams =
        LayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams =
        LayoutParams(p)

    open class LayoutParams : CollapsingToolbarLayout.LayoutParams {
        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(p: ViewGroup.LayoutParams) : super(p)
        constructor(source: ViewGroup.MarginLayoutParams) : super(source)
        @RequiresApi(19) constructor(source: FrameLayout.LayoutParams) : super(source)
    }

    internal fun updateScrimVisibility() {
        if (_contentScrim != null || _statusBarScrim != null) {
            setScrimsShown(height + currentOffset < scrimVisibleHeightTrigger)
        }
    }

    internal val View.maxOffsetForPinChild: Int
        get() {
            val offsetHelper = getViewOffsetHelper(this)
            val lp = layoutParams as LayoutParams
            return this@SubtitleCollapsingToolbarLayout2.height -
                offsetHelper.layoutTop - height - lp.bottomMargin
        }

    private inner class OffsetUpdateListener : AppBarLayout.OnOffsetChangedListener {
        override fun onOffsetChanged(layout: AppBarLayout, verticalOffset: Int) {
            currentOffset = verticalOffset
            val insetTop = lastInsets?.systemWindowInsetTop ?: 0
            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                val offsetHelper = getViewOffsetHelper(child)
                when (lp.collapseMode) {
                    COLLAPSE_MODE_PIN -> offsetHelper.topAndBottomOffset = MathUtils.clamp(
                        -verticalOffset, 0, child.maxOffsetForPinChild)
                    COLLAPSE_MODE_PARALLAX -> offsetHelper.topAndBottomOffset =
                        (-verticalOffset * lp.parallaxMultiplier).roundToInt()
                }
                i++
            }
            updateScrimVisibility()
            if (_statusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(this@SubtitleCollapsingToolbarLayout2)
            }
            val expandRange = height -
                ViewCompat.getMinimumHeight(this@SubtitleCollapsingToolbarLayout2) - insetTop
            collapsingTextHelper.expansionFraction = abs(verticalOffset) / expandRange.toFloat()
        }
    }

    private companion object {

        const val DEFAULT_SCRIM_ANIMATION_DURATION = 600

        val View.heightWithMargins: Int
            get() {
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    val mlp = layoutParams as ViewGroup.MarginLayoutParams
                    return height + mlp.topMargin + mlp.bottomMargin
                }
                return height
            }

        fun Context.isScreenSizeAtLeast(size: Int): Boolean {
            val screenSize = resources.configuration.screenLayout and SCREENLAYOUT_SIZE_MASK
            return screenSize != SCREENLAYOUT_SIZE_UNDEFINED && screenSize >= size
        }
    }
}