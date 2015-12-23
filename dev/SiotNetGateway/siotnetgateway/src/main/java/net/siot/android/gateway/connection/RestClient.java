package net.siot.android.gateway.connection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sathesh on 30.10.15.
 * REST API client. Used to get the URLs from the siot.net URL service.
 */
public class RestClient {

    private static final String TAG = "SensorPub/RestClient";

    /**
     * Getting URL without user specified timeout (default 60s)
     * @param url URL of the URL service
     * @return JSON formatted String containing siot.net URLs (IoT center, MQTT broker)
     */
    public static String getSiotNetBrokerUrl(String url) {
        return getSiotNetBrokerUrl(url, 60000);
    }

    /**
     * Getting URL with self defined timeout
     * @param url URL of the URL service
     * @param timeout timeout in millis
     * @return JSON formatted String containing siot.net URLs (IoT center, MQTT broker)
     */
    public static String getSiotNetBrokerUrl(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
        return null;
    }

}
