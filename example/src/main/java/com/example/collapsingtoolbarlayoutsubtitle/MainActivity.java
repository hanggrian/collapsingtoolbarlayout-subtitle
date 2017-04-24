package com.example.collapsingtoolbarlayoutsubtitle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button_main_default).setOnClickListener(this);
        findViewById(R.id.button_main_subtitle).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_main_default:
                startActivity(new Intent(this, DefaultActivity.class));
                break;
            case R.id.button_main_subtitle:
                startActivity(new Intent(this, SubtitleActivity.class));
                break;
        }
    }
}