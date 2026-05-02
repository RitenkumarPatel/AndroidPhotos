package com.example.photosapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import com.example.photosapplication.R;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.photosapplication.model.Album;
import com.example.photosapplication.model.PersistenceManager;
import com.example.photosapplication.model.Photo;
import com.example.photosapplication.model.Tag;
import com.example.photosapplication.model.User;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PhotoDisplayActivity extends AppCompatActivity {

    private ImageView displayImage;
    private TextView displayCaption;
    private TextView displayTags;
    private Button btnPrevious, btnNext, btnManageTags;

    private User currentUser;
    private Album album;
    private List<Photo> photos;
    private int currentIndex;
    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        displayImage = findViewById(R.id.display_image);
        displayCaption = findViewById(R.id.display_caption);
        displayTags = findViewById(R.id.display_tags);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnManageTags = findViewById(R.id.btn_manage_tags);

        albumName = getIntent().getStringExtra("ALBUM_NAME");
        currentIndex = getIntent().getIntExtra("PHOTO_INDEX", 0);

        loadData();

        if (album == null || photos == null || photos.isEmpty()) {
            Toast.makeText(this, "Error loading photos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateDisplay();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnPrevious.setOnClickListener(v -> {
            currentIndex = (currentIndex - 1 + photos.size()) % photos.size();
            updateDisplay();
        });

        btnNext.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % photos.size();
            updateDisplay();
        });

        btnManageTags.setOnClickListener(v -> showManageTagsDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadData() {
        try {
            List<User> users = PersistenceManager.load(this);
            if (!users.isEmpty()) {
                currentUser = users.get(0);
                album = currentUser.getAlbumByName(albumName);
                if (album != null) {
                    photos = album.getPhotos();
                }
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

    private void updateDisplay() {
        Photo currentPhoto = photos.get(currentIndex);
        displayCaption.setText(currentPhoto.getCaption().isEmpty() ? "No caption" : currentPhoto.getCaption());
        
        String tagsStr = currentPhoto.getTags().stream()
                .map(Tag::toString)
                .collect(Collectors.joining(", "));
        displayTags.setText("Tags: " + (tagsStr.isEmpty() ? "none" : tagsStr));

        try {
            Uri uri = Uri.parse(currentPhoto.getUriString());
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            displayImage.setImageBitmap(bitmap);
            if (is != null) is.close();
        } catch (Exception e) {
            displayImage.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        
        setTitle("Photo " + (currentIndex + 1) + " of " + photos.size());
    }

    private void showManageTagsDialog() {
        Photo currentPhoto = photos.get(currentIndex);
        String[] options = {"Add Tag", "Remove Tag", "Edit Caption"};
        
        new AlertDialog.Builder(this)
                .setTitle("Manage Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showAddTagDialog(currentPhoto);
                    else if (which == 1) showRemoveTagDialog(currentPhoto);
                    else if (which == 2) showEditCaptionDialog(currentPhoto);
                })
                .show();
    }

    private void showEditCaptionDialog(Photo photo) {
        final EditText input = new EditText(this);
        input.setText(photo.getCaption());
        new AlertDialog.Builder(this)
                .setTitle("Edit Caption")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    photo.setCaption(input.getText().toString().trim());
                    saveData();
                    updateDisplay();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddTagDialog(Photo photo) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final Spinner typeSpinner = new Spinner(this);
        String[] validTypes = {"Person", "Location"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, validTypes);
        typeSpinner.setAdapter(adapter);

        final EditText valueInput = new EditText(this);
        valueInput.setHint("Tag Value");

        layout.addView(new TextView(this) {{ setText("Select Tag Type:"); }});
        layout.addView(typeSpinner);
        layout.addView(valueInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Tag")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String type = typeSpinner.getSelectedItem().toString();
                    String value = valueInput.getText().toString().trim();
                    if (!value.isEmpty()) {
                        Tag newTag = new Tag(type, value);
                        if (photo.addTag(newTag)) {
                            saveData();
                            updateDisplay();
                        } else {
                            Toast.makeText(this, "Tag already exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRemoveTagDialog(Photo photo) {
        if (photo.getTags().isEmpty()) {
            Toast.makeText(this, "No tags to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Tag> tags = photo.getTags();
        String[] tagStrings = tags.stream().map(Tag::toString).toArray(String[]::new);

        new AlertDialog.Builder(this)
                .setTitle("Remove Tag")
                .setItems(tagStrings, (dialog, which) -> {
                    photo.removeTag(tags.get(which));
                    saveData();
                    updateDisplay();
                })
                .show();
    }
}
