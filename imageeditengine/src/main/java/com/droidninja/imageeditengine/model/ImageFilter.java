package com.droidninja.imageeditengine.model;

import android.graphics.Bitmap;

public class ImageFilter {
  public String filterName;
  public Bitmap filterImage;
  public int opacity = 255;
  public boolean isSelected;

  public ImageFilter(String filterName, Bitmap bitmap) {
    this.filterName = filterName;
    filterImage = bitmap;
  }

  public ImageFilter(String filterName) {
    this.filterName = filterName;
  }
}
