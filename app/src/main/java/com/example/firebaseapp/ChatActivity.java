package com.example.firebaseapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.adapter.MensajeAdapter;
import com.example.firebaseapp.model.Mensaje;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 300;

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ProgressBar progressBar;
    private ImageView btnSendImage;

    private MensajeAdapter adapter;
    private final List<Mensaje> mensajes = new ArrayList<>();

    private FirebaseManager fm;
    private DatabaseReference messagesRef;

    private String otherUid;
    private String convoId;
    private FirebaseUser currentUser;

    private Utils.MqttManager mqtt;
    private NotificationSender notificationSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        fm = FirebaseManager.get();
        mqtt = Utils.MqttManager.get(getApplicationContext());
        notificationSender = new NotificationSender(this);

        currentUser = fm.getAuth().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Inicia sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        otherUid = getIntent().getStringExtra("otherUid");
        if (TextUtils.isEmpty(otherUid)) {
            finish();
            return;
        }

        convoId = Utils.getConversationId(currentUser.getUid(), otherUid);
        messagesRef = fm.getRealtimeRef("conversations").child(convoId).child("messages");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        progressBar = findViewById(R.id.chatProgressBar);
        btnSendImage = findViewById(R.id.btnSendImage);

        adapter = new MensajeAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.sendButton).setOnClickListener(v -> sendTextMessage());
        btnSendImage.setOnClickListener(v -> pickImage());

        setupMqttListeners();
        startFirebaseListener();
    }



    private void setupMqttListeners() {
        mqtt.connect();

        String topicText = "chat/" + convoId + "/messages";
        String topicImage = "chat/" + convoId + "/images";

        mqtt.subscribe(topicText, 1);
        mqtt.subscribe(topicImage, 1);

        mqtt.setOnMessageListener((topic, message) -> {
            String payload = new String(message.getPayload());

            if (topic.equals(topicText)) {
                handleIncomingText(payload);
            } else if (topic.equals(topicImage)) {
                handleIncomingImage(payload);
            }
        });
    }

    private void handleIncomingText(String texto) {
        runOnUiThread(() -> {
            long ts = System.currentTimeMillis();
            Mensaje m = new Mensaje(otherUid, "Usuario", texto, ts, null);

            messagesRef.push().setValue(m);
        });
    }

    private void handleIncomingImage(String base64) {
        runOnUiThread(() -> {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            String tempUrl = Utils.saveBitmapTemp(ChatActivity.this, bmp);

            sendMessageImage(tempUrl);
        });
    }



    private void sendTextMessage() {
        String texto = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        mqtt.publish("chat/" + convoId + "/messages", texto);

        saveMessageToFirebase(texto, null);

        messageEditText.setText("");
    }



    private void sendMessageImage(String imgUrl) {
        saveMessageToFirebase("Imagen", imgUrl);
    }


    private void saveMessageToFirebase(String texto, String imgUrl) {
        long ts = System.currentTimeMillis();
        String uid = currentUser.getUid();
        String name = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario";

        Mensaje m = new Mensaje(uid, name, texto, ts, imgUrl);

        messagesRef.push().setValue(m)
                .addOnSuccessListener(aVoid ->
                        notificationSender.sendPushNotification(otherUid, texto, imgUrl)
                );
    }



    private void startFirebaseListener() {
        progressBar.setIndeterminate(true);

        messagesRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot snapshot, String prev) {
                Mensaje m = snapshot.getValue(Mensaje.class);
                if (m == null) return;

                mensajes.add(m);
                adapter.submitList(new ArrayList<>(mensajes));
                recyclerView.scrollToPosition(mensajes.size() - 1);

                progressBar.setIndeterminate(false);
            }

            @Override public void onChildChanged(com.google.firebase.database.DataSnapshot snapshot, String prev) {}
            @Override public void onChildRemoved(com.google.firebase.database.DataSnapshot snapshot) {}
            @Override public void onChildMoved(com.google.firebase.database.DataSnapshot snapshot, String prev) {}
            @Override public void onCancelled(com.google.firebase.database.DatabaseError error) {
                progressBar.setIndeterminate(false);
            }
        });
    }



    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_IMAGE && res == RESULT_OK && data != null) {
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri uri) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("chat_images/")
                .child(System.currentTimeMillis() + ".jpg");

        ref.putFile(uri).addOnSuccessListener(task ->
                ref.getDownloadUrl().addOnSuccessListener(url -> {

                    Bitmap bmp = Utils.uriToBitmap(this, uri);
                    mqtt.publish("chat/" + convoId + "/images", convertToBase64(bmp));

                    sendMessageImage(url.toString());
                })
        );
    }


    private String convertToBase64(Bitmap b) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, out);
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }



    @Override
    protected void onStop() {
        super.onStop();
        mqtt.unsubscribe("chat/" + convoId + "/messages");
        mqtt.unsubscribe("chat/" + convoId + "/images");
    }
}
