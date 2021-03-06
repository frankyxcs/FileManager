package cz.brauntadeas.filemanager.files;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.brauntadeas.filemanager.R;
import cz.brauntadeas.filemanager.preference.SettingsActivity;
import cz.brauntadeas.filemanager.util.FileUtils;

public class FilesView extends AppCompatActivity implements FilesContract.View {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private static final String CURRENT_FOLDER = "current_folder";
    private static final String MULTI_SELECT = "multi_select";
    private static final String SELECTED_ITEMS = "selected_items";

    private FilesContract.Presenter presenter;
    private FilesAdapter filesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            setPresenter(savedInstanceState);
                        } else {
                            Toast.makeText(getApplicationContext(), "You must grant permissions in order to run the app.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    public int getDeviceOrientation() {
        return getResources().getConfiguration().orientation;
    }

    @Override
    public LinearLayoutManager getLinearLayoutManager() {
        return new LinearLayoutManager(this);
    }

    @Override
    public GridLayoutManager getGridLayoutManager() {
        return new GridLayoutManager(this, 2);
    }

    @Override
    public void openFileInDefaultApp(File file) {
        Intent intent = new Intent();
        Uri uri = FileUtils.getFileUri(file, this);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, FileUtils.getMimeType(uri, this));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.no_activity, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setUpRecyclerView(RecyclerView.LayoutManager layoutManager) {
        filesAdapter = new FilesAdapter(presenter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(filesAdapter);
    }

    @Override
    public FilesContract.Adapter getAdapter() {
        return filesAdapter;
    }

    public void setPresenter(Bundle bundle) {
        presenter = new FilesPresenter(this, new FilesModel(PreferenceManager.getDefaultSharedPreferences(this)));
        if (bundle == null) {
            presenter.start();
        } else {
            presenter.start(
                    bundle.getBoolean(MULTI_SELECT, false),
                    (File) bundle.getSerializable(CURRENT_FOLDER),
                    new Gson().fromJson(bundle.getString(SELECTED_ITEMS), new TypeToken<List<File>>(){}.getType()));
        }
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
    }

    @Override
    public void defaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void startActionMode() {
        startSupportActionMode(actionModeCallbacks);
    }

    @Override
    public void scrollToTop() {
        recyclerView.scrollToPosition(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            presenter.refreshFileList();
            return true;
        }

        if (id == R.id.action_settings) {
            SettingsActivity.start(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(MULTI_SELECT, presenter.isMultiSelect());
        outState.putSerializable(CURRENT_FOLDER, presenter.getCurrentFolder());
        outState.putString(SELECTED_ITEMS, new Gson().toJson(presenter.getSelectedFiles()));
        super.onSaveInstanceState(outState);
    }

    private void showDeleteDialog(ActionMode actionMode) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    presenter.deleteSelectedFiles();
                    actionMode.finish();
                })
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            presenter.multiSelectOn();
            actionMode.getMenuInflater().inflate(R.menu.contextual, menu);
            actionMode.setTitle(getResources().getString(R.string.select_items));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            showDeleteDialog(actionMode);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            presenter.multiSelectOff();
        }
    };
}
