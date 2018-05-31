package com.droidninja.imageeditengine.filter;

/**
 * Created by droidNinja on 05/03/18.
 */

public class FilterConfig {
  public int opacity;
  public int type;

  public FilterConfig(int position, int opacity) {
    this.opacity = opacity;
    this.type = position;
  }
}
