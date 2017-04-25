package com.example.collapsingtoolbarlayoutsubtitle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.support.design.widget.SubtitleCollapsingToolbarLayout;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class SubtitleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitle);
        SubtitleCollapsingToolbarLayout subtitlecollapsingtoolbarlayout = (SubtitleCollapsingToolbarLayout) findViewById(R.id.subtitlecollapsingtoolbarlayout);
        // subtitlecollapsingtoolbarlayout.setExpandedTitleColor(ContextCompat.getColor(this, R.color.colorAccent));
        // subtitlecollapsingtoolbarlayout.setExpandedSubtitleColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }
}