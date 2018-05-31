package com.droidninja.imageeditengine.utils;

public interface TaskCallback<T> {
  void onTaskDone(T data);
}
