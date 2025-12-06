package com.example.firebaseapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 200;

    private ImageView profileImage;
    private EditText editName;
    private Button btnSave;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileImage);
        editName = findViewById(R.id.editName);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profiles");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserInfo();

        profileImage.setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadUserInfo() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editName.setText(doc.getString("name"));
                        String imageUrl = doc.getString("imageUrl");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(profileImage);
                        }
                    }
                });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        StorageReference fileRef = storageRef.child(userId + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(task -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                db.collection("users").document(userId)
                        .update("imageUrl", uri.toString());
            });
        });
    }

    private void saveChanges() {
        String newName = editName.getText().toString().trim();

        db.collection("users").document(userId)
                .update("name", newName)
                .addOnSuccessListener(a ->
                        finish() // <-- vuelve a MainActivity
                );
    }
}
