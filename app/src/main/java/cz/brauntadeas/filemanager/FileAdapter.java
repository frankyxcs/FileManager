package cz.brauntadeas.filemanager;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private List<File> fileList = new ArrayList<>();
    private File currentFolder;

    FileAdapter(File file) {
        updateList(file);
    }

    private void setFileList(List<File> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }

    void updateList() {
        newListTask(currentFolder);
    }

    private void updateList(File file) {
        currentFolder = file;
        newListTask(file);
    }

    void navigateUp() {
        updateList(currentFolder.getParentFile());
    }

    boolean canNavigateUp() {
        return !currentFolder.equals(Environment.getExternalStorageDirectory());
    }

    File getCurrentFolder() {
        return currentFolder;
    }

    private void newListTask(File file) {
        new ListDirectoryTask(this).execute(file);
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_file, parent,false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileViewHolder holder, int position) {
        final File file = fileList.get(position);
        holder.textFileName.setText(file.getName());
        holder.imageFileType.setImageResource(getFileIcon(file.isDirectory()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.isDirectory()) {
                    updateList(file);
                } else {
                    Intent intent = new Intent();
                    Context context = holder.itemView.getContext();
                    Uri uri = getFileUri(file, context);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(uri, getMimeType(uri, context));
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(context, R.string.no_activity, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private int getFileIcon(boolean isDirectory) {
        return isDirectory ? R.drawable.folder : R.drawable.file_outline;
    }

    private String getMimeType(Uri uri, Context context) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
            mimeType = contentResolver.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private Uri getFileUri(File file, Context context) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_file_name)
        TextView textFileName;
        @BindView(R.id.image_file_type)
        ImageView imageFileType;

        FileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private static class ListDirectoryTask extends AsyncTask<File, Integer, List<File>> {
        private WeakReference<FileAdapter> adapterReference;

        ListDirectoryTask(FileAdapter fileAdapter) {
            adapterReference = new WeakReference<>(fileAdapter);
        }

        @Override
        protected List<File> doInBackground(File... files) {
            File file = files[0];
            List<File> fileList = Arrays.asList(file.listFiles());

            Collections.sort(fileList, (fileOne, fileTwo) -> {
                int directoryCompare = -1 * Boolean.compare(fileOne.isDirectory(), fileTwo.isDirectory());
                if (directoryCompare == 0) {
                    return fileOne.getName().toLowerCase().compareTo(fileTwo.getName().toLowerCase());
                }
                return directoryCompare;
            });

            return fileList;
        }

        @Override
        protected void onPostExecute(List<File> fileList) {
            adapterReference.get().setFileList(fileList);
        }
    }
}
