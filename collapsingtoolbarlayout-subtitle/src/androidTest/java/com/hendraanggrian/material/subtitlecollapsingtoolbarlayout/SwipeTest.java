package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SwipeTest {
    @Rule public ActivityTestRule<TestActivity> rule = new ActivityTestRule(TestActivity.class, false, true);
    private Snackbar snackbar;

    private static final float EDGE_FUXX_FACTOR = 0.083f;

    @Before
    public void initSnackbar() {
        onView(withId(R.id.frameLayout)).perform(Views.perform(FrameLayout.class, "Initiating snackbar", frameLayout -> {
            snackbar = Snackbar.make(frameLayout, "Please Wait", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }));
    }

    @Test
    public void swipe() {
        onView(withId(R.id.toolbarLayout)).perform(
            Views.perform(SubtitleCollapsingToolbarLayout.class, null, view -> {
                snackbar.setText("Swiping up...");
            }),
            swipeUp()
        );
        onView(withId(R.id.toolbar)).perform(
            Views.perform(Toolbar.class, null, toolbar -> snackbar.setText("Swiping down...")),
            swipeDown(),
            swipeDown(),
            swipeDown(),
            Views.perform(Toolbar.class, null, toolbar -> snackbar.setText("Done"))
        );
    }

    private static ViewAction swipeUp() {
        return ViewActions.actionWithAssertions(new GeneralSwipeAction(
            Swipe.SLOW,
            translate(GeneralLocation.BOTTOM_CENTER, 0f, -EDGE_FUXX_FACTOR),
            GeneralLocation.TOP_CENTER,
            Press.FINGER
        ));
    }

    private static ViewAction swipeDown() {
        return ViewActions.actionWithAssertions(new GeneralSwipeAction(
            Swipe.FAST,
            translate(GeneralLocation.TOP_CENTER, 0f, EDGE_FUXX_FACTOR),
            GeneralLocation.BOTTOM_CENTER,
            Press.FINGER
        ));
    }

    private static CoordinatesProvider translate(CoordinatesProvider coords, float dx, float dy) {
        return view -> {
            float[] xy = coords.calculateCoordinates(view);
            xy[0] += dx * view.getWidth();
            xy[1] += dy * view.getHeight();
            return xy;
        };
    }
}
