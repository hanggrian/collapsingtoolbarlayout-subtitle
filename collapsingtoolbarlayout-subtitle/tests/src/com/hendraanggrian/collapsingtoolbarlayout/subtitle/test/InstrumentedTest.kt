package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test

import android.graphics.Typeface
import android.support.design.widget.Errorbar
import android.support.design.widget.SubtitleCollapsingToolbarLayout
import android.support.design.widget.errorbar
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.*
import android.support.test.espresso.action.ViewActions.actionWithAssertions
import android.support.test.espresso.action.ViewActions.swipeDown
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.widget.FrameLayout
import com.hendraanggrian.collapsingtoolbarlayout.subtitle.test.activity.InstrumentedActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class InstrumentedTest {

    @Rule @JvmField val rule = ActivityTestRule(InstrumentedActivity::class.java)
    lateinit var errorbar: Errorbar

    @Before
    fun errorbarInit() {
        onView(withId(R.id.frameLayout)).perform(object : SimpleViewAction<FrameLayout>(FrameLayout::class.java) {
            override fun onPerform(t: FrameLayout) {
                errorbar = errorbar(t, "")
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
        onView(withId(R.id.toolbarLayout)).perform(
                object : SimpleViewAction<SubtitleCollapsingToolbarLayout>(SubtitleCollapsingToolbarLayout::class.java) {
                    override fun onPerform(t: SubtitleCollapsingToolbarLayout) {
                        perform.invoke(t)
                        errorbar.setLogoResource(R.drawable.up)
                        errorbar.setText("Swiping up...")
                    }
                },
                slowerSwipeUp())
        onView(withId(R.id.toolbar)).perform(
                object : SimpleViewAction<Toolbar>(Toolbar::class.java) {
                    override fun onPerform(t: Toolbar) {
                        errorbar.setLogoResource(R.drawable.down)
                        errorbar.setText("Swiping down...")
                    }
                },
                swipeDown(),
                swipeDown(),
                swipeDown(),
                swipeDown(),
                swipeDown(),
                object : SimpleViewAction<Toolbar>(Toolbar::class.java) {
                    override fun onPerform(t: Toolbar) {
                        errorbar.setLogoDrawable(null)
                        errorbar.setText("Done")
                    }
                })
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