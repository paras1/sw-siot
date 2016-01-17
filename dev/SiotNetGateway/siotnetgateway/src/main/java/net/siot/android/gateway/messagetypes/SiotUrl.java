package net.siot.android.gateway.messagetypes;

/**
 * Data object class SiotUrl. Representation of a JSON format.
 * Created by Sathesh on 30.10.15.
 */
public class SiotUrl {

    private Urls iot;
    private Urls mqtt;
    private int error_code;
    private int versionAPI;
    private String authType;
    private String uuid;

    public Urls getIot() {
        return iot;
    }

    public void setIot(Urls iot) {
        this.iot = iot;
    }

    public Urls getMqtt() {
        return mqtt;
    }

    public void setMqtt(Urls mqtt) {
        this.mqtt = mqtt;
    }

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public int getVersionAPI() {
        return versionAPI;
    }

    public void setVersionAPI(int versionAPI) {
        this.versionAPI = versionAPI;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
