package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

public class Views {
    private Views() {
    }

    public static <T extends View> ViewAction perform(@NonNull Class<T> constraint, @NonNull Consumer<T> action) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(constraint);
            }

            @Override
            public String getDescription() {
                return constraint.getSimpleName();
            }

            @Override
            public void perform(UiController uiController, View view) {
                action.accept((T) view);
            }
        };
    }

    public static <T extends View> ViewAssertion check(@NonNull Consumer<T> action) {
        return (view, noViewFoundException) -> action.accept((T) view);
    }
}
