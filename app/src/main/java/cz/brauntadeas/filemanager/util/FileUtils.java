package cz.brauntadeas.filemanager.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Objects;

import cz.brauntadeas.filemanager.BuildConfig;

public class FileUtils {

    public static String getMimeType(Uri uri, Context context) {
        String mimeType;
        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
            mimeType = contentResolver.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static Uri getFileUri(File file, Context context) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
