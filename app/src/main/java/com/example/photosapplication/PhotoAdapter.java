package com.example.photosapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photosapplication.model.Photo;
import java.io.InputStream;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final List<Photo> photos;
    private final OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo, int position);
    }

    public PhotoAdapter(List<Photo> photos, OnPhotoClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(photos.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView caption;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.photo_thumbnail);
            caption = itemView.findViewById(R.id.photo_caption);
        }

        public void bind(Photo photo, int position, OnPhotoClickListener listener) {
            caption.setText(photo.getCaption().isEmpty() ? "No caption" : photo.getCaption());
            
            // Basic image loading from URI
            try {
                Uri uri = Uri.parse(photo.getUriString());
                InputStream is = itemView.getContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                thumbnail.setImageBitmap(bitmap);
                if (is != null) is.close();
            } catch (Exception e) {
                thumbnail.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            itemView.setOnClickListener(v -> listener.onPhotoClick(photo, position));
        }
    }
}
