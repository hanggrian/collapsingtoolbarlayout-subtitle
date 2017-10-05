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

@file:JvmName("SubtitleCollapsingToolbarLayout")

package android.support.design.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.*
import android.support.annotation.IntRange
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.WindowInsetsCompat
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.hendraanggrian.collapsingtoolbarlayout.subtitle.R
import isScreenSizeAtLeast

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 * *
 * @see CollapsingToolbarLayout
 */
class SubtitleCollapsingToolbarLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mRefreshToolbar = true
    private var mToolbarId: Int
    private var mToolbar: Toolbar? = null
    private var mToolbarDirectChild: View? = null
    private var mDummyView: View? = null

    private var mExpandedMarginStart = 0
    private var mExpandedMarginTop = 0
    private var mExpandedMarginEnd = 0
    private var mExpandedMarginBottom = 0

    private val mTmpRect = Rect()
    internal val mCollapsingTextHelper: SubtitleCollapsingTextHelper?
    private var mCollapsingTitleEnabled = false
    private var mDrawCollapsingTitle = false

    var contentScrim: Drawable? = null
        set(value) {
            if (field != value) {
                if (field != null) {
                    field!!.callback = null
                }
                field = value?.mutate()
                if (field != null) {
                    field!!.setBounds(0, 0, width, height)
                    field!!.callback = this
                    field!!.alpha = scrimAlpha
                }
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    var statusBarScrim: Drawable? = null
        set(value) {
            if (field != value) {
                if (field != null) {
                    field!!.callback = null
                }
                field = value?.mutate()
                if (field != null) {
                    if (field!!.isStateful) {
                        field!!.state = drawableState
                    }
                    DrawableCompat.setLayoutDirection(field!!, ViewCompat.getLayoutDirection(this))
                    field!!.setVisible(visibility == View.VISIBLE, false)
                    field!!.callback = this
                    field!!.alpha = scrimAlpha
                }
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    internal var scrimAlpha: Int = 0
        set(value) {
            if (value != scrimAlpha) {
                val contentScrim = this.contentScrim
                if (contentScrim != null && mToolbar != null) {
                    ViewCompat.postInvalidateOnAnimation(mToolbar)
                }
                field = value
                ViewCompat.postInvalidateOnAnimation(this@SubtitleCollapsingToolbarLayout)
            }
        }
    private var mScrimsAreShown = false
    private var mScrimAnimator: ValueAnimator? = null
    var scrimAnimationDuration = 0L
    private var mScrimVisibleHeightTrigger = -1

    private var mOnOffsetChangedListener: AppBarLayout.OnOffsetChangedListener? = null

    internal var mCurrentOffset = 0

    internal var mLastInsets: WindowInsetsCompat? = null

    private var useCorrectPadding = false

    init {
        ThemeUtils.checkAppCompatTheme(context)

        mCollapsingTextHelper = SubtitleCollapsingTextHelper(this)
        mCollapsingTextHelper.textSizeInterpolator = AnimationUtils.DECELERATE_INTERPOLATOR

        val a = context.obtainStyledAttributes(attrs, R.styleable.SubtitleCollapsingToolbarLayout, defStyleAttr, R.style.Widget_Design_CollapsingToolbar_Subtitle)

        mCollapsingTextHelper.expandedTextGravity = a.getInt(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleGravity, GravityCompat.START or Gravity.BOTTOM)
        mCollapsingTextHelper.collapsedTextGravity = a.getInt(R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleGravity, GravityCompat.START or Gravity.CENTER_VERTICAL)

        mExpandedMarginBottom = a.getDimensionPixelSize(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMargin, 0)
        mExpandedMarginEnd = mExpandedMarginBottom
        mExpandedMarginTop = mExpandedMarginEnd
        mExpandedMarginStart = mExpandedMarginTop

        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart)) {
            mExpandedMarginStart = a.getDimensionPixelSize(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd)) {
            mExpandedMarginEnd = a.getDimensionPixelSize(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop)) {
            mExpandedMarginTop = a.getDimensionPixelSize(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = a.getDimensionPixelSize(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom, 0)
        }

        mCollapsingTitleEnabled = a.getBoolean(R.styleable.SubtitleCollapsingToolbarLayout_titleEnabled, true)
        mCollapsingTextHelper.title = a.getText(R.styleable.SubtitleCollapsingToolbarLayout_title)
        mCollapsingTextHelper.subtitle = a.getText(R.styleable.SubtitleCollapsingToolbarLayout_subtitle)

        // First load the default text appearances
        mCollapsingTextHelper.setExpandedTitleAppearance(R.style.TextAppearance_Design_CollapsingToolbar_Expanded)
        mCollapsingTextHelper.setCollapsedTitleAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title)

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance)) {
            mCollapsingTextHelper.setExpandedTitleAppearance(a.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance, 0))
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance)) {
            mCollapsingTextHelper.setCollapsedTitleAppearance(a.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance, 0))
        }

        mCollapsingTextHelper.setExpandedSubtitleAppearance(a.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance, 0))
        mCollapsingTextHelper.setCollapsedSubtitleAppearance(a.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance, 0))

        mScrimVisibleHeightTrigger = a.getDimensionPixelSize(R.styleable.SubtitleCollapsingToolbarLayout_scrimVisibleHeightTrigger, -1)
        scrimAnimationDuration = a.getInt(R.styleable.SubtitleCollapsingToolbarLayout_scrimAnimationDuration, DEFAULT_SCRIM_ANIMATION_DURATION).toLong()

        contentScrim = a.getDrawable(R.styleable.SubtitleCollapsingToolbarLayout_contentScrim)
        statusBarScrim = a.getDrawable(R.styleable.SubtitleCollapsingToolbarLayout_statusBarScrim)

        mToolbarId = a.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_toolbarId, -1)

        useCorrectPadding = a.getBoolean(R.styleable.SubtitleCollapsingToolbarLayout_useCorrectPadding, false)

        a.recycle()

        setWillNotDraw(false)

        ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets -> onWindowInsetChanged(insets) }
    }

    var title: CharSequence?
        get() = if (mCollapsingTitleEnabled) mCollapsingTextHelper!!.title else null
        set(value) {
            mCollapsingTextHelper!!.title = value
        }

    var subtitle: CharSequence?
        get() = if (mCollapsingTitleEnabled) mCollapsingTextHelper!!.subtitle else null
        set(value) {
            mCollapsingTextHelper!!.subtitle = value
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val parent = parent
        if (parent is AppBarLayout) {
            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows(parent as View))
            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = OffsetUpdateListener()
            }
            parent.addOnOffsetChangedListener(mOnOffsetChangedListener)
            ViewCompat.requestApplyInsets(this)
        }
    }

    override fun onDetachedFromWindow() {
        val parent = parent
        if (mOnOffsetChangedListener != null && parent is AppBarLayout) {
            parent.removeOnOffsetChangedListener(mOnOffsetChangedListener)
        }
        super.onDetachedFromWindow()
    }

    internal fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null
        if (ViewCompat.getFitsSystemWindows(this)) {
            newInsets = insets
        }
        if (mLastInsets == newInsets) {
            mLastInsets = newInsets
            requestLayout()
        }
        return insets.consumeSystemWindowInsets()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        ensureToolbar()
        if (mToolbar == null && contentScrim != null && scrimAlpha > 0) {
            contentScrim!!.mutate().alpha = scrimAlpha
            contentScrim!!.draw(canvas)
        }
        if (mCollapsingTitleEnabled && mDrawCollapsingTitle) {
            mCollapsingTextHelper!!.draw(canvas)
        }
        if (statusBarScrim != null && scrimAlpha > 0) {
            val topInset = if (mLastInsets != null) mLastInsets!!.systemWindowInsetTop else 0
            if (topInset > 0) {
                statusBarScrim!!.setBounds(0, -mCurrentOffset, width, topInset - mCurrentOffset)
                statusBarScrim!!.mutate().alpha = scrimAlpha
                statusBarScrim!!.draw(canvas)
            }
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        var invalidated = false
        if (contentScrim != null && scrimAlpha > 0 && isToolbarChild(child)) {
            contentScrim!!.mutate().alpha = scrimAlpha
            contentScrim!!.draw(canvas)
            invalidated = true
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (contentScrim != null) {
            contentScrim!!.setBounds(0, 0, w, h)
        }
    }

    private fun ensureToolbar() {
        if (!mRefreshToolbar) return
        mToolbar = null
        mToolbarDirectChild = null
        if (mToolbarId != -1) {
            mToolbar = findViewById<Toolbar>(mToolbarId)
            if (mToolbar != null) {
                mToolbarDirectChild = findDirectChild(mToolbar!!)
            }
        }
        if (mToolbar == null) {
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
            mToolbar = toolbar
        }
        updateDummyView()
        mRefreshToolbar = false
    }

    private fun isToolbarChild(child: View) =
            if (mToolbarDirectChild == null || mToolbarDirectChild == this) child == mToolbar
            else child == mToolbarDirectChild

    private fun findDirectChild(descendant: View): View {
        var directChild = descendant
        var p = descendant.parent
        while (p != this && p != null) {
            if (p is View) {
                directChild = p
            }
            p = p.parent
        }
        return directChild
    }

    private fun updateDummyView() {
        if (!mCollapsingTitleEnabled && mDummyView != null) {
            val parent = mDummyView!!.parent
            (parent as? ViewGroup)?.removeView(mDummyView)
        }
        if (mCollapsingTitleEnabled && mToolbar != null) {
            if (mDummyView == null) {
                mDummyView = View(context)
            }
            if (mDummyView!!.parent == null) {
                mToolbar!!.addView(mDummyView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        ensureToolbar()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mLastInsets != null) {
            val insetTop = mLastInsets!!.systemWindowInsetTop
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
        if (mCollapsingTitleEnabled && mDummyView != null) {
            mDrawCollapsingTitle = ViewCompat.isAttachedToWindow(mDummyView) && mDummyView!!.visibility == View.VISIBLE
            if (mDrawCollapsingTitle) {
                val isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
                val maxOffset = getMaxOffsetForPinChild(if (mToolbarDirectChild != null) mToolbarDirectChild!! else mToolbar!!)
                ViewGroupUtils.getDescendantRect(this, mDummyView!!, mTmpRect)
                var collapsedBoundsLeft = mTmpRect.left +
                        if (isRtl) mToolbar!!.titleMarginEnd
                        else mToolbar!!.titleMarginStart
                var collapsedBoundsRight = mTmpRect.right +
                        if (isRtl) mToolbar!!.titleMarginStart
                        else mToolbar!!.titleMarginEnd
                if (useCorrectPadding && mToolbar!!.menu != null && !mToolbar!!.menu.hasVisibleItems()) {
                    val padding = context.resources.getDimensionPixelSize(
                            if (context.isScreenSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) R.dimen.appbar_horizontal_padding_large
                            else R.dimen.appbar_horizontal_padding)
                    if (isRtl) collapsedBoundsLeft += padding
                    else collapsedBoundsRight -= padding
                }
                mCollapsingTextHelper!!.collapsedBounds = Rect(
                        collapsedBoundsLeft,
                        mTmpRect.top + maxOffset + mToolbar!!.titleMarginTop,
                        collapsedBoundsRight,
                        mTmpRect.bottom + maxOffset - mToolbar!!.titleMarginBottom
                )
                mCollapsingTextHelper.expandedBounds = Rect(
                        if (isRtl) mExpandedMarginEnd else mExpandedMarginStart,
                        mTmpRect.top + mExpandedMarginTop,
                        right - left - if (isRtl) mExpandedMarginStart else mExpandedMarginEnd,
                        bottom - top - mExpandedMarginBottom
                )
                mCollapsingTextHelper.recalculate()
            }
        }
        var i = 0
        val z = childCount
        while (i < z) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout()
            i++
        }
        if (mToolbar != null) {
            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper!!.title)) {
                mCollapsingTextHelper.title = mToolbar!!.title
            }
            if (mToolbarDirectChild == null || mToolbarDirectChild === this) {
                minimumHeight = getHeightWithMargins(mToolbar!!)
            } else {
                minimumHeight = getHeightWithMargins(mToolbarDirectChild!!)
            }
        }
        updateScrimVisibility()
    }

    fun setUseCorrectPadding(useCorrectPadding: Boolean) {
        this.useCorrectPadding = useCorrectPadding
        requestLayout()
    }

    var isTitleEnabled: Boolean
        get() = mCollapsingTitleEnabled
        set(enabled) {
            if (enabled != mCollapsingTitleEnabled) {
                mCollapsingTitleEnabled = enabled
                updateDummyView()
                requestLayout()
            }
        }

    @JvmOverloads
    fun setScrimsShown(shown: Boolean, animate: Boolean = ViewCompat.isLaidOut(this) && !isInEditMode) {
        if (mScrimsAreShown != shown) {
            if (animate) {
                animateScrim(if (shown) 0xFF else 0x0)
            } else {
                scrimAlpha = if (shown) 0xFF else 0x0
            }
            mScrimsAreShown = shown
        }
    }

    private fun animateScrim(targetAlpha: Int) {
        ensureToolbar()
        if (mScrimAnimator == null) {
            mScrimAnimator = ValueAnimator()
            mScrimAnimator!!.duration = scrimAnimationDuration
            mScrimAnimator!!.interpolator = if (targetAlpha > scrimAlpha)
                AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
            else
                AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR
            mScrimAnimator!!.addUpdateListener { animator -> scrimAlpha = animator.animatedValue as Int }
        } else if (mScrimAnimator!!.isRunning) {
            mScrimAnimator!!.cancel()
        }
        mScrimAnimator!!.setIntValues(scrimAlpha, targetAlpha)
        mScrimAnimator!!.start()
    }

    fun setContentScrimColor(@ColorInt color: Int) {
        contentScrim = ColorDrawable(color)
    }

    fun setContentScrimResource(@DrawableRes resId: Int) {
        contentScrim = ContextCompat.getDrawable(context, resId)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val state = drawableState
        var changed = false
        var d = statusBarScrim
        if (d != null && d.isStateful) {
            changed = d.setState(state)
        }
        d = contentScrim
        if (d != null && d.isStateful) {
            changed = changed or d.setState(state)
        }
        if (mCollapsingTextHelper != null) {
            changed = changed or mCollapsingTextHelper.setState(state)
        }
        if (changed) {
            invalidate()
        }
    }

    override fun verifyDrawable(who: Drawable) = super.verifyDrawable(who) || who === contentScrim || who === statusBarScrim

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        val visible = visibility == View.VISIBLE
        if (statusBarScrim != null && statusBarScrim!!.isVisible != visible) {
            statusBarScrim!!.setVisible(visible, false)
        }
        if (contentScrim != null && contentScrim!!.isVisible != visible) {
            contentScrim!!.setVisible(visible, false)
        }
    }

    fun setStatusBarScrimColor(@ColorInt color: Int) {
        statusBarScrim = ColorDrawable(color)
    }

    fun setStatusBarScrimResource(@DrawableRes resId: Int) {
        statusBarScrim = ContextCompat.getDrawable(context, resId)
    }

    fun setCollapsedTitleAppearance(@StyleRes resId: Int) {
        mCollapsingTextHelper!!.setCollapsedTitleAppearance(resId)
    }

    fun setCollapsedSubtitleAppearance(@StyleRes resId: Int) {
        mCollapsingTextHelper!!.setCollapsedSubtitleAppearance(resId)
    }

    fun setCollapsedTitleColor(@ColorInt color: Int) {
        setCollapsedTitleColor(ColorStateList.valueOf(color))
    }

    fun setCollapsedTitleColorRes(@ColorRes res: Int) {
        setCollapsedTitleColor(ContextCompat.getColor(context, res))
    }

    fun setCollapsedTitleColor(colors: ColorStateList) {
        mCollapsingTextHelper!!.collapsedTitleColor = colors
    }

    fun setCollapsedSubtitleColor(@ColorInt color: Int) {
        setCollapsedSubtitleColor(ColorStateList.valueOf(color))
    }

    fun setCollapsedSubtitleColorRes(@ColorRes res: Int) {
        setCollapsedSubtitleColor(ContextCompat.getColor(context, res))
    }

    fun setCollapsedSubtitleColor(colors: ColorStateList) {
        mCollapsingTextHelper!!.collapsedSubtitleColor = colors
    }

    var collapsedTitleGravity: Int
        get() = mCollapsingTextHelper!!.collapsedTextGravity
        set(gravity) {
            mCollapsingTextHelper!!.collapsedTextGravity = gravity
        }

    fun setExpandedTitleAppearance(@StyleRes resId: Int) {
        mCollapsingTextHelper!!.setExpandedTitleAppearance(resId)
    }

    fun setExpandedSubtitleAppearance(@StyleRes resId: Int) {
        mCollapsingTextHelper!!.setExpandedSubtitleAppearance(resId)
    }

    fun setExpandedTitleColor(@ColorInt color: Int) {
        setExpandedTitleColor(ColorStateList.valueOf(color))
    }

    fun setExpandedTitleColorRes(@ColorRes res: Int) {
        setExpandedTitleColor(ContextCompat.getColor(context, res))
    }

    fun setExpandedTitleColor(colors: ColorStateList) {
        mCollapsingTextHelper!!.expandedTitleColor = colors
    }

    fun setExpandedSubtitleColor(@ColorInt color: Int) {
        setExpandedSubtitleColor(ColorStateList.valueOf(color))
    }

    fun setExpandedSubtitleColorRes(@ColorRes res: Int) {
        setExpandedSubtitleColor(ContextCompat.getColor(context, res))
    }

    fun setExpandedSubtitleColor(colors: ColorStateList) {
        mCollapsingTextHelper!!.expandedSubtitleColor = colors
    }

    var expandedTitleGravity: Int
        get() = mCollapsingTextHelper!!.expandedTextGravity
        set(gravity) {
            mCollapsingTextHelper!!.expandedTextGravity = gravity
        }

    var collapsedTitleTypeface: Typeface
        get() = mCollapsingTextHelper!!.collapsedTitleTypeface
        set(typeface) {
            mCollapsingTextHelper!!.collapsedTitleTypeface = typeface
        }

    var expandedTitleTypeface: Typeface
        get() = mCollapsingTextHelper!!.expandedTitleTypeface
        set(typeface) {
            mCollapsingTextHelper!!.expandedTitleTypeface = typeface
        }

    var collapsedSubtitleTypeface: Typeface
        get() = mCollapsingTextHelper!!.collapsedSubtitleTypeface
        set(typeface) {
            mCollapsingTextHelper!!.collapsedSubtitleTypeface = typeface
        }

    var expandedSubtitleTypeface: Typeface
        get() = mCollapsingTextHelper!!.expandedSubtitleTypeface
        set(typeface) {
            mCollapsingTextHelper!!.expandedSubtitleTypeface = typeface
        }

    fun setExpandedTitleMargin(start: Int, top: Int, end: Int, bottom: Int) {
        mExpandedMarginStart = start
        mExpandedMarginTop = top
        mExpandedMarginEnd = end
        mExpandedMarginBottom = bottom
        requestLayout()
    }

    var expandedTitleMarginStart: Int
        get() = mExpandedMarginStart
        set(margin) {
            mExpandedMarginStart = margin
            requestLayout()
        }

    var expandedTitleMarginTop: Int
        get() = mExpandedMarginTop
        set(margin) {
            mExpandedMarginTop = margin
            requestLayout()
        }

    var expandedTitleMarginEnd: Int
        get() = mExpandedMarginEnd
        set(margin) {
            mExpandedMarginEnd = margin
            requestLayout()
        }

    var expandedTitleMarginBottom: Int
        get() = mExpandedMarginBottom
        set(margin) {
            mExpandedMarginBottom = margin
            requestLayout()
        }

    var scrimVisibleHeightTrigger: Int
        get() {
            if (mScrimVisibleHeightTrigger >= 0)
                return mScrimVisibleHeightTrigger
            val insetTop = if (mLastInsets != null) mLastInsets!!.systemWindowInsetTop else 0
            val minHeight = ViewCompat.getMinimumHeight(this)
            if (minHeight > 0)
                return Math.min(minHeight * 2 + insetTop, height)
            return height / 3
        }
        set(@IntRange(from = 0) height) {
            if (mScrimVisibleHeightTrigger != height) {
                mScrimVisibleHeightTrigger = height
                updateScrimVisibility()
            }
        }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams) = p is LayoutParams

    override fun generateDefaultLayoutParams() = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    override fun generateLayoutParams(attrs: AttributeSet) = LayoutParams(context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams) = LayoutParams(p)

    class LayoutParams : FrameLayout.LayoutParams {
        companion object {
            private const val DEFAULT_PARALLAX_MULTIPLIER = 0.5f
            const val COLLAPSE_MODE_OFF = 0
            const val COLLAPSE_MODE_PIN = 1
            const val COLLAPSE_MODE_PARALLAX = 2
        }

        var collapseMode = COLLAPSE_MODE_OFF
        var parallaxMultiplier = DEFAULT_PARALLAX_MULTIPLIER

        constructor(c: Context, attrs: AttributeSet) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, R.styleable.CollapsingToolbarLayout_Layout)
            collapseMode = a.getInt(R.styleable.CollapsingToolbarLayout_Layout_layout_collapseMode, COLLAPSE_MODE_OFF)
            parallaxMultiplier = a.getFloat(R.styleable.CollapsingToolbarLayout_Layout_layout_collapseParallaxMultiplier, DEFAULT_PARALLAX_MULTIPLIER)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)

        constructor(p: ViewGroup.LayoutParams) : super(p)

        constructor(source: ViewGroup.MarginLayoutParams) : super(source)

        @RequiresApi(19)
        @TargetApi(19)
        constructor(source: FrameLayout.LayoutParams) : super(source)
    }

    internal fun updateScrimVisibility() {
        if (contentScrim != null || statusBarScrim != null)
            setScrimsShown(height + mCurrentOffset < scrimVisibleHeightTrigger)
    }

    internal fun getMaxOffsetForPinChild(child: View): Int {
        val offsetHelper = getViewOffsetHelper(child)
        val lp = child.layoutParams as LayoutParams
        return height - offsetHelper.layoutTop - child.height - lp.bottomMargin
    }

    private inner class OffsetUpdateListener internal constructor() : AppBarLayout.OnOffsetChangedListener {
        override fun onOffsetChanged(layout: AppBarLayout, verticalOffset: Int) {
            mCurrentOffset = verticalOffset
            val insetTop = if (mLastInsets != null) mLastInsets!!.systemWindowInsetTop else 0
            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                val offsetHelper = getViewOffsetHelper(child)
                when (lp.collapseMode) {
                    LayoutParams.COLLAPSE_MODE_PIN -> offsetHelper.topAndBottomOffset = MathUtils.constrain(-verticalOffset, 0, getMaxOffsetForPinChild(child))
                    LayoutParams.COLLAPSE_MODE_PARALLAX -> offsetHelper.topAndBottomOffset = Math.round(-verticalOffset * lp.parallaxMultiplier)
                }
                i++
            }
            updateScrimVisibility()
            if (statusBarScrim != null && insetTop > 0)
                ViewCompat.postInvalidateOnAnimation(this@SubtitleCollapsingToolbarLayout)
            val expandRange = height - ViewCompat.getMinimumHeight(this@SubtitleCollapsingToolbarLayout) - insetTop
            mCollapsingTextHelper!!.expansionFraction = Math.abs(verticalOffset) / expandRange.toFloat()
        }
    }

    companion object {
        private val DEFAULT_SCRIM_ANIMATION_DURATION = 600

        private fun getHeightWithMargins(view: View): Int {
            val lp = view.layoutParams
            if (lp is ViewGroup.MarginLayoutParams) {
                val mlp = lp
                return view.height + mlp.topMargin + mlp.bottomMargin
            }
            return view.height
        }

        internal fun getViewOffsetHelper(view: View): ViewOffsetHelper {
            var offsetHelper = view.getTag(R.id.view_offset_helper) as ViewOffsetHelper?
            if (offsetHelper == null) {
                offsetHelper = ViewOffsetHelper(view)
                view.setTag(R.id.view_offset_helper, offsetHelper)
            }
            return offsetHelper
        }
    }
}