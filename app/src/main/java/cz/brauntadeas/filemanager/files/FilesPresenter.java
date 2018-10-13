package cz.brauntadeas.filemanager.files;

import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cz.brauntadeas.filemanager.os.ListFilesTask;
import cz.brauntadeas.filemanager.R;
import cz.brauntadeas.filemanager.util.FileUtils;

class FilesPresenter implements FilesContract.Presenter {
    private final FilesContract.View filesView;
    private final FilesContract.Model filesModel;

    private ListFilesTask listFilesTask;
    private boolean isListMultiSelect = false;
    private File currentFolder;
    private List<File> selectedFilesList = new ArrayList<>();

    FilesPresenter(FilesContract.View filesView, FilesContract.Model filesModel) {
        this.filesView = filesView;
        this.filesModel = filesModel;
    }

    @Override
    public void start() {
        filesView.setUpRecyclerView(getLayoutManager(filesView.getDeviceOrientation()));
        listFiles();
    }

    public void start(boolean isListMultiSelect, File currentFolder, List<File> selectedFilesList) {
        this.isListMultiSelect = isListMultiSelect;
        this.currentFolder = currentFolder;
        this.selectedFilesList = selectedFilesList;
        filesView.setUpRecyclerView(getLayoutManager(filesView.getDeviceOrientation()));
        listFiles(currentFolder);
        if (isListMultiSelect) {
            filesView.startActionMode();
        }
    }

    @Override
    public void stop() {
        listFilesTask.cancel(true);
    }

    @Override
    public void setFileList(List<File> fileList, boolean isSameFolder) {
        filesView.getAdapter().setFileList(fileList);
        if (!isSameFolder) {
            filesView.scrollToTop();
        }
    }

    @Override
    public void onFileClick(File file, FilesContract.HolderView holderView) {
        if (isListMultiSelect) {
            if (selectedFilesList.contains(file)) {
                deselectFile(file, holderView);
            } else {
                selectFile(file, holderView);
            }
        } else {
            if (file.isDirectory()) {
                listFiles(file);
            } else {
                filesView.openFileInDefaultApp(file);
            }
        }
    }

    @Override
    public int getFileIcon(File file) {
        return file.isDirectory() ? R.drawable.folder : R.drawable.file_outline;
    }

    @Override
    public void deleteSelectedFiles() {
        for (File file : selectedFilesList) {
            try {
                FileUtils.deleteFile(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void multiSelectOn() {
        isListMultiSelect = true;
    }

    @Override
    public void multiSelectOff() {
        isListMultiSelect = false;
        selectedFilesList.clear();
        refreshFileList();
    }

    @Override
    public void refreshFileList() {
        listFiles(currentFolder);
    }

    @Override
    public void onBackPressed() {
        if (currentFolder.equals(filesModel.getInternalStorageFolder())) {
            filesView.defaultOnBackPressed();
        } else {
            listFiles(currentFolder.getParentFile());
        }
    }

    @Override
    public void handleHolderBackground(File file, FilesContract.HolderView holderView) {
        if (isListMultiSelect && selectedFilesList.contains(file)) {
            holderView.select();
        } else {
            holderView.deselect();
        }
    }

    @Override
    public void selectFile(File file, FilesContract.HolderView holderView) {
        holderView.select();
        selectedFilesList.add(file);
    }

    @Override
    public boolean isMultiSelect() {
        return isListMultiSelect;
    }

    @Override
    public File getCurrentFolder() {
        return currentFolder;
    }

    @Override
    public List<File> getSelectedFiles() {
        return selectedFilesList;
    }

    @Override
    public void onLongFileClick(File file, FilesContract.HolderView holder) {
        filesView.startActionMode();
        selectFile(file, holder);
    }

    private void deselectFile(File file, FilesContract.HolderView holderView) {
        holderView.deselect();
        selectedFilesList.remove(file);
    }

    private void listFiles() {
        if (currentFolder == null) {
            listFiles(filesModel.getDefaultFolder());
        } else {
            listFiles(currentFolder);
        }
    }

    private void listFiles(File file) {
        boolean isSameFolder = isSameFolder(file);
        currentFolder = file;
        listFilesTask = new ListFilesTask(this, isSameFolder);
        listFilesTask.execute(file);
    }

    private boolean isSameFolder(File file) {
        return file == currentFolder;
    }

    private RecyclerView.LayoutManager getLayoutManager(int deviceOrientation) {
        if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return filesView.getGridLayoutManager();
        } else {
            return filesView.getLinearLayoutManager();
        }
    }
}
