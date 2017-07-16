package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test;

import android.os.SystemClock;
import android.support.test.espresso.UiController;
import android.support.test.espresso.action.MotionEvents;
import android.support.test.espresso.action.Swiper;
import android.util.Log;
import android.view.MotionEvent;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkElementIndex;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 * @see android.support.test.espresso.action.Swipe
 */
public class SlowerSwipe implements Swiper {

    public static final Swiper INSTANCE = new SlowerSwipe();

    private SlowerSwipe() {
    }

    private static final int SWIPE_EVENT_COUNT = 10;
    private static final int SWIPE_SLOWER_DURATION_MS = 5000;

    @Override
    public Status sendSwipe(UiController uiController, float[] startCoordinates, float[] endCoordinates, float[] precision) {
        return sendLinearSwipe(uiController, startCoordinates, endCoordinates, precision, SWIPE_SLOWER_DURATION_MS);
    }

    private static Swiper.Status sendLinearSwipe(UiController uiController, float[] startCoordinates, float[] endCoordinates, float[] precision, int duration) {
        checkNotNull(uiController);
        checkNotNull(startCoordinates);
        checkNotNull(endCoordinates);
        checkNotNull(precision);

        float[][] steps = interpolate(startCoordinates, endCoordinates, SWIPE_EVENT_COUNT);
        final int delayBetweenMovements = duration / steps.length;

        MotionEvent downEvent = MotionEvents.sendDown(uiController, startCoordinates, precision).down;
        try {
            for (int i = 0; i < steps.length; i++) {
                if (!MotionEvents.sendMovement(uiController, downEvent, steps[i])) {
                    Log.e("SlowerSwipe", "Injection of move event as part of the swipe failed. Sending cancel event.");
                    MotionEvents.sendCancel(uiController, downEvent);
                    return Swiper.Status.FAILURE;
                }

                long desiredTime = downEvent.getDownTime() + delayBetweenMovements * i;
                long timeUntilDesired = desiredTime - SystemClock.uptimeMillis();
                if (timeUntilDesired > 10) {
                    uiController.loopMainThreadForAtLeast(timeUntilDesired);
                }
            }

            if (!MotionEvents.sendUp(uiController, downEvent, endCoordinates)) {
                Log.e("SlowerSwipe", "Injection of up event as part of the swipe failed. Sending cancel event.");
                MotionEvents.sendCancel(uiController, downEvent);
                return Swiper.Status.FAILURE;
            }
        } finally {
            downEvent.recycle();
        }
        return Swiper.Status.SUCCESS;
    }

    private static float[][] interpolate(float[] start, float[] end, int steps) {
        checkElementIndex(1, start.length);
        checkElementIndex(1, end.length);

        float[][] res = new float[steps][2];

        for (int i = 1; i < steps + 1; i++) {
            res[i - 1][0] = start[0] + (end[0] - start[0]) * i / (steps + 2f);
            res[i - 1][1] = start[1] + (end[1] - start[1]) * i / (steps + 2f);
        }

        return res;
    }
}