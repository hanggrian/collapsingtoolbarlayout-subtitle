package com.hendraanggrian.collapsingtoolbarlayout.subtitle

import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.view.View
import org.hamcrest.Matcher

@JvmOverloads
internal inline fun <reified T : View> viewActionOf(
    desc: String? = null,
    noinline onPerform: (T) -> Unit
): ViewAction = object : ViewAction {

    override fun getDescription() = desc ?: T::class.java.simpleName

    override fun getConstraints(): Matcher<View> = isAssignableFrom(T::class.java)

    override fun perform(uiController: UiController, view: View) = onPerform(view as T)
}