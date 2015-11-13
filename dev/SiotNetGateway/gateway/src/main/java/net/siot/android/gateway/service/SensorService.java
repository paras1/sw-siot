package net.siot.android.gateway.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseLongArray;

import net.siot.android.gateway.connection.MQTTClient;
import net.siot.android.gateway.util.JSONWriter;
import net.siot.android.gateway.util.SensorTypeKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;




/**
 * Created by Sathesh Paramasamy on 02.10.15.
 */
public class SensorService extends Service implements SensorEventListener {
    private static final String TAG = "siotag/SensorService";

    SensorManager mSensorManager;

    Sensor accelerometerSensor;
    Sensor ambientTemperatureSensor;
    Sensor gameRotationVectorSensor;
    Sensor geomagneticSensor;
    Sensor gravitySensor;
    Sensor gyroscopeSensor;
    Sensor gyroscopeUncalibratedSensor;
    Sensor heartrateSensor;
    Sensor heartrateSamsungSensor;
    Sensor lightSensor;
    Sensor linearAccelerationSensor;
    Sensor magneticFieldSensor;
    Sensor magneticFieldUncalibratedSensor;
    Sensor pressureSensor;
    Sensor proximitySensor;
    Sensor humiditySensor;
    Sensor rotationVectorSensor;
    Sensor significantMotionSensor;
    Sensor stepCounterSensor;
    Sensor stepDetectorSensor;

    MQTTClient mqttClient;

    Context ctx;

    private SparseLongArray lastSensorData;
    private ExecutorService executorService;
    private int filterId;

    public SensorService(Context ctx, MQTTClient mqttClient) {

        this.ctx = ctx;
        this.mqttClient = mqttClient;

        mSensorManager = ((SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE));

        executorService = Executors.newCachedThreadPool();
        lastSensorData = new SparseLongArray();

        accelerometerSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_ACCELEROMETER);
        ambientTemperatureSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_AMBIENT_TEMPERATURE);
        gameRotationVectorSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_GAME_ROTATION_VECTOR);
        geomagneticSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_GEOMAGNETIC);
        gravitySensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_GRAVITY);
        gyroscopeSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_GYROSCOPE);
        gyroscopeUncalibratedSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_GYROSCOPE_UNCALIBRATED);
        heartrateSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_HEARTRATE);
        heartrateSamsungSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_HEARTRATE_SAMSUNG);
        lightSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_LIGHT);
        linearAccelerationSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_LINEAR_ACCELERATION);
        magneticFieldSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_MAGNETIC_FIELD);
        magneticFieldUncalibratedSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_MAGNETIC_FIELD_UNCALIBRATED);
        pressureSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_PRESSURE);
        proximitySensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_PROXIMITY);
        humiditySensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_HUMIDITY);
        rotationVectorSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_ROTATION_VECTOR);
        significantMotionSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_SIGNIFICANT_MOTION);
        stepCounterSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_STEP_COUNTER);
        stepDetectorSensor = mSensorManager.getDefaultSensor(SensorTypeKeys.SENS_STEP_DETECTOR);

    }

    public void startAllListeners() {

        //Register sensor listeners
        if (mSensorManager != null) {
            if (accelerometerSensor != null) {
                mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Accelerometer found");
            }

            if (ambientTemperatureSensor != null) {
                mSensorManager.registerListener(this, ambientTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "Ambient Temperature Sensor not found");
            }

            if (gameRotationVectorSensor != null) {
                mSensorManager.registerListener(this, gameRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "Gaming Rotation Vector Sensor not found");
            }

            if (geomagneticSensor != null) {
                mSensorManager.registerListener(this, geomagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Geomagnetic Sensor found");
            }

            if (gravitySensor != null) {
                mSensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Gravity Sensor");
            }

            if (gyroscopeSensor != null) {
                mSensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Gyroscope Sensor found");
            }

            if (gyroscopeUncalibratedSensor != null) {
                mSensorManager.registerListener(this, gyroscopeUncalibratedSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.w(TAG, "No Uncalibrated Gyroscope Sensor found");
            }

            if (heartrateSensor != null) {
                final int measurementDuration   = 10;   // Seconds
                final int measurementBreak      = 5;    // Seconds

                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "register Heartrate Sensor");
                                mSensorManager.registerListener(SensorService.this, heartrateSensor, SensorManager.SENSOR_DELAY_NORMAL);

                                try {
                                    Thread.sleep(measurementDuration * 1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                                }

                                Log.d(TAG, "unregister Heartrate Sensor");
                                mSensorManager.unregisterListener(SensorService.this, heartrateSensor);
                            }
                        }, 3, measurementDuration + measurementBreak, TimeUnit.SECONDS);
            } else {
                Log.d(TAG, "No Heartrate Sensor found");
            }

            if (heartrateSamsungSensor != null) {
                mSensorManager.registerListener(this, heartrateSamsungSensor, SensorManager.SENSOR_DELAY_FASTEST);
            } else {
                Log.d(TAG, "No Samsungs Heartrate Sensor found");
            }

            if (lightSensor != null) {
                mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Light Sensor found");
            }

            if (linearAccelerationSensor != null) {
                mSensorManager.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Linear Acceleration Sensor found");
            }

            if (magneticFieldSensor != null) {
                mSensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Magnetic Field Sensor found");
            }

            if (magneticFieldUncalibratedSensor != null) {
                mSensorManager.registerListener(this, magneticFieldUncalibratedSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No uncalibrated Magnetic Field Sensor found");
            }

            if (pressureSensor != null) {
                mSensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Pressure Sensor found");
            }

            if (proximitySensor != null) {
                mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Proximity Sensor found");
            }

            if (humiditySensor != null) {
                mSensorManager.registerListener(this, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Humidity Sensor found");
            }

            if (rotationVectorSensor != null) {
                mSensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Rotation Vector Sensor found");
            }

            if (significantMotionSensor != null) {
                mSensorManager.registerListener(this, significantMotionSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Significant Motion Sensor found");
            }

            if (stepCounterSensor != null) {
                mSensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Step Counter Sensor found");
            }

            if (stepDetectorSensor != null) {
                mSensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Log.d(TAG, "No Step Detector Sensor found");
            }
        }

    }

    public void stopAllListeners() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        sendSensorData(sensorEvent.sensor.getName(), sensorEvent.sensor.getType(), sensorEvent.accuracy, sensorEvent.timestamp, sensorEvent.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();

        this.stopAllListeners();
    }

    public void startListener(Sensor sensor) {
        if (sensor != null) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.w(TAG, "No "+sensor.getName()+" found");
        }
    }

    public void stopListener(Sensor sensor) {
        if (sensor != null) {
            mSensorManager.unregisterListener(this, sensor);
        } else {
            Log.w(TAG, "No "+sensor.getName()+" found");
        }
    }

    public void sendSensorData(final String sensorName, final int sensorType, final int accuracy, final long timestamp, final float[] values) {
        long t = System.currentTimeMillis();

        long lastTimestamp = lastSensorData.get(sensorType);
        long timeAgo = t - lastTimestamp;

        if (lastTimestamp != 0) {
            if (filterId == sensorType && timeAgo < 100) {
                return;
            }

            if (filterId != sensorType && timeAgo < 3000) {
                return;
            }
        }

        lastSensorData.put(sensorType, t);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendSensorDataInBackground(sensorName, sensorType, accuracy, timestamp, values);
            }
        });
    }

    public void setSensorFilter(int filterId) {
        Log.d(TAG, "Now filtering by sensor: " + filterId);

        this.filterId = filterId;
    }

    private void sendSensorDataInBackground(String guid, int sensorType, int accuracy, long timestamp, float[] values) {
        if (sensorType == filterId) {
            Log.i(TAG, "Sensor " + sensorType + " " + guid + " = " + Arrays.toString(values));
        } else {
            Log.d(TAG, "Sensor " + sensorType + " " + guid + " = " + Arrays.toString(values));
        }
        JSONObject outJSON = new JSONObject();
        try {
            outJSON.put(guid, JSONWriter.getSensorJSON(guid, sensorType, accuracy, timestamp, values));
            mqttClient.publishData(outJSON, guid);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
