package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test;

import android.support.annotation.NonNull;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;
import android.view.View;

import com.hendraanggrian.widget.ErrorView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.actionWithAssertions;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

    private static final float EDGE_FUZZ_FACTOR = 0.083f;

    @Rule
    public ActivityTestRule<InstrumentedActivity> rule = new ActivityTestRule<>(InstrumentedActivity.class);

    @Test
    public void gravity() {
        turn(new OnPerform<SubtitleCollapsingToolbarLayout>() {
            @Override
            public void onPerform(SubtitleCollapsingToolbarLayout layout) {
            }
        });
        turn(new OnPerform<SubtitleCollapsingToolbarLayout>() {
            @Override
            public void onPerform(SubtitleCollapsingToolbarLayout layout) {
                layout.setExpandedTitleGravity(Gravity.END);
                layout.setCollapsedTitleGravity(Gravity.CENTER);
            }
        });
    }

    public static void turn(final OnPerform<SubtitleCollapsingToolbarLayout> perform) {
        onView(withId(R.id.errorView)).perform(new SimpleViewAction<ErrorView>(ErrorView.class) {
            @Override
            public void onPerform(ErrorView view) {
                view.setLogoDrawable(R.drawable.up);
                view.setText("Swiping up...");
            }
        });
        onView(withId(R.id.toolbarLayout)).perform(new SimpleViewAction<SubtitleCollapsingToolbarLayout>(SubtitleCollapsingToolbarLayout.class) {
            @Override
            public void onPerform(SubtitleCollapsingToolbarLayout layout) {
                perform.onPerform(layout);
            }
        }, slowerSwipeUp());
        onView(withId(R.id.errorView)).perform(new SimpleViewAction<ErrorView>(ErrorView.class) {
            @Override
            public void onPerform(ErrorView view) {
                view.setLogoDrawable(R.drawable.down);
                view.setText("Swiping down...");
            }
        });
        onView(withId(R.id.toolbar)).perform(swipeDown(), swipeDown(), swipeDown(), swipeDown(), swipeDown());
        onView(withId(R.id.errorView)).perform(new SimpleViewAction<ErrorView>(ErrorView.class) {
            @Override
            public void onPerform(ErrorView view) {
                view.setLogoDrawable(null);
                view.setText("Done");
            }
        });
    }

    @NonNull
    public static ViewAction slowerSwipeUp() {
        return actionWithAssertions(new GeneralSwipeAction(SlowerSwipe.INSTANCE,
                translate(GeneralLocation.BOTTOM_CENTER, 0, -EDGE_FUZZ_FACTOR),
                GeneralLocation.TOP_CENTER, Press.FINGER));
    }

    @NonNull
    public static ViewAction slowerSwipeDown() {
        return actionWithAssertions(new GeneralSwipeAction(Swipe.SLOW,
                translate(GeneralLocation.TOP_CENTER, 0, EDGE_FUZZ_FACTOR),
                GeneralLocation.BOTTOM_CENTER, Press.FINGER));
    }

    @NonNull
    private static CoordinatesProvider translate(final CoordinatesProvider coords, final float dx, final float dy) {
        return new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float xy[] = coords.calculateCoordinates(view);
                xy[0] += dx * view.getWidth();
                xy[1] += dy * view.getHeight();
                return xy;
            }
        };
    }
}