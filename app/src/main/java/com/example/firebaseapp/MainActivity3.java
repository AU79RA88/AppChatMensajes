package com.example.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import java.util.HashMap;

public class MainActivity3 extends AppCompatActivity {

    EditText nombreEditText, emailEditText;
    Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Registro de Usuario");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        FirebaseApp.initializeApp(this);

        DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");
        DatabaseReference mensajesRef = FirebaseDatabase.getInstance().getReference("mensajes");

        nombreEditText = findViewById(R.id.nombreEditText);
        emailEditText = findViewById(R.id.emailEditText);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> {
            String nombre = nombreEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = usuariosRef.push().getKey();

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("nombre", nombre);
            userMap.put("correo", email);

            usuariosRef.child(userId).setValue(userMap)
                    .addOnSuccessListener(aVoid -> {

                        String mensajeId = mensajesRef.push().getKey();
                        HashMap<String, Object> msg = new HashMap<>();
                        msg.put("de", "Sistema");
                        msg.put("para", userId);
                        msg.put("texto", "Bienvenido al chat, " + nombre + "!");
                        msg.put("timestamp", System.currentTimeMillis());

                        mensajesRef.child(mensajeId).setValue(msg);
                        Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                    });
        });


        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_chat) {
                startActivity(new Intent(this, MainActivity2.class));
                return true;
            }
            return false;
        });
    }
}
