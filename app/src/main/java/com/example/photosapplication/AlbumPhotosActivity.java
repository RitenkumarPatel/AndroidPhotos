package com.example.photosapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.example.photosapplication.R;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photosapplication.model.Album;
import com.example.photosapplication.model.PersistenceManager;
import com.example.photosapplication.model.Photo;
import com.example.photosapplication.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class AlbumPhotosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddPhoto;
    private Album album;
    private User currentUser;
    private PhotoAdapter adapter;
    private String albumName;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(imageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            // This can happen if the URI is not persistable
                        }
                        addPhotoToAlbum(imageUri.toString());
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_photos);

        albumName = getIntent().getStringExtra("ALBUM_NAME");
        loadData();

        if (album == null) {
            Toast.makeText(this, "Error: Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(album.getName());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.photo_recycler_view);
        fabAddPhoto = findViewById(R.id.fab_add_photo);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PhotoAdapter(album.getPhotos(), new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(Photo photo, int position) {
                Intent intent = new Intent(AlbumPhotosActivity.this, PhotoDisplayActivity.class);
                intent.putExtra("ALBUM_NAME", albumName);
                intent.putExtra("PHOTO_INDEX", position);
                startActivity(intent);
            }

            @Override
            public void onPhotoLongClick(Photo photo, int position) {
                showPhotoOptionsDialog(photo, position);
            }
        });
        recyclerView.setAdapter(adapter);

        fabAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data in case captions/tags were changed in the display activity
        loadData();
        if (adapter != null && album != null) {
            adapter.updateData(album.getPhotos());
        }
    }

    private void loadData() {
        try {
            List<User> users = PersistenceManager.load(this);
            if (!users.isEmpty()) {
                currentUser = users.get(0);
                album = currentUser.getAlbumByName(albumName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            List<User> users = new ArrayList<>();
            users.add(currentUser);
            PersistenceManager.save(this, users);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPhotoToAlbum(String uriString) {
        // Check for duplicates
        for (Photo p : album.getPhotos()) {
            if (p.getUriString().equals(uriString)) {
                Toast.makeText(this, "Photo already exists in this album!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Photo newPhoto = new Photo(uriString);
        album.addPhoto(newPhoto);
        saveData();
        adapter.notifyItemInserted(album.getPhotos().size() - 1);
    }

    private void showPhotoOptionsDialog(Photo photo, int position) {
        String[] options = {"Move to another album", "Delete Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Photo Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showMovePhotoDialog(photo, position);
                    } else if (which == 1) {
                        showDeletePhotoConfirmDialog(photo, position);
                    }
                })
                .show();
    }

    private void showDeletePhotoConfirmDialog(Photo photo, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to remove this photo from the album?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    album.removePhoto(photo);
                    saveData();
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMovePhotoDialog(Photo photo, int position) {
        List<Album> otherAlbums = new ArrayList<>();
        for (Album a : currentUser.getAlbums()) {
            if (!a.getName().equals(album.getName())) {
                otherAlbums.add(a);
            }
        }

        if (otherAlbums.isEmpty()) {
            Toast.makeText(this, "No other albums available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] albumNames = new String[otherAlbums.size()];
        for (int i = 0; i < otherAlbums.size(); i++) {
            albumNames[i] = otherAlbums.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Move to Album")
                .setItems(albumNames, (dialog, which) -> {
                    Album targetAlbum = otherAlbums.get(which);
                    
                    // Check if photo already exists in target album
                    boolean exists = false;
                    for (Photo p : targetAlbum.getPhotos()) {
                        if (p.getUriString().equals(photo.getUriString())) {
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        Toast.makeText(this, "Photo already exists in " + targetAlbum.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        targetAlbum.addPhoto(photo);
                        album.removePhoto(photo);
                        saveData();
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Moved to " + targetAlbum.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
