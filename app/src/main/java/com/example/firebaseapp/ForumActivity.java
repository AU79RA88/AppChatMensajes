package com.example.firebaseapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.adapter.ForumAdapter;
import com.example.firebaseapp.model.Post;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ForumActivity extends AppCompatActivity {

    private FirebaseManager fm;
    private CollectionReference postsRef;

    EditText postTextEt;
    RecyclerView recycler;
    ForumAdapter adapter;
    List<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        fm = FirebaseManager.get();
        postsRef = fm.getFirestore().collection("posts");

        postTextEt = findViewById(R.id.postEditText);
        recycler = findViewById(R.id.forumRecyclerView);

        adapter = new ForumAdapter(posts);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btnPost).setOnClickListener(v -> createPost());

        startListeningPosts();
    }

    private void createPost() {
        FirebaseUser user = fm.getAuth().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario desconocido. Inicia sesión para publicar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String texto = postTextEt.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        fm.getFirestore().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = "Desconocido";
                    if (doc != null && doc.exists()) {
                        String n = doc.getString("nombre");
                        if (n != null && !n.isEmpty()) name = n;
                    } else if (user.getDisplayName() != null) {
                        name = user.getDisplayName();
                    }
                    long ts = System.currentTimeMillis();
                    Post p = new Post(user.getUid(), name, texto, ts);
                    postsRef.add(p);
                    postTextEt.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void startListeningPosts() {
        postsRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(200)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots == null) return;
                    List<Post> list = snapshots.toObjects(Post.class);
                    adapter.update(list);
                });
    }
}
