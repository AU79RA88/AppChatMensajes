package com.example.firebaseapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseManager {

    private static FirebaseManager instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseDatabase realtime;
    private final FirebaseStorage storage;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        realtime = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static FirebaseManager get() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public DatabaseReference getRealtimeRef(String path) {
        return realtime.getReference(path);
    }

    public FirebaseStorage getStorage() {
        return storage;
    }


    public StorageReference getStorageRef() {
        return storage.getReference();
    }
}
