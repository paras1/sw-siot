package net.siot.android.gateway.util;

import android.util.Log;

/**
 * Created by Sathesh on 13.11.15.
 */
public class TopicUtil {
    private static final String TAG = "SensorPub/TopicUtil";

    public static final int TOPIC_TYPE_MNF = 0;
    public static final int TOPIC_TYPE_CNF = 1;
    public static final int TOPIC_TYPE_DAT = 2;
    public static final int TOPIC_TYPE_TRG = 3;

    public static String PREFIX_MNF = "siot/MNF";
    public static String PREFIX_CNF = "siot/CNF";
    public static String PREFIX_DAT = "siot/DAT";

    /**
     * Getter for MQTT topic without siot.net IoT center GUID
     * @param topicType topic type: Manifest, Config, Data, Trigger
     * @param GUID sensor/actor GUID
     * @return absolut topic path
     */
    public static String getTopic(int topicType, String GUID) {
        return getTopic(topicType, null, GUID);
    }

    /**
     * Getter for MQTT topic according the requested topicType
     * @param topicType topic type: Manifest, Config, Data, Trigger
     * @param GUID_cen siot.net IoT center GUID (same as license)
     * @param GUID sensor/actor GUID
     * @return absolut topic path
     */
    public static String getTopic(int topicType, String GUID_cen, String GUID) {
        String topic = "";
        switch (topicType) {
            case 0:
                topic = PREFIX_MNF+"/"+GUID_cen+"/"+GUID+"/";
                break;
            case 1:
                topic = PREFIX_CNF+"/"+GUID_cen+"/"+GUID+"/";
                break;
            case 2:
                topic = PREFIX_DAT+"/"+GUID_cen+"/"+GUID+"/";
                break;
            case 3:
                topic = PREFIX_DAT+"/"+GUID+"/";
                break;
            default:
                topic = "";
                Log.e(TAG, "Unknown topicType");
        }
        return topic;
    }
}
