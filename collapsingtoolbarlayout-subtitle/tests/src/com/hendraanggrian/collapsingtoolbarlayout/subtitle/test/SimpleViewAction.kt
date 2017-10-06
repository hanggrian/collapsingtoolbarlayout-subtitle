package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test

import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.view.View

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
abstract class SimpleViewAction<T : View> @JvmOverloads constructor(
        val viewCls: Class<T>,
        val desc: String? = null,
        vararg val formatArgs: Any = emptyArray()
) : ViewAction {

    abstract fun onPerform(t: T)

    override fun getConstraints() = isAssignableFrom(viewCls)

    override fun getDescription() = desc

    @Suppress("UNCHECKED_CAST")
    override fun perform(uiController: UiController, view: View) = onPerform(view as T)
}