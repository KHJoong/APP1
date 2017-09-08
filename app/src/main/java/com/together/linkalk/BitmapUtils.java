package com.together.linkalk;

/**
 * Created by kimhj on 2017-09-08.
 */

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;

import android.graphics.BitmapFactory.Options;

import android.util.FloatMath;


/**
 * BitmapUtils
 * @author Nilesh Patel
 */

public class BitmapUtils {
    /** Used to tag logs */
    @SuppressWarnings("unused")
    private static final String TAG = "BitmapUtils";

    public static Bitmap getSampledBitmap(String filePath, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = (int)Math.floor(((float)height / reqHeight)+0.5f); //Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = (int)Math.floor(((float)width / reqWidth)+0.5f); //Math.round((float)width / (float)reqWidth);
            }
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    public static BitmapSize getBitmapSize(String filePath) {
        Options options = new Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        return new BitmapSize(options.outWidth, options.outHeight);
    }

    public static BitmapSize getScaledSize(int originalWidth, int originalHeight, int numPixels) {
        float ratio = (float)originalWidth/originalHeight;

        int scaledHeight = (int)Math.sqrt((float)numPixels/ratio);
        int scaledWidth = (int)(ratio * Math.sqrt((float)numPixels/ratio));

        return new BitmapSize(scaledWidth, scaledHeight);
    }

    public static class BitmapSize {
        public int width;
        public int height;

        public BitmapSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}