package net.siot.android.gateway.messagetypes;

/**
 * Data object class SensorActorManifest class. Representation of a JSON format.
 * Created by Sathesh on 13.11.15.
 */
public class SensorActorManifest {
    private String name;
    private String type;
    private String description;
    private String valueType;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
}
