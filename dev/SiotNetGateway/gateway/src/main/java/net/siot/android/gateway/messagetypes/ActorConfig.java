package net.siot.android.gateway.messagetypes;

/**
 * Created by Sathesh on 13.11.15.
 */
public class ActorConfig {
    private String sensor;
    private String lastwill;


    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getLastwill() {
        return lastwill;
    }

    public void setLastwill(String lastwill) {
        this.lastwill = lastwill;
    }

}
