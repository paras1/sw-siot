package net.siot.android.gateway.connection;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.siot.android.gateway.util.GUIDUtil;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Sathesh Paramasamy on 02.10.15.
 * MQTT client using eclipse paho client
 */
public class MQTTClient implements MqttCallback {
    private static final String TAG = "siotag/MQTTClient";


    public static final String FILTER = "filter";

    private String sBrokerURL;
    private String sGUID;

    private MqttAsyncClient mqttClient;

    private Context ctx;

    public MQTTClient(Context ctx, String sCenterGUID, String brokerURL) {
        this.ctx = ctx;
        this.sGUID = sCenterGUID;
        this.sBrokerURL = brokerURL;
    }



    public void connectBroker() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttAsyncClient(sBrokerURL, sGUID+"_"+GUIDUtil.getGUID(), persistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            mqttClient.connect(options);
            mqttClient.setCallback(this);
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

    public void publishData(String sTopic, String values) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage message = new MqttMessage(values.toString().getBytes());
                mqttClient.publish(sTopic, message);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.i(TAG, "Not connected to MQTT broker");
        }
    }

    public void subscribeData(String topic) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try{
                mqttClient.subscribe(topic, 2);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.i(TAG, "Not connected to MQTT broker");
        }
    }

    public boolean isConnected() {

        return mqttClient.isConnected();

    }

    @Override
    public void connectionLost(Throwable cause) {
        Toast.makeText(ctx.getApplicationContext(), "Connection lost to MQTT broker.", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Connection lost: "+cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG, "Message received - TOPIC: "+topic+" MSG: "+message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
