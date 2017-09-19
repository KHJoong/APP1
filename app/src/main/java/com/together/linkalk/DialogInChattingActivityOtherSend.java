package com.together.linkalk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by kimhj on 2017-09-19.
 */

public class DialogInChattingActivityOtherSend extends Activity {

    static Activity dialogOtherSend;

    private final static int PICT_CROP_CODE = 5555;
    private final static int CAMERA_CODE = 1111;
    private final static int GALLERY_CODE = 3333;
    private static Uri mUri;
    private static String mCurrentPhotoPath;

    Button btnOtherCamera;
    Button btnOtherGallery;
    FaceOverlayView2 fovImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.in_chat_room_find_other);

        dialogOtherSend = DialogInChattingActivityOtherSend.this;

        Button.OnClickListener btnClickListener = new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btnOtherCamera :
                        TakePicIntent();
                        break;
                    case R.id.btnOtherGallery :
                        GetPicIntent();
                        break;
                }
            }
        };

        btnOtherCamera = (Button)findViewById(R.id.btnOtherCamera);
        btnOtherGallery = (Button)findViewById(R.id.btnOtherGallery);
        btnOtherCamera.setOnClickListener(btnClickListener);
        btnOtherGallery.setOnClickListener(btnClickListener);
        fovImage = (FaceOverlayView2)findViewById(R.id.fovImage);
        fovImage.setVisibility(View.GONE);
    }   // onCreate 끝

    public File createImageFile() throws IOException {
        Log.i("createImageFile", "Call");
        // Create an image file name, 외장 메모리 저장
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File imageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/linkalkPic/" + imageFileName);

        if(!imageFile.exists()) {
            imageFile.getParentFile().mkdirs();
            imageFile.createNewFile();
        }

        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    public void TakePicIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("captureCamera Error", ex.toString());
            }
            if (photoFile != null) {
                // photoURI : file://로 시작, FileProvider(Content Provider 하위)는 content://로 시작
                // 누가(7.0)이상부터는 file://로 시작되는 Uri의 값을 다른 앱과 주고 받기가 불가능하여 content://로 변경
                Uri providerURI = FileProvider.getUriForFile(getApplicationContext(), "com.together.linkalk", photoFile);
                mUri = providerURI;
                Log.i("imageUri", mUri.toString());

                // 인텐트에 전달할 때는 FileProvier의 Return값인 content://로만!!, providerURI의 값에 카메라 데이터를 넣어 보냄
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                // file provider permission denial 해결 부분, 패키지를 필요로 하는 모든 패키지에 권한 부여 해줌
                List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getApplicationContext().grantUriPermission(packageName, mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                startActivityForResult(takePictureIntent, CAMERA_CODE);
            }
        }
    }

    public void GetPicIntent(){
        Intent pickPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickPictureIntent.setType("image/*");
        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, GALLERY_CODE);
        }
    }

    public Uri createSaveCropFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";

        Uri uri;
        String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/linkalkPic/" + imageFileName + "_crop.jpg";
        uri = Uri.fromFile(new File(url));

        return uri;
    }

    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private File getImageFile(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        if (uri == null) {
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        Cursor mCursor = getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
        if(mCursor == null || mCursor.getCount() < 1) {
            return null; // no cursor or no record
        }
        int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        mCursor.moveToFirst();

        String path = mCursor.getString(column_index);

        if (mCursor !=null ) {
            mCursor.close();
            mCursor = null;
        }

        return new File(path);
    }

    public void CropPic(Uri uri){
        Intent cropPictureIntent = new Intent("com.android.camera.action.CROP");
        cropPictureIntent.setDataAndType(uri, "image/*");
        cropPictureIntent.putExtra("output", uri);
        cropPictureIntent.putExtra("outputX", 640); // crop한 이미지의 x축 크기 (integer)
        cropPictureIntent.putExtra("outputY", 480); // crop한 이미지의 y축 크기 (integer)
        cropPictureIntent.putExtra("return-data", true);
        if (cropPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cropPictureIntent, PICT_CROP_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CAMERA_CODE){
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File file = new File(mCurrentPhotoPath);
                mUri = Uri.fromFile(file);
                mediaScanIntent.setData(mUri);
                sendBroadcast(mediaScanIntent);

                CropPic(mUri);
            } else if(requestCode==GALLERY_CODE){
                mUri = data.getData();
                File origin = getImageFile(mUri);
                mUri = createSaveCropFile();
                File copy = new File(mUri.getPath());
                copyFile(origin, copy);

                CropPic(mUri);
            } else if(requestCode==PICT_CROP_CODE){
                String path = mUri.getPath();
                Bitmap imageBitmap = BitmapFactory.decodeFile(path);
                fovImage.setVisibility(View.VISIBLE);
                fovImage.setBitmap(imageBitmap);
            }
        }
    }

}
