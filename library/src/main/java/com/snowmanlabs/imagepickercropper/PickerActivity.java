package com.snowmanlabs.imagepickercropper;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by felipepadilha on 21/03/17.
 */

public class PickerActivity extends AppCompatActivity implements SourceChooserDialog.SourceChooser {

    private static final String TITLE = "title";
    public static final String IMAGE = "image";
    private static final int PERMISSIONS_REQUEST_CAMERA = 101;
    private static final int PERMISSIONS_REQUEST_GALLERY = 102;
    private static final int REQUEST_TAKE_PICTURE = 100;
    private static final int REQUEST_LOAD_IMAGE = 200;

    private static final String FILENAME = "image.jpeg";

    private ImageWrapper mImageWrapper;
    private SourceChooserDialog sourceChooser;
    private String title;

    public static Intent getIntent(Context context, String title) {
        Intent intent = new Intent(context, PickerActivity.class);
        intent.putExtra(TITLE, title);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        title = extras.getString(TITLE);
        sourceChooser = SourceChooserDialog.getInstance(title);
        sourceChooser.show(getSupportFragmentManager(), "SourceChooser");
        sourceChooser.addEventListener(this);
    }

    @Override
    public void onDismiss() {
        if (mImageWrapper == null)
            finish();
    }

    @Override
    public void onChooseCamera() {
        if (ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

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


            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivityForResult(intent, REQUEST_TAKE_PICTURE);

        } else {
            ActivityCompat.requestPermissions(PickerActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        }

    }

    @Override
    public void onChooseGallery() {
        if (ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PickerActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, title), REQUEST_LOAD_IMAGE);


        } else {
            ActivityCompat.requestPermissions(PickerActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_GALLERY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean crop = true;
        Uri outputUri = null;
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PICTURE) {
                outputUri = mImageWrapper.getUri();
                if (outputUri != null)
                    if (crop) {
                        CropImage.activity(outputUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(this);
                        return;
                    }

            } else if (requestCode == REQUEST_LOAD_IMAGE && null != data) {
                outputUri = data.getData();
                if (outputUri != null)
                    if (crop) {
                        CropImage.activity(outputUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(this);
                        return;
                    }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                outputUri = result.getUri();
            }

            compressImage(new File(outputUri.getPath()));

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GALLERY:
                // If request is cancelled, the  result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onChooseGallery();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //TODO: permission denied
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            case PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onChooseCamera();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //TODO: permission denied
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    private void compressImage(File file){

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
                        throwable.printStackTrace();
                    }
                });
    }
}
