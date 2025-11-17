package com.example.firebaseapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.adapter.UsuarioAdapter;
import com.example.firebaseapp.model.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    UsuarioAdapter adapter;
    ArrayList<Usuario> usuariosList = new ArrayList<>();
    FirebaseManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fm = FirebaseManager.get();

        recyclerView = findViewById(R.id.chatListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UsuarioAdapter(usuariosList, usuario -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("otherUid", usuario.getUid());
            intent.putExtra("otherName", usuario.getNombre());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        fm.getRealtimeRef("usuarios").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                usuariosList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario u = ds.getValue(Usuario.class);
                    if (u != null) usuariosList.add(u);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) return true;
            if (item.getItemId() == R.id.nav_chat) return true;
            if (item.getItemId() == R.id.nav_forum) {
                startActivity(new Intent(this, ForumActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_registro) {
                startActivity(new Intent(this, RegisterActivity.class));
                return true;
            }
            return false;
        });
    }
}
