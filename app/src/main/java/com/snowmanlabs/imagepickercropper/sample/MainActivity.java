package com.snowmanlabs.imagepickercropper.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.snowmanlabs.imagepickercropper.PickerActivity;

/**
 * Created by felipepadilha on 06/10/17.
 */

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST_CODE = 101;
    private Button picButton;
    private ImageView image;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflateViews();

        addListeners();

    }

    private void inflateViews() {
        picButton = findViewById(R.id.picButton);
        image = findViewById(R.id.image);
    }

    private void addListeners() {
        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(PickerActivity.getIntent(getApplicationContext(), "Select Source"), IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && resultCode == Activity.RESULT_OK) {
            if (IMAGE_REQUEST_CODE == requestCode) {
                if(data.getExtras().containsKey("image")){
                    Uri mImageUri = (Uri) data.getExtras().get("image");
                    image.setImageURI(mImageUri);
                }
            }
        }
    }

}
