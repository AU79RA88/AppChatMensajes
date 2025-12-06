package com.example.app.utils;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTManager {

    private static MQTTManager instance;
    private MqttAndroidClient client;

    private IMqttMessageListener globalListener;

    private static final String MQTT_URI =
            "ssl://e2a8215d405f481c9ed7129714708fe7.s1.eu.hivemq.cloud:8884";

    private static final String USERNAME = "Apknose";
    private static final String PASSWORD = "R6BjtKuE!g7Rf9v";

    public static MQTTManager getInstance(Context ctx) {
        if (instance == null)
            instance = new MQTTManager(ctx.getApplicationContext());
        return instance;
    }

    private MQTTManager(Context context) {
        client = new MqttAndroidClient(
                context,
                MQTT_URI,
                "ANDROID_CLIENT_" + System.currentTimeMillis()
        );
    }

    public void connect() {

        if (client.isConnected()) {
            Log.d("MQTT", "Ya conectado");
            return;
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            client.connect(options, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT", "Conectado a HiveMQ");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "Error al conectar: " + exception.getMessage());
                }
            });

        } catch (MqttException e) {
            Log.e("MQTT", "Error en connect(): " + e.getMessage());
        }


        client.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("MQTT", reconnect ? "Reconectado" : "Conectado por primera vez");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e("MQTT", "Conexión perdida: " + cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                if (globalListener != null) {
                    globalListener.messageArrived(topic, message);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic, 1);
            Log.d("MQTT", "Suscrito a " + topic);
        } catch (Exception e) {
            Log.e("MQTT", "Error al suscribirse a " + topic + ": " + e.getMessage());
        }
    }

    public void subscribe(String topic, IMqttMessageListener listener) {
        try {
            client.subscribe(topic, 1, listener);
            Log.d("MQTT", "Suscrito con listener a " + topic);
        } catch (Exception e) {
            Log.e("MQTT", "Error al suscribirse con listener: " + e.getMessage());
        }
    }

    public void unsubscribe(String topic) {
        try {
            client.unsubscribe(topic);
            Log.d("MQTT", "Desuscrito de " + topic);
        } catch (Exception ignored) {}
    }

    public void publish(String topic, String message) {
        try {
            MqttMessage msg = new MqttMessage(message.getBytes());
            msg.setQos(1);
            client.publish(topic, msg);

            Log.d("MQTT", "Publicado en " + topic + ": " + message);

        } catch (Exception e) {
            Log.e("MQTT", "Error publicando en " + topic + ": " + e.getMessage());
        }
    }

    public void setGlobalMessageListener(IMqttMessageListener listener) {
        this.globalListener = listener;
    }
}
