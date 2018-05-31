package com.droidninja.imageeditengine.filter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import com.droidninja.imageeditengine.Constants;
import com.droidninja.imageeditengine.model.ImageFilter;

/**
 * 图片处理类
 *
 * @author 潘易
 */
public class PhotoProcessing {
    private static final String TAG = "PhotoProcessing";

    public static Bitmap filterPhoto(Bitmap bitmap, ImageFilter filterConfig) {
        if (bitmap != null) {
            sendBitmapToNative(bitmap);
        }
        switch (filterConfig.filterName) {
            case Constants.FILTER_ORIGINAL: // Original
                break;
            case Constants.FILTER_INSTAFIX: // Instafix
                nativeApplyInstafix();
                break;
            case Constants.FILTER_ANSEL: // Ansel
                nativeApplyAnsel();
                break;
            case Constants.FILTER_TESTINO: // Testino
                nativeApplyTestino();
                break;
            case Constants.FILTER_XPRO: // XPro
                nativeApplyXPro();
                break;
            case Constants.FILTER_RETRO: // Retro
                nativeApplyRetro();
                break;
            case Constants.FILTER_BW: // Black & White
                nativeApplyBW();
                break;
            case Constants.FILTER_SEPIA: // Sepia
                nativeApplySepia();
                break;
            case Constants.FILTER_CYANO: // Cyano
                nativeApplyCyano();
                break;
            case Constants.FILTER_GEORGIA: // Georgia
                nativeApplyGeorgia();
                break;
            case Constants.FILTER_SAHARA: // Sahara
                nativeApplySahara();
                break;
            case Constants.FILTER_HDR: // HDR
                nativeApplyHDR();
                break;
        }
        Bitmap filteredBitmap = getBitmapFromNative(bitmap);
        nativeDeleteBitmap();

        return filteredBitmap;
    }

    public static Bitmap combineImages(Bitmap bmp1, Bitmap bmp2, int alpha) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        Log.i("hasAlpha",bmp2.hasAlpha()+"");
        canvas.drawBitmap(bmp1, new Matrix(), null);
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(alpha);
        canvas.drawBitmap(bmp2, 0, 0, alphaPaint);
        return bmOverlay;
    }

    // /////////////////////////////////////////////
    static {
        System.loadLibrary("photoprocessing");
    }

    public static native int nativeInitBitmap(int width, int height);

    public static native void nativeGetBitmapRow(int y, int[] pixels);

    public static native void nativeSetBitmapRow(int y, int[] pixels);

    public static native int nativeGetBitmapWidth();

    public static native int nativeGetBitmapHeight();

    public static native void nativeDeleteBitmap();

    public static native int nativeRotate90();

    public static native void nativeRotate180();

    public static native void nativeFlipHorizontally();

    public static native void nativeApplyInstafix();

    public static native void nativeApplyCustomFilter(int alpha);

    public static native void nativeApplyAnsel();

    public static native void nativeApplyTestino();

    public static native void nativeApplyXPro();

    public static native void nativeApplyRetro();

    public static native void nativeApplyBW();

    public static native void nativeApplySepia();

    public static native void nativeApplyCyano();

    public static native void nativeApplyGeorgia();

    public static native void nativeApplySahara();

    public static native void nativeApplyHDR();

    public static native void nativeLoadResizedJpegBitmap(byte[] jpegData,
                                                          int size, int maxPixels);

    public static native void nativeResizeBitmap(int newWidth, int newHeight);

    public static native void handleSmooth(Bitmap bitmap,float smoothValue);

    public static native void handleWhiteSkin(Bitmap bitmap,float whiteValue);

    public static native void handleSmoothAndWhiteSkin(Bitmap bitmap,float smoothValue,float whiteValue);

    public static native void freeBeautifyMatrix();

    private static void sendBitmapToNative(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        nativeInitBitmap(width, height);
        int[] pixels = new int[width];
        for (int y = 0; y < height; y++) {
            bitmap.getPixels(pixels, 0, width, 0, y, width, 1);
            nativeSetBitmapRow(y, pixels);
        }
    }

    private static Bitmap getBitmapFromNative(Bitmap bitmap) {
        int width = nativeGetBitmapWidth();
        int height = nativeGetBitmapHeight();

        if (bitmap == null || width != bitmap.getWidth()
                || height != bitmap.getHeight() || !bitmap.isMutable()) { // in
            Config config = Config.ARGB_8888;
            if (bitmap != null) {
                config = bitmap.getConfig();
                bitmap.recycle();
            }
            bitmap = Bitmap.createBitmap(width, height, config);
        }

        int[] pixels = new int[width];
        for (int y = 0; y < height; y++) {
            nativeGetBitmapRow(y, pixels);
            bitmap.setPixels(pixels, 0, width, 0, y, width, 1);
        }

        return bitmap;
    }

    public static Bitmap makeBitmapMutable(Bitmap bitmap) {
        sendBitmapToNative(bitmap);
        return getBitmapFromNative(bitmap);
    }

    public static Bitmap rotate(Bitmap bitmap, int angle) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Config config = bitmap.getConfig();
        nativeInitBitmap(width, height);
        sendBitmapToNative(bitmap);

        if (angle == 90) {
            nativeRotate90();
            bitmap.recycle();
            bitmap = Bitmap.createBitmap(height, width, config);
            bitmap = getBitmapFromNative(bitmap);
            nativeDeleteBitmap();
        } else if (angle == 180) {
            nativeRotate180();
            bitmap.recycle();
            bitmap = Bitmap.createBitmap(width, height, config);
            bitmap = getBitmapFromNative(bitmap);
            nativeDeleteBitmap();
        } else if (angle == 270) {
            nativeRotate180();
            nativeRotate90();
            bitmap.recycle();
            bitmap = Bitmap.createBitmap(height, width, config);
            bitmap = getBitmapFromNative(bitmap);
            nativeDeleteBitmap();
        }
        return bitmap;
    }

    public static Bitmap flipHorizontally(Bitmap bitmap) {
        nativeInitBitmap(bitmap.getWidth(), bitmap.getHeight());
        sendBitmapToNative(bitmap);
        nativeFlipHorizontally();
        bitmap = getBitmapFromNative(bitmap);
        nativeDeleteBitmap();
        return bitmap;
    }
}// end class
