package com.example.firebaseapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

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

    private static final int PICK_IMAGE = 101;
    private ImageView userImage;

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
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usuariosList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario u = ds.getValue(Usuario.class);
                    if (u != null) usuariosList.add(u);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });


        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {

                return true;
            }

            if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatGlobalActivity.class));
                return true;
            }

            if (id == R.id.nav_forum) {
                startActivity(new Intent(this, ForumActivity.class));
                return true;
            }

            if (id == R.id.nav_registro) {
                startActivity(new Intent(this, RegisterActivity.class));
                return true;
            }

            return false;
        });


        userImage = findViewById(R.id.userImage);

        userImage.setOnClickListener(v -> openGallery());

        userImage.setOnLongClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            userImage.setImageURI(imageUri);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    private void loadUserInfo() {

    }
}
