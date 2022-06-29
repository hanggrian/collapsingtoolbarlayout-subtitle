package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

class CustomThemeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R.style.AppTheme);
    }
}
