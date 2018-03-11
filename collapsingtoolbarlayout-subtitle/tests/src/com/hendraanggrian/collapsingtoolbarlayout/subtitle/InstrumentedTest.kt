package com.hendraanggrian.collapsingtoolbarlayout.subtitle

import android.graphics.Typeface.createFromAsset
import android.support.design.widget.Errorbar
import android.support.design.widget.SubtitleCollapsingToolbarLayout
import android.support.design.widget.errorbar
import android.support.test.InstrumentationRegistry.getTargetContext
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.CoordinatesProvider
import android.support.test.espresso.action.GeneralLocation.BOTTOM_CENTER
import android.support.test.espresso.action.GeneralLocation.TOP_CENTER
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press.FINGER
import android.support.test.espresso.action.Swipe.SLOW
import android.support.test.espresso.action.ViewActions.actionWithAssertions
import android.support.test.espresso.action.ViewActions.swipeDown
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.Toolbar
import android.view.Gravity.CENTER
import android.view.Gravity.END
import android.widget.FrameLayout
import com.hendraanggrian.collapsingtoolbarlayout.subtitle.activity.InstrumentedActivity
import com.hendraanggrian.collapsingtoolbarlayout.subtitle.test.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {

    @Rule @JvmField val rule = ActivityTestRule(InstrumentedActivity::class.java)
    private lateinit var errorbar: Errorbar

    @Before
    fun errorbarInit() {
        onView(withId(R.id.frameLayout)).perform(viewActionOf<FrameLayout> {
            errorbar = errorbar(it, "Initializing ...")
        })
    }

    @Test
    @Throws(Exception::class)
    fun gravity() {
        turn({ })
        turn({
            it.expandedTitleGravity = END
            it.collapsedTitleGravity = CENTER
        })
    }

    @Test
    @Throws(Exception::class)
    fun typeface() {
        turn({ })
        turn({
            val assets = getTargetContext().assets
            it.expandedTitleTypeface = createFromAsset(assets, "SourceCodePro-Bold.ttf")
            it.expandedSubtitleTypeface = createFromAsset(assets, "SourceCodePro-Regular.ttf")
        })
    }

    private fun turn(perform: (SubtitleCollapsingToolbarLayout) -> Unit) {
        onView(withId(R.id.toolbarLayout)).perform(
            viewActionOf<SubtitleCollapsingToolbarLayout> {
                perform(it)
                errorbar.setLogoResource(R.drawable.up)
                errorbar.setText("Swiping up...")
            },
            slowerSwipeUp())
        onView(withId(R.id.toolbar)).perform(
            viewActionOf<Toolbar> {
                errorbar.setLogoResource(R.drawable.down)
                errorbar.setText("Swiping down...")
            },
            swipeDown(),
            swipeDown(),
            swipeDown(),
            swipeDown(),
            swipeDown(),
            viewActionOf<Toolbar> {
                errorbar.setLogoDrawable(null)
                errorbar.setText("Done")
            })
    }

    private companion object {
        private val SLOWER_SWIPE = SlowerSwipe()
        const val EDGE_FUZZ_FACTOR = 0.083f

        fun slowerSwipeUp(): ViewAction = actionWithAssertions(GeneralSwipeAction(
            SLOWER_SWIPE,
            translate(BOTTOM_CENTER, 0f, -EDGE_FUZZ_FACTOR),
            TOP_CENTER,
            FINGER))

        fun slowerSwipeDown(): ViewAction = actionWithAssertions(GeneralSwipeAction(
            SLOW,
            translate(TOP_CENTER, 0f, EDGE_FUZZ_FACTOR),
            BOTTOM_CENTER,
            FINGER))

        fun translate(coords: CoordinatesProvider, dx: Float, dy: Float) = CoordinatesProvider { view ->
            val xy = coords.calculateCoordinates(view)
            xy[0] += dx * view.width
            xy[1] += dy * view.height
            xy
        }
    }
}