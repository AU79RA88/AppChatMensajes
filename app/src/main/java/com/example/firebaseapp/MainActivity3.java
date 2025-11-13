package com.example.firebaseapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import com.google.firebase.FirebaseApp;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity3 extends AppCompatActivity {



    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios");
ref.child("usuario1").setValue("Stinky");



}