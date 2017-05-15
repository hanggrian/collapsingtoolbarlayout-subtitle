package com.example.collapsingtoolbarlayoutsubtitle;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.hendraanggrian.bundler.Bundler;

import butterknife.BindViews;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindViews({R.id.button_main_default, R.id.button_main_subtitle}) Button[] buttons;

    @Override
    int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (Button button : buttons)
            button.setOnClickListener(this);
        Log.d("ASD", String.valueOf(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK));
    }

    @Override
    public void onClick(View v) {
        final int layoutRes;
        if (v.getId() == R.id.button_main_default)
            layoutRes = R.layout.activity_article_default;
        else
            layoutRes = R.layout.activity_article_subtitle;
        startActivity(new Intent(this, ArticleActivity.class)
                .putExtras(Bundler.wrap(ArticleActivity.class, layoutRes)));
    }
}