package net.siot.android.gateway.util;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Sathesh on 13.11.15.
 * GUID helper class, generates GUIDs
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
}
