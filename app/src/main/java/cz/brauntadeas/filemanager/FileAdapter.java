package cz.brauntadeas.filemanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private File[] files;

    FileAdapter(File file) {
        files = file.listFiles();
    }

    private void updateList(File file) {
        files = file.listFiles();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_file, parent,false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileViewHolder holder, int position) {
        final File file = files[position];
        holder.textFileName.setText(file.getName());
        holder.imageFileType.setImageResource(getFileIcon(file.isDirectory()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.isDirectory()) {
                    updateList(file);
                } else {

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    private int getFileIcon(boolean isDirectory) {
        return isDirectory ? R.drawable.folder : R.drawable.file_outline;
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
}
