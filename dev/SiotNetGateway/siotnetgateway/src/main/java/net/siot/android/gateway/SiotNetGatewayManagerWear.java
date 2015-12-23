package net.siot.android.gateway;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import net.siot.android.gateway.service.SensorServiceWear;

/**
 * Created by <a href="mailto:sathesh.paramasamy@students.bfh.ch">Sathesh Paramasamy</a> on 28.11.15.
 *
 * siot.net gateway library SiotNetGatewayManagerWear. Use to connect the android wear device through the mobile to the siot.net and using the sensorservice correctly configured.
 */
public class SiotNetGatewayManagerWear {
    private static final String TAG = "siotgw/SNGWManager";

    private Context ctx;

    SensorServiceWear sensorService;

    GoogleApiClient googleApiClient;
    String sGAnodeId;

    /**
     * Constructor for getting an Instance of the GatewayLibrary for android wear devices
     * @param ctx context of the user app which must be set
     * @param googleApiClient GoogleApiClient for the communication between mobile and wearable
     * @param sGAnodeId known GoogleApiClient nodeId of the wearable device
     */
    public SiotNetGatewayManagerWear(Context ctx, GoogleApiClient googleApiClient, String sGAnodeId) {

        this.ctx = ctx;
        this.googleApiClient = googleApiClient;
        this.sGAnodeId = sGAnodeId;

    }

    /**
     * Connection to the siot.net through the mobile
     * @param sCenterGUID license code of siot.net
     * @return connection state to mobile
     */
    public boolean connectToMobile(String sCenterGUID) {
        if (googleApiClient == null)
            return false;

        //sensorService = new SensorServiceWear(ctx, sCenterGUID, googleApiClient, sGAnodeId);
        if (googleApiClient.isConnected()) {
            sensorService = new SensorServiceWear(ctx, sCenterGUID, googleApiClient, sGAnodeId);
        }
        else {
            Log.i(TAG, "Connection to companion mobile device could not be established");
        }
        return googleApiClient.isConnected();
    }

    //Getter and Setters


    /**
     * Getter of the Android Gateway sensorService
     * @return SensorServiceWear instance
     */
    public SensorServiceWear getSensorService() {
        return sensorService;
    }

    /**
     * Setter of the Android Gateway sensorService
     * @param sensorService SensorServiceWear instance
     */
    public void setSensorService(SensorServiceWear sensorService) {
        this.sensorService = sensorService;
    }

    /**
     * Getter of the GoogleApiClient which manages the connection to the mobile device
     * @return GoogleApiClient instance
     */
    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    /**
     * Setter to the GoogleApiClient which manages the connection to the mobile device
     * @param googleApiClient GoogleApiClient instance
     */
    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    /**
     * Getter of the known wearable GoogleApiClient NodeId
     * @return nodeId of wearable
     */
    public String getsGAnodeId() {
        return sGAnodeId;
    }

    /**
     * Setter of the known wearble GoogleApiClient NodeId
     * @param sGAnodeId holds the nodeId name of the wearable
     */
    public void setsGAnodeId(String sGAnodeId) {
        this.sGAnodeId = sGAnodeId;
    }
}
