package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public abstract class ScrimTester {

  @NonNull
  public abstract Context getContext();

  public abstract void set(@Nullable Drawable drawable);

  public abstract void setColor(@ColorInt int color);

  public abstract void setResources(@DrawableRes int res);

  @Nullable
  public abstract Drawable get();

  public void test() {
    final Drawable drawable1 = new ColorDrawable(Color.RED);
    set(drawable1);
    assertEquals(drawable1, get());

    final ColorDrawable drawable2 = new ColorDrawable(Color.GREEN);
    drawable2.setAlpha(0);
    setColor(Color.GREEN);
    assertEquals(drawable2.getColor(), ((ColorDrawable) get()).getColor());

    final Drawable drawable3 = ContextCompat.getDrawable(getContext(),
        android.R.drawable.btn_radio);
    setResources(android.R.drawable.btn_radio);
    assertTrue(getBitmap(drawable3).sameAs(getBitmap(get())));
  }

  // https://stackoverflow.com/questions/9125229/comparing-two-drawables-in-android
  private static Bitmap getBitmap(Drawable drawable) {
    final Bitmap result;
    if (drawable instanceof BitmapDrawable) {
      result = ((BitmapDrawable) drawable).getBitmap();
    } else {
      int width = drawable.getIntrinsicWidth();
      int height = drawable.getIntrinsicHeight();
      if (width <= 0) {
        width = 1;
      }
      if (height <= 0) {
        height = 1;
      }
      result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      final Canvas canvas = new Canvas(result);
      drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
      drawable.draw(canvas);
    }
    return result;
  }
}
