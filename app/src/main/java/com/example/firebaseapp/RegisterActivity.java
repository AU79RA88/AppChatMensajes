package com.example.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebaseapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText nombreEt, emailEt, passEt;
    Button btnRegister, btnGoLogin;
    private FirebaseManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fm = FirebaseManager.get();

        nombreEt = findViewById(R.id.nombreEt);
        emailEt = findViewById(R.id.emailEt);
        passEt = findViewById(R.id.passEt);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);

        btnRegister.setOnClickListener(v -> {
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

                        Usuario u = new Usuario(user.getUid(), nombre, email);
                        fm.getFirestore().collection("users").document(user.getUid()).set(u);
                        fm.getRealtimeRef("usuarios").child(user.getUid()).setValue(u)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error guardando usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnGoLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }
}
