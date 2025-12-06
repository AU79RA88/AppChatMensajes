package com.example.firebaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.adapter.UsuarioAdapter;
import com.example.firebaseapp.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ProgressBar loading;
    private UsuarioAdapter adapter;
    private final List<Usuario> lista = new ArrayList<>();

    private FirebaseManager fm;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        recycler = findViewById(R.id.recyclerContacts);
        loading = findViewById(R.id.progressBarContacts);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuarioAdapter(lista, usuario -> {

            Intent i = new Intent(ContactsActivity.this, ChatActivity.class);
            i.putExtra("otherUid", usuario.getUid());
            i.putExtra("otherName", usuario.getNombre());
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        fm = FirebaseManager.get();
        myUid = FirebaseAuth.getInstance().getUid();

        loadContacts();
    }

    private void loadContacts() {
        showLoading(true);
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lista.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Usuario u = doc.toObject(Usuario.class);
                        if (u == null || u.getUid() == null) continue;
                        if (u.getUid().equals(myUid)) continue; // no mostrarme
                        lista.add(u);
                    }
                    adapter.notifyDataSetChanged();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error cargando usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean s) {
        loading.setVisibility(s ? View.VISIBLE : View.GONE);
    }
}
