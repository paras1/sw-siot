package net.siot.android.gateway.util;

import android.util.Log;

/**
 * Created by Sathesh on 13.11.15.
 */
public class TopicUtil {
    private static final String TAG = "SensorPub/TopicUtil";

    public static final int TOPIC_TYPE_MNF = 0;
    public static final int TOPIC_TYPE_CON = 1;
    public static final int TOPIC_TYPE_DAT = 2;
    public static final int TOPIC_TYPE_TRG = 3;

    private static String PREFIX_MNF = "SPA/TEST/AndroidSW/MNF";
    private static String PREFIX_CON = "SPA/TEST/AndroidSW/CON";
    private static String PREFIX_DAT = "SPA/TEST/AndroidSW/DAT";

    public static String getTopic(int topicType, String GUID) {
        return getTopic(topicType, null, GUID);
    }

    public static String getTopic(int topicType, String GUID_cen, String GUID) {
        String topic = "";
        switch (topicType) {
            case 0:
                topic = PREFIX_MNF+"/"+GUID_cen+"/"+GUID+"/";
                break;
            case 1:
                topic = PREFIX_CON+"/"+GUID_cen+"/"+GUID+"/";
                break;
            case 2:
                topic = PREFIX_DAT+"/"+GUID_cen+"/"+GUID+"/";
                break;
            case 3:
                topic = PREFIX_DAT+"/"+GUID+"/";
                break;
            default:
                topic = "";
                Log.e(TAG, "Unknown TOPIC_TYPE");
        }
        return topic;
    }
}
