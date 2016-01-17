package net.siot.android.gateway.messagetypes;

/**
 * Data object class WearableData. Representation of a JSON format.
 * Created by Sathesh on 21.12.15.
 */
public class WearableData {

    private String topic;
    private String data;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
