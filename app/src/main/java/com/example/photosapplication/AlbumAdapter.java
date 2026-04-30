package com.example.photosapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photosapplication.model.Album;
import com.example.photosapplication.model.Photo;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final List<Album> albums;
    private final OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
        void onAlbumLongClick(Album album);
    }

    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.bind(albums.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final TextView albumName;
        private final TextView photoCount;
        private final TextView dateRange;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.album_name);
            photoCount = itemView.findViewById(R.id.photo_count);
            dateRange = itemView.findViewById(R.id.date_range);
        }

        public void bind(Album album, OnAlbumClickListener listener) {
            albumName.setText(album.getName());
            List<Photo> photos = album.getPhotos();
            int count = photos.size();
            photoCount.setText(String.format(Locale.getDefault(), "%d %s", count, count == 1 ? "photo" : "photos"));

            if (count > 0) {
                Calendar minDate = null;
                Calendar maxDate = null;

                for (Photo photo : photos) {
                    Calendar photoDate = photo.getDate();
                    if (minDate == null || photoDate.before(minDate)) {
                        minDate = photoDate;
                    }
                    if (maxDate == null || photoDate.after(maxDate)) {
                        maxDate = photoDate;
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                if (minDate != null && maxDate != null) {
                    dateRange.setText(String.format("%s - %s", sdf.format(minDate.getTime()), sdf.format(maxDate.getTime())));
                } else {
                    dateRange.setText("No dates available");
                }
            } else {
                dateRange.setText("No photos");
            }

            itemView.setOnClickListener(v -> listener.onAlbumClick(album));
            itemView.setOnLongClickListener(v -> {
                listener.onAlbumLongClick(album);
                return true;
            });
        }
    }
}
