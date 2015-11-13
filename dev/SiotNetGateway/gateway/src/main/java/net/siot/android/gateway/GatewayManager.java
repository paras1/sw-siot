package net.siot.android.gateway;

/**
 * Created by Sathesh on 28.10.15.
 */
public class GatewayManager {
    private static final String TAG = "siotag/GatewayManager";

    GatewayManager singleton;

    public GatewayManager getInstance() {

        if (singleton == null) {
            singleton = new GatewayManager();
        }
        return singleton;
    }
}
