package com.example.photosapplication.model;

import android.content.Context;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading application data using Android's internal storage.
 *
 * @author Abhi Challa
 * @author Ritenkumar Patel
 */
public class PersistenceManager {

    private static final String DATA_FILE = "users.dat";

    /**
     * Saves the list of users to the app's internal private storage.
     * Use context.getFilesDir() to get the correct path.
     */
    public static void save(Context context, List<User> users) throws IOException {
        // context.openFileOutput handles the creation of the file in the internal sandbox
        try (FileOutputStream fos = context.openFileOutput(DATA_FILE, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(users);
        }
    }

    /**
     * Loads the list of users from the app's internal private storage.
     */
    @SuppressWarnings("unchecked")
    public static List<User> load(Context context) throws IOException, ClassNotFoundException {
        File file = new File(context.getFilesDir(), DATA_FILE);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileInputStream fis = context.openFileInput(DATA_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<User>) ois.readObject();
        }
    }
}