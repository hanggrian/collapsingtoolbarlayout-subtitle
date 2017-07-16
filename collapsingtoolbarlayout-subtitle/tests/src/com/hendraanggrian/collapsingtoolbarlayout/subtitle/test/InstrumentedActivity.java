package com.hendraanggrian.collapsingtoolbarlayout.subtitle.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Hendra Anggrian (hendraanggrian@gmail.com)
 */
public class InstrumentedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrumented);
    }
}