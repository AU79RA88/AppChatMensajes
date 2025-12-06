package com.example.firebaseapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class NotificationHelper {

    private static final String CHANNEL_ID = "chat_messages_channel";

    public static void showNotification(Context context, String title, String body, String imageUrl, String senderId) {

        createNotificationChannel(context);

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("userId", senderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                senderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (imageUrl == null || imageUrl.isEmpty()) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(senderId.hashCode(), builder.build());
            return;
        }

        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {

                        builder.setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .setSummaryText(body)
                        );

                        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.notify(senderId.hashCode(), builder.build());
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {}
                });
    }

    private static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "Mensajes del chat";
            String description = "Notificaciones de mensajes nuevos";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    public void sendPushNotification(String userToken, String text, String imageUrl, String senderId) {

        JSONObject json = new JSONObject();
        try {
            json.put("to", userToken);

            JSONObject notification = new JSONObject();
            notification.put("title", "Nuevo mensaje");
            notification.put("body", text != null ? text : "Imagen recibida");

            JSONObject data = new JSONObject();
            data.put("imageUrl", imageUrl);
            data.put("senderId", senderId);

            json.put("notification", notification);
            json.put("data", data);

        } catch (Exception e) {}


    }
}
