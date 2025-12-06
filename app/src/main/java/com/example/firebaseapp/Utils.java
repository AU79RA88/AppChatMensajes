package com.example.firebaseapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Utils {


    public static String getConversationId(String a, String b) {
        if (a == null || b == null) return a + "_" + b;
        if (a.compareTo(b) < 0) return a + "_" + b;
        return b + "_" + a;
    }


    public static Bitmap uriToBitmap(Context ctx, Uri uri) {
        try {
            InputStream input = ctx.getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String saveBitmapTemp(Context ctx, Bitmap bmp) {
        try {
            File file = new File(ctx.getCacheDir(), "temp_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }




    public static class MqttManager {

        private static final String TAG = "MqttManager";

        private static MqttManager instance;

        private static final String BROKER =
                "tcp://7a5c28a5fe6544c8b18a8329526400b6.s1.eu.hivemq.cloud:1883";

        private static final String CLIENT_ID = "apknose_android_" + System.currentTimeMillis();
        private static final String MQTT_USER = "Apknose";
        private static final String MQTT_PASS = "R6BjtKuE!g7Rf9v";

        private final Context context;
        private MqttAndroidClient client;
        private boolean connected = false;

        private OnMessageListener onMessageListener;
        private OnConnectionListener onConnectionListener;


        public static synchronized MqttManager get(Context ctx) {
            if (instance == null)
                instance = new MqttManager(ctx);
            return instance;
        }

        private MqttManager(Context ctx) {
            this.context = ctx.getApplicationContext();
            initClient();
        }

        private void initClient() {
            client = new MqttAndroidClient(context, BROKER, CLIENT_ID);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    connected = false;

                    if (onConnectionListener != null)
                        onConnectionListener.onDisconnected();

                    reconnect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    if (onMessageListener != null)
                        onMessageListener.onMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });
        }


        public void connect() {
            try {
                if (client.isConnected()) return;

                MqttConnectOptions opts = new MqttConnectOptions();
                opts.setUserName(MQTT_USER);
                opts.setPassword(MQTT_PASS.toCharArray());
                opts.setAutomaticReconnect(true);
                opts.setCleanSession(true);

                client.connect(opts, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken token) {
                        connected = true;

                        if (onConnectionListener != null)
                            onConnectionListener.onConnected();
                    }

                    @Override
                    public void onFailure(IMqttToken token, Throwable e) {
                        connected = false;
                        reconnect();
                    }
                });

            } catch (Exception ignored) {}
        }

        private void reconnect() {
            try { Thread.sleep(1500); } catch (Exception ignored) {}
            connect();
        }


        public void publish(String topic, String payload) {
            try {
                if (!connected || client == null) connect();

                MqttMessage msg = new MqttMessage(payload.getBytes());
                msg.setQos(1);
                client.publish(topic, msg);

            } catch (Exception ignored) {}
        }

        public void subscribe(String topic, int qos) {
            try {
                client.subscribe(topic, qos);
            } catch (Exception ignored) {}
        }

        public void unsubscribe(String topic) {
            try {
                if (client.isConnected()) client.unsubscribe(topic);
            } catch (Exception ignored) {}
        }

        public boolean isConnected() {
            return connected;
        }

        public interface OnMessageListener {
            void onMessage(String topic, MqttMessage message);
        }

        public void setOnMessageListener(OnMessageListener listener) {
            this.onMessageListener = listener;
        }

        public interface OnConnectionListener {
            void onConnected();
            void onDisconnected();
        }

        public void setOnConnectionListener(OnConnectionListener listener) {
            this.onConnectionListener = listener;
        }
    }
}
