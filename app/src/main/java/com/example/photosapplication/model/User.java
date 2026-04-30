package com.example.photosapplication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private List<Album> albums;
    private List<String> customTagTypes;

    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
        this.customTagTypes = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public List<String> getCustomTagTypes() {
        return customTagTypes;
    }

    public void addCustomTagType(String type) {
        if (type == null || type.trim().isEmpty()) return;

        for (String t : customTagTypes) {
            if (t.equalsIgnoreCase(type)) {
                return;
            }
        }
        customTagTypes.add(type);
    }
    public Album getAlbumByName(String name) {
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                return album;
            }
        }
        return null;
    }
    @Override
    public String toString() {
        return username;
    }
}