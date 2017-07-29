package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test

import android.graphics.Typeface
import android.support.design.widget.Errorbar
import android.support.design.widget.SubtitleCollapsingToolbarLayout
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.*
import android.support.test.espresso.action.ViewActions.actionWithAssertions
import android.support.test.espresso.action.ViewActions.swipeDown
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.Gravity
import android.widget.FrameLayout
import com.hendraanggrian.collapsingtoolbarlayout.subtitle.test.activity.InstrumentedActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {

    @Rule @JvmField val rule = ActivityTestRule(InstrumentedActivity::class.java)
    lateinit var errorbar: Errorbar

    @Before
    fun errorbarInit() {
        onView(withId(R.id.frameLayout)).perform(object : SimpleViewAction<FrameLayout>(FrameLayout::class.java) {
            override fun onPerform(t: FrameLayout) {
                errorbar = Errorbar.make(t, "", Errorbar.LENGTH_INDEFINITE)
                errorbar.show()
            }
        })
    }

    @Test
    @Throws(Exception::class)
    fun gravity() {
        turn({ })
        turn({ t ->
            t.expandedTitleGravity = Gravity.END
            t.collapsedTitleGravity = Gravity.CENTER
        })
    }

    @Test
    @Throws(Exception::class)
    fun typeface() {
        turn({ })
        turn({ t ->
            val assets = getTargetContext().assets
            t.expandedTitleTypeface = Typeface.createFromAsset(assets, "SourceCodePro-Bold.ttf")
            t.expandedSubtitleTypeface = Typeface.createFromAsset(assets, "SourceCodePro-Regular.ttf")
        })
    }

    private fun turn(perform: (SubtitleCollapsingToolbarLayout) -> Unit) {
        rule.activity.runOnUiThread {
            errorbar.setLogoResource(R.drawable.up)
            errorbar.setText("Swiping up...")
        }
        onView(withId(R.id.toolbarLayout)).perform(object : SimpleViewAction<SubtitleCollapsingToolbarLayout>(SubtitleCollapsingToolbarLayout::class.java) {
            override fun onPerform(t: SubtitleCollapsingToolbarLayout) {
                perform.invoke(t)
            }
        }, slowerSwipeUp())

        rule.activity.runOnUiThread {
            errorbar.setLogoResource(R.drawable.down)
            errorbar.setText("Swiping down...")
        }
        onView(withId(R.id.toolbar)).perform(swipeDown(), swipeDown(), swipeDown(), swipeDown(), swipeDown())

        rule.activity.runOnUiThread {
            errorbar.setLogoDrawable(null)
            errorbar.setText("Done")
        }
    }

    companion object {
        private val EDGE_FUZZ_FACTOR = 0.083f

        private fun slowerSwipeUp() = actionWithAssertions(GeneralSwipeAction(SlowerSwipe.INSTANCE,
                translate(GeneralLocation.BOTTOM_CENTER, 0f, -EDGE_FUZZ_FACTOR),
                GeneralLocation.TOP_CENTER, Press.FINGER))

        private fun slowerSwipeDown() = actionWithAssertions(GeneralSwipeAction(Swipe.SLOW,
                translate(GeneralLocation.TOP_CENTER, 0f, EDGE_FUZZ_FACTOR),
                GeneralLocation.BOTTOM_CENTER, Press.FINGER))

        private fun translate(coords: CoordinatesProvider, dx: Float, dy: Float) = CoordinatesProvider { view ->
            val xy = coords.calculateCoordinates(view)
            xy[0] += dx * view.width
            xy[1] += dy * view.height
            xy
        }
    }
}