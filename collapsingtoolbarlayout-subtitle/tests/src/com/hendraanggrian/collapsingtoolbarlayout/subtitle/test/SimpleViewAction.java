package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public abstract class SimpleViewAction<T extends View> implements ViewAction, OnPerform<T> {

    @NonNull private final Class<T> viewCls;
    @Nullable private final String desc;

    SimpleViewAction(@NonNull Class<T> viewCls) {
        this.viewCls = viewCls;
        this.desc = null;
    }

    SimpleViewAction(@NonNull Class<T> viewCls, @NonNull String desc) {
        this.viewCls = viewCls;
        this.desc = desc;
    }

    SimpleViewAction(@NonNull Class<T> viewCls, @NonNull String format, @NonNull Object... formatArgs) {
        this.viewCls = viewCls;
        this.desc = String.format(format, formatArgs);
    }

    @Override
    public Matcher<View> getConstraints() {
        return isAssignableFrom(viewCls);
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void perform(UiController uiController, View view) {
        onPerform((T) view);
    }
}