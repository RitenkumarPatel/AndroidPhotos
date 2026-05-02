package com.example.photosapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photosapplication.model.Album;
import com.example.photosapplication.model.PersistenceManager;
import com.example.photosapplication.model.Photo;
import com.example.photosapplication.model.Tag;
import com.example.photosapplication.model.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private Spinner spinnerType1, spinnerType2;
    private EditText inputValue1, inputValue2;
    private RadioGroup radioOperator;
    private RadioButton radioSingle, radioAnd, radioOr;
    private RecyclerView resultsRecycler;
    private PhotoAdapter adapter;
    private List<Photo> allPhotos = new ArrayList<>();
    private final List<Photo> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle("Search Photos");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerType1 = findViewById(R.id.spinner_type1);
        spinnerType2 = findViewById(R.id.spinner_type2);
        inputValue1 = findViewById(R.id.input_value1);
        inputValue2 = findViewById(R.id.input_value2);
        radioOperator = findViewById(R.id.radio_operator);
        radioSingle = findViewById(R.id.radio_none);
        radioAnd = findViewById(R.id.radio_and);
        radioOr = findViewById(R.id.radio_or);
        Button btnSearch = findViewById(R.id.btn_search);
        resultsRecycler = findViewById(R.id.search_results_recycler);

        String[] tagTypes = {"Person", "Location"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tagTypes);
        spinnerType1.setAdapter(spinnerAdapter);
        spinnerType2.setAdapter(spinnerAdapter);

        resultsRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PhotoAdapter(searchResults, new PhotoAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoClick(Photo photo, int position) {
                // For search results, we just show a toast for now
                Toast.makeText(SearchActivity.this, "Result photo selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPhotoLongClick(Photo photo, int position) {
                // No options for search result items
            }
        });
        resultsRecycler.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> performSearch());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadAllPhotos() {
        try {
            List<User> users = PersistenceManager.load(this);
            if (!users.isEmpty()) {
                User user = users.get(0);
                Set<Photo> uniquePhotos = new HashSet<>();
                for (Album album : user.getAlbums()) {
                    uniquePhotos.addAll(album.getPhotos());
                }
                allPhotos = new ArrayList<>(uniquePhotos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performSearch() {
        loadAllPhotos();
        String type1 = spinnerType1.getSelectedItem().toString();
        String val1 = inputValue1.getText().toString().trim();
        String type2 = spinnerType2.getSelectedItem().toString();
        String val2 = inputValue2.getText().toString().trim();

        if (val1.isEmpty()) {
            Toast.makeText(this, "Please enter a value for Clause 1", Toast.LENGTH_SHORT).show();
            return;
        }

        searchResults.clear();
        for (Photo photo : allPhotos) {
            boolean match1 = matches(photo, type1, val1);
            boolean match2 = matches(photo, type2, val2);

            if (radioSingle.isChecked()) {
                if (match1) searchResults.add(photo);
            } else if (radioAnd.isChecked()) {
                if (val2.isEmpty()) {
                    Toast.makeText(this, "Please enter a value for Clause 2 for AND search", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (match1 && match2) searchResults.add(photo);
            } else if (radioOr.isChecked()) {
                if (val2.isEmpty()) {
                    Toast.makeText(this, "Please enter a value for Clause 2 for OR search", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (match1 || match2) searchResults.add(photo);
            }
        }

        adapter.notifyDataSetChanged();
        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No photos found matching criteria", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean matches(Photo photo, String type, String value) {
        if (value.isEmpty()) return false;
        for (Tag tag : photo.getTags()) {
            if (tag.getName().equalsIgnoreCase(type) && tag.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
