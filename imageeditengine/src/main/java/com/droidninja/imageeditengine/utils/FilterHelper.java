package com.droidninja.imageeditengine.utils;

import com.droidninja.imageeditengine.Constants;
import com.droidninja.imageeditengine.model.ImageFilter;
import java.util.ArrayList;

public class FilterHelper {

  public FilterHelper() {
  }

  public ArrayList<ImageFilter> getFilters(){
    ArrayList<ImageFilter> imageFilters = new ArrayList<>();
    imageFilters.add(new ImageFilter(Constants.FILTER_ORIGINAL));
    imageFilters.add(new ImageFilter(Constants.FILTER_ANSEL));
    imageFilters.add(new ImageFilter(Constants.FILTER_BW));
    imageFilters.add(new ImageFilter(Constants.FILTER_CYANO));
    imageFilters.add(new ImageFilter(Constants.FILTER_GEORGIA));
    imageFilters.add(new ImageFilter(Constants.FILTER_HDR));
    imageFilters.add(new ImageFilter(Constants.FILTER_INSTAFIX));
    imageFilters.add(new ImageFilter(Constants.FILTER_RETRO));
    imageFilters.add(new ImageFilter(Constants.FILTER_SAHARA));
    imageFilters.add(new ImageFilter(Constants.FILTER_SEPIA));
    imageFilters.add(new ImageFilter(Constants.FILTER_TESTINO));
    imageFilters.add(new ImageFilter(Constants.FILTER_XPRO));
    return imageFilters;
  }
}
