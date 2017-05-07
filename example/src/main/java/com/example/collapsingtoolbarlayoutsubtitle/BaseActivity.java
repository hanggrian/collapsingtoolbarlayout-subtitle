package com.example.collapsingtoolbarlayoutsubtitle;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hendraanggrian.bundler.Bundler;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public abstract class BaseActivity extends AppCompatActivity {

    @LayoutRes
    abstract int getContentView();

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getExtras() != null)
            Bundler.bind(this);
        setContentView(getContentView());
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}