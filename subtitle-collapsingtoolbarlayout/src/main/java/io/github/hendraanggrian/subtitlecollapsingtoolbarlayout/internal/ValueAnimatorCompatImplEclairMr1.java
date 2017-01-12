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

package io.github.hendraanggrian.subtitlecollapsingtoolbarlayout.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;


class ValueAnimatorCompatImplEclairMr1 extends ValueAnimatorCompat.Impl {

    private static final int HANDLER_DELAY = 10;
    private static final int DEFAULT_DURATION = 200;

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    private long mStartTime;
    private boolean mIsRunning;

    private final int[] mIntValues = new int[2];

    private int mDuration = DEFAULT_DURATION;
    private Interpolator mInterpolator;
    private AnimatorUpdateListenerProxy mUpdateListener;

    private float mAnimatedFraction;

    @Override
    public void start() {
        if (mIsRunning) {

            return;
        }

        if (mInterpolator == null) {
            mInterpolator = new AccelerateDecelerateInterpolator();
        }

        mStartTime = SystemClock.uptimeMillis();
        mIsRunning = true;

        sHandler.postDelayed(mRunnable, HANDLER_DELAY);
    }

    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    @Override
    public void setUpdateListener(AnimatorUpdateListenerProxy updateListener) {
        mUpdateListener = updateListener;
    }

    @Override
    public void setIntValues(int from, int to) {
        mIntValues[0] = from;
        mIntValues[1] = to;
    }

    @Override
    public int getAnimatedIntValue() {
        return AnimationUtils.lerp(mIntValues[0], mIntValues[1], getAnimatedFraction());
    }

    @Override
    public void setDuration(int duration) {
        mDuration = duration;
    }

    @Override
    public void cancel() {
        mIsRunning = false;
        sHandler.removeCallbacks(mRunnable);
    }

    @Override
    public float getAnimatedFraction() {
        return mAnimatedFraction;
    }

    @Override
    public void end() {
        if (mIsRunning) {
            mIsRunning = false;
            sHandler.removeCallbacks(mRunnable);


            mAnimatedFraction = 1f;

            if (mUpdateListener != null) {
                mUpdateListener.onAnimationUpdate();
            }
        }
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    private void update() {
        if (mIsRunning) {

            final long elapsed = SystemClock.uptimeMillis() - mStartTime;
            final float linearFraction = elapsed / (float) mDuration;
            mAnimatedFraction = mInterpolator != null
                    ? mInterpolator.getInterpolation(linearFraction)
                    : linearFraction;


            if (mUpdateListener != null) {
                mUpdateListener.onAnimationUpdate();
            }


            if (SystemClock.uptimeMillis() >= (mStartTime + mDuration)) {
                mIsRunning = false;
            }
        }

        if (mIsRunning) {

            sHandler.postDelayed(mRunnable, HANDLER_DELAY);
        }
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            update();
        }
    };
}
