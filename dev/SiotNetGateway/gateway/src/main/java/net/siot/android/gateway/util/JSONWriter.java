package net.siot.android.gateway.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sathesh on 04.10.15.
 */
public class JSONWriter {
    private static final String TAG = "siotag/JSONWriter";

    public static final String ACCURACY = "accuracy";
    public static final String TIMESTAMP = "timestamp";
    public static final String VALUES = "values";

    public static JSONObject getJSONWithSensorValues(int sensorType, float[] values){
        JSONObject sensorValuesJSON = new JSONObject();

        try {
            if (sensorType == SensorTypeKeys.SENS_ACCELEROMETER ||
                    sensorType == SensorTypeKeys.SENS_GRAVITY ||
                    sensorType == SensorTypeKeys.SENS_LINEAR_ACCELERATION ||
                    sensorType == SensorTypeKeys.SENS_MAGNETIC_FIELD) {
                sensorValuesJSON.put("x", values[0]);
                sensorValuesJSON.put("y", values[1]);
                sensorValuesJSON.put("z", values[2]);
            } else if (sensorType == SensorTypeKeys.SENS_MAGNETIC_FIELD_UNCALIBRATED) {
                sensorValuesJSON.put("x_uncalib", values[0]);
                sensorValuesJSON.put("y_uncalib", values[1]);
                sensorValuesJSON.put("z_uncalib", values[2]);
                sensorValuesJSON.put("x_bias", values[3]);
                sensorValuesJSON.put("y_bias", values[4]);
                sensorValuesJSON.put("z_bias", values[5]);
            } else if (sensorType == SensorTypeKeys.SENS_AMBIENT_TEMPERATURE) {
                sensorValuesJSON.put("temp", values[0]);
            } else if (sensorType == SensorTypeKeys.SENS_GAME_ROTATION_VECTOR || sensorType == SensorTypeKeys.SENS_ROTATION_VECTOR) {
                sensorValuesJSON.put("x", values[0]);
                sensorValuesJSON.put("y", values[1]);
                sensorValuesJSON.put("z", values[2]);
                sensorValuesJSON.put("half-angle", values[3]);
                sensorValuesJSON.put("estimated_heading_acc", values[4]);
            } else if (sensorType == SensorTypeKeys.SENS_GEOMAGNETIC) {
                //TODO
            } else if (sensorType == SensorTypeKeys.SENS_LIGHT) {
                sensorValuesJSON.put("lux", values[0]);
            } else if (sensorType == SensorTypeKeys.SENS_GYROSCOPE) {
                sensorValuesJSON.put("speed_x", values[0]);
                sensorValuesJSON.put("speed_y", values[1]);
                sensorValuesJSON.put("speed_z", values[2]);
            } else if (sensorType == SensorTypeKeys.SENS_GYROSCOPE_UNCALIBRATED) {
                sensorValuesJSON.put("speed_x", values[0]);
                sensorValuesJSON.put("speed_y", values[1]);
                sensorValuesJSON.put("speed_z", values[2]);
                sensorValuesJSON.put("drift_x", values[3]);
                sensorValuesJSON.put("drift_y", values[4]);
                sensorValuesJSON.put("drift_z", values[5]);
            } else if (sensorType == SensorTypeKeys.SENS_HUMIDITY) {
                sensorValuesJSON.put("humidity", values[0]);
            } else if (sensorType == SensorTypeKeys.SENS_HEARTRATE) {
                //sensorValuesJSON.put("")
                Log.i(TAG, "Values size HEARTRATE: " + values.length);
                for (int i = 0; i < values.length; i++) {
                    Log.i(TAG, "Values HEARTRATE"+i+": "+ values[i]);
                }
            } else if (sensorType == SensorTypeKeys.SENS_PRESSURE) {
                sensorValuesJSON.put("pressure", values[0]);
            } else if (sensorType == SensorTypeKeys.SENS_PROXIMITY) {
                sensorValuesJSON.put("proximity", values[0]);
            } else if (sensorType == SensorTypeKeys.SENS_STEP_COUNTER) {
                sensorValuesJSON.put("steps-since-reboot", values[0]);
            } else if (sensorType == SensorTypeKeys.SENS_STEP_DETECTOR) {
                sensorValuesJSON.put("step", 1);
            } else if (sensorType == SensorTypeKeys.SENS_SIGNIFICANT_MOTION) {
                //sensorValuesJSON.put("")
                Log.i(TAG, "Values size SIGNIFICANT_MOTION: "+values.length);
                for (int i = 0; i < values.length; i++) {
                    Log.i(TAG, "Values SIGNIFICANT_MOTION"+i+": "+ values[i]);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return sensorValuesJSON;
    }

    public static JSONObject getSensorJSON(String sensorName, int sensorType, int accuracy, long timestamp, float[] values) {
        JSONObject outJSON = new JSONObject();
        try {
            outJSON.put(ACCURACY, accuracy);
            outJSON.put(TIMESTAMP, timestamp);
            outJSON.put(VALUES, getJSONWithSensorValues(sensorType, values));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }


        return outJSON;
    }
}
