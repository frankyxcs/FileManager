package cz.brauntadeas.filemanager.files;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.util.List;

import cz.brauntadeas.filemanager.BasePresenter;
import cz.brauntadeas.filemanager.BaseView;

public interface FilesContract {
    interface View extends BaseView<Presenter> {
        void setUpRecyclerView(RecyclerView.LayoutManager layoutManager);
        Adapter getAdapter();
        int getDeviceOrientation();
        LinearLayoutManager getLinearLayoutManager();
        GridLayoutManager getGridLayoutManager();
        void openFileInDefaultApp(File file);
        void defaultOnBackPressed();
        void startActionMode();
    }

    interface Presenter extends BasePresenter {
        void start();
        void start(boolean isListMultiSelect, File currentFolder, List<File> selectedFilesList);
        void stop();
        void setFileList(List<File> fileList, boolean isSameFolder);
        void onFileClick(File file, HolderView holderView);
        int getFileIcon(File file);
        void deleteSelectedFiles();
        void multiSelectOn();
        void multiSelectOff();
        void refreshFileList();
        void onBackPressed();
        void handleHolderBackground(File file, HolderView holderView);
        void selectFile(File file, HolderView holderView);
        boolean isMultiSelect();
        File getCurrentFolder();
        List<File> getSelectedFiles();
    }

    interface Model {
        File getDefaultFolder();
        File getInternalStorageFolder();
    }

    interface HolderView {
        void select();
        void deselect();
    }

    interface Adapter {
        void setFileList(List<File> fileList);
        void scrollToTop();
    }
}
