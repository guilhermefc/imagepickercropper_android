package com.snowmanlabs.imagepickercropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.Serializable;

public class ImageWrapper implements Parcelable, Serializable {
    private Uri uri;
    private String fileName;
    private Bitmap bitmap;
    private long imageID;
    private String mimetype;

    public ImageWrapper(Uri uri, String fileName, long imageID) {
        this.uri = uri;
        this.fileName = fileName;
        this.imageID = imageID;
    }

    public ImageWrapper() {
    }

    public String getFilePath(Context context) {
        String filePath = FileUtil.getPath(context, this.uri);
        String nameSplit[] = filePath.split("/");
        this.fileName = nameSplit[nameSplit.length - 1];
        return filePath;
    }

    public Bitmap getBitmap(Context context) {
        return bitmap == null ? generateBitmap(context) : bitmap;
    }

    public long getImageID() {
        return imageID;
    }

    private Bitmap generateBitmap(Context context) {
        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),
                imageID, MediaStore.Images.Thumbnails.MICRO_KIND, null);
        this.bitmap = bitmap;
        return bitmap;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getMimeType(Context context) {
        return mimetype == null ? context.getContentResolver().getType(uri) : mimetype;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.uri, 0);
        dest.writeString(this.fileName);
        dest.writeLong(this.imageID);
        dest.writeString(this.mimetype);
    }

    private ImageWrapper(Parcel in) {
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.fileName = in.readString();
        this.imageID = in.readLong();
        this.mimetype = in.readString();
    }

    public static final Parcelable.Creator<ImageWrapper> CREATOR = new Parcelable.Creator<ImageWrapper>() {
        public ImageWrapper createFromParcel(Parcel source) {
            return new ImageWrapper(source);
        }

        public ImageWrapper[] newArray(int size) {
            return new ImageWrapper[size];
        }
    };

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }
}