package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import org.hamcrest.Matcher

inline fun <reified T : View> viewActionOf(
    desc: String? = null,
    noinline onPerform: (T) -> Unit
): ViewAction = object : ViewAction {
    override fun getDescription() = desc ?: T::class.java.simpleName
    override fun getConstraints(): Matcher<View> = isAssignableFrom(T::class.java)
    override fun perform(uiController: UiController, view: View) = onPerform(view as T)
}