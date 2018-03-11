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

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import android.content.res.Configuration.SCREENLAYOUT_SIZE_UNDEFINED
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.IntRange
import android.support.annotation.RequiresApi
import android.support.annotation.StyleRes
import android.support.design.widget.AnimationUtils.DECELERATE_INTERPOLATOR
import android.support.design.widget.AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
import android.support.design.widget.AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR
import android.support.design.widget.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
import android.support.design.widget.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
import android.support.design.widget.CollapsingToolbarLayout.getViewOffsetHelper
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.math.MathUtils
import android.support.v4.util.ObjectsCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.WindowInsetsCompat
import android.support.v4.widget.ViewGroupUtils
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewParent
import android.widget.FrameLayout
import com.hendraanggrian.collapsingtoolbarlayout.subtitle.R
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
@Suppress("LeakingThis")
open class SubtitleCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mRefreshToolbar = true
    private var mToolbarId: Int
    private var mToolbar: Toolbar? = null
    private var mToolbarDirectChild: View? = null
    private var mDummyView: View? = null

    private var mExpandedMarginStart: Int
    private var mExpandedMarginTop: Int
    private var mExpandedMarginEnd: Int
    private var mExpandedMarginBottom: Int

    private val mTmpRect = Rect()
    internal val mCollapsingTextHelper: SubtitleCollapsingTextHelper
    private var mCollapsingTitleEnabled: Boolean
    private var mDrawCollapsingTitle = false

    private var mContentScrim: Drawable?
    internal var mStatusBarScrim: Drawable?
    private var mScrimAlpha = 0
    private var mScrimsAreShown = false
    private var mScrimAnimator: ValueAnimator? = null
    private var mScrimAnimationDuration: Long
    private var mScrimVisibleHeightTrigger = -1

    private var mOnOffsetChangedListener: AppBarLayout.OnOffsetChangedListener? = null

    internal var mCurrentOffset = 0

    internal var mLastInsets: WindowInsetsCompat? = null

    private var mFixMenuPadding: Boolean

    init {
        ThemeUtils.checkAppCompatTheme(context)

        mCollapsingTextHelper = SubtitleCollapsingTextHelper(this)
        mCollapsingTextHelper.setTextSizeInterpolator(DECELERATE_INTERPOLATOR)

        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.SubtitleCollapsingToolbarLayout,
            defStyleAttr,
            R.style.Widget_Design_CollapsingToolbar_Subtitle
        )

        mCollapsingTextHelper.expandedTextGravity = a.getInt(
            R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleGravity,
            GravityCompat.START or Gravity.BOTTOM)
        mCollapsingTextHelper.collapsedTextGravity = a.getInt(
            R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleGravity,
            GravityCompat.START or Gravity.CENTER_VERTICAL)

        mExpandedMarginBottom = a.getDimensionPixelSize(
            R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMargin, 0)
        mExpandedMarginEnd = mExpandedMarginBottom
        mExpandedMarginTop = mExpandedMarginEnd
        mExpandedMarginStart = mExpandedMarginTop

        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart)) {
            mExpandedMarginStart = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd)) {
            mExpandedMarginEnd = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop)) {
            mExpandedMarginTop = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop, 0)
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = a.getDimensionPixelSize(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom, 0)
        }

        mCollapsingTitleEnabled = a.getBoolean(
            R.styleable.SubtitleCollapsingToolbarLayout_titleEnabled, true)
        mCollapsingTextHelper.title = a.getText(
            R.styleable.SubtitleCollapsingToolbarLayout_title)
        mCollapsingTextHelper.subtitle = a.getText(
            R.styleable.SubtitleCollapsingToolbarLayout_subtitle)

        // First load the default text appearances
        mCollapsingTextHelper.setExpandedTitleTextAppearance(
            R.style.TextAppearance_Design_CollapsingToolbar_Expanded)
        mCollapsingTextHelper.setCollapsedTitleTextAppearance(
            android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title)

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance)) {
            mCollapsingTextHelper.setExpandedTitleTextAppearance(a.getResourceId(
                R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance, 0))
        }
        if (a.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance)) {
            mCollapsingTextHelper.setCollapsedTitleTextAppearance(a.getResourceId(
                R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance, 0))
        }

        mCollapsingTextHelper.setExpandedSubtitleTextAppearance(a.getResourceId(
            R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance, 0))
        mCollapsingTextHelper.setCollapsedSubtitleTextAppearance(a.getResourceId(
            R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance, 0))

        mScrimVisibleHeightTrigger = a.getDimensionPixelSize(
            R.styleable.SubtitleCollapsingToolbarLayout_scrimVisibleHeightTrigger, -1)
        mScrimAnimationDuration = a.getInt(
            R.styleable.SubtitleCollapsingToolbarLayout_scrimAnimationDuration,
            DEFAULT_SCRIM_ANIMATION_DURATION).toLong()

        mContentScrim = a.getDrawable(R.styleable.SubtitleCollapsingToolbarLayout_contentScrim)
        mStatusBarScrim = a.getDrawable(R.styleable.SubtitleCollapsingToolbarLayout_statusBarScrim)

        mToolbarId = a.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_toolbarId, -1)
        mFixMenuPadding = a.getBoolean(
            R.styleable.SubtitleCollapsingToolbarLayout_fixMenuPadding, false)

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

    internal open fun onWindowInsetChanged(insets: WindowInsetsCompat): WindowInsetsCompat {
        var newInsets: WindowInsetsCompat? = null
        if (ViewCompat.getFitsSystemWindows(this)) {
            newInsets = insets
        }
        if (!ObjectsCompat.equals(mLastInsets, newInsets)) {
            mLastInsets = newInsets
            requestLayout()
        }
        return insets.consumeSystemWindowInsets()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        ensureToolbar()
        if (mToolbar == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim!!.mutate().alpha = mScrimAlpha
            mContentScrim!!.draw(canvas)
        }
        if (mCollapsingTitleEnabled && mDrawCollapsingTitle) {
            mCollapsingTextHelper.draw(canvas)
        }
        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            val topInset = if (mLastInsets != null) mLastInsets!!.systemWindowInsetTop else 0
            if (topInset > 0) {
                mStatusBarScrim!!.setBounds(0, -mCurrentOffset, width, topInset - mCurrentOffset)
                mStatusBarScrim!!.mutate().alpha = mScrimAlpha
                mStatusBarScrim!!.draw(canvas)
            }
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        var invalidated = false
        if (mContentScrim != null && mScrimAlpha > 0 && isToolbarChild(child)) {
            mContentScrim!!.mutate().alpha = mScrimAlpha
            mContentScrim!!.draw(canvas)
            invalidated = true
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mContentScrim != null) {
            mContentScrim!!.setBounds(0, 0, w, h)
        }
    }

    private fun ensureToolbar() {
        if (!mRefreshToolbar) return
        mToolbar = null
        mToolbarDirectChild = null
        if (mToolbarId != -1) {
            mToolbar = findViewById(mToolbarId)
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

    private fun isToolbarChild(child: View): Boolean = child == when (mToolbarDirectChild) {
        null, this -> mToolbar
        else -> mToolbarDirectChild
    }

    private fun findDirectChild(descendant: View): View {
        var directChild = descendant
        var p: ViewParent? = descendant.parent
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
                mToolbar!!.addView(mDummyView, MATCH_PARENT, MATCH_PARENT)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        ensureToolbar()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val topInset = mLastInsets?.systemWindowInsetTop ?: 0
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(measuredHeight + topInset, MeasureSpec.EXACTLY))
    }

    @SuppressLint("RestrictedApi")
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
            mDrawCollapsingTitle = ViewCompat.isAttachedToWindow(mDummyView) &&
                mDummyView!!.visibility == VISIBLE
            if (mDrawCollapsingTitle) {
                val isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
                val maxOffset = getMaxOffsetForPinChild(mToolbarDirectChild ?: mToolbar!!)
                ViewGroupUtils.getDescendantRect(this, mDummyView!!, mTmpRect)
                var collapsedBoundsLeft = mTmpRect.left + when {
                    isRtl -> mToolbar!!.titleMarginEnd
                    else -> mToolbar!!.titleMarginStart
                }
                var collapsedBoundsRight = mTmpRect.right + when {
                    isRtl -> mToolbar!!.titleMarginStart
                    else -> mToolbar!!.titleMarginEnd
                }
                if (mFixMenuPadding && mToolbar!!.menu != null &&
                    !mToolbar!!.menu.hasVisibleItems()) {
                    val padding = context.resources.getDimensionPixelSize(when {
                        context.isScreenSizeAtLeast(SCREENLAYOUT_SIZE_LARGE) ->
                            R.dimen.appbar_horizontal_padding_large
                        else -> R.dimen.appbar_horizontal_padding
                    })
                    if (isRtl) collapsedBoundsLeft += padding
                    else collapsedBoundsRight -= padding
                }
                mCollapsingTextHelper.setCollapsedBounds(
                    collapsedBoundsLeft,
                    mTmpRect.top + maxOffset + mToolbar!!.titleMarginTop,
                    collapsedBoundsRight,
                    mTmpRect.bottom + maxOffset - mToolbar!!.titleMarginBottom)
                mCollapsingTextHelper.setExpandedBounds(
                    if (isRtl) mExpandedMarginEnd
                    else mExpandedMarginStart,
                    mTmpRect.top + mExpandedMarginTop,
                    right - left - if (isRtl) mExpandedMarginStart else mExpandedMarginEnd,
                    bottom - top - mExpandedMarginBottom)
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
            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper.title)) {
                mCollapsingTextHelper.title = mToolbar!!.title
            }
            minimumHeight = when (mToolbarDirectChild) {
                null, this -> mToolbar!!.heightWithMargins
                else -> mToolbarDirectChild!!.heightWithMargins
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
        get() = if (mCollapsingTitleEnabled) mCollapsingTextHelper.title else null
        set(title) {
            mCollapsingTextHelper.title = title
        }

    /**
     * Subtitle of this toolbar layout.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_subtitle
     */
    open var subtitle: CharSequence?
        get() = if (mCollapsingTitleEnabled) mCollapsingTextHelper.subtitle else null
        set(subtitle) {
            mCollapsingTextHelper.subtitle = subtitle
        }

    /**
     * Checks whether this view should display its own title, also affects subtitle.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_titleEnabled
     */
    open var isTitleEnabled: Boolean
        get() = mCollapsingTitleEnabled
        set(enabled) {
            if (enabled != mCollapsingTitleEnabled) {
                mCollapsingTitleEnabled = enabled
                updateDummyView()
                requestLayout()
            }
        }

    /** Sets whether the content scrim should be shown. */
    @JvmOverloads
    open fun setScrimsShown(
        shown: Boolean,
        animate: Boolean = ViewCompat.isLaidOut(this) && !isInEditMode
    ) {
        if (mScrimsAreShown != shown) {
            when {
                animate -> animateScrim(if (shown) 0xFF else 0x0)
                else -> scrimAlpha = if (shown) 0xFF else 0x0
            }
            mScrimsAreShown = shown
        }
    }

    private fun animateScrim(targetAlpha: Int) {
        ensureToolbar()
        when {
            mScrimAnimator == null -> {
                mScrimAnimator = ValueAnimator()
                mScrimAnimator!!.duration = mScrimAnimationDuration
                mScrimAnimator!!.interpolator = when {
                    targetAlpha > scrimAlpha -> FAST_OUT_LINEAR_IN_INTERPOLATOR
                    else -> LINEAR_OUT_SLOW_IN_INTERPOLATOR
                }
                mScrimAnimator!!.addUpdateListener { animator ->
                    scrimAlpha = animator.animatedValue as Int
                }
            }
            mScrimAnimator!!.isRunning -> mScrimAnimator!!.cancel()
        }
        mScrimAnimator!!.setIntValues(mScrimAlpha, targetAlpha)
        mScrimAnimator!!.start()
    }

    internal open var scrimAlpha: Int
        get() = mScrimAlpha
        set(alpha) {
            if (alpha != mScrimAlpha) {
                val contentScrim = mContentScrim
                if (contentScrim != null && mToolbar != null) {
                    ViewCompat.postInvalidateOnAnimation(mToolbar)
                }
                mScrimAlpha = alpha
                ViewCompat.postInvalidateOnAnimation(this@SubtitleCollapsingToolbarLayout)
            }
        }

    /**
     * Drawable to use for the content scrim from resources
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_contentScrim
     */
    open var contentScrim: Drawable?
        get() = mContentScrim
        set(drawable) {
            if (mContentScrim != drawable) {
                if (mContentScrim != null) {
                    mContentScrim!!.callback = null
                }
                mContentScrim = drawable?.mutate()
                if (mContentScrim != null) {
                    mContentScrim!!.setBounds(0, 0, width, height)
                    mContentScrim!!.callback = this
                    mContentScrim!!.alpha = mScrimAlpha
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
        get() = mStatusBarScrim
        set(drawable) {
            if (mStatusBarScrim != drawable) {
                if (mStatusBarScrim != null) {
                    mStatusBarScrim!!.callback = null
                }
                mStatusBarScrim = drawable?.mutate()
                if (mStatusBarScrim != null) {
                    if (mStatusBarScrim!!.isStateful) {
                        mStatusBarScrim!!.state = drawableState
                    }
                    DrawableCompat.setLayoutDirection(
                        mStatusBarScrim!!, ViewCompat.getLayoutDirection(this))
                    mStatusBarScrim!!.setVisible(visibility == VISIBLE, false)
                    mStatusBarScrim!!.callback = this
                    mStatusBarScrim!!.alpha = mScrimAlpha
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

    /**
     * Checks whether this toolbar layout should add menu item padding.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_fixMenuPadding
     */
    open var fixMenuPadding: Boolean
        get() = mFixMenuPadding
        set(value) {
            mFixMenuPadding = value
            requestLayout()
        }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val state = drawableState
        var changed = false
        var d = mStatusBarScrim
        if (d != null && d.isStateful) {
            changed = d.setState(state)
        }
        d = mContentScrim
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

    override fun verifyDrawable(who: Drawable) = super.verifyDrawable(who) ||
        who == mContentScrim ||
        who == mStatusBarScrim

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        val visible = visibility == VISIBLE
        if (mStatusBarScrim != null && mStatusBarScrim!!.isVisible != visible) {
            mStatusBarScrim!!.setVisible(visible, false)
        }
        if (mContentScrim != null && mContentScrim!!.isVisible != visible) {
            mContentScrim!!.setVisible(visible, false)
        }
    }

    /**
     * Sets text color and size for the collapsed title.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_collapsedTitleTextAppearance
     */
    open fun setCollapsedTitleTextAppearance(@StyleRes resId: Int) =
        mCollapsingTextHelper.setCollapsedTitleTextAppearance(resId)

    /**
     * Sets text color and size for the collapsed subtitle.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance
     */
    open fun setCollapsedSubtitleTextAppearance(@StyleRes resId: Int) =
        mCollapsingTextHelper.setCollapsedSubtitleTextAppearance(resId)

    /** Sets text color of the collapsed title from color state list. */
    open fun setCollapsedTitleTextColor(colors: ColorStateList) {
        mCollapsingTextHelper.collapsedTitleColor = colors
    }

    /** Sets text color of the collapsed title from color resource. */
    open fun setCollapsedTitleTextColor(@ColorInt color: Int) =
        setCollapsedTitleTextColor(ColorStateList.valueOf(color))

    /** Sets text color of the collapsed subtitle from color state list. */
    open fun setCollapsedSubtitleTextColor(colors: ColorStateList) {
        mCollapsingTextHelper.collapsedSubtitleColor = colors
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
        get() = mCollapsingTextHelper.collapsedTextGravity
        set(gravity) {
            mCollapsingTextHelper.collapsedTextGravity = gravity
        }

    /**
     * Sets text color and size for the expanded title.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleTextAppearance
     */
    open fun setExpandedTitleTextAppearance(@StyleRes resId: Int) =
        mCollapsingTextHelper.setExpandedTitleTextAppearance(resId)

    /**
     * Sets text color and size for the expanded subtitle.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance
     */
    open fun setExpandedSubtitleTextAppearance(@StyleRes resId: Int) =
        mCollapsingTextHelper.setExpandedSubtitleTextAppearance(resId)

    /** Sets text color of the expanded title from color state list. */
    open fun setExpandedTitleTextColor(colors: ColorStateList) {
        mCollapsingTextHelper.expandedTitleColor = colors
    }

    /** Sets text color of the expanded title from color resource. */
    open fun setExpandedTitleTextColor(@ColorInt color: Int) =
        setExpandedTitleTextColor(ColorStateList.valueOf(color))

    /** Sets text color of the expanded subtitle from color state list. */
    open fun setExpandedSubtitleTextColor(colors: ColorStateList) {
        mCollapsingTextHelper.expandedSubtitleColor = colors
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
        get() = mCollapsingTextHelper.expandedTextGravity
        set(gravity) {
            mCollapsingTextHelper.expandedTextGravity = gravity
        }

    /** Typeface used for the collapsed title. */
    open var collapsedTitleTypeface: Typeface
        get() = mCollapsingTextHelper.collapsedTitleTypeface
        set(typeface) {
            mCollapsingTextHelper.collapsedTitleTypeface = typeface
        }

    /** Typeface used for the collapsed subtitle. */
    open var collapsedSubtitleTypeface: Typeface
        get() = mCollapsingTextHelper.collapsedSubtitleTypeface
        set(typeface) {
            mCollapsingTextHelper.collapsedSubtitleTypeface = typeface
        }

    /** Typeface used for the expanded title. */
    open var expandedTitleTypeface: Typeface
        get() = mCollapsingTextHelper.expandedTitleTypeface
        set(typeface) {
            mCollapsingTextHelper.expandedTitleTypeface = typeface
        }

    /** Typeface used for the expanded subtitle. */
    open var expandedSubtitleTypeface: Typeface
        get() = mCollapsingTextHelper.expandedSubtitleTypeface
        set(typeface) {
            mCollapsingTextHelper.expandedSubtitleTypeface = typeface
        }

    /** Sets the expanded title margins. */
    open fun setExpandedTitleMargin(start: Int, top: Int, end: Int, bottom: Int) {
        mExpandedMarginStart = start
        mExpandedMarginTop = top
        mExpandedMarginEnd = end
        mExpandedMarginBottom = bottom
        requestLayout()
    }

    /**
     * Starting expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginStart
     */
    open var expandedTitleMarginStart: Int
        get() = mExpandedMarginStart
        set(margin) {
            mExpandedMarginStart = margin
            requestLayout()
        }

    /**
     * Top expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginTop
     */
    open var expandedTitleMarginTop: Int
        get() = mExpandedMarginTop
        set(margin) {
            mExpandedMarginTop = margin
            requestLayout()
        }

    /**
     * Ending expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginEnd
     */
    open var expandedTitleMarginEnd: Int
        get() = mExpandedMarginEnd
        set(margin) {
            mExpandedMarginEnd = margin
            requestLayout()
        }

    /**
     * Bottom expanded title margin in pixels.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_expandedTitleMarginBottom
     */
    open var expandedTitleMarginBottom: Int
        get() = mExpandedMarginBottom
        set(margin) {
            mExpandedMarginBottom = margin
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
            if (mScrimVisibleHeightTrigger >= 0) return mScrimVisibleHeightTrigger
            val insetTop = mLastInsets?.systemWindowInsetTop ?: 0
            val minHeight = ViewCompat.getMinimumHeight(this)
            if (minHeight > 0) return min(minHeight * 2 + insetTop, height)
            return height / 3
        }
        set(@IntRange(from = 0) height) {
            if (mScrimVisibleHeightTrigger != height) {
                mScrimVisibleHeightTrigger = height
                updateScrimVisibility()
            }
        }

    /**
     * Set the duration used for scrim visibility animations.
     *
     * @see R.styleable.SubtitleCollapsingToolbarLayout_scrimAnimationDuration
     */
    open var scrimAnimationDuration: Long
        get() = mScrimAnimationDuration
        set(@IntRange(from = 0) duration) {
            mScrimAnimationDuration = duration
        }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean = p is LayoutParams

    override fun generateDefaultLayoutParams(): LayoutParams = LayoutParams(
        MATCH_PARENT, MATCH_PARENT)

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams = LayoutParams(
        context, attrs)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): LayoutParams = LayoutParams(p)

    open class LayoutParams : CollapsingToolbarLayout.LayoutParams {
        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(p: ViewGroup.LayoutParams) : super(p)
        constructor(source: ViewGroup.MarginLayoutParams) : super(source)
        @RequiresApi(19) constructor(source: FrameLayout.LayoutParams) : super(source)
    }

    internal fun updateScrimVisibility() {
        if (mContentScrim != null || mStatusBarScrim != null) {
            setScrimsShown(height + mCurrentOffset < scrimVisibleHeightTrigger)
        }
    }

    internal fun getMaxOffsetForPinChild(child: View): Int {
        val offsetHelper = getViewOffsetHelper(child)
        val lp = child.layoutParams as LayoutParams
        return height - offsetHelper.layoutTop - child.height - lp.bottomMargin
    }

    private inner class OffsetUpdateListener : AppBarLayout.OnOffsetChangedListener {
        override fun onOffsetChanged(layout: AppBarLayout, verticalOffset: Int) {
            mCurrentOffset = verticalOffset
            val insetTop = mLastInsets?.systemWindowInsetTop ?: 0
            var i = 0
            val z = childCount
            while (i < z) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                val offsetHelper = getViewOffsetHelper(child)
                when (lp.collapseMode) {
                    COLLAPSE_MODE_PIN -> offsetHelper.topAndBottomOffset = MathUtils.clamp(
                        -verticalOffset, 0, getMaxOffsetForPinChild(child))
                    COLLAPSE_MODE_PARALLAX -> offsetHelper.topAndBottomOffset =
                        (-verticalOffset * lp.parallaxMultiplier).roundToInt()
                }
                i++
            }
            updateScrimVisibility()
            if (mStatusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(this@SubtitleCollapsingToolbarLayout)
            }
            val expandRange = height -
                ViewCompat.getMinimumHeight(this@SubtitleCollapsingToolbarLayout) - insetTop
            mCollapsingTextHelper.expansionFraction = abs(verticalOffset) / expandRange.toFloat()
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

        fun Context.isScreenSizeAtLeast(size: Int): Boolean =
            (resources.configuration.screenLayout and SCREENLAYOUT_SIZE_MASK).let { screenSize ->
                screenSize != SCREENLAYOUT_SIZE_UNDEFINED && screenSize >= size
            }
    }
}