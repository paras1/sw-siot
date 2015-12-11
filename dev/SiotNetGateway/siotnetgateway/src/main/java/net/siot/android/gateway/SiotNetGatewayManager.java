package net.siot.android.gateway;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import net.siot.android.gateway.connection.MQTTClient;
import net.siot.android.gateway.messagetypes.SiotUrl;
import net.siot.android.gateway.service.SensorService;
import net.siot.android.gateway.util.RestClient;

/**
 * Created by Sathesh on 28.10.15.
 */
public class SiotNetGatewayManager {
    private static final String TAG = "siotgw/SNGWManager";

    private Context ctx;
    private String sURLServiceLocationPrefix;

    public String sURLServiceLocation;

    public String sMqttBrokerUrl;

    public MQTTClient mqttClient;
    SensorService sensorService;


    public SiotNetGatewayManager(Context ctx) {

        this.ctx = ctx;
        sURLServiceLocationPrefix = "http://url.siot.net/?licence=";
    }

    public boolean connectToSiotNet(String sLicense) {

        setURLServiceLocation(sURLServiceLocationPrefix + sLicense);
        if((mqttClient == null || !mqttClient.isConnected()) && (sLicense != null && !sLicense.equals(""))) {
            Log.i(TAG, "JSON: " + RestClient.getSiotNetBrokerUrl(sURLServiceLocation));
            SiotUrl siotUrl = new Gson().fromJson(RestClient.getSiotNetBrokerUrl(sURLServiceLocation), SiotUrl.class);
            setMqttBrokerUrl("tcp://"+siotUrl.getMqtt().getUrls().get(0)+":1883");
            Log.i(TAG, "JSON mqtt url:" + siotUrl.getMqtt().getUrls().get(0));

            return connectBroker(sLicense, sMqttBrokerUrl);

        } else if (mqttClient.isConnected()) {
            Log.i(TAG, "Already connected to the MQTT broker");
            return true;

        } else {
            Log.i(TAG, "license number is empty");
            return false;
        }
    }

    public boolean disconnectFromSiotNet() {
        mqttClient.disconnectBroker();
        return mqttClient.isConnected();
    }

    private boolean connectBroker(String sCenterGUID, String sBrokerURL) {
        if (mqttClient == null);
            mqttClient = new MQTTClient(ctx, sCenterGUID, sBrokerURL);

        mqttClient.connectBroker();
        int i = 0;
        while (!mqttClient.isConnected() || i<100) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        if (mqttClient.isConnected()) {
            sensorService = new SensorService(ctx, sCenterGUID, mqttClient);
        } else {
            Log.i(TAG, "Connection to MQTT Broker "+sBrokerURL+" could not be established");
        }
        return mqttClient.isConnected();
    }

    //Getter and Setters

    public void setURLServiceLocation(String sURLServiceURL) {
        this.sURLServiceLocation = sURLServiceURL;
    }

    public String getURLServiceLocation() {
        return sURLServiceLocation;
    }

    public void setMqttBrokerUrl(String sMqttBrokerUrl) {
        this.sMqttBrokerUrl = sMqttBrokerUrl;
    }

    public String getMqttBrokerUrl() {
        return sMqttBrokerUrl;
    }

    public MQTTClient getMqttClient() {
        return mqttClient;
    }

    public void setMqttClient(MQTTClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public SensorService getSensorService() {
        return sensorService;
    }

    public void setSensorService(SensorService sensorService) {
        this.sensorService = sensorService;
    }
}
