package com.example.photosapplication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a photo album in the application - stores the album name
 * and the collection of photos contained in the album.
 *
 * @author Abhi Challa
 * @author Ritenkumar Patel
 */
public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Photo> getPhotos() { return photos; }

    /**
     * Adds a photo to the album if it doesn't already exist.
     * Uses the URI string to check for duplicates.
     */
    public void addPhoto(Photo photo) {
        for (Photo p : photos) {
            // Updated to use the new URI field from the Photo class
            if (p.getUriString().equals(photo.getUriString())) {
                return;
            }
        }
        photos.add(photo);
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }

    @Override
    public String toString() {
        return name;
    }
}