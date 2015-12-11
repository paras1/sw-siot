package net.siot.android.gateway.util;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Sathesh on 13.11.15.
 */
public class GUIDUtil {

    public static String getUUID() {

        long millis = new Date().getTime();

        UUID GUID = new UUID(millis, millis/2);

        return GUID.toString();
    }
}
