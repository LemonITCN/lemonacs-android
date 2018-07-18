package cn.lemage_scanlib.decode;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * @author zhaoguangyang
 */
public class ImageUtil {

    @TargetApi(19)
    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context != null && imageUri != null) {
            if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context, imageUri)) {
                String docId;
                String[] split;
                String type;
                if (isExternalStorageDocument(imageUri)) {
                    docId = DocumentsContract.getDocumentId(imageUri);
                    split = docId.split(":");
                    type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else {
                    if (isDownloadsDocument(imageUri)) {
                        docId = DocumentsContract.getDocumentId(imageUri);
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                        return getDataColumn(context, contentUri, (String)null, (String[])null);
                    }

                    if (isMediaDocument(imageUri)) {
                        docId = DocumentsContract.getDocumentId(imageUri);
                        split = docId.split(":");
                        type = split[0];
                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{split[1]};
                        return getDataColumn(context, contentUri, selection, selectionArgs);
                    }
                }
            } else {
                if ("content".equalsIgnoreCase(imageUri.getScheme())) {
                    if (isGooglePhotosUri(imageUri)) {
                        return imageUri.getLastPathSegment();
                    }

                    return getDataColumn(context, imageUri, (String)null, (String[])null);
                }

                if ("file".equalsIgnoreCase(imageUri.getScheme())) {
                    return imageUri.getPath();
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, (String)null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                String var8 = cursor.getString(index);
                return var8;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
