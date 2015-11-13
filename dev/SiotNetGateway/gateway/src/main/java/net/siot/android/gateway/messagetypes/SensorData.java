package net.siot.android.gateway.messagetypes;

import org.json.JSONObject;

/**
 * Created by Sathesh on 13.11.15.
 */
public class SensorData {
    private String name;
    private String type;
    private JSONObject data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
