package cz.brauntadeas.filemanager.files;

import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;

class FilesModel implements FilesContract.Model {
    private File defaultFolder;

    FilesModel(@NonNull SharedPreferences sharedPreferences) {
        String path = sharedPreferences.getString("default_folder", null);
        if (path != null) {
            defaultFolder = new File(path);
        } else {
            defaultFolder = Environment.getExternalStorageDirectory();
        }
    }

    @Override
    public File getDefaultFolder() {
        return defaultFolder;
    }

    @Override
    public File getInternalStorageFolder() {
        return Environment.getExternalStorageDirectory();
    }
}
