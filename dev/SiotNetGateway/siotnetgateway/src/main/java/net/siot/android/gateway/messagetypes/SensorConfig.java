package net.siot.android.gateway.messagetypes;

/**
 * Created by Sathesh on 13.11.15.
 * Data object class SensorConfig. Representation of a JSON format.
 */
public class SensorConfig {
    private int sendtime;
    private String onrequest;
    private String maxsize;

    public int getSendtime() {
        return sendtime;
    }

    public void setSendtime(int sendtime) {
        this.sendtime = sendtime;
    }

    public String getOnrequest() {
        return onrequest;
    }

    public void setOnrequest(String onrequest) {
        this.onrequest = onrequest;
    }

    public String getMaxsize() {
        return maxsize;
    }

    public void setMaxsize(String maxsize) {
        this.maxsize = maxsize;
    }
}
