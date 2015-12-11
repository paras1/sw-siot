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

    Context ctx;
    String sLicense;
    String sURLServiceURL;


    MQTTClient mqttClient;
    SensorService sensorService;


    public SiotNetGatewayManager(Context ctx, String sLicense) {

        this.ctx = ctx;
        this.sLicense = sLicense;

        sURLServiceURL = "http://url.siot.net/?licence=";
    }

    public boolean connectToSiotNet() {

        if(!mqttClient.isConnected() && sLicense != null && !sLicense.equals("")) {
            Log.i(TAG, "JSON: " + RestClient.getSiotNetBrokerUrl(sURLServiceURL + sLicense));
            SiotUrl siotUrl = new Gson().fromJson(RestClient.getSiotNetBrokerUrl(sURLServiceURL + sLicense), SiotUrl.class);
            Log.i(TAG, "JSON mqtt url:" + siotUrl.getMqtt().getUrls().get(0));

            return connectBroker(sLicense, "tcp://"+siotUrl.getMqtt().getUrls().get(0)+":1883");

        } else if (mqttClient.isConnected()) {
            Log.i(TAG, "Already connected to the MQTT broker");
            return true;

        } else {
            Log.i(TAG, "license number is empty");
            return false;
        }
    }

    private boolean connectBroker(String sCenterGUID, String sBrokerURL) {
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
            sensorService = new SensorService(sCenterGUID, mqttClient);
        } else {
            Log.i(TAG, "Connection to MQTT Broker "+sBrokerURL+" could not be established");
        }
        return mqttClient.isConnected();
    }


}
