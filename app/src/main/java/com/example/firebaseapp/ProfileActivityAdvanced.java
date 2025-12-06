package com.example.firebaseapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.firebaseapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivityAdvanced extends AppCompatActivity {

    private ImageView imgProfile;
    private Button btnChangePhoto, btnSave, btnLogout;
    private androidx.appcompat.widget.AppCompatEditText etName, etStatus;
    private ProgressBar progressBar;

    private FirebaseManager fm;
    private FirebaseFirestore fs;
    private String uid;
    private Uri selectedImageUri;
    private String currentPhotoUrl;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgProfile.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_advanced);

        imgProfile = findViewById(R.id.imgProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
        etName = findViewById(R.id.etName);
        etStatus = findViewById(R.id.etStatus);
        progressBar = findViewById(R.id.profileProgressBar);

        fm = FirebaseManager.get();
        fs = fm.getFirestore();
        if (fm.getAuth() == null || fm.getAuth().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = fm.getAuth().getCurrentUser().getUid();

        loadUserProfile();

        btnChangePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText() != null ? etName.getText().toString().trim() : "";
            String newStatus = etStatus.getText() != null ? etStatus.getText().toString().trim() : "";
            if (newName.isEmpty()) {
                etName.setError("El nombre no puede estar vacío");
                return;
            }
            saveProfile(newName, newStatus);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivityAdvanced.this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        fs.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String nombre = doc.getString("nombre");
                        String foto = doc.getString("fotoUrl");
                        String status = doc.getString("status");
                        if (nombre != null) etName.setText(nombre);
                        if (status != null) etStatus.setText(status);
                        currentPhotoUrl = foto;
                        if (foto != null && !foto.isEmpty()) {
                            Glide.with(this).load(foto).placeholder(R.drawable.default_user).into(imgProfile);
                        } else {
                            imgProfile.setImageResource(R.drawable.default_user);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivityAdvanced.this, "Error cargando perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile(String name, String status) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);


        if (selectedImageUri != null) {
            uploadAndSave(selectedImageUri, name, status);
        } else {

            Map<String, Object> updates = new HashMap<>();
            updates.put("nombre", name);
            updates.put("status", status);

            fs.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {

                        fm.getRealtimeRef("usuarios").child(uid).child("nombre").setValue(name);
                        fm.getRealtimeRef("usuarios").child(uid).child("status").setValue(status);
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(ProfileActivityAdvanced.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(ProfileActivityAdvanced.this, "Error actualizando perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void uploadAndSave(Uri uri, String name, String status) {
        try {

            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bmp = com.bumptech.glide.Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .submit(800, 800)
                    .get();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            StorageReference ref = fm.getStorageRef().child("profiles").child(uid + ".jpg");
            ref.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String url = downloadUri.toString();
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("nombre", name);
                                updates.put("status", status);
                                updates.put("fotoUrl", url);

                                fs.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {

                                            fm.getRealtimeRef("usuarios").child(uid).child("nombre").setValue(name);
                                            fm.getRealtimeRef("usuarios").child(uid).child("status").setValue(status);
                                            fm.getRealtimeRef("usuarios").child(uid).child("fotoUrl").setValue(url);

                                            progressBar.setVisibility(View.GONE);
                                            btnSave.setEnabled(true);
                                            selectedImageUri = null;
                                            Toast.makeText(ProfileActivityAdvanced.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnSave.setEnabled(true);
                                            Toast.makeText(ProfileActivityAdvanced.this, "Error guardando perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnSave.setEnabled(true);
                                Toast.makeText(ProfileActivityAdvanced.this, "Error obteniendo URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(ProfileActivityAdvanced.this, "Error subiendo imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            Toast.makeText(this, "Error procesando imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
