package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test

import android.os.SystemClock
import android.support.test.espresso.UiController
import android.support.test.espresso.action.MotionEvents
import android.support.test.espresso.action.Swiper
import android.support.test.espresso.core.deps.guava.base.Preconditions.checkElementIndex
import android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull
import android.util.Log

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 * *
 * @see android.support.test.espresso.action.Swipe
 */
class SlowerSwipe private constructor() : Swiper {

    override fun sendSwipe(uiController: UiController, startCoordinates: FloatArray, endCoordinates: FloatArray, precision: FloatArray): Swiper.Status {
        return sendLinearSwipe(uiController, startCoordinates, endCoordinates, precision, SWIPE_SLOWER_DURATION_MS)
    }

    companion object {
        val INSTANCE: Swiper = SlowerSwipe()

        private val SWIPE_EVENT_COUNT = 10
        private val SWIPE_SLOWER_DURATION_MS = 5000

        private fun sendLinearSwipe(uiController: UiController, startCoordinates: FloatArray, endCoordinates: FloatArray, precision: FloatArray, duration: Int): Swiper.Status {
            checkNotNull(uiController)
            checkNotNull(startCoordinates)
            checkNotNull(endCoordinates)
            checkNotNull(precision)

            val steps = interpolate(startCoordinates, endCoordinates, SWIPE_EVENT_COUNT)
            val delayBetweenMovements = duration / steps.size

            val downEvent = MotionEvents.sendDown(uiController, startCoordinates, precision).down
            try {
                for (i in steps.indices) {
                    if (!MotionEvents.sendMovement(uiController, downEvent, steps[i])) {
                        Log.e("SlowerSwipe", "Injection of move event as part of the swipe failed. Sending cancel event.")
                        MotionEvents.sendCancel(uiController, downEvent)
                        return Swiper.Status.FAILURE
                    }

                    val desiredTime = downEvent.downTime + delayBetweenMovements * i
                    val timeUntilDesired = desiredTime - SystemClock.uptimeMillis()
                    if (timeUntilDesired > 10) {
                        uiController.loopMainThreadForAtLeast(timeUntilDesired)
                    }
                }

                if (!MotionEvents.sendUp(uiController, downEvent, endCoordinates)) {
                    Log.e("SlowerSwipe", "Injection of up event as part of the swipe failed. Sending cancel event.")
                    MotionEvents.sendCancel(uiController, downEvent)
                    return Swiper.Status.FAILURE
                }
            } finally {
                downEvent.recycle()
            }
            return Swiper.Status.SUCCESS
        }

        private fun interpolate(start: FloatArray, end: FloatArray, steps: Int): Array<FloatArray> {
            checkElementIndex(1, start.size)
            checkElementIndex(1, end.size)

            val res = Array(steps) { FloatArray(2) }

            for (i in 1..steps + 1 - 1) {
                res[i - 1][0] = start[0] + (end[0] - start[0]) * i / (steps + 2f)
                res[i - 1][1] = start[1] + (end[1] - start[1]) * i / (steps + 2f)
            }

            return res
        }
    }
}