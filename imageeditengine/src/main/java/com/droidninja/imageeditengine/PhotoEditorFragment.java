package com.droidninja.imageeditengine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.droidninja.imageeditengine.adapters.FilterImageAdapter;
import com.droidninja.imageeditengine.filter.ApplyFilterTask;
import com.droidninja.imageeditengine.filter.GetFiltersTask;
import com.droidninja.imageeditengine.filter.PhotoProcessing;
import com.droidninja.imageeditengine.filter.ProcessingImage;
import com.droidninja.imageeditengine.model.ImageFilter;
import com.droidninja.imageeditengine.utils.FilterHelper;
import com.droidninja.imageeditengine.utils.FilterTouchListener;
import com.droidninja.imageeditengine.utils.Matrix3;
import com.droidninja.imageeditengine.utils.TaskCallback;
import com.droidninja.imageeditengine.utils.Utility;
import com.droidninja.imageeditengine.views.PhotoEditorView;
import com.droidninja.imageeditengine.views.VerticalSlideColorPicker;
import com.droidninja.imageeditengine.views.ViewTouchListener;
import com.droidninja.imageeditengine.views.imagezoom.ImageViewTouch;
import java.util.ArrayList;

public class PhotoEditorFragment extends BaseFragment
    implements View.OnClickListener, ViewTouchListener,
    FilterImageAdapter.FilterImageAdapterListener {

  ImageViewTouch mainImageView;
  ImageView cropButton;
  ImageView stickerButton;
  ImageView addTextButton;
  PhotoEditorView photoEditorView;
  ImageView paintButton;
  ImageView deleteButton;
  VerticalSlideColorPicker colorPickerView;
  //CustomPaintView paintEditView;
  View toolbarLayout;
  RecyclerView filterRecylerview;
  View filterLayout;
  View filterLabel;
  FloatingActionButton doneBtn;
  private Bitmap mainBitmap;
  private LruCache<Integer, Bitmap> cacheStack;
  private int filterLayoutHeight;
  private OnFragmentInteractionListener mListener;
  public static final int MODE_NONE = 0;
  public static final int MODE_PAINT = 1;
  public static final int MODE_ADD_TEXT = 2;
  public static final int MODE_STICKER = 3;

  protected int currentMode;
  private ImageFilter selectedFilter;
  private Bitmap originalBitmap;

  public static PhotoEditorFragment newInstance(String imagePath) {
    Bundle bundle = new Bundle();
    bundle.putString(ImageEditor.EXTRA_IMAGE_PATH, imagePath);
    PhotoEditorFragment photoEditorFragment = new PhotoEditorFragment();
    photoEditorFragment.setArguments(bundle);
    return photoEditorFragment;
  }

  public PhotoEditorFragment() {
    // Required empty public constructor
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_photo_editor, container, false);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
  }



  public interface OnFragmentInteractionListener {
    void onCropClicked(Bitmap bitmap);

    void onDoneClicked(String imagePath);
  }

  public void setImageBitmap(Bitmap bitmap) {
    mainImageView.setImageBitmap(bitmap);
    mainImageView.post(new Runnable() {
      @Override public void run() {
        photoEditorView.setBounds(mainImageView.getBitmapRect());
      }
    });
  }

  public void setImageWithRect(Rect rect) {
    mainBitmap = getScaledBitmap(getCroppedBitmap(getBitmapCache(originalBitmap), rect));
    mainImageView.setImageBitmap(mainBitmap);
    mainImageView.post(new Runnable() {
      @Override public void run() {
        photoEditorView.setBounds(mainImageView.getBitmapRect());
      }
    });

    new GetFiltersTask(new TaskCallback<ArrayList<ImageFilter>>() {
      @Override public void onTaskDone(ArrayList<ImageFilter> data) {
        FilterImageAdapter filterImageAdapter = (FilterImageAdapter) filterRecylerview.getAdapter();
        if (filterImageAdapter != null) {
          filterImageAdapter.setData(data);
          filterImageAdapter.notifyDataSetChanged();
        }
      }
    }, mainBitmap).execute();
  }

  private Bitmap getScaledBitmap(Bitmap resource){
    int currentBitmapWidth = resource.getWidth();
    int currentBitmapHeight = resource.getHeight();
    int ivWidth = mainImageView.getWidth();
    int newHeight = (int) Math.floor(
        (double) currentBitmapHeight * ((double) ivWidth / (double) currentBitmapWidth));
    return Bitmap.createScaledBitmap(resource, ivWidth, newHeight, true);
  }

  private Bitmap getCroppedBitmap(Bitmap srcBitmap, Rect rect){
    // Crop the subset from the original Bitmap.
    return Bitmap.createBitmap(srcBitmap,
        rect.left,
        rect.top,
        (rect.right-rect.left),
        (rect.bottom-rect.top));
  }

  public void reset(){
    photoEditorView.reset();
  }

  protected void initView(View view) {
    mainImageView = view.findViewById(R.id.image_iv);
    cropButton = view.findViewById(R.id.crop_btn);
    stickerButton = view.findViewById(R.id.stickers_btn);
    addTextButton = view.findViewById(R.id.add_text_btn);
    deleteButton = view.findViewById(R.id.delete_view);
    photoEditorView = view.findViewById(R.id.photo_editor_view);
    paintButton = view.findViewById(R.id.paint_btn);
    colorPickerView = view.findViewById(R.id.color_picker_view);
    //paintEditView = findViewById(R.id.paint_edit_view);
    toolbarLayout = view.findViewById(R.id.toolbar_layout);
    filterRecylerview = view.findViewById(R.id.filter_list_rv);
    filterLayout = view.findViewById(R.id.filter_list_layout);
    filterLabel = view.findViewById(R.id.filter_label);
    doneBtn = view.findViewById(R.id.done_btn);

    if (getArguments() != null && getActivity()!=null && getActivity().getIntent()!=null) {
      final String imagePath = getArguments().getString(ImageEditor.EXTRA_IMAGE_PATH);
      //mainImageView.post(new Runnable() {
      //  @Override public void run() {
      //    mainBitmap = Utility.decodeBitmap(imagePath,mainImageView.getWidth(),mainImageView.getHeight());
      //
      //  }
      //});
      Glide.with(this).asBitmap().load(imagePath).into(new SimpleTarget<Bitmap>() {
        @Override public void onResourceReady(@NonNull Bitmap resource,
            @Nullable Transition<? super Bitmap> transition) {
          int currentBitmapWidth = resource.getWidth();
          int currentBitmapHeight = resource.getHeight();
          int ivWidth = mainImageView.getWidth();
          int newHeight = (int) Math.floor(
              (double) currentBitmapHeight * ((double) ivWidth / (double) currentBitmapWidth));
          originalBitmap = Bitmap.createScaledBitmap(resource, ivWidth, newHeight, true);
          mainBitmap = originalBitmap;
          setImageBitmap(mainBitmap);

          new GetFiltersTask(new TaskCallback<ArrayList<ImageFilter>>() {
            @Override public void onTaskDone(ArrayList<ImageFilter> data) {
              FilterImageAdapter filterImageAdapter = (FilterImageAdapter) filterRecylerview.getAdapter();
              if (filterImageAdapter != null) {
                filterImageAdapter.setData(data);
                filterImageAdapter.notifyDataSetChanged();
              }
            }
          }, mainBitmap).execute();
        }
      });


      Intent intent = getActivity().getIntent();
      setVisibility(addTextButton,intent.getBooleanExtra(ImageEditor.EXTRA_IS_TEXT_MODE, false));
      setVisibility(cropButton,intent.getBooleanExtra(ImageEditor.EXTRA_IS_CROP_MODE, false));
      setVisibility(stickerButton,intent.getBooleanExtra(ImageEditor.EXTRA_IS_STICKER_MODE, false));
      setVisibility(paintButton,intent.getBooleanExtra(ImageEditor.EXTRA_IS_PAINT_MODE, false));
      setVisibility(filterLayout,intent.getBooleanExtra(ImageEditor.EXTRA_HAS_FILTERS, false));



      photoEditorView.setImageView(mainImageView, deleteButton, this);
      //stickerEditorView.setImageView(mainImageView, deleteButton,this);
      cropButton.setOnClickListener(this);
      stickerButton.setOnClickListener(this);
      addTextButton.setOnClickListener(this);
      paintButton.setOnClickListener(this);
      doneBtn.setOnClickListener(this);
      view.findViewById(R.id.back_iv).setOnClickListener(this);

      colorPickerView.setOnColorChangeListener(
          new VerticalSlideColorPicker.OnColorChangeListener() {
            @Override public void onColorChange(int selectedColor) {
              if (currentMode == MODE_PAINT) {
                paintButton.setBackground(
                    Utility.tintDrawable(getContext(), R.drawable.circle, selectedColor));
                photoEditorView.setColor(selectedColor);
              } else if (currentMode == MODE_ADD_TEXT) {
                addTextButton.setBackground(
                    Utility.tintDrawable(getContext(), R.drawable.circle, selectedColor));
                photoEditorView.setTextColor(selectedColor);
              }
            }
          });
      photoEditorView.setColor(colorPickerView.getDefaultColor());
      photoEditorView.setTextColor(colorPickerView.getDefaultColor());

      if(intent.getBooleanExtra(ImageEditor.EXTRA_HAS_FILTERS, false)) {
        filterLayout.post(new Runnable() {
          @SuppressLint("ClickableViewAccessibility") @Override public void run() {
            filterLayoutHeight = filterLayout.getHeight();
            filterLayout.setTranslationY(filterLayoutHeight);
            photoEditorView.setOnTouchListener(
                new FilterTouchListener(filterLayout, filterLayoutHeight, mainImageView,
                    photoEditorView, filterLabel, doneBtn));
          }
        });

        FilterHelper filterHelper = new FilterHelper();
        filterRecylerview.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        FilterImageAdapter filterImageAdapter =
            new FilterImageAdapter(filterHelper.getFilters(), this);
        filterRecylerview.setAdapter(filterImageAdapter);
      }
    }
  }

  protected void onModeChanged(int currentMode) {
    Log.i(ImageEditActivity.class.getSimpleName(), "CM: " + currentMode);
    onStickerMode(currentMode == MODE_STICKER);
    onAddTextMode(currentMode == MODE_ADD_TEXT);
    onPaintMode(currentMode == MODE_PAINT);

    if (currentMode == MODE_PAINT || currentMode == MODE_ADD_TEXT) {
      AnimationHelper.animate(getContext(), colorPickerView, R.anim.slide_in_right, View.VISIBLE,
          null);
    } else {
      AnimationHelper.animate(getContext(), colorPickerView, R.anim.slide_out_right, View.INVISIBLE,
          null);
    }
  }

  @Override public void onClick(final View view) {
    int id = view.getId();
    if (id == R.id.crop_btn) {
      if(selectedFilter!=null){
        new ApplyFilterTask(new TaskCallback<Bitmap>() {
          @Override public void onTaskDone(Bitmap data) {
            if(data!=null) {
              mListener.onCropClicked(getBitmapCache(data));
              photoEditorView.hidePaintView();
            }
          }
        }, Bitmap.createBitmap(originalBitmap)).execute(selectedFilter);
      }
      else{
        mListener.onCropClicked(getBitmapCache(originalBitmap));
        photoEditorView.hidePaintView();
      }
    } else if (id == R.id.stickers_btn) {
      setMode(MODE_STICKER);
    } else if (id == R.id.add_text_btn) {
      setMode(MODE_ADD_TEXT);
    } else if (id == R.id.paint_btn) {
      setMode(MODE_PAINT);
    } else if (id == R.id.back_iv) {
      getActivity().onBackPressed();
    }else if (id == R.id.done_btn) {
      if(selectedFilter!=null) {
        new ApplyFilterTask(new TaskCallback<Bitmap>() {
          @Override public void onTaskDone(Bitmap data) {
            if (data != null) {
              new ProcessingImage(getBitmapCache(data), Utility.getCacheFilePath(view.getContext()),
                  new TaskCallback<String>() {
                    @Override public void onTaskDone(String data) {
                      mListener.onDoneClicked(data);
                    }
                  }).execute();
            }
          }
        }, Bitmap.createBitmap(mainBitmap)).execute(selectedFilter);
      }
      else{
        new ProcessingImage(getBitmapCache(mainBitmap), Utility.getCacheFilePath(view.getContext()),
            new TaskCallback<String>() {
              @Override public void onTaskDone(String data) {
                mListener.onDoneClicked(data);
              }
            }).execute();
      }
    }

    if (currentMode != MODE_NONE) {
      filterLabel.setAlpha(0f);
      mainImageView.animate().scaleX(1f);
      photoEditorView.animate().scaleX(1f);
      mainImageView.animate().scaleY(1f);
      photoEditorView.animate().scaleY(1f);
      filterLayout.animate().translationY(filterLayoutHeight);
      //touchView.setVisibility(View.GONE);
    } else {
      filterLabel.setAlpha(1f);
      //touchView.setVisibility(View.VISIBLE);
    }
  }

  private void onAddTextMode(boolean status) {
    if (status) {
      addTextButton.setBackground(
          Utility.tintDrawable(getContext(), R.drawable.circle, photoEditorView.getColor()));
      //photoEditorView.setTextColor(photoEditorView.getColor());
      photoEditorView.addText();
    } else {
      addTextButton.setBackground(null);
      photoEditorView.hideTextMode();
    }
  }

  private void onPaintMode(boolean status) {
    if (status) {
      paintButton.setBackground(
          Utility.tintDrawable(getContext(), R.drawable.circle, photoEditorView.getColor()));
      photoEditorView.showPaintView();
      //paintEditView.setVisibility(View.VISIBLE);
    } else {
      paintButton.setBackground(null);
      photoEditorView.hidePaintView();
      //photoEditorView.enableTouch(true);
      //paintEditView.setVisibility(View.GONE);
    }
  }

  private void onStickerMode(boolean status) {
    if (status) {
      stickerButton.setBackground(
          Utility.tintDrawable(getContext(), R.drawable.circle, photoEditorView.getColor()));
      if(getActivity()!=null && getActivity().getIntent()!=null) {
        String folderName = getActivity().getIntent().getStringExtra(ImageEditor.EXTRA_STICKER_FOLDER_NAME);
        photoEditorView.showStickers(folderName);
      }
    } else {
      stickerButton.setBackground(null);
      photoEditorView.hideStickers();
    }
  }

  @Override public void onStartViewChangeListener(final View view) {
    Log.i(ImageEditActivity.class.getSimpleName(), "onStartViewChangeListener" + "" + view.getId());
    toolbarLayout.setVisibility(View.GONE);
    AnimationHelper.animate(getContext(), deleteButton, R.anim.fade_in_medium, View.VISIBLE, null);
  }

  @Override public void onStopViewChangeListener(View view) {
    Log.i(ImageEditActivity.class.getSimpleName(), "onStopViewChangeListener" + "" + view.getId());
    deleteButton.setVisibility(View.GONE);
    AnimationHelper.animate(getContext(), toolbarLayout, R.anim.fade_in_medium, View.VISIBLE, null);
  }

  private Bitmap getBitmapCache(Bitmap bitmap) {
    Matrix touchMatrix = mainImageView.getImageViewMatrix();

    Bitmap resultBit = Bitmap.createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true);
    Canvas canvas = new Canvas(resultBit);

    float[] data = new float[9];
    touchMatrix.getValues(data);
    Matrix3 cal = new Matrix3(data);
    Matrix3 inverseMatrix = cal.inverseMatrix();
    Matrix m = new Matrix();
    m.setValues(inverseMatrix.getValues());

    float[] f = new float[9];
    m.getValues(f);
    int dx = (int) f[Matrix.MTRANS_X];
    int dy = (int) f[Matrix.MTRANS_Y];
    float scale_x = f[Matrix.MSCALE_X];
    float scale_y = f[Matrix.MSCALE_Y];
    canvas.save();
    canvas.translate(dx, dy);
    canvas.scale(scale_x, scale_y);

    photoEditorView.setDrawingCacheEnabled(true);
    if (photoEditorView.getDrawingCache() != null) {
      canvas.drawBitmap(photoEditorView.getDrawingCache(), 0, 0, null);
    }

    if (photoEditorView.getPaintBit() != null) {
      canvas.drawBitmap(photoEditorView.getPaintBit(), 0, 0, null);
    }

    canvas.restore();
    return resultBit;
  }

  @Override public void onFilterSelected(ImageFilter imageFilter) {
    selectedFilter = imageFilter;
    new ApplyFilterTask(new TaskCallback<Bitmap>() {
      @Override public void onTaskDone(Bitmap data) {
        if(data!=null) {
          setImageBitmap(data);
        }
      }
    }, Bitmap.createBitmap(mainBitmap)).execute(imageFilter);
  }

  protected void setMode(int mode) {
    if (currentMode != mode) {
      onModeChanged(mode);
    } else {
      mode = MODE_NONE;
      onModeChanged(mode);
    }
    this.currentMode = mode;
  }
}
