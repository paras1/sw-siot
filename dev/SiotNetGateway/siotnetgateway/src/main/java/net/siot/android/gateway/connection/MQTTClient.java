package net.siot.android.gateway.connection;

import android.util.Log;

import net.siot.android.gateway.util.GUIDUtil;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT client implementation for siot.net, using eclipse paho client
 * Created by Sathesh Paramasamy on 02.10.15.
 */
public class MQTTClient implements MqttCallback {
    private static final String TAG = "siotag/MQTTClient";

    private String sBrokerURL;
    private String sGUID;

    private MqttAsyncClient mqttClient;

    private static MQTTClient singleton;

    private MQTTClient(){}

    /**
     * getInstance of the MQTTClient singlton
     * @return
     */
    public static MQTTClient getInstance(){

        if(singleton == null)
            singleton = new MQTTClient();

        return singleton;
    }

    /**
     * get MQTT client connection, with given parameters
     * @param sGUID GUID or license of the siot.net center
     * @param sBrokerURL MQTT broker url from siot.net URL-Service
     */
    public void connectBroker(String sGUID, String sBrokerURL) {
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

    /**
     * disconnect the MQTT client
     */
    public void disconnectBroker() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * method to publishing data to the connected MQTT broker
     * @param sTopic publishing topicname
     * @param values data to publish
     */
    public void publishData(String sTopic, String values) {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                MqttMessage message = new MqttMessage(values.toString().getBytes());
                mqttClient.publish(sTopic, message);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "Not connected to MQTT broker");
        }
    }

    /**
     * subscribing data from the MQTT broker
     * @param topic subscribing topicname
     */
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

    /**
     * get the connection state of the client
     * @return connection state
     */
    public boolean isConnected() {

        return mqttClient.isConnected();

    }

    @Override
    public void connectionLost(Throwable cause) {
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
