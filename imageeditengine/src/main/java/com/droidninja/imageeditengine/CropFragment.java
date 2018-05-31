package com.droidninja.imageeditengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.droidninja.imageeditengine.views.cropimage.CropImageView;

public class CropFragment extends BaseFragment implements View.OnClickListener{

  private OnFragmentInteractionListener mListener;
  private CropImageView cropImageView;
  private int currentAngle;

  public CropFragment() {
    // Required empty public constructor
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_crop, container, false);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public static CropFragment newInstance(Bitmap bitmap, Rect cropRect) {
    CropFragment cropFragment =  new CropFragment();
    Bundle bundle = new Bundle();
    bundle.putParcelable(ImageEditor.EXTRA_ORIGINAL,bitmap);
    bundle.putParcelable(ImageEditor.EXTRA_CROP_RECT,cropRect);
    cropFragment.setArguments(bundle);

    return cropFragment;
  }

  public void setImageBitmap(Bitmap bitmap) {
    cropImageView.setImageBitmap(bitmap);
  }

  public interface OnFragmentInteractionListener {
    void onImageCropped(Bitmap bitmap, Rect cropRect);
    void onCancelCrop();
  }

  @Override protected void initView(View view) {
    cropImageView = view.findViewById(R.id.image_iv);
    view.findViewById(R.id.cancel_tv).setOnClickListener(this);
    view.findViewById(R.id.back_iv).setOnClickListener(this);
    view.findViewById(R.id.rotate_iv).setOnClickListener(this);
    view.findViewById(R.id.done_tv).setOnClickListener(this);
    if(getArguments()!=null) {
      final Bitmap bitmapimage = getArguments().getParcelable(ImageEditor.EXTRA_ORIGINAL);
      if(bitmapimage!=null){
        cropImageView.setImageBitmap(bitmapimage);
        cropImageView.setAspectRatio(1, 1);
        cropImageView.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
        final Rect cropRect = getArguments().getParcelable(ImageEditor.EXTRA_CROP_RECT);
        if(cropRect!=null){
          cropImageView.setCropRect(cropRect);
        }
      }
    }
  }

  @Override public void onClick(View view) {
    if(view.getId()==R.id.rotate_iv){
      cropImageView.rotateImage(90);
    }
    else if(view.getId()==R.id.cancel_tv){
      mListener.onCancelCrop();
    }
    else if(view.getId()==R.id.done_tv){
      final Bitmap original = getArguments().getParcelable(ImageEditor.EXTRA_ORIGINAL);
      mListener.onImageCropped(cropImageView.getCroppedImage(), cropImageView.getCropRect());
    }
    else if(view.getId()==R.id.done_tv){
      getActivity().onBackPressed();
    }
  }
}
