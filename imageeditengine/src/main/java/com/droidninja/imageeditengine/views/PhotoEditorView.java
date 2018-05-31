package com.droidninja.imageeditengine.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Dimension;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.droidninja.imageeditengine.R;
import com.droidninja.imageeditengine.utils.KeyboardHeightProvider;
import com.droidninja.imageeditengine.utils.MultiTouchListener;
import com.droidninja.imageeditengine.utils.Utility;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhotoEditorView extends FrameLayout implements ViewTouchListener,
    KeyboardHeightProvider.KeyboardHeightObserver {

  RelativeLayout container;
  RecyclerView recyclerView;
  CustomPaintView customPaintView;
  private String folderName;
  private ImageView imageView;
  private ImageView deleteView;
  private ViewTouchListener viewTouchListener;
  private View selectedView;
  private int selectViewIndex;
  private EditText inputTextET;
  private KeyboardHeightProvider keyboardHeightProvider;
  private float initialY;
  private View containerView;

  public PhotoEditorView(Context context) {
    super(context);
    init(context, null, 0);
  }

  public PhotoEditorView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public PhotoEditorView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(Context context, AttributeSet attrs, int defStyle) {
    View view = inflate(getContext(), R.layout.photo_editor_view, null);
    container = view.findViewById(R.id.container);
    containerView = view.findViewById(R.id.container_view);
    recyclerView = view.findViewById(R.id.recyclerview);
    inputTextET = view.findViewById(R.id.add_text_et);
    customPaintView = view.findViewById(R.id.paint_view);
    inputTextET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          if (selectedView != null) {
            ((AutofitTextView) selectedView).setText(inputTextET.getText());
            Utility.hideSoftKeyboard((Activity) getContext());
          } else {
            createText(inputTextET.getText().toString());
            Utility.hideSoftKeyboard((Activity) getContext());
          }
          inputTextET.setVisibility(INVISIBLE);
        }
        return false;
      }
    });
    keyboardHeightProvider = new KeyboardHeightProvider((Activity) getContext());
    keyboardHeightProvider.setKeyboardHeightObserver(this);

    GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
    recyclerView.setLayoutManager(gridLayoutManager);

    StickerListAdapter stickerAdapter = new StickerListAdapter(new ArrayList<String>());
    recyclerView.setAdapter(stickerAdapter);

    view.post(new Runnable() {
      @Override public void run() {
        keyboardHeightProvider.start();
      }
    });
    inputTextET.post(new Runnable() {
      @Override public void run() {
        initialY = inputTextET.getY();
      }
    });
    addView(view);
  }

  public void showPaintView() {
    recyclerView.setVisibility(GONE);
    inputTextET.setVisibility(GONE);
    Utility.hideSoftKeyboard((Activity) getContext());
    customPaintView.bringToFront();
  }

  public void setBounds(RectF bitmapRect) {
    customPaintView.setBounds(bitmapRect);
  }

  public void setColor(int selectedColor) {
    customPaintView.setColor(selectedColor);
  }

  public int getColor() {
    return customPaintView.getColor();
  }

  public Bitmap getPaintBit() {
    return customPaintView.getPaintBit();
  }

  public void hidePaintView() {
    containerView.bringToFront();
  }

  //text mode methods
  public void setImageView(ImageView imageView, ImageView deleteButton,
      ViewTouchListener viewTouchListener) {
    this.imageView = imageView;
    this.deleteView = deleteButton;
    this.viewTouchListener = viewTouchListener;
  }

  public void setTextColor(int selectedColor) {
    AutofitTextView autofitTextView = null;
    if(selectedView!=null){
      autofitTextView = (AutofitTextView) selectedView;
      autofitTextView.setTextColor(selectedColor);
    }
    else {
      View view = getViewChildAt(selectViewIndex);
      if(view!=null && view instanceof AutofitTextView) {
        autofitTextView = (AutofitTextView) view;
        autofitTextView.setTextColor(selectedColor);
      }
    }
    inputTextET.setTextColor(selectedColor);
  }

  @SuppressLint("ClickableViewAccessibility") public void addText() {
    inputTextET.setVisibility(VISIBLE);
    recyclerView.setVisibility(GONE);
    containerView.bringToFront();
    inputTextET.setText(null);
    Utility.showSoftKeyboard((Activity) getContext(), inputTextET);
  }

  public void hideTextMode(){
    Utility.hideSoftKeyboard((Activity) getContext());
    inputTextET.setVisibility(INVISIBLE);
  }

  @SuppressLint("ClickableViewAccessibility") @Override public void setOnTouchListener(OnTouchListener l) {
    super.setOnTouchListener(l);
    containerView.setOnTouchListener(l);
  }

  @SuppressLint("ClickableViewAccessibility") private void createText(String text){
    final AutofitTextView autofitTextView =
        (AutofitTextView) LayoutInflater.from(getContext()).inflate(R.layout.text_editor, null);
    autofitTextView.setId(container.getChildCount());
    autofitTextView.setText(text);
    autofitTextView.setTextColor(inputTextET.getCurrentTextColor());
    autofitTextView.setMaxTextSize(Dimension.SP,50);
    MultiTouchListener multiTouchListener =
        new MultiTouchListener(deleteView, container, this.imageView, true, this);
    multiTouchListener.setOnMultiTouchListener(new MultiTouchListener.OnMultiTouchListener() {

      @Override public void onRemoveViewListener(View removedView) {
        container.removeView(removedView);
        inputTextET.setText(null);
        inputTextET.setVisibility(INVISIBLE);
        selectedView = null;
      }
    });
    multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
      @Override public void onClick(View currentView) {
        if(currentView!=null) {
          selectedView = currentView;
          selectViewIndex = currentView.getId();
          inputTextET.setVisibility(VISIBLE);
          inputTextET.setText(((AutofitTextView) currentView).getText());
          inputTextET.setSelection(inputTextET.getText().length());
          Log.i("ViewNum", ":" + selectViewIndex + " " + ((AutofitTextView) currentView).getText());
        }

        Utility.showSoftKeyboard((Activity) getContext(), inputTextET);
      }

      @Override public void onLongClick() {

      }
    });
    autofitTextView.setOnTouchListener(multiTouchListener);

    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    container.addView(autofitTextView, params);

    selectViewIndex = container.getChildAt(container.getChildCount()-1).getId();
    selectedView = null;
  }

  @Override public void onStartViewChangeListener(View view) {
    Utility.hideSoftKeyboard((Activity) getContext());
    if (viewTouchListener != null) {
      viewTouchListener.onStartViewChangeListener(view);
    }
  }

  @Override public void onStopViewChangeListener(View view) {
    if (viewTouchListener != null) {
      viewTouchListener.onStopViewChangeListener(view);
    }
  }

  private View getViewChildAt(int index){
    if(index>container.getChildCount()-1){
      return null;
    }
    return container.getChildAt(index);
  }

  @Override public void onKeyboardHeightChanged(int height, int orientation) {
    if(height == 0){
      inputTextET.setY(initialY);
      inputTextET.requestLayout();
    }else {

      float newPosition = initialY - height;
      inputTextET.setY(newPosition);
      inputTextET.requestLayout();
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    keyboardHeightProvider.close();
  }

  public void showStickers(String stickersFolder) {
    containerView.bringToFront();
    recyclerView.setVisibility(VISIBLE);
    inputTextET.setVisibility(GONE);
    Utility.hideSoftKeyboard((Activity) getContext());
    this.folderName = stickersFolder;
    StickerListAdapter stickerListAdapter = (StickerListAdapter) recyclerView.getAdapter();
    if(stickerListAdapter!=null){
      stickerListAdapter.setData(getStickersList(stickersFolder));
    }
  }

  public void hideStickers(){
    recyclerView.setVisibility(GONE);
  }

  private List<String> getStickersList(String folderName){
    AssetManager assetManager = getContext().getAssets();
    try {
      String[] lists = assetManager.list(folderName);
      return  Arrays.asList(lists);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @SuppressLint("ClickableViewAccessibility") public void onItemClick(Bitmap bitmap) {
    recyclerView.setVisibility(GONE);
    ImageView stickerImageView =
        (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.sticker_view, null);
    stickerImageView.setImageBitmap(bitmap);
    stickerImageView.setId(container.getChildCount());
    MultiTouchListener multiTouchListener =
        new MultiTouchListener(deleteView, container, this.imageView, true, this);
    multiTouchListener.setOnMultiTouchListener(new MultiTouchListener.OnMultiTouchListener() {

      @Override public void onRemoveViewListener(View removedView) {
        container.removeView(removedView);
        selectedView = null;
      }
    });
    multiTouchListener.setOnGestureControl(new MultiTouchListener.OnGestureControl() {
      @Override public void onClick(View currentView) {
        if(currentView!=null) {
          selectedView = currentView;
          selectViewIndex = currentView.getId();
        }
      }

      @Override public void onLongClick() {

      }
    });
    stickerImageView.setOnTouchListener(multiTouchListener);

    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    container.addView(stickerImageView, params);
  }

  public void reset() {
    container.removeAllViews();
    customPaintView.reset();
    invalidate();
  }

  public void crop(Rect cropRect) {
    container.removeAllViews();
    customPaintView.reset();
    invalidate();
  }

  public class StickerListAdapter extends RecyclerView.Adapter<StickerListAdapter.ViewHolder> {

    private List<String> stickers;

    public StickerListAdapter(ArrayList<String> list) {
      stickers = list;
    }

    public void setData(List<String> stickersList) {
      this.stickers = stickersList;
      notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

      public ViewHolder(View v) {
        super(v);
      }
    }

    public void add(int position, String item) {
      stickers.add(position, item);
      notifyItemInserted(position);
    }

    public void remove(int position) {
      stickers.remove(position);
      notifyItemRemoved(position);
    }

    @Override
    public StickerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      View v = inflater.inflate(R.layout.sticker_view, parent, false);
      // set the view's size, margins, paddings and layout parameters
      ViewHolder vh = new ViewHolder(v);
      return vh;
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      final String path = stickers.get(position);
      holder.itemView.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View view) {
          onItemClick(getImageFromAssetsFile(path));
        }
      });
      ((ImageView)holder.itemView).setImageBitmap(getImageFromAssetsFile(path));
    }

    @Override public int getItemCount() {
      return stickers.size();
    }

    private Bitmap getImageFromAssetsFile(String fileName) {
      Bitmap image = null;
      AssetManager am = getResources().getAssets();
      try {
        InputStream is = am.open(folderName +"/"+ fileName);
        image = BitmapFactory.decodeStream(is);
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return image;
    }
  }
}