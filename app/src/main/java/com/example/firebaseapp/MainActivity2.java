package com.example.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.firebaseapp.adapter.MensajeAdapter;
import com.example.firebaseapp.model.Mensaje;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity2 extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText messageEditText;
    ImageButton sendButton;
    ArrayList<Mensaje> mensajesList = new ArrayList<>();
    MensajeAdapter adapter;

    String usuarioId;
    DatabaseReference mensajesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        usuarioId = getIntent().getStringExtra("usuarioId");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MensajeAdapter(mensajesList);
        recyclerView.setAdapter(adapter);

        mensajesRef = FirebaseDatabase.getInstance().getReference("mensajes");

        sendButton.setOnClickListener(v -> {
            String texto = messageEditText.getText().toString().trim();
            if (!texto.isEmpty()) {
                HashMap<String, Object> msg = new HashMap<>();
                msg.put("de", "UsuarioLocal");
                msg.put("para", usuarioId);
                msg.put("texto", texto);
                msg.put("timestamp", System.currentTimeMillis());
                mensajesRef.push().setValue(msg);
                messageEditText.setText("");
            }
        });

        mensajesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mensajesList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Mensaje m = ds.getValue(Mensaje.class);
                    mensajesList.add(m);
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(mensajesList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_registro) {
                startActivity(new Intent(this, MainActivity3.class));
                return true;
            }
            return false;
        });

    }
}
