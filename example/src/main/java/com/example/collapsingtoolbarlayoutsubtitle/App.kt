package com.example.collapsingtoolbarlayoutsubtitle

import android.app.Application
import butterknife.ButterKnife
import com.hendraanggrian.bundler.Bundler

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ButterKnife.setDebug(BuildConfig.DEBUG)
        Bundler.setDebug(BuildConfig.DEBUG)
    }
}