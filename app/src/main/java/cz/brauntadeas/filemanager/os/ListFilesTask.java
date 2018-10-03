package cz.brauntadeas.filemanager.os;

import android.os.AsyncTask;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.brauntadeas.filemanager.files.FilesContract;

public class ListFilesTask extends AsyncTask<File, Integer, List<File>> {
    private WeakReference<FilesContract.Presenter> presenterReference;
    private boolean isSameFolder;

    public ListFilesTask(FilesContract.Presenter filesPresenter, boolean isSameFolder) {
        presenterReference = new WeakReference<>(filesPresenter);
        this.isSameFolder = isSameFolder;
    }

    @Override
    protected List<File> doInBackground(File... files) {
        File file = files[0];
        List<File> filesList = Arrays.asList(file.listFiles());

        Collections.sort(filesList, (fileOne, fileTwo) -> {
            int directoryCompare = -1 * Boolean.compare(fileOne.isDirectory(), fileTwo.isDirectory());
            if (directoryCompare == 0) {
                return fileOne.getName().toLowerCase().compareTo(fileTwo.getName().toLowerCase());
            }
            return directoryCompare;
        });

        return filesList;
    }

    @Override
    protected void onPostExecute(List<File> filesList) {
        presenterReference.get().setFileList(filesList, isSameFolder);
    }
}
