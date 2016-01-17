package net.siot.android.gateway.messagetypes;

/**
 * Data object class SensorData. Representation of a JSON format.
 * Created by Sathesh on 13.11.15.
 */
public class SensorData {
    private long time;
    private String type;
    private String data;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
