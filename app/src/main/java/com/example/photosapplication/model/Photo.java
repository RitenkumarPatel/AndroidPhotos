package com.example.photosapplication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents a single photo in the application.
 * Stores the photo's URI, metadata, captions, and tags.
 *
 * @author Abhi Challa
 * @author Ritenkumar Patel
 */
public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uriString; // Changed from filePath
    private String caption;
    private Calendar date;
    private List<Tag> tags;

    public Photo(String uriString) {
        this.uriString = uriString;
        this.caption = "";
        this.tags = new ArrayList<>();

        // TODO: Figure out how to get the date/time
        // For Android, we typically capture the current time as the "date added"
        // since accessing the underlying filesystem date for a URI requires
        // a ContentResolver, which doesn't belong in a Model class.
        this.date = Calendar.getInstance();
        this.date.set(Calendar.MILLISECOND, 0);
    }

    // Getters and Setters
    public String getUriString() {
        return uriString;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Calendar getDate() {
        return date;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public boolean addTag(Tag tag) {
        for (Tag existing : tags) {
            if (existing.equals(tag)) {
                return false;
            }
        }
        tags.add(tag);
        return true;
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
}