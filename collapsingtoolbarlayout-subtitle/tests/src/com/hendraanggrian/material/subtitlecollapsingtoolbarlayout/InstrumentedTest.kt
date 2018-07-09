package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout

import android.graphics.Color
import android.graphics.Typeface.createFromAsset
import android.view.Gravity.CENTER
import android.view.Gravity.END
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralLocation.BOTTOM_CENTER
import androidx.test.espresso.action.GeneralLocation.TOP_CENTER
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press.FINGER
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout
import com.hendraanggrian.material.errorbar.Errorbar
import com.hendraanggrian.material.errorbar.indefiniteErrorbar
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.activity.InstrumentedActivity
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class InstrumentedTest {

    @Rule @JvmField val rule = ActivityTestRule(InstrumentedActivity::class.java)
    private lateinit var errorbar: Errorbar

    @Before fun errorbarInit() {
        onView(withId(R.id.frameLayout)).perform(viewActionOf<FrameLayout> {
            errorbar = it.indefiniteErrorbar("Initializing ...")
        })
    }

    @Test fun gravity() {
        turn { }
        turn {
            it.expandedTitleGravity = END
            it.collapsedTitleGravity = CENTER
        }
    }

    @Test fun typeface() {
        turn { }
        turn {
            val assets = getTargetContext().assets
            it.setExpandedTitleTypeface(createFromAsset(assets, "SourceCodePro-Bold.ttf"))
            it.setExpandedSubtitleTypeface(createFromAsset(assets, "SourceCodePro-Regular.ttf"))
        }
    }

    private fun turn(perform: (SubtitleCollapsingToolbarLayout) -> Unit) {
        onView(withId(R.id.toolbarLayout)).perform(
            viewActionOf<SubtitleCollapsingToolbarLayout> {
                perform(it)
                errorbar.setImage(R.drawable.up)
                errorbar.setText("Swiping up...")
            },
            slowerSwipeUp())
        onView(withId(R.id.toolbar)).perform(
            viewActionOf<Toolbar> {
                errorbar.setImage(R.drawable.down)
                errorbar.setText("Swiping down...")
            },
            swipeDown(),
            swipeDown(),
            swipeDown(),
            swipeDown(),
            swipeDown(),
            viewActionOf<Toolbar> {
                errorbar.setImage(Color.TRANSPARENT)
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
            androidx.test.espresso.action.Swipe.SLOW,
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