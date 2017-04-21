package com.example.collapsingtoolbarlayoutsubtitle;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.hendraanggrian.widget.SubtitleCollapsingToolbarLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SubtitleCollapsingToolbarLayout subtitlecollapsingtoolbarlayout = (SubtitleCollapsingToolbarLayout) findViewById(R.id.subtitlecollapsingtoolbarlayout);
        subtitlecollapsingtoolbarlayout.setExpandedTitleColor(ContextCompat.getColor(this, R.color.colorAccent));
        subtitlecollapsingtoolbarlayout.setExpandedSubtitleColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }
}