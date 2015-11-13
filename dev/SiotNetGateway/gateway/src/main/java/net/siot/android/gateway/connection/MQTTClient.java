package net.siot.android.gateway.connection;

/**
 * Created by Sathesh Paramasamy on 02.10.15.
 */

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;


public class MQTTClient {
    private static final String TAG = "siotag/MQTTClient";


    public static final String FILTER = "filter";

    private String sBrokerURL;
    private String sGUID;

    private MqttClient mqttClient;

    private Context ctx;

    public MQTTClient(Context ctx, String guid, String clientIdPrefix, String brokerURL, String mqttTopicPrefix) {

        this.ctx = ctx;
        this.sGUID = guid;
        this.sBrokerURL = brokerURL;

        connectBroker();
    }



    public void connectBroker() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(sBrokerURL, sGUID, persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            mqttClient.connect(options);
            Log.i(TAG, "CONNECTED to MQTT broker");
            Log.i(TAG, "MQTT ClientId is: " + mqttClient.getClientId());
        } catch (MqttException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void disconnectBroker() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void publishData(String topic, JSONObject siotJson) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage message = new MqttMessage(siotJson.toString().getBytes());
                mqttClient.publish(topic, message);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public boolean isConnected() {

        return mqttClient.isConnected();

    }
}
