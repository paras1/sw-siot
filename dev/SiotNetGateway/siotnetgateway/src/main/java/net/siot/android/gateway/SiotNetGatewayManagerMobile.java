package net.siot.android.gateway;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import net.siot.android.gateway.connection.MQTTClient;
import net.siot.android.gateway.connection.RestClient;
import net.siot.android.gateway.messagetypes.SiotUrl;
import net.siot.android.gateway.service.SensorServiceMobile;

/**
 * siot.net gateway library SiotNetGatewayManagerMobile. Use to connect the mobile device and a wearable to the siot.net and using the sensorservice correctly configured.
 *
 * Created by <a href="mailto:sathesh.paramasamy@students.bfh.ch">Sathesh Paramasamy</a> on 28.10.15.
 */
public class SiotNetGatewayManagerMobile {
    // TAG for the android logger
    private static final String TAG = "siotgw/SNGWManager";

    private Context ctx;
    private String sURLServiceLocationPrefix = "http://url.siot.net/?licence=";
    public String sURLServiceLocation;
    public String sMqttBrokerUrl;
    public String sLicense;

    public MQTTClient mqttClient;
    public SensorServiceMobile sensorService;

    /**
     * Constructor for getting an Instance of the GatewayLibrary for android mobile devices
     * @param ctx context of the user app which must be set
     */
    public SiotNetGatewayManagerMobile(Context ctx) {

        this.ctx = ctx;

    }

    /**
     * Connection method, using the siot.net license code passed from user app.
     * String for connection is requested dynamical from the siot.net url REST-service by the RestClient.class
     * @param sLicense String, have to be passed from the user app
     * @return connection state to mqtt broker
     */
    public boolean connectToSiotNet(String sLicense) {

        setLicense(sLicense);
        setURLServiceLocation(sURLServiceLocationPrefix + sLicense);
        if((mqttClient == null || !mqttClient.isConnected()) && (sLicense != null && !sLicense.equals(""))) {
            String restResponse = RestClient.getSiotNetBrokerUrl(sURLServiceLocation);
            Log.i(TAG, "JSON: " + restResponse);
            if (restResponse.contains("Incorrect licence")) {
                Toast.makeText(ctx.getApplicationContext(), "Incorrect license entered", Toast.LENGTH_SHORT).show();
                return false;
            }
            SiotUrl siotUrl = new Gson().fromJson(RestClient.getSiotNetBrokerUrl(sURLServiceLocation), SiotUrl.class);
            if(siotUrl.getMqtt() == null || siotUrl.getMqtt().getUrls() == null || siotUrl.getMqtt().getUrls().get(0).equals("")){
                Toast.makeText(ctx.getApplicationContext(), "Wrong license or service not reachable", Toast.LENGTH_SHORT).show();
                return false;
            }
            setMqttBrokerUrl("tcp://" + siotUrl.getMqtt().getUrls().get(0) + ":1883");
            Log.i(TAG, "JSON mqtt url:" + siotUrl.getMqtt().getUrls().get(0));
            //Toast.makeText(ctx.getApplicationContext(), "Connecting to siot.net...", Toast.LENGTH_SHORT).show();
            return connectBroker(sLicense, sMqttBrokerUrl);

        } else if (sLicense == null || sLicense.equals("")) {
            Log.i(TAG, "license number is empty");
            Toast.makeText(ctx.getApplicationContext(), "siot.net license number is empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (mqttClient != null || mqttClient.isConnected()) {
            Log.i(TAG, "Already connected to the MQTT broker");
            Toast.makeText(ctx.getApplicationContext(), "Already connected to the MQTT broker", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    /**
     * Disconnect methode, clean unbinding of connection to siot.net
     * @return connection state to mqtt broker
     */
    public boolean disconnectFromSiotNet() {
        mqttClient.disconnectBroker();
        return mqttClient.isConnected();
    }

    /**
     * Connects to the MQTT broker of your siot.net center
     * @param sCenterGUID is the same as the siot.net center license
     * @param sBrokerURL siot.net URL service will provide this url
     * @return connection state to mqtt broker
     */
    private boolean connectBroker(String sCenterGUID, String sBrokerURL) {
        if (mqttClient == null);
            mqttClient = MQTTClient.getInstance();

        mqttClient.connectBroker(sCenterGUID, sBrokerURL);
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

            sensorService = new SensorServiceMobile(ctx, sCenterGUID, mqttClient);

        } else {
            Log.i(TAG, "Connection to MQTT Broker " + sBrokerURL + " could not be established");
        }
        return mqttClient.isConnected();
    }

    /**
     * Sends a message to mqttclient publishing it to siot.net
     * @param topic topicname in which the message should be published
     * @param data payload
     */
    public void publishData(String topic, String data){
        if(mqttClient.isConnected()){
            mqttClient.publishData(topic, data);
        }
    }

    /**
     * Sends a message to mqttclient subscribing to a topic on siot.net
     * @param topic topicname to subscribe
     */
    public void subscribeData(String topic) {
        if(mqttClient.isConnected()){
            mqttClient.subscribeData(topic);
        }
    }

    //Getter and Setters

    /**
     * Setter of URL for getting the siot.net center and broker URL
     * @param sURLServiceURL URL of URL service
     */
    public void setURLServiceLocation(String sURLServiceURL) {
        this.sURLServiceLocation = sURLServiceURL;
    }

    /**
     * Getter of URL for getting the siot.net center and broker URL
     * @return URL of URL service
     */
    public String getURLServiceLocation() {
        return sURLServiceLocation;
    }

    /**
     * Getter of the siot.net center GUID same as license
     * @return license / center GUID
     */
    public String getLicense() {
        return this.sLicense;
    }

    /**
     * Setter fo the siot.net center GUID same as license
     * @param sLicense license / center GUID
     */
    public void setLicense(String sLicense) {
        this.sLicense = sLicense;
    }
    /**
     * Setter of the siot.net MQTT broker URL
     * @param sMqttBrokerUrl MQTT broker URL
     */
    public void setMqttBrokerUrl(String sMqttBrokerUrl) {
        this.sMqttBrokerUrl = sMqttBrokerUrl;
    }

    /**
     * Getter of the siot.net MQTT broker URL
     * @return MQTT broker URL
     */
    public String getMqttBrokerUrl() {
        return sMqttBrokerUrl;
    }

    /**
     * Setter of the MQTT client and connection to the siot.net broker
     * @param mqttClient MQTTClient(net.siot.android.gateway.connection) instance
     */
    public void setMqttClient(MQTTClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    /**
     * Getter of the MQTT client and connection to the siot.net broker
     * @return MQTTClient(net.siot.android.gateway.connection) instance
     */
    public MQTTClient getMqttClient() {
        return mqttClient;
    }

    /**
     * Setter of the Android Gateway sensorService
     * @param sensorService SensorServiceMobile instance
     */
    public void setSensorService(SensorServiceMobile sensorService) {
        this.sensorService = sensorService;
    }

    /**
     * Getter of the Android Gateway sensorService
     * @return SensorServiceMobile instance
     */
    public SensorServiceMobile getSensorService() {
        return sensorService;
    }

}
