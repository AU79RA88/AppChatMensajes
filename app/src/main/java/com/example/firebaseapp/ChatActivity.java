package com.example.firebaseapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.adapter.MensajeAdapter;
import com.example.firebaseapp.model.Mensaje;
import com.example.firebaseapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ProgressBar progressBar;

    private MensajeAdapter adapter;
    private final List<Mensaje> mensajes = new ArrayList<>();

    private FirebaseManager fm;
    private DatabaseReference messagesRef;
    private ChildEventListener messageListener;

    private String otherUid;
    private String convoId;
    private FirebaseUser currentUser;


    private final Map<String,String> userNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fm = FirebaseManager.get();
        currentUser = fm.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario desconocido. Inicia sesión para chatear.", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        otherUid = getIntent().getStringExtra("otherUid");
        if (otherUid == null || otherUid.isEmpty()) { finish(); return; }

        convoId = Utils.getConversationId(currentUser.getUid(), otherUid);
        messagesRef = fm.getRealtimeRef("conversations").child(convoId).child("messages");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        progressBar = findViewById(R.id.chatProgressBar);

        adapter = new MensajeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());


        fm.getRealtimeRef("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Usuario u = ds.getValue(Usuario.class);
                    if (u != null && u.getUid() != null) userNames.put(u.getUid(), u.getNombre());
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        startListen();
    }

    private void sendMessage() {
        String texto = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        String uidFrom = currentUser.getUid();
        long ts = System.currentTimeMillis();
        String nameFrom = userNames.containsKey(uidFrom) ? userNames.get(uidFrom) :
                (currentUser.getDisplayName() == null ? "Desconocido" : currentUser.getDisplayName());

        Mensaje m = new Mensaje(uidFrom, nameFrom, texto, ts);
        DatabaseReference newMsgRef = messagesRef.push();
        newMsgRef.setValue(m)
                .addOnSuccessListener(aVoid -> messageEditText.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void startListen() {
        progressBar.setIndeterminate(true);
        messageListener = new ChildEventListener() {
            @Override public void onChildAdded(DataSnapshot snapshot, String prevChild) {
                Mensaje m = snapshot.getValue(Mensaje.class);
                if (m == null) return;

                if (m.getDeName() == null && m.getDe() != null) {
                    String cached = userNames.get(m.getDe());
                    if (cached != null) m.setDeName(cached);
                }
                mensajes.add(m);
                adapter.submitList(new ArrayList<>(mensajes));
                recyclerView.scrollToPosition(mensajes.size() - 1);
                progressBar.setIndeterminate(false);
            }
            @Override public void onChildChanged(DataSnapshot snapshot, String prevChild) {}
            @Override public void onChildRemoved(DataSnapshot snapshot) {}
            @Override public void onChildMoved(DataSnapshot snapshot, String prevChild) {}
            @Override public void onCancelled(DatabaseError error) {
                progressBar.setIndeterminate(false);
                Toast.makeText(ChatActivity.this, "Error BD: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        messagesRef.addChildEventListener(messageListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messageListener != null && messagesRef != null) messagesRef.removeEventListener(messageListener);
    }
}

