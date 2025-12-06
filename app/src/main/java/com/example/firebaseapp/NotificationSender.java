package com.example.firebaseapp;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationSender {

    private final Context context;

    public NotificationSender(Context context) {
        this.context = context;
    }

    public void sendPushNotification(String userToken, String text, String imageUrl) {

        JSONObject json = new JSONObject();
        try {
            json.put("to", userToken);

            JSONObject notification = new JSONObject();
            notification.put("title", "Nuevo mensaje");
            notification.put("body", text != null ? text : "Imagen recibida");

            JSONObject data = new JSONObject();
            data.put("imageUrl", imageUrl);

            json.put("notification", notification);
            json.put("data", data);

        } catch (Exception e) {}

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest req = new JsonObjectRequest(
                "https://fcm.googleapis.com/fcm/send",
                json,
                response -> {},
                error -> {}
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "cebdd50f91156ac68271b6c4fb0649f3ac61a698");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(req);
    }
}
