package cz.brauntadeas.filemanager.files;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.brauntadeas.filemanager.R;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> implements FilesContract.Adapter {
    private List<File> fileList = new ArrayList<>();
    private FilesContract.Presenter presenter;

    FilesAdapter(FilesContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new FileViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_file, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int i) {
        File file = fileList.get(i);
        holder.textFileName.setText(file.getName());
        holder.imageFileType.setImageResource(presenter.getFileIcon(file));
        presenter.handleHolderBackground(file, holder);
        holder.itemView.setOnClickListener(view -> presenter.onFileClick(file, holder));
        holder.itemView.setOnLongClickListener(view -> {
            presenter.onLongFileClick(file, holder);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder implements FilesContract.HolderView {
        @BindView(R.id.text_file_name)
        TextView textFileName;
        @BindView(R.id.image_file_type)
        ImageView imageFileType;

        @BindColor(R.color.colorSelected)
        int colorSelected;
        @BindColor(R.color.colorBackground)
        int colorBackground;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void select() {
            itemView.setBackgroundColor(colorSelected);
        }

        @Override
        public void deselect() {
            itemView.setBackgroundColor(colorBackground);
        }
    }
}