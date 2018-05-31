package com.droidninja.imageeditengine.filter;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import com.droidninja.imageeditengine.model.ImageFilter;
import com.droidninja.imageeditengine.utils.FilterHelper;
import com.droidninja.imageeditengine.utils.TaskCallback;
import com.droidninja.imageeditengine.utils.Utility;
import java.util.ArrayList;

public final class GetFiltersTask extends AsyncTask<Void, Void, ArrayList<ImageFilter>> {
  private final TaskCallback<ArrayList<ImageFilter>> listenerRef;
  private Bitmap srcBitmap;

  public GetFiltersTask(TaskCallback<ArrayList<ImageFilter>> taskCallbackWeakReference, Bitmap srcBitmap) {
    this.srcBitmap = srcBitmap;
    this.listenerRef = taskCallbackWeakReference;
  }

  @Override protected void onCancelled() {
    super.onCancelled();
  }

  @Override protected void onPostExecute(ArrayList<ImageFilter> result) {
    super.onPostExecute(result);
    if(listenerRef!=null){
      listenerRef.onTaskDone(result);
    }
  }

  @Override protected ArrayList<ImageFilter> doInBackground(Void... params) {
    FilterHelper filterHelper = new FilterHelper();
    ArrayList<ImageFilter> filters = filterHelper.getFilters();
    for (int index = 0; index < filters.size(); index++) {
      ImageFilter imageFilter = filters.get(index);
      imageFilter.filterImage = PhotoProcessing.filterPhoto(getScaledBitmap(srcBitmap), imageFilter);
    }
    return filters;
  }

  @Override protected void onPreExecute() {
    super.onPreExecute();
  }

  private Bitmap getScaledBitmap(Bitmap srcBitmap){
    // Determine how much to scale down the image
    int srcWidth = srcBitmap.getWidth();
    int srcHeight = srcBitmap.getHeight();

    int targetWidth = 320;
    int targetHeight = 240;
    if(srcWidth<targetWidth || srcHeight<targetHeight) {
      return srcBitmap;
    }

    float scaleFactor =
        Math.max(
            (float) srcWidth / targetWidth,
            (float) srcHeight / targetHeight);

    return
        Bitmap.createScaledBitmap(
            srcBitmap,
            (int) (srcWidth/ scaleFactor),
            (int) (srcHeight / scaleFactor),
            true);
  }
}// end inner class