package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout

import android.os.SystemClock
import android.util.Log
import androidx.test.espresso.UiController
import androidx.test.espresso.action.MotionEvents.sendCancel
import androidx.test.espresso.action.MotionEvents.sendDown
import androidx.test.espresso.action.MotionEvents.sendMovement
import androidx.test.espresso.action.MotionEvents.sendUp
import androidx.test.espresso.action.Swiper
import androidx.test.espresso.action.Swiper.Status.FAILURE
import androidx.test.espresso.action.Swiper.Status.SUCCESS
import androidx.test.espresso.core.internal.deps.guava.base.Preconditions

/**
 * @see androidx.test.espresso.action.Swipe
 */
class SlowerSwipe : Swiper {

    override fun sendSwipe(
        uiController: UiController,
        startCoordinates: FloatArray,
        endCoordinates: FloatArray,
        precision: FloatArray
    ): Swiper.Status = sendLinearSwipe(uiController, startCoordinates, endCoordinates, precision,
        SWIPE_SLOWER_DURATION_MS)

    private companion object {
        const val SWIPE_EVENT_COUNT = 10
        const val SWIPE_SLOWER_DURATION_MS = 5000

        fun sendLinearSwipe(
            uiController: UiController,
            startCoordinates: FloatArray,
            endCoordinates: FloatArray,
            precision: FloatArray,
            duration: Int
        ): Swiper.Status {
            val steps = interpolate(startCoordinates, endCoordinates, SWIPE_EVENT_COUNT)
            val delayBetweenMovements = duration / steps.size
            val downEvent = sendDown(uiController, startCoordinates, precision).down
            try {
                for (i in steps.indices) {
                    if (!sendMovement(uiController, downEvent, steps[i])) {
                        Log.e("SlowerSwipe",
                            "Injection of move event as part of the swipe failed. " +
                                "Sending cancel event.")
                        sendCancel(uiController, downEvent)
                        return FAILURE
                    }

                    val desiredTime = downEvent.downTime + delayBetweenMovements * i
                    val timeUntilDesired = desiredTime - SystemClock.uptimeMillis()
                    if (timeUntilDesired > 10) {
                        uiController.loopMainThreadForAtLeast(timeUntilDesired)
                    }
                }
                if (!sendUp(uiController, downEvent, endCoordinates)) {
                    Log.e("SlowerSwipe",
                        "Injection of up event as part of the swipe failed. " +
                            "Sending cancel event.")
                    sendCancel(uiController, downEvent)
                    return FAILURE
                }
            } finally {
                downEvent.recycle()
            }
            return SUCCESS
        }

        fun interpolate(start: FloatArray, end: FloatArray, steps: Int): Array<FloatArray> {
            Preconditions.checkElementIndex(1, start.size)
            Preconditions.checkElementIndex(1, end.size)
            val res = Array(steps) { FloatArray(2) }
            for (i in 1 until steps + 1) {
                res[i - 1][0] = start[0] + (end[0] - start[0]) * i / (steps + 2f)
                res[i - 1][1] = start[1] + (end[1] - start[1]) * i / (steps + 2f)
            }
            return res
        }
    }
}
