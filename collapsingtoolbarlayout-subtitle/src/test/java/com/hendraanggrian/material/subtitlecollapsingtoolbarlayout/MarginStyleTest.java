package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static org.junit.Assert.assertEquals;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.SubtitleCollapsingToolbarLayout;
import com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link SubtitleCollapsingToolbarLayout} with custom styling, sorted by original class. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
public class MarginStyleTest {

    private AppCompatActivity activity;
    private SubtitleCollapsingToolbarLayout toolbarLayout;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(StyleTestActivity.class).setup().get();
        toolbarLayout = (SubtitleCollapsingToolbarLayout) activity.getLayoutInflater()
            .inflate(R.layout.test_subtitlecollapsingtoolbarlayout, null);
    }

    @Test
    public void margin() {
        assertEquals(1, toolbarLayout.getExpandedTitleMarginStart(), 0);
        assertEquals(2, toolbarLayout.getExpandedTitleMarginEnd(), 0);
        assertEquals(3, toolbarLayout.getExpandedTitleMarginTop(), 0);
        assertEquals(4, toolbarLayout.getExpandedTitleMarginBottom(), 0);
    }

    private static class StyleTestActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            setTheme(com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.test.R.style.Theme_Margin);
        }
    }
}
