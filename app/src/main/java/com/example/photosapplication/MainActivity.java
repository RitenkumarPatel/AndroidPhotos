package com.example.photosapplication;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photosapplication.model.Album;
import com.example.photosapplication.model.User;
import com.example.photosapplication.model.PersistenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddAlbum;
    private User currentUser;
    private AlbumAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize UI components
        recyclerView = findViewById(R.id.album_recycler_view);
        fabAddAlbum = findViewById(R.id.fab_add_album);

        // 2. Load Data (assuming single-user focus)
        loadData();

        // 3. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbumAdapter(currentUser.getAlbums(), new AlbumAdapter.OnAlbumClickListener() {
            @Override
            public void onAlbumClick(Album album) {
                // TODO: Open album photos view
                Toast.makeText(MainActivity.this, "Selected: " + album.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAlbumLongClick(Album album) {
                showAlbumOptionsDialog(album);
            }
        });
        recyclerView.setAdapter(adapter);

        // 4. Handle FAB Click (Logic for adding an album)
        fabAddAlbum.setOnClickListener(v -> showAddAlbumDialog());
    }

    private void loadData() {
        try {
            List<User> users = PersistenceManager.load(this);
            if (users == null || users.isEmpty()) {
                currentUser = new User("default_user");
            } else {
                currentUser = users.get(0);
                if (currentUser == null) {
                    currentUser = new User("default_user");
                }
            }
        } catch (Exception e) {
            currentUser = new User("default_user");
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

    private void showAddAlbumDialog() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Album")
                .setMessage("Enter the name of the new album")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String albumName = input.getText().toString().trim();
                    if (!albumName.isEmpty()) {
                        if (currentUser.getAlbumByName(albumName) == null) {
                            currentUser.getAlbums().add(new Album(albumName));
                            saveData();
                            adapter.notifyItemInserted(currentUser.getAlbums().size() - 1);
                        } else {
                            Toast.makeText(this, "Album already exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAlbumOptionsDialog(Album album) {
        String[] options = {"Rename", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle(album.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameAlbumDialog(album);
                    } else if (which == 1) {
                        showDeleteAlbumConfirmDialog(album);
                    }
                })
                .show();
    }

    private void showRenameAlbumDialog(Album album) {
        final EditText input = new EditText(this);
        input.setText(album.getName());
        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(album.getName())) {
                        if (currentUser.getAlbumByName(newName) == null) {
                            album.setName(newName);
                            saveData();
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Album already exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAlbumConfirmDialog(Album album) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete '" + album.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    currentUser.getAlbums().remove(album);
                    saveData();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
