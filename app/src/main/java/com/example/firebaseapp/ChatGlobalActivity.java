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

public class ChatGlobalActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 200;

    RecyclerView recycler;
    EditText input;
    ImageView btnSend, btnImage;
    ProgressBar progressBar;

    MensajeAdapter adapter;
    ArrayList<Mensaje> mensajes = new ArrayList<>();

    FirebaseManager fm;
    FirebaseUser user;
    DatabaseReference refChatGlobal;

    Utils.MqttManager mqtt;
    NotificationSender notif;

    private final String MQTT_TOPIC_TEXT = "global_chat/messages";
    private final String MQTT_TOPIC_IMG = "global_chat/images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_global);

        fm = FirebaseManager.get();
        user = fm.getAuth().getCurrentUser();
        mqtt = Utils.MqttManager.get(this);
        notif = new NotificationSender(this);

        if (user == null) {
            finish();
            return;
        }

        refChatGlobal = fm.getRealtimeRef("chat_global/messages");

        recycler = findViewById(R.id.globalChatRecycler);
        input = findViewById(R.id.globalMessageEditText);
        btnSend = findViewById(R.id.sendGlobalButton);
        btnImage = findViewById(R.id.btnSendGlobalImage);
        progressBar = findViewById(R.id.progressGlobal);

        adapter = new MensajeAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendText());
        btnImage.setOnClickListener(v -> pickImage());

        setupMqtt();
        listenFirebase();
    }


    private void setupMqtt() {
        mqtt.connect();
        mqtt.subscribe(MQTT_TOPIC_TEXT, 1);
        mqtt.subscribe(MQTT_TOPIC_IMG, 1);

        mqtt.setOnMessageListener((topic, message) -> {
            String payload = new String(message.getPayload());

            if (topic.equals(MQTT_TOPIC_TEXT)) {
                handleIncomingText(payload);

            } else if (topic.equals(MQTT_TOPIC_IMG)) {
                handleIncomingImage(payload);
            }
        });
    }

    private void handleIncomingText(String texto) {
        long ts = System.currentTimeMillis();
        Mensaje m = new Mensaje("global", "Global", texto, ts, null);
        refChatGlobal.push().setValue(m);
    }

    private void handleIncomingImage(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        String temp = Utils.saveBitmapTemp(this, bmp);

        sendImage(temp);
    }

    private void sendText() {
        String txt = input.getText().toString();
        if (TextUtils.isEmpty(txt)) return;

        mqtt.publish(MQTT_TOPIC_TEXT, txt);
        saveMessage(txt, null);
        input.setText("");
    }

    private void sendImage(String url) {
        saveMessage("Imagen", url);
    }

    private void saveMessage(String txt, String imgUrl) {
        long ts = System.currentTimeMillis();

        String name = user.getDisplayName() != null ? user.getDisplayName() : "Usuario";

        Mensaje m = new Mensaje(user.getUid(), name, txt, ts, imgUrl);

        refChatGlobal.push().setValue(m)
                .addOnSuccessListener(a -> notif.sendPushNotification("global", txt, imgUrl));
    }

    private void listenFirebase() {
        refChatGlobal.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(com.google.firebase.database.DataSnapshot snapshot, String prev) {
                Mensaje m = snapshot.getValue(Mensaje.class);
                if (m == null) return;

                mensajes.add(m);
                adapter.submitList(new ArrayList<>(mensajes));
                recycler.scrollToPosition(mensajes.size() - 1);
            }

            @Override public void onChildChanged(com.google.firebase.database.DataSnapshot snapshot, String prev) {}
            @Override public void onChildRemoved(com.google.firebase.database.DataSnapshot snapshot) {}
            @Override public void onChildMoved(com.google.firebase.database.DataSnapshot snapshot, String prev) {}
            @Override public void onCancelled(com.google.firebase.database.DatabaseError error) {}
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
                .getReference("global_images/")
                .child(System.currentTimeMillis() + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(t ->
                        ref.getDownloadUrl().addOnSuccessListener(url -> {
                            Bitmap bmp = Utils.uriToBitmap(this, uri);
                            mqtt.publish(MQTT_TOPIC_IMG, toBase64(bmp));
                            sendImage(url.toString());
                        })
                );
    }

    private String toBase64(Bitmap b) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, out);
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }
}
