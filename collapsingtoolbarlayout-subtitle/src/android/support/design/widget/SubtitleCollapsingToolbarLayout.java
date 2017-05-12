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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.hendraanggrian.collapsingtoolbarlayout.subtitle.R;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 * @see CollapsingToolbarLayout
 */
public class SubtitleCollapsingToolbarLayout extends FrameLayout {

    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;

    private boolean mRefreshToolbar = true;
    private int mToolbarId;
    private Toolbar mToolbar;
    private View mToolbarDirectChild;
    private View mDummyView;

    private int mExpandedMarginStart;
    private int mExpandedMarginTop;
    private int mExpandedMarginEnd;
    private int mExpandedMarginBottom;

    private final Rect mTmpRect = new Rect();
    final SubtitleCollapsingTextHelper mCollapsingTextHelper;
    private boolean mCollapsingTitleEnabled;
    private boolean mDrawCollapsingTitle;

    private Drawable mContentScrim;
    Drawable mStatusBarScrim;
    private int mScrimAlpha;
    private boolean mScrimsAreShown;
    private ValueAnimatorCompat mScrimAnimator;
    private long mScrimAnimationDuration;
    private int mScrimVisibleHeightTrigger = -1;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    int mCurrentOffset;

    WindowInsetsCompat mLastInsets;

    // extra attr
    private boolean useCorrectPadding;

    public SubtitleCollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public SubtitleCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitleCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ThemeUtils.checkAppCompatTheme(context);

        mCollapsingTextHelper = new SubtitleCollapsingTextHelper(this);
        mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);

        TypedArray a1 = context.obtainStyledAttributes(attrs, R.styleable.CollapsingToolbarLayout, defStyleAttr, R.style.Widget_Design_CollapsingToolbar);

        mCollapsingTextHelper.setExpandedTextGravity(a1.getInt(R.styleable.CollapsingToolbarLayout_expandedTitleGravity, GravityCompat.START | Gravity.BOTTOM));
        mCollapsingTextHelper.setCollapsedTextGravity(a1.getInt(R.styleable.CollapsingToolbarLayout_collapsedTitleGravity, GravityCompat.START | Gravity.CENTER_VERTICAL));

        mExpandedMarginStart = mExpandedMarginTop = mExpandedMarginEnd = mExpandedMarginBottom = a1.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMargin, 0);

        if (a1.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart))
            mExpandedMarginStart = a1.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginStart, 0);
        if (a1.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd))
            mExpandedMarginEnd = a1.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginEnd, 0);
        if (a1.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop))
            mExpandedMarginTop = a1.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginTop, 0);
        if (a1.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom))
            mExpandedMarginBottom = a1.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_expandedTitleMarginBottom, 0);

        mCollapsingTitleEnabled = a1.getBoolean(R.styleable.CollapsingToolbarLayout_titleEnabled, true);
        setTitle(a1.getText(R.styleable.CollapsingToolbarLayout_title));

        // First load the default text appearances
        mCollapsingTextHelper.setExpandedTitleAppearance(R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
        mCollapsingTextHelper.setCollapsedTitleAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);

        // Now overlay any custom text appearances
        if (a1.hasValue(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance))
            mCollapsingTextHelper.setExpandedTitleAppearance(a1.getResourceId(R.styleable.CollapsingToolbarLayout_expandedTitleTextAppearance, 0));
        if (a1.hasValue(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance))
            mCollapsingTextHelper.setCollapsedTitleAppearance(a1.getResourceId(R.styleable.CollapsingToolbarLayout_collapsedTitleTextAppearance, 0));

        // begin modification
        TypedArray a2 = context.obtainStyledAttributes(attrs, R.styleable.SubtitleCollapsingToolbarLayout, defStyleAttr, R.style.SubtitleCollapsingToolbarLayout);
        setSubtitle(a2.getText(R.styleable.SubtitleCollapsingToolbarLayout_subtitle));

        useCorrectPadding = a2.getBoolean(R.styleable.SubtitleCollapsingToolbarLayout_useCorrectPadding, false);

        // First load the default text appearances
        mCollapsingTextHelper.setCollapsedSubtitleAppearance(R.style.CollapsedSubtitleAppearance);
        mCollapsingTextHelper.setExpandedSubtitleAppearance(R.style.ExpandedSubtitleAppearance);

        // Now overlay any custom text appearances
        if (a2.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance))
            mCollapsingTextHelper.setCollapsedSubtitleAppearance(a2.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_collapsedSubtitleTextAppearance, 0));
        if (a2.hasValue(R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance))
            mCollapsingTextHelper.setExpandedSubtitleAppearance(a2.getResourceId(R.styleable.SubtitleCollapsingToolbarLayout_expandedSubtitleTextAppearance, 0));

        mScrimVisibleHeightTrigger = a1.getDimensionPixelSize(R.styleable.CollapsingToolbarLayout_scrimVisibleHeightTrigger, -1);

        mScrimAnimationDuration = a1.getInt(R.styleable.CollapsingToolbarLayout_scrimAnimationDuration, DEFAULT_SCRIM_ANIMATION_DURATION);

        setContentScrim(a1.getDrawable(R.styleable.CollapsingToolbarLayout_contentScrim));
        setStatusBarScrim(a1.getDrawable(R.styleable.CollapsingToolbarLayout_statusBarScrim));

        mToolbarId = a1.getResourceId(R.styleable.CollapsingToolbarLayout_toolbarId, -1);

        a1.recycle();
        a2.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this, new android.support.v4.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                return onWindowInsetChanged(insets);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));
            if (mOnOffsetChangedListener == null)
                mOnOffsetChangedListener = new OffsetUpdateListener();
            ((AppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);
            ViewCompat.requestApplyInsets(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        final ViewParent parent = getParent();
        if (mOnOffsetChangedListener != null && parent instanceof AppBarLayout)
            ((AppBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        super.onDetachedFromWindow();
    }

    WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;
        if (ViewCompat.getFitsSystemWindows(this))
            newInsets = insets;
        if (!ViewUtils.objectEquals(mLastInsets, newInsets)) {
            mLastInsets = newInsets;
            requestLayout();
        }
        return insets.consumeSystemWindowInsets();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        ensureToolbar();
        if (mToolbar == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }
        if (mCollapsingTitleEnabled && mDrawCollapsingTitle) {
            mCollapsingTextHelper.draw(canvas);
        }
        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                mStatusBarScrim.setBounds(0, -mCurrentOffset, getWidth(), topInset - mCurrentOffset);
                mStatusBarScrim.mutate().setAlpha(mScrimAlpha);
                mStatusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean invalidated = false;
        if (mContentScrim != null && mScrimAlpha > 0 && isToolbarChild(child)) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
            invalidated = true;
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mContentScrim != null)
            mContentScrim.setBounds(0, 0, w, h);
    }

    private void ensureToolbar() {
        if (!mRefreshToolbar)
            return;
        mToolbar = null;
        mToolbarDirectChild = null;
        if (mToolbarId != -1) {
            mToolbar = (Toolbar) findViewById(mToolbarId);
            if (mToolbar != null)
                mToolbarDirectChild = findDirectChild(mToolbar);
        }
        if (mToolbar == null) {
            Toolbar toolbar = null;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (child instanceof Toolbar) {
                    toolbar = (Toolbar) child;
                    break;
                }
            }
            mToolbar = toolbar;
        }
        updateDummyView();
        mRefreshToolbar = false;
    }

    private boolean isToolbarChild(View child) {
        return (mToolbarDirectChild == null || mToolbarDirectChild == this)
                ? child == mToolbar
                : child == mToolbarDirectChild;
    }

    private View findDirectChild(final View descendant) {
        View directChild = descendant;
        for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
            if (p instanceof View) {
                directChild = (View) p;
            }
        }
        return directChild;
    }

    private void updateDummyView() {
        if (!mCollapsingTitleEnabled && mDummyView != null) {
            final ViewParent parent = mDummyView.getParent();
            if (parent instanceof ViewGroup)
                ((ViewGroup) parent).removeView(mDummyView);
        }
        if (mCollapsingTitleEnabled && mToolbar != null) {
            if (mDummyView == null)
                mDummyView = new View(getContext());
            if (mDummyView.getParent() == null)
                mToolbar.addView(mDummyView, CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, CollapsingToolbarLayout.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mLastInsets != null) {
            final int insetTop = mLastInsets.getSystemWindowInsetTop();
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (!ViewCompat.getFitsSystemWindows(child))
                    if (child.getTop() < insetTop)
                        ViewCompat.offsetTopAndBottom(child, insetTop);
            }
        }
        if (mCollapsingTitleEnabled && mDummyView != null) {
            mDrawCollapsingTitle = ViewCompat.isAttachedToWindow(mDummyView) && mDummyView.getVisibility() == VISIBLE;
            if (mDrawCollapsingTitle) {
                final boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
                final int maxOffset = getMaxOffsetForPinChild(mToolbarDirectChild != null ? mToolbarDirectChild : mToolbar);
                ViewGroupUtils.getDescendantRect(this, mDummyView, mTmpRect);
                int collapsedBoundsLeft = mTmpRect.left + (isRtl
                        ? mToolbar.getTitleMarginEnd()
                        : mToolbar.getTitleMarginStart());
                int collapsedBoundsRight = mTmpRect.right + (isRtl
                        ? mToolbar.getTitleMarginStart()
                        : mToolbar.getTitleMarginEnd());
                if (useCorrectPadding && mToolbar.getMenu() != null && !mToolbar.getMenu().hasVisibleItems())
                    if (isRtl)
                        collapsedBoundsLeft += getContext().getResources().getDimension(R.dimen.appbar_horizontal_padding);
                    else
                        collapsedBoundsRight -= getContext().getResources().getDimension(R.dimen.appbar_horizontal_padding);
                mCollapsingTextHelper.setCollapsedBounds(
                        collapsedBoundsLeft,
                        mTmpRect.top + maxOffset + mToolbar.getTitleMarginTop(),
                        collapsedBoundsRight,
                        mTmpRect.bottom + maxOffset - mToolbar.getTitleMarginBottom());
                mCollapsingTextHelper.setExpandedBounds(
                        isRtl ? mExpandedMarginEnd : mExpandedMarginStart,
                        mTmpRect.top + mExpandedMarginTop,
                        right - left - (isRtl ? mExpandedMarginStart : mExpandedMarginEnd),
                        bottom - top - mExpandedMarginBottom);
                mCollapsingTextHelper.recalculate();
            }
        }
        for (int i = 0, z = getChildCount(); i < z; i++)
            getViewOffsetHelper(getChildAt(i)).onViewLayout();
        if (mToolbar != null) {
            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper.getTitle()))
                mCollapsingTextHelper.setTitle(mToolbar.getTitle());
            if (mToolbarDirectChild == null || mToolbarDirectChild == this)
                setMinimumHeight(getHeightWithMargins(mToolbar));
            else
                setMinimumHeight(getHeightWithMargins(mToolbarDirectChild));
        }
        updateScrimVisibility();
    }

    public void setUseCorrectPadding(boolean useCorrectPadding) {
        this.useCorrectPadding = useCorrectPadding;
        requestLayout();
    }

    private static int getHeightWithMargins(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) lp;
            return view.getHeight() + mlp.topMargin + mlp.bottomMargin;
        }
        return view.getHeight();
    }

    static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    public void setTitle(@Nullable CharSequence title) {
        mCollapsingTextHelper.setTitle(title);
    }

    @Nullable
    public CharSequence getTitle() {
        return mCollapsingTitleEnabled ? mCollapsingTextHelper.getTitle() : null;
    }

    public void setSubtitle(@Nullable CharSequence subtitle) {
        mCollapsingTextHelper.setSubtitle(subtitle);
    }

    @Nullable
    public CharSequence getSubtitle() {
        return mCollapsingTitleEnabled ? mCollapsingTextHelper.getSubtitle() : null;
    }

    public boolean isTitleEnabled() {
        return mCollapsingTitleEnabled;
    }

    public void setTitleEnabled(boolean enabled) {
        if (enabled != mCollapsingTitleEnabled) {
            mCollapsingTitleEnabled = enabled;
            updateDummyView();
            requestLayout();
        }
    }

    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    public void setScrimsShown(boolean shown, boolean animate) {
        if (mScrimsAreShown != shown) {
            if (animate)
                animateScrim(shown ? 0xFF : 0x0);
            else
                setScrimAlpha(shown ? 0xFF : 0x0);
            mScrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (mScrimAnimator == null) {
            mScrimAnimator = ViewUtils.createAnimator();
            mScrimAnimator.setDuration(mScrimAnimationDuration);
            mScrimAnimator.setInterpolator(
                    targetAlpha > mScrimAlpha
                            ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
                            : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.addUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimatorCompat animator) {
                    setScrimAlpha(animator.getAnimatedIntValue());
                }
            });
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }
        mScrimAnimator.setIntValues(mScrimAlpha, targetAlpha);
        mScrimAnimator.start();
    }

    void setScrimAlpha(int alpha) {
        if (alpha != mScrimAlpha) {
            final Drawable contentScrim = mContentScrim;
            if (contentScrim != null && mToolbar != null)
                ViewCompat.postInvalidateOnAnimation(mToolbar);
            mScrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(SubtitleCollapsingToolbarLayout.this);
        }
    }

    int getScrimAlpha() {
        return mScrimAlpha;
    }

    public void setContentScrim(@Nullable Drawable drawable) {
        if (mContentScrim != drawable) {
            if (mContentScrim != null)
                mContentScrim.setCallback(null);
            mContentScrim = drawable != null ? drawable.mutate() : null;
            if (mContentScrim != null) {
                mContentScrim.setBounds(0, 0, getWidth(), getHeight());
                mContentScrim.setCallback(this);
                mContentScrim.setAlpha(mScrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setContentScrimColor(@ColorInt int color) {
        setContentScrim(new ColorDrawable(color));
    }

    public void setContentScrimResource(@DrawableRes int resId) {
        setContentScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    @Nullable
    public Drawable getContentScrim() {
        return mContentScrim;
    }

    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (mStatusBarScrim != drawable) {
            if (mStatusBarScrim != null)
                mStatusBarScrim.setCallback(null);
            mStatusBarScrim = drawable != null ? drawable.mutate() : null;
            if (mStatusBarScrim != null) {
                if (mStatusBarScrim.isStateful())
                    mStatusBarScrim.setState(getDrawableState());
                DrawableCompat.setLayoutDirection(mStatusBarScrim, ViewCompat.getLayoutDirection(this));
                mStatusBarScrim.setVisible(getVisibility() == VISIBLE, false);
                mStatusBarScrim.setCallback(this);
                mStatusBarScrim.setAlpha(mScrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final int[] state = getDrawableState();
        boolean changed = false;
        Drawable d = mStatusBarScrim;
        if (d != null && d.isStateful())
            changed |= d.setState(state);
        d = mContentScrim;
        if (d != null && d.isStateful())
            changed |= d.setState(state);
        if (mCollapsingTextHelper != null)
            changed |= mCollapsingTextHelper.setState(state);
        if (changed)
            invalidate();
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == mContentScrim || who == mStatusBarScrim;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        final boolean visible = visibility == VISIBLE;
        if (mStatusBarScrim != null && mStatusBarScrim.isVisible() != visible)
            mStatusBarScrim.setVisible(visible, false);
        if (mContentScrim != null && mContentScrim.isVisible() != visible)
            mContentScrim.setVisible(visible, false);
    }

    public void setStatusBarScrimColor(@ColorInt int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    public void setStatusBarScrimResource(@DrawableRes int resId) {
        setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    @Nullable
    public Drawable getStatusBarScrim() {
        return mStatusBarScrim;
    }

    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setCollapsedTitleAppearance(resId);
    }

    public void setCollapsedSubtitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setCollapsedSubtitleAppearance(resId);
    }

    public void setCollapsedTitleTextColor(@ColorInt int color) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color));
    }

    public void setCollapsedSubtitleTextColor(@ColorInt int color) {
        setCollapsedSubtitleTextColor(ColorStateList.valueOf(color));
    }

    public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
        mCollapsingTextHelper.setCollapsedTitleColor(colors);
    }

    public void setCollapsedSubtitleTextColor(@NonNull ColorStateList colors) {
        mCollapsingTextHelper.setCollapsedSubtitleColor(colors);
    }

    public void setCollapsedTitleGravity(int gravity) {
        mCollapsingTextHelper.setCollapsedTextGravity(gravity);
    }

    public int getCollapsedTitleGravity() {
        return mCollapsingTextHelper.getCollapsedTextGravity();
    }

    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setExpandedTitleAppearance(resId);
    }

    public void setExpandedSubtitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setExpandedSubtitleAppearance(resId);
    }

    public void setExpandedTitleColor(@ColorInt int color) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color));
    }

    public void setExpandedSubtitleColor(@ColorInt int color) {
        setExpandedSubtitleTextColor(ColorStateList.valueOf(color));
    }

    public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
        mCollapsingTextHelper.setExpandedTitleColor(colors);
    }

    public void setExpandedSubtitleTextColor(@NonNull ColorStateList colors) {
        mCollapsingTextHelper.setExpandedSubtitleColor(colors);
    }

    public void setExpandedTitleGravity(int gravity) {
        mCollapsingTextHelper.setExpandedTextGravity(gravity);
    }

    public int getExpandedTitleGravity() {
        return mCollapsingTextHelper.getExpandedTextGravity();
    }

    public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setCollapsedTypeface(typeface);
    }

    @NonNull
    public Typeface getCollapsedTitleTypeface() {
        return mCollapsingTextHelper.getCollapsedTypeface();
    }

    public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setExpandedTypeface(typeface);
    }

    @NonNull
    public Typeface getExpandedTitleTypeface() {
        return mCollapsingTextHelper.getExpandedTypeface();
    }

    public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
        mExpandedMarginStart = start;
        mExpandedMarginTop = top;
        mExpandedMarginEnd = end;
        mExpandedMarginBottom = bottom;
        requestLayout();
    }

    public int getExpandedTitleMarginStart() {
        return mExpandedMarginStart;
    }

    public void setExpandedTitleMarginStart(int margin) {
        mExpandedMarginStart = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginTop() {
        return mExpandedMarginTop;
    }

    public void setExpandedTitleMarginTop(int margin) {
        mExpandedMarginTop = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginEnd() {
        return mExpandedMarginEnd;
    }

    public void setExpandedTitleMarginEnd(int margin) {
        mExpandedMarginEnd = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginBottom() {
        return mExpandedMarginBottom;
    }

    public void setExpandedTitleMarginBottom(int margin) {
        mExpandedMarginBottom = margin;
        requestLayout();
    }

    @SuppressWarnings("Range")
    public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
        if (mScrimVisibleHeightTrigger != height) {
            mScrimVisibleHeightTrigger = height;
            updateScrimVisibility();
        }
    }

    public int getScrimVisibleHeightTrigger() {
        if (mScrimVisibleHeightTrigger >= 0)
            return mScrimVisibleHeightTrigger;
        final int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight > 0)
            return Math.min((minHeight * 2) + insetTop, getHeight());
        return getHeight() / 3;
    }

    public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
        mScrimAnimationDuration = duration;
    }

    public long getScrimAnimationDuration() {
        return mScrimAnimationDuration;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CollapsingToolbarLayout.LayoutParams;
    }

    @Override
    protected CollapsingToolbarLayout.LayoutParams generateDefaultLayoutParams() {
        return new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, CollapsingToolbarLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CollapsingToolbarLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CollapsingToolbarLayout.LayoutParams(p);
    }

    final void updateScrimVisibility() {
        if (mContentScrim != null || mStatusBarScrim != null)
            setScrimsShown(getHeight() + mCurrentOffset < getScrimVisibleHeightTrigger());
    }

    final int getMaxOffsetForPinChild(View child) {
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
        final CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) child.getLayoutParams();
        return getHeight()
                - offsetHelper.getLayoutTop()
                - child.getHeight()
                - lp.bottomMargin;
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {
        }

        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            mCurrentOffset = verticalOffset;
            final int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
                switch (lp.mCollapseMode) {
                    case CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN:
                        offsetHelper.setTopAndBottomOffset(MathUtils.constrain(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
                        break;
                    case CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.mParallaxMult));
                        break;
                }
            }
            updateScrimVisibility();
            if (mStatusBarScrim != null && insetTop > 0)
                ViewCompat.postInvalidateOnAnimation(SubtitleCollapsingToolbarLayout.this);
            final int expandRange = getHeight() - ViewCompat.getMinimumHeight(SubtitleCollapsingToolbarLayout.this) - insetTop;
            mCollapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset) / (float) expandRange);
        }
    }
}