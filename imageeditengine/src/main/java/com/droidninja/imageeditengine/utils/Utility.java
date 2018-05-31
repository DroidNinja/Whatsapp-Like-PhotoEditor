package com.droidninja.imageeditengine.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import java.io.File;
import java.io.FileOutputStream;

public class Utility {

  //public static Drawable tintDrawable(Context context, @DrawableRes int drawableRes, @ColorRes int colorRes){
  //  Drawable drawable = ContextCompat.getDrawable(context,drawableRes);
  //  if(drawable!=null) {
  //    drawable.mutate();
  //    DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorRes));
  //  }
  //  return drawable;
  //}

  public static Drawable tintDrawable(Context context, @DrawableRes int drawableRes, int colorCode){
    Drawable drawable = ContextCompat.getDrawable(context,drawableRes);
    if(drawable!=null) {
      drawable.mutate();
      DrawableCompat.setTint(drawable, colorCode);
    }
    return drawable;
  }


  /**
   * Hides the soft keyboard
   */
  public static void hideSoftKeyboard(Activity context) {
    if (context.getCurrentFocus() != null) {
      InputMethodManager inputMethodManager =
          (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
    }
  }

  /**
   * Shows the soft keyboard
   */
  public static void showSoftKeyboard(Activity context, View view) {
    InputMethodManager inputMethodManager =
        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    view.requestFocus();
    inputMethodManager.showSoftInput(view, 0);
  }

  public static int dpToPx(Context context, int dp) {
    float density = context.getResources()
        .getDisplayMetrics()
        .density;
    return Math.round((float) dp * density);
  }

  public static String saveBitmap(Bitmap bitmap, String imagePath){
    try {
      File outputFile = new File(imagePath);
      //save the resized and compressed file to disk cache
      FileOutputStream bmpFile = new FileOutputStream(outputFile);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bmpFile);

      bmpFile.flush();
      bmpFile.close();
      return outputFile.getAbsolutePath();
    } catch (Exception e) {
      return null;
    }
  }

  public static String getCacheFilePath(Context context) {
    return context.getCacheDir()+ "edited_"+ System.currentTimeMillis() +".jpg";
  }

  public static int calculateInSampleSize(
      BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) >= reqHeight
          && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  public static Bitmap decodeBitmap(String imagePath,
      int reqWidth, int reqHeight) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(imagePath,options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeFile(imagePath, options);
  }
}
