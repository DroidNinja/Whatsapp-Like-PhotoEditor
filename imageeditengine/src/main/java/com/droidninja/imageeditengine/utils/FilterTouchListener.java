package com.droidninja.imageeditengine.utils;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.droidninja.imageeditengine.views.PhotoEditorView;

/**
 * @author Simon Lightfoot <simon@demondevelopers.com>
 */
public class FilterTouchListener implements View.OnTouchListener {
  private final ImageView mainImageView;
  private final int screenHeight;
  private final View filterLabel;
  private final PhotoEditorView photoEditorView;
  private final FloatingActionButton doneBtn;
  private float viewHeight;
  private View mView;
  private float mMotionDownY;

  public FilterTouchListener(View filterLayout, float filterLayoutHeight,
      final ImageView mainImageView, final PhotoEditorView photoEditorView, View filterLabel,
      FloatingActionButton doneBtn) {
    mView = filterLayout;
    this.filterLabel = filterLabel;
    this.doneBtn = doneBtn;
    this.mainImageView = mainImageView;
    this.photoEditorView = photoEditorView;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    ((Activity) mainImageView.getContext()).getWindowManager()
        .getDefaultDisplay()
        .getMetrics(displayMetrics);
    screenHeight = displayMetrics.heightPixels;
    viewHeight = filterLayoutHeight;
  }

  @Override public boolean onTouch(View v, MotionEvent e) {
    int action = e.getAction();
    float yPost = 0f;
    switch (action & e.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        mMotionDownY = e.getRawY() - mView.getTranslationY();
        break;
      case MotionEvent.ACTION_MOVE:
        yPost = e.getRawY() - mMotionDownY;
        Log.i(FilterTouchListener.class.getSimpleName(), String.valueOf(1f - Math.abs(yPost) / 1000)
            + "--"
            + yPost
            + " - "
            + viewHeight
            + " - "
            + mView.getY()
            + "s - "
            + screenHeight
            + " d="
            + (screenHeight - mView.getY()));
        if ((yPost >= 0 && yPost < viewHeight)) {
          mView.setTranslationY(yPost);
          filterLabel.setAlpha(Math.abs(yPost) / 1000);
          doneBtn.setAlpha(Math.abs(yPost) / 1000);
          //mainImageView.setScaleX(1f-Math.abs(yPost)/1000);
          //mainImageView.setScaleY(1f-Math.abs(yPost)/1000);
          Log.i(FilterTouchListener.class.getSimpleName(), "moved");
        }
        break;
      case MotionEvent.ACTION_CANCEL:
        Log.i(FilterTouchListener.class.getSimpleName(), "ACTION_CANCEL");
        break;
      case MotionEvent.ACTION_UP:

        yPost = e.getRawY() - mMotionDownY;
        Log.i(FilterTouchListener.class.getSimpleName(), "ACTION_UP" + yPost);
        float middle = viewHeight / 2;
        float diff = (screenHeight - mView.getY());
        if (diff < middle) {
          mView.animate().translationY(viewHeight);
          mainImageView.animate().scaleX(1f);
          mainImageView.animate().scaleY(1f);
          photoEditorView.animate().scaleX(1f);
          photoEditorView.animate().scaleY(1f);
          filterLabel.animate().alpha(1f);
          doneBtn.animate().alpha(1f);
        } else {
          mView.animate().translationY(0);
          mainImageView.animate().scaleX(0.7f);
          mainImageView.animate().scaleY(0.7f);
          photoEditorView.animate().scaleX(0.7f);
          photoEditorView.animate().scaleY(0.7f);
          filterLabel.animate().alpha(0f);
          doneBtn.animate().alpha(0f);
        }

        break;
    }
    return true;
  }
}