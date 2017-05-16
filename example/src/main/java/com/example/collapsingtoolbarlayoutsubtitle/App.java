package com.example.collapsingtoolbarlayoutsubtitle;

import android.app.Application;

import com.hendraanggrian.bundler.Bundler;

import butterknife.ButterKnife;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(true);
        Bundler.setDebug(true);
    }
}
