package com.together.linkalk;

/**
 * Created by kimhj on 2017-09-08.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class PhotoFilterActivity extends Activity implements OnLongClickListener {
    private static final String TAG = "PhotoProcessingActivity";

    public static final int REQUEST_CODE_SELECT_PHOTO = 1;
    public static final int REQUEST_CODE_CAMERA = 2;

    private static final String SAVE_STATE_PATH = "com.together.linkalk.PhotoProcessing.mOriginalPhotoPath";
    private static final String SAVE_CURRENT_FILTER = "com.together.linkalk.PhotoProcessing.mCurrentFilter";
    private static final String SAVE_EDIT_ACTIONS = "com.together.linkalk.PhotoProcessing.mEditActions";
    private static final String SAVE_CAMERA_FILE_PATH = "com.together.linkalk.PhotoProcessing.mCurrentCameraFilePath";

    private String mOriginalPhotoPath = null;
    private Bitmap mBitmap = null;
    private ImageView mImageView = null;
    private ListView mFilterListView = null;
    private boolean mIsFilterListShowing = false;
    private boolean mIsEditListShowing = false;

    private int mCurrentFilter = 0;
    private ArrayList<Integer> mEditActions = new ArrayList<Integer>();

    private static FilterTask sFilterTask;
    private static SavePhotoTask sSavePhotoTask;

    private ProgressDialog mProgressDialog = null;

    private Vibrator mVibrator = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_filter_activity);

        mImageView = (ImageView)findViewById(R.id.imageViewPhoto);

        mFilterListView = (ListView)findViewById(R.id.filterList);
        mFilterListView.setVisibility(View.INVISIBLE);
        mFilterListView.setAdapter(new FilterListAdapter(this));
        mFilterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                sFilterTask = new FilterTask(PhotoFilterActivity.this);
                mCurrentFilter = position;
                sFilterTask.execute(position);
            }
        });

        findViewById(R.id.buttonFilter).setEnabled(false);
        findViewById(R.id.buttonSave).setEnabled(false);

        ImageButton galleryButton = (ImageButton)findViewById(R.id.buttonGallery);
        galleryButton.setOnLongClickListener(this);
        ImageButton filterButton = (ImageButton)findViewById(R.id.buttonFilter);
        filterButton.setOnLongClickListener(this);
        filterButton.setEnabled(false);
        ImageButton saveButton = (ImageButton)findViewById(R.id.buttonSave);
        saveButton.setOnLongClickListener(this);
        saveButton.setEnabled(false);

        mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onPause() {
        hideProgressDialog();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (sFilterTask != null) {
            sFilterTask.reattachActivity(this);
        }
        if (sSavePhotoTask != null) {
            sSavePhotoTask.reattachActivity(this);
        }
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_STATE_PATH, mOriginalPhotoPath);
        outState.putInt(SAVE_CURRENT_FILTER, mCurrentFilter);
        outState.putIntegerArrayList(SAVE_EDIT_ACTIONS, mEditActions);
        outState.putString(SAVE_CAMERA_FILE_PATH, mOriginalPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mOriginalPhotoPath = savedInstanceState.getString(SAVE_STATE_PATH);
        mCurrentFilter = savedInstanceState.getInt(SAVE_CURRENT_FILTER);
        mEditActions = savedInstanceState.getIntegerArrayList(SAVE_EDIT_ACTIONS);
        if (mEditActions == null) {
            mEditActions = new ArrayList<Integer>();
        }
        String currentCameraFilePath = savedInstanceState.getString(SAVE_CAMERA_FILE_PATH);
        if (currentCameraFilePath != null) {
            mOriginalPhotoPath = currentCameraFilePath;
        }
        if (mOriginalPhotoPath != null) {
            loadFromCache();
            mImageView.setImageBitmap(mBitmap);
        }
    }

    @Override
    public void onBackPressed() {
        if (mIsFilterListShowing) {
            hideFilterList();
        } else if (mIsEditListShowing) {
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == RESULT_OK) {
            if (mEditActions != null) {
                mEditActions.clear();
            }
            Uri photoUri = data.getData();
            mImageView.setImageBitmap(null);
            mOriginalPhotoPath = MediaUtils.getPath(this, photoUri);
            loadPhoto(mOriginalPhotoPath);
            mImageView.setImageBitmap(mBitmap);
            saveToCache(mBitmap);
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            mImageView.setImageBitmap(null);
            loadPhoto(mOriginalPhotoPath);
            mImageView.setImageBitmap(mBitmap);
            saveToCache(mBitmap);
        }
    }

    private void enableFilterEditAndSaveButtons() {
        findViewById(R.id.buttonFilter).setEnabled(true);
        findViewById(R.id.buttonSave).setEnabled(true);
    }

    public void onGalleryButtonClick(View v) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_CODE_SELECT_PHOTO);
    }


    public void onFilterButtonClick(View v) {
        if (mIsFilterListShowing) {
            hideFilterList();
        } else {
            showFilterList();
        }
    }


    public void onSaveButtonClick(View v) {
        sSavePhotoTask = new SavePhotoTask(this);
        sSavePhotoTask.execute();
    }


    private void showFilterList() {
        if (mIsFilterListShowing) {
            return;
        }

        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 1.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setInterpolator(new DecelerateInterpolator(2.0f));
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(translateAnimation);
        animation.addAnimation(alphaAnimation);
		/*animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mFilterListView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// Do nothing
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// Do nothing
			}
		});*/
        animation.setDuration(500);
        animation.setFillAfter(true);
        mFilterListView.startAnimation(animation);

        //This is needed because some older android
        //versions contain a bug where AnimationListeners
        //are not called in Animations
        mFilterListView.setVisibility(View.VISIBLE);

        mIsFilterListShowing = true;
    }

    private void hideFilterList() {
        if (!mIsFilterListShowing) {
            return;
        }

        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 1.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f,
                TranslateAnimation.RELATIVE_TO_SELF, 0.0f);
        translateAnimation.setInterpolator(new DecelerateInterpolator(2.0f));
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(translateAnimation);
        animation.addAnimation(alphaAnimation);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFilterListView.setVisibility(View.INVISIBLE);
            }
        });
        animation.setDuration(500);
        mFilterListView.startAnimation(animation);

        mIsFilterListShowing = false;
    }

    private void saveToCache(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        File cacheFile = new File(getCacheDir(), "cached.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cacheFile);
        } catch (FileNotFoundException e) {
            // do nothing
        } finally {
            if (fos != null) {
                bitmap.compress(CompressFormat.JPEG, 100, fos);
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
    }

    private String savePhoto(Bitmap bitmap) {
        File file = new File(mOriginalPhotoPath);
        File saveDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Lightbox/");
        saveDir.mkdir();
        String name = file.getName().substring(0, file.getName().lastIndexOf('.')) + "_";
        int count = 0;
        String format = String.format("%%0%dd", 3);
        File saveFile;
        do {
            count++;
            String filename = name + String.format(format, count) +".jpeg";
            saveFile = new File(saveDir, filename);
        } while (saveFile.exists());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(saveFile);
            bitmap.compress(CompressFormat.JPEG, 95, fos);

            return saveFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.w(TAG, e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }

        return "";
    }

    private void loadPhoto(String path) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        if (mBitmap != null) {
            mBitmap.recycle();
        }

        mBitmap = BitmapUtils.getSampledBitmap(path, displayMetrics.widthPixels, displayMetrics.heightPixels);

        if (mBitmap != null && !mBitmap.isMutable()) {
            mBitmap = PhotoProcessing.makeBitmapMutable(mBitmap);
        }

        int angle = MediaUtils.getExifOrientation(path);
        mBitmap = PhotoProcessing.rotate(mBitmap, angle);

        enableFilterEditAndSaveButtons();
    }

    private void showTempPhotoInImageView() {
        if (mBitmap != null) {
            Bitmap bitmap = Bitmap.createScaledBitmap(mBitmap, mBitmap.getWidth()/4, mBitmap.getHeight()/4, true);
            mImageView.setImageBitmap(bitmap);
        }
    }

    private void loadFromCache() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        if (mBitmap != null) {
            mBitmap.recycle();
        }

        File cacheFile = new File(getCacheDir(), "cached.jpg");
        mBitmap = BitmapUtils.getSampledBitmap(cacheFile.getAbsolutePath(), displayMetrics.widthPixels, displayMetrics.heightPixels);

        enableFilterEditAndSaveButtons();
    }

    private void showFilterProgressDialog() {
        String message = (mCurrentFilter == 0 ? getString(R.string.reverting_to_original) : getString(R.string.applying_filter, getString(PhotoProcessing.FILTERS[mCurrentFilter])));
        mProgressDialog = ProgressDialog.show(this, "", message);
    }

    private void showSavingProgressDialog() {
        String message = "Saving...";
        mProgressDialog = ProgressDialog.show(this, "", message);
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.buttonGallery:
                mVibrator.vibrate(40);
                Toast.makeText(this, R.string.toast_hint_gallery, Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonFilter:
                mVibrator.vibrate(40);
                Toast.makeText(this, R.string.toast_hint_filter, Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonSave:
                mVibrator.vibrate(40);
                Toast.makeText(this, R.string.toast_hint_save, Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    private static class FilterListAdapter extends BaseAdapter {
        private LayoutInflater mInflator;
        private Context mContext;

        public FilterListAdapter(Context context) {
            mContext = context;
            mInflator = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return PhotoProcessing.FILTERS.length;
        }

        @Override
        public Object getItem(int position) {
            return mContext.getString(PhotoProcessing.FILTERS[position]);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = mInflator.inflate(R.layout.photo_filter_frame, null);
            }

            ((TextView)view.findViewById(R.id.filterNameTextView)).setText((CharSequence)getItem(position));

            return view;
        }
    }


    private static class FilterTask extends AsyncTask<Integer, Void, Bitmap> {
        WeakReference<PhotoFilterActivity> mActivityRef;

        public FilterTask(PhotoFilterActivity activity) {
            mActivityRef = new WeakReference<PhotoFilterActivity>(activity);
        }

        public void reattachActivity(PhotoFilterActivity activity) {
            mActivityRef = new WeakReference<PhotoFilterActivity>(activity);
            if (getStatus().equals(Status.RUNNING)) {
                activity.showFilterProgressDialog();
            }
        }

        private PhotoFilterActivity getActivity() {
            if (mActivityRef == null) {
                return null;
            }

            return mActivityRef.get();
        }

        @Override
        protected void onPreExecute() {
            PhotoFilterActivity activity = getActivity();
            if (activity != null) {
                activity.showFilterProgressDialog();
                activity.showTempPhotoInImageView();
            }
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            PhotoFilterActivity activity = getActivity();

            if (activity != null) {
                activity.loadPhoto(activity.mOriginalPhotoPath);
                int position = params[0];
                Bitmap bitmap = PhotoProcessing.filterPhoto(activity.mBitmap, position);
                for (Integer editAction : activity.mEditActions) {
                    bitmap = PhotoProcessing.applyEditAction(bitmap, editAction);
                }
                activity.saveToCache(bitmap);

                return bitmap;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            PhotoFilterActivity activity = getActivity();
            if (activity != null) {
                activity.mBitmap = result;
                activity.mImageView.setImageBitmap(result);
                activity.hideProgressDialog();
            }
        }
    }


    private static class SavePhotoTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<PhotoFilterActivity> mActivityRef;
        private String mSavePath;

        public SavePhotoTask(PhotoFilterActivity activity) {
            mActivityRef = new WeakReference<PhotoFilterActivity>(activity);

        }

        public void reattachActivity(PhotoFilterActivity activity) {
            mActivityRef = new WeakReference<PhotoFilterActivity>(activity);
            if (getStatus().equals(Status.RUNNING)) {
                activity.showSavingProgressDialog();
            }
        }

        private PhotoFilterActivity getActivity() {
            if (mActivityRef == null) {
                return null;
            }

            return mActivityRef.get();
        }

        @Override
        protected void onPreExecute() {
            PhotoFilterActivity activity = getActivity();
            if (activity != null) {
                activity.showSavingProgressDialog();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            PhotoFilterActivity activity = getActivity();
            if (activity != null) {
                File jpegFile = new File(activity.mOriginalPhotoPath);
                try {
                    byte[] jpegData = FileUtils.readFileToByteArray(jpegFile);
                    PhotoProcessing.nativeLoadResizedJpegBitmap(jpegData, jpegData.length, 1024 * 1024 * 2);
                    Bitmap bitmap = PhotoProcessing.filterPhoto(null, activity.mCurrentFilter);
                    int angle = MediaUtils.getExifOrientation(activity.mOriginalPhotoPath);
                    bitmap = PhotoProcessing.rotate(bitmap, angle);
                    for (Integer editAction : activity.mEditActions) {
                        bitmap = PhotoProcessing.applyEditAction(bitmap, editAction);
                    }
                    mSavePath = activity.savePhoto(bitmap);
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            PhotoFilterActivity activity = getActivity();
            if (activity != null) {
                activity.hideProgressDialog();
                Toast.makeText(activity, activity.getString(R.string.saved_photo_toast_message, mSavePath), Toast.LENGTH_LONG).show();
            }
        }
    }
}