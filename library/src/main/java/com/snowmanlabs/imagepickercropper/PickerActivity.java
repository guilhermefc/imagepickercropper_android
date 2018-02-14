package com.snowmanlabs.imagepickercropper;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.Serializable;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by felipepadilha on 21/03/17.
 */

public class PickerActivity extends AppCompatActivity implements SourceChooserDialog.SourceChooser {

    private static final String TITLE = "screenTitle";
    public static final String IMAGE = "image";
    private static final int PERMISSIONS_REQUEST_CAMERA = 101;
    private static final int PERMISSIONS_REQUEST_GALLERY = 102;
    private static final int REQUEST_TAKE_PICTURE = 100;
    private static final int REQUEST_LOAD_IMAGE = 200;

    private static final String FILENAME = "image.jpeg";
    private static final String IMAGE_WRAPPER = "image_wrapper";
    private static final String SOURCE_CHOOSER = "source_chooser";
    private static final String ASPECT_RATIO = "aspect_ratio";
    private static final String TAG = PickerActivity.class.getSimpleName();

    private ImageWrapper mImageWrapper;
    private SourceChooserDialog sourceChooser;
    private String screenTitle;
    private boolean hasAspectRatio;
    private AspectRatioWrapper aspectRatioWrapper;

    public static Intent getIntent(Context context, String title) {
        Intent intent = new Intent(context, PickerActivity.class);
        intent.putExtra(TITLE, title);
        return intent;
    }

    public static Intent getIntent(Context context, String title, AspectRatioWrapper aspectRatioWrapper) {
        Intent intent = new Intent(context, PickerActivity.class);
        intent.putExtra(TITLE, title);
        intent.putExtra(ASPECT_RATIO, aspectRatioWrapper);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(IMAGE_WRAPPER))
            mImageWrapper = (ImageWrapper) savedInstanceState.getSerializable(IMAGE_WRAPPER);
        if (savedInstanceState != null && savedInstanceState.containsKey(SOURCE_CHOOSER))
            sourceChooser = (SourceChooserDialog) savedInstanceState.getSerializable(SOURCE_CHOOSER);

        Bundle extras = getIntent().getExtras();
        screenTitle = extras.getString(TITLE);

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(ASPECT_RATIO)) {
            this.aspectRatioWrapper = (AspectRatioWrapper) getIntent().getExtras().get(ASPECT_RATIO);
            hasAspectRatio = true;
        }

        if (sourceChooser == null) {
            sourceChooser = SourceChooserDialog.getInstance(screenTitle);
            sourceChooser.show(getSupportFragmentManager(), "SourceChooser");
            sourceChooser.addEventListener(this);
        }
    }

    @Override
    public void onDismiss() {
        if (mImageWrapper == null)
            finish();
    }

    @Override
    public void onChooseCamera() {
        if (isPermissionsForCameraGranted()) {
            mImageWrapper = new ImageWrapper();
            mImageWrapper.setFileName(FILENAME);

            //define the file-name to save photo taken by Camera activity
            //create parameters for Intent with filename
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, FILENAME);
            values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");

            //imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
            mImageWrapper.setUri(getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));

            //create new Intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageWrapper.getUri());
            intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(intent, REQUEST_TAKE_PICTURE);

        } else {
            ActivityCompat.requestPermissions(PickerActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        }

    }

    private boolean isPermissionsForCameraGranted() {
        return ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onChooseGallery() {
        if (isPermissionsForGalleryGranted()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, screenTitle), REQUEST_LOAD_IMAGE);
        } else {
            ActivityCompat.requestPermissions(PickerActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_GALLERY);
        }
    }

    private boolean isPermissionsForGalleryGranted() {
        return ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(IMAGE_WRAPPER, mImageWrapper);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri outputUri = null;
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PICTURE) {
                outputUri = mImageWrapper.getUri();
                if (outputUri != null) {
                    startCropImage(outputUri);
                }

            } else if (requestCode == REQUEST_LOAD_IMAGE && null != data) {
                outputUri = data.getData();
                if (outputUri != null) {
                    startCropImage(outputUri);
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                outputUri = result.getUri();
            }

            if (outputUri != null) {
                compressImage(new File(outputUri.getPath()));
            }else {
                onUriNull();
            }
        }

    }

    private void onUriNull() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        sourceChooser.dismiss();
        finish();
    }

    private void startCropImage(Uri outputUri) {
        if (hasAspectRatio) {
            CropImage.activity(outputUri)
                    .setAutoZoomEnabled(true)
                    .setAspectRatio(aspectRatioWrapper.getRatioX(), aspectRatioWrapper.getRatioY())
                    .start(this);
        } else {
            CropImage.activity(outputUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_GALLERY && grantResults.length > 0) {
            onChooseGallery();
        } else if (requestCode == PERMISSIONS_REQUEST_CAMERA && grantResults.length > 0) {
            onChooseCamera();
        }
    }

    private void compressImage(File file) {

        new Compressor(this)
                .setQuality(75)
                .setMaxWidth(640)
                .setMaxHeight(480)
                .compressToFileAsFlowable(file)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(IMAGE, Uri.fromFile(file));
                        setResult(Activity.RESULT_OK, resultIntent);

                        sourceChooser.dismiss();
                        finish();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.e(TAG, throwable.toString());
                    }
                });
    }

    public static class AspectRatioWrapper implements Serializable {
        private int ratioX;
        private int ratioY;

        public AspectRatioWrapper(int ratioX, int ratioY) {
            this.ratioX = ratioX;
            this.ratioY = ratioY;
        }

        int getRatioX() {
            return ratioX;
        }

        int getRatioY() {
            return ratioY;
        }
    }
}
