package io.playpet.imagepickercropper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Created by mayowa.adegeye on 28/06/2016.
 */
public class SourceChooserDialog extends BottomSheetDialogFragment {
    private static final String TITLE = "title";
    private static final String CALLBACK = "callback";
    private String mTitle;
    private SourceChooser callback;

    public static SourceChooserDialog getInstance(String title/*, SourceChooser callback*/) {
        SourceChooserDialog fragment = new SourceChooserDialog();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
//        bundle.putSerializable(CALLBACK, callback);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void addEventListener(SourceChooser callback){
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getArguments().getString(TITLE);
//        callback = (SourceChooser) getArguments().getSerializable(CALLBACK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_custom_bottom_sheet, container, false);
        TextView title = (TextView) v.findViewById(R.id.title);

        title.setText(mTitle);
        View galleryButton = v.findViewById(R.id.gallery_icon);
        View cameraButton = v.findViewById(R.id.camera_icon);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onChooseGallery();
            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onChooseCamera();
            }
        });

        return v;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        callback.onDismiss();
    }

    public interface SourceChooser{
        void onDismiss();
        void onChooseCamera();
        void onChooseGallery();
    }
}