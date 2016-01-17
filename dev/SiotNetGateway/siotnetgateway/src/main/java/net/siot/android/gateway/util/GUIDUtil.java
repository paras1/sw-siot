package net.siot.android.gateway.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUID helper class, generates GUIDs
 * Created by Sathesh on 13.11.15.
 */
public class GUIDUtil {

    /**
     * Generate a GUID using current time millis as most significant byte and current time millis divided by a random number between 1 and 99
     * @return GUID string
     */
    public static String getGUID() {

        long millis = new Date().getTime();

        UUID GUID = new UUID(millis, millis/(1 + (int)(Math.random() * ((99 - 1) + 1))));

        return GUID.toString();
    }

    public static HashMap getSensorGUIDs() {

        return null;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
