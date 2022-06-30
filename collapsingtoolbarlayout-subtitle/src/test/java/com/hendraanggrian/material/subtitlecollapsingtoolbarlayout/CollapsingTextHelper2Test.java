package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build.VERSION_CODES;
import android.view.Gravity;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.internal.CollapsingTextHelper2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.LOLLIPOP)
@DoNotInstrument
public class CollapsingTextHelper2Test {

    private AppCompatActivity activity;
    private CollapsingTextHelper2 textHelper2;

    @Before
    public void setUpActivityAndResources() {
        activity = Robolectric.buildActivity(TestActivity.class).setup().get();
        textHelper2 = new CollapsingTextHelper2(new View(activity));
    }

    @Test
    public void textSize() {
        assertEquals(15f, textHelper2.getExpandedTextSize(), 0);
        assertEquals(15f, textHelper2.getExpandedTextSize2(), 0);
        assertEquals(15f, textHelper2.getCollapsedTextSize(), 0);
        assertEquals(15f, textHelper2.getCollapsedTextSize2(), 0);

        textHelper2.setExpandedTextSize(1f);
        textHelper2.setExpandedTextSize2(2f);
        assertEquals(1f, textHelper2.getExpandedTextSize(), 0);
        assertEquals(2f, textHelper2.getExpandedTextSize2(), 0);

        textHelper2.setCollapsedTextSize(3f);
        textHelper2.setCollapsedTextSize2(4f);
        assertEquals(3f, textHelper2.getCollapsedTextSize(), 0);
        assertEquals(4f, textHelper2.getCollapsedTextSize2(), 0);
    }

    @Test
    public void textColor() {
        assertNull(textHelper2.getExpandedTextColor());
        assertNull(textHelper2.getExpandedTextColor2());
        assertNull(textHelper2.getCollapsedTextColor());
        assertNull(textHelper2.getCollapsedTextColor2());

        textHelper2.setExpandedTextColor(ColorStateList.valueOf(Color.RED));
        textHelper2.setExpandedTextColor2(ColorStateList.valueOf(Color.GREEN));
        assertEquals(Color.RED, textHelper2.getExpandedTextColor().getDefaultColor());
        assertEquals(Color.GREEN, textHelper2.getExpandedTextColor2().getDefaultColor());

        textHelper2.setCollapsedTextColor(ColorStateList.valueOf(Color.BLUE));
        textHelper2.setCollapsedTextColor2(ColorStateList.valueOf(Color.CYAN));
        assertEquals(Color.BLUE, textHelper2.getCollapsedTextColor().getDefaultColor());
        assertEquals(Color.CYAN, textHelper2.getCollapsedTextColor2().getDefaultColor());
    }

    @Test
    public void gravity() {
        assertEquals(Gravity.CENTER_VERTICAL, textHelper2.getCollapsedTextGravity());
        assertEquals(Gravity.CENTER_VERTICAL, textHelper2.getExpandedTextGravity());

        textHelper2.setCollapsedTextGravity(Gravity.TOP);
        textHelper2.setExpandedTextGravity(Gravity.BOTTOM);
        assertEquals(Gravity.TOP, textHelper2.getCollapsedTextGravity());
        assertEquals(Gravity.BOTTOM, textHelper2.getExpandedTextGravity());
    }

    @Test
    public void typeface() {
        assertEquals(Typeface.DEFAULT, textHelper2.getCollapsedTypeface());
        assertEquals(Typeface.DEFAULT, textHelper2.getCollapsedTypeface2());
        assertEquals(Typeface.DEFAULT, textHelper2.getExpandedTypeface());
        assertEquals(Typeface.DEFAULT, textHelper2.getExpandedTypeface2());

        AssetManager assets = activity.getAssets();
        Typeface bold1 = Typeface.createFromAsset(assets, "OpenSans-Bold.ttf");
        Typeface regular1 = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf");
        Typeface bold2 = Typeface.createFromAsset(assets, "Lato-Bold.ttf");
        Typeface regular2 = Typeface.createFromAsset(assets, "Lato-Regular.ttf");
        textHelper2.setCollapsedTypeface(bold1);
        textHelper2.setCollapsedTypeface2(regular1);
        textHelper2.setExpandedTypeface(bold2);
        textHelper2.setExpandedTypeface2(regular2);
        assertEquals(bold1, textHelper2.getCollapsedTypeface());
        assertEquals(regular1, textHelper2.getCollapsedTypeface2());
        assertEquals(bold2, textHelper2.getExpandedTypeface());
        assertEquals(regular2, textHelper2.getExpandedTypeface2());
    }

    @Test
    public void expansionFraction() {
        assertEquals(0f, textHelper2.getExpansionFraction(), 0);

        textHelper2.setExpansionFraction(0.5f);
        assertEquals(0.5f, textHelper2.getExpansionFraction(), 0);
    }

    @Test
    public void text() {
        assertNull(textHelper2.getText());
        assertNull(textHelper2.getText2());

        textHelper2.setText("Title");
        textHelper2.setText2("Subtitle");
        assertEquals("Title", textHelper2.getText());
        assertEquals("Subtitle", textHelper2.getText2());
    }

    @Test
    public void maxLines() {
        assertEquals(1, textHelper2.getMaxLines());

        textHelper2.setMaxLines(2);
        assertEquals(2, textHelper2.getMaxLines());
    }
}
