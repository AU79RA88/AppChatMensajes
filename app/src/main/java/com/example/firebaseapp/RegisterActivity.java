package com.example.firebaseapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends AppCompatActivity {

    EditText nombreEt, emailEt, passEt;
    Button btnRegister, btnGoLogin;
    ImageView imagePreview;

    private FirebaseManager fm;
    private Uri selectedImage = null;

    ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImage = uri;
                            imagePreview.setImageURI(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fm = FirebaseManager.get();

        nombreEt = findViewById(R.id.nombreEt);
        emailEt = findViewById(R.id.emailEt);
        passEt = findViewById(R.id.passEt);
        imagePreview = findViewById(R.id.imgPreview);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        imagePreview.setOnClickListener(v -> pickImage.launch("image/*"));

        btnRegister.setOnClickListener(v -> registerUser());

        btnGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void registerUser() {
        String nombre = nombreEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String pass = passEt.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        fm.getAuth().createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = fm.getAuth().getCurrentUser();
                    if (user == null) return;

                    if (selectedImage != null) uploadPhoto(user, nombre, email);
                    else saveUser(user, nombre, email, "");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadPhoto(FirebaseUser user, String nombre, String email) {
        StorageReference ref = fm.getStorage()
                .getReference("profiles/" + user.getUid() + ".jpg");

        ref.putFile(selectedImage)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(url ->
                                saveUser(user, nombre, email, url.toString())
                        ))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error subiendo foto: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUser(FirebaseUser user, String nombre, String email, String fotoUrl) {
        Usuario u = new Usuario(user.getUid(), nombre, email, fotoUrl);

        fm.getFirestore().collection("users").document(user.getUid()).set(u);
        fm.getRealtimeRef("usuarios").child(user.getUid()).setValue(u)
                .addOnSuccessListener(aVoid -> {
                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(token -> {
                                fm.getRealtimeRef("usuarios")
                                        .child(user.getUid())
                                        .child("deviceToken")
                                        .setValue(token);
                            });

                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error guardando usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}