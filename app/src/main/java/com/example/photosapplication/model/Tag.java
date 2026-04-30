package com.example.photosapplication.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents tag associated with photo, stores tag values used to organize and describe photos.
 *
 * @author Abhi Challa
 * @author Ritenkumar Patel
 */

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    // Standard getters
    public String getName() { return name; }
    public String getValue() { return value; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            Tag other = (Tag) obj; // Manual cast required in Java 11
            return name.equalsIgnoreCase(other.name) && value.equalsIgnoreCase(other.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }


    @Override
    public String toString() {
        return name + ":" + value;
    }
}