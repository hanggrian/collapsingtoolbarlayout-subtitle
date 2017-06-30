package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test;

import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ProgressBar;

import org.hamcrest.Matcher;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.hendraanggrian.collapsingtoolbarlayout.subtitle.test.NestedScrollViewAction.nestedScrollTo;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InstrumentedTest {

    private static final long DELAY_COUNTDOWN = 3000;

    @Rule
    public ActivityTestRule<InstrumentedActivity> rule = new ActivityTestRule<>(InstrumentedActivity.class);

    @Test
    public void test1_introduction() {
        onView(withId(R.id.textView))
                .perform(nestedScrollTo())
                .check(matches(isDisplayed()));
        onView(withId(R.id.collapsingToolbarLayout))
                .perform(delay());
    }

    @NonNull
    public ViewAction delay() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "delay for " + DELAY_COUNTDOWN;
            }

            @Override
            public void perform(UiController uiController, View view) {
                final ProgressBar progressBar = rule.getActivity().progressBar;
                new CountDownTimer(DELAY_COUNTDOWN, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar.setProgress((int) (progressBar.getMax() * millisUntilFinished / DELAY_COUNTDOWN), true);
                        } else {
                            progressBar.setProgress((int) (progressBar.getMax() * millisUntilFinished / DELAY_COUNTDOWN));
                        }
                    }

                    @Override
                    public void onFinish() {
                        progressBar.setVisibility(View.GONE);
                    }
                }.start();
                uiController.loopMainThreadForAtLeast(DELAY_COUNTDOWN);
            }
        };
    }
}