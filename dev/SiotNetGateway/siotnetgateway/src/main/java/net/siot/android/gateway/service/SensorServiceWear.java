package net.siot.android.gateway.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseLongArray;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import net.siot.android.gateway.messagetypes.SensorActorManifest;
import net.siot.android.gateway.messagetypes.SensorData;
import net.siot.android.gateway.messagetypes.WearableData;
import net.siot.android.gateway.util.GUIDUtil;
import net.siot.android.gateway.util.SensorTypeKeys;
import net.siot.android.gateway.util.TopicUtil;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by Sathesh Paramasamy on 28.11.15.
 * Pre-configured SensorService for android wear devices which will be connected to siot.net.
 */
public class SensorServiceWear extends Service implements SensorEventListener {
    private final static String TAG = "SensorPub/SenServWear";
    private final static String MSG_PATH_SEND_DATA = "sendMessage";


    SensorManager mSensorManager;

    public Sensor accelerometerSensor;
    public Sensor ambientTemperatureSensor;
    public Sensor gameRotationVectorSensor;
    public Sensor geomagneticSensor;
    public Sensor gravitySensor;
    public Sensor gyroscopeSensor;
    public Sensor gyroscopeUncalibratedSensor;
    public Sensor heartrateSensor;
    public Sensor heartrateSamsungSensor;
    public Sensor lightSensor;
    public Sensor linearAccelerationSensor;
    public Sensor magneticFieldSensor;
    public Sensor magneticFieldUncalibratedSensor;
    public Sensor pressureSensor;
    public Sensor proximitySensor;
    public Sensor humiditySensor;
    public Sensor rotationVectorSensor;
    public Sensor significantMotionSensor;
    public Sensor stepCounterSensor;
    public Sensor stepDetectorSensor;

    HashMap sensorMap = new HashMap<Integer, HashMap<String, String>>();

    Context ctx;
    GoogleApiClient googleApiClient;
    String sGANodeId;

    private String sCenterGUID;

    private SparseLongArray lastSensorData;
    private ExecutorService executorService;

    /**
     * Constructor for SensorServiceMobile.
     * @param ctx needed because android sensor must have an Context
     * @param sCenterGUID for publishes and subscribes to and from the correct siot.net center
     * @param googleApiClient manages the connection of the messageApi to the mobile device
     * @param sGANodeId on mobile device known nodeId for message exchange over googleApiClient
     */
    public SensorServiceWear(Context ctx, String sCenterGUID, GoogleApiClient googleApiClient, String sGANodeId) {

        this.sCenterGUID = sCenterGUID;
        this.ctx = ctx;
        this.googleApiClient = googleApiClient;
        this.sGANodeId = sGANodeId;

        executorService = Executors.newCachedThreadPool();
        lastSensorData = new SparseLongArray();

        mSensorManager = ((SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE));

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

    /**
     * Starts all listeners of available sensors on the device.
     * The correct connection to the siot.net is TODO.
     */
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
                                mSensorManager.registerListener(SensorServiceWear.this, heartrateSensor, SensorManager.SENSOR_DELAY_NORMAL);

                                try {
                                    Thread.sleep(measurementDuration * 1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted while waitting to unregister Heartrate Sensor");
                                }

                                Log.d(TAG, "unregister Heartrate Sensor");
                                mSensorManager.unregisterListener(SensorServiceWear.this, heartrateSensor);
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

    /**
     * Stops all registered listeners on the SensorService.
     */
    public void stopAllListeners() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    /**
     * This override method from SensorEventListener reports sensor datas when they have changed.
     * @param sensorEvent notifying SensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.i(TAG, "SENSOR Changed: " + sensorEvent.sensor.getName());

        Sensor sensor = sensorEvent.sensor;
        if (sensor != null) {
            int sensorType = sensor.getType();
            SensorData senDat = new SensorData();
            float[] values = sensorEvent.values;
            if (sensorType == SensorTypeKeys.SENS_ACCELEROMETER ||
                    sensorType == SensorTypeKeys.SENS_GRAVITY ||
                    sensorType == SensorTypeKeys.SENS_LINEAR_ACCELERATION ||
                    sensorType == SensorTypeKeys.SENS_MAGNETIC_FIELD) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData("" + values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "x"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[1]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "y"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[2]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "z"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_MAGNETIC_FIELD_UNCALIBRATED) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "x"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[1]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "y"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[2]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "z"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[3]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "x_bias"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[4]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "y_bias"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[5]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "z_bias"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_AMBIENT_TEMPERATURE) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "temp"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_GAME_ROTATION_VECTOR ||
                    sensorType == SensorTypeKeys.SENS_ROTATION_VECTOR) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "x"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[1]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "y"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[2]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "z"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[3]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "half_angle"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[4]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "est_head_acc"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_GEOMAGNETIC) {
                //TODO
            } else if (sensorType == SensorTypeKeys.SENS_LIGHT) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "lux"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_GYROSCOPE) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "x_speed"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[1]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "y_speed"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[2]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "z_speed"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_GYROSCOPE_UNCALIBRATED) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "x_speed"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[1]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "y_speed"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[2]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "z_speed"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[3]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "x_drift"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[4]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "y_drift"), new Gson().toJson(senDat));
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[5]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "z_drift"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_HUMIDITY) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "humidity"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_HEARTRATE) {
                //sensorValuesJSON.put("")
                Log.i(TAG, "Values size HEARTRATE: " + values.length);
                for (int i = 0; i < values.length; i++) {
                    Log.i(TAG, "Values HEARTRATE"+i+": "+ ""+values[i]);
                }
            } else if (sensorType == SensorTypeKeys.SENS_PRESSURE) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "pressure"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_PROXIMITY) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "proximity"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_STEP_COUNTER) {
                senDat.setType("float");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData(""+values[0]);
                sendSensorData(sensorType, getSensorGUID(sensorType, "steps"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_STEP_DETECTOR) {
                senDat.setType("boolean");
                senDat.setTime(sensorEvent.timestamp);
                senDat.setData("true");
                sendSensorData(sensorType, getSensorGUID(sensorType, "stepped"), new Gson().toJson(senDat));
            } else if (sensorType == SensorTypeKeys.SENS_SIGNIFICANT_MOTION) {
                //sensorValuesJSON.put("")
                Log.i(TAG, "Values size SIGNIFICANT_MOTION: " + values.length);
                for (int i = 0; i < values.length; i++) {
                    Log.i(TAG, "Values SIGNIFICANT_MOTION"+i+": "+ ""+values[i]);
                }
            }
        }
    }

    /**
     * Override method nothing to do at the moment.
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * Override method nothing to do at the moment.
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Stops all sensor listeners when de application is destroyed.
     */
    public void onDestroy() {
        super.onDestroy();
        this.stopAllListeners();
    }

    /**
     * RegisterSensor notifies siot.net that a new sensor has connected.
     * A manifest message is generated and sends to the mobile device which publishes to the MQTT broker.
     * @param sensor sensor to register on siot.net
     */
    public void registerSensor(Sensor sensor) {
        if (sensor != null) {
            int sensorType = sensor.getType();
            SensorActorManifest senMnf = new SensorActorManifest();
            String sensorGUID;
            if (sensorType == SensorTypeKeys.SENS_ACCELEROMETER) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x");
                senMnf.setName(sensor.getName() + "_X");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Accelerometer X-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, sensorGUID), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y");
                senMnf.setName(sensor.getName() + "_Y");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Accelerometer Y-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, sensorGUID), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z");
                senMnf.setName(sensor.getName()+"_Z");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Accelerometer Z-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, sensorGUID), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_GRAVITY) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x");
                senMnf.setName(sensor.getName()+"_X");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gravitation X-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y");
                senMnf.setName(sensor.getName()+"_Y");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gravitation Y-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z");
                senMnf.setName(sensor.getName()+"_Z");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gravitation Z-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_LINEAR_ACCELERATION) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x");
                senMnf.setName(sensor.getName()+"_X");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Linear Accelerometer X-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y");
                senMnf.setName(sensor.getName()+"_Y");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Linear Accelerometer Y-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z");
                senMnf.setName(sensor.getName()+"_Z");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Linear Accelerometer Z-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_MAGNETIC_FIELD) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x");
                senMnf.setName(sensor.getName()+"_X");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field X-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y");
                senMnf.setName(sensor.getName()+"_Y");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Y-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z");
                senMnf.setName(sensor.getName()+"_Z");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Z-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_MAGNETIC_FIELD_UNCALIBRATED) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x");
                senMnf.setName(sensor.getName() + "_X");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Uncalibrated X-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y");
                senMnf.setName(sensor.getName() + "_Y");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Uncalibrated Y-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z");
                senMnf.setName(sensor.getName() + "_Z");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Uncalibrated Z-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x_bias");
                senMnf.setName(sensor.getName() + "_X_bias");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Uncalibrated X-axis bias of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y_bias");
                senMnf.setName(sensor.getName() + "_Y_bias");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Uncalibrated Y-axis bias of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z_bias");
                senMnf.setName(sensor.getName() + "_Z_bias");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Magnetic Field Uncalibrated Z-axis bias of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_AMBIENT_TEMPERATURE) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "temp");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Ambient Temperature of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_GAME_ROTATION_VECTOR) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x");
                senMnf.setName(sensor.getName() + "_X");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Game Rotation Vector X-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y");
                senMnf.setName(sensor.getName() + "_Y");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Game Rotation Vector Y-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z");
                senMnf.setName(sensor.getName() + "_Z");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Game Rotation Vector Z-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "half_angle");
                senMnf.setName(sensor.getName() + "_Half_Angle");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Game Rotation Vector half angle of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "est_head_acc");
                senMnf.setName(sensor.getName() + "_Est_head_acc");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Game Rotation Vector estimated heading accuracy of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_ROTATION_VECTOR) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x");
                senMnf.setName(sensor.getName()+"_X");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Rotation Vector X-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y");
                senMnf.setName(sensor.getName()+"_Y");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Rotation Vector Y-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z");
                senMnf.setName(sensor.getName()+"_Z");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Rotation Vector Z-axis of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "half_angle");
                senMnf.setName(sensor.getName()+"_Half_Angle");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Rotation Vector half angle of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "est_head_acc");
                senMnf.setName(sensor.getName()+"_Est_head_acc");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Rotation Vector estimated heading accuracy of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_GEOMAGNETIC) {
                //TODO
            } else if (sensorType == SensorTypeKeys.SENS_LIGHT) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "lux");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Light of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_GYROSCOPE) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x_speed");
                senMnf.setName(sensor.getName()+"_X_Speed");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope X-axis speed of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y_speed");
                senMnf.setName(sensor.getName()+"_Y_Speed");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Y-axis speed of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z_speed");
                senMnf.setName(sensor.getName()+"_Z_Speed");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Z-axis speed of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_GYROSCOPE_UNCALIBRATED) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x_speed");
                senMnf.setName(sensor.getName() + "_X_Speed");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Uncalibrated X-axis speed of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y_speed");
                senMnf.setName(sensor.getName() + "_Y_Speed");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Uncalibrated Y-axis speed of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z_bias");
                senMnf.setName(sensor.getName() + "_Z_Speed");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Uncalibrated Z-axis speed of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "x_drift");
                senMnf.setName(sensor.getName() + "_X_Drift");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Uncalibrated X-axis drift of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "y_drift");
                senMnf.setName(sensor.getName() + "_Y_Drift");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Uncalibrated Y-axis drift of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "z_drift");
                senMnf.setName(sensor.getName() + "_Z_Drift");
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Gyroscope Uncalibrated Z-axis drift of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_HUMIDITY) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "humidity");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Humidity of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_HEARTRATE) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "bpm");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Heartrate of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_PRESSURE) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "pressure");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Pressure of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_PROXIMITY) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "proximity");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Proximity of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_STEP_COUNTER) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "steps");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Step Counter of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_STEP_DETECTOR) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "stepped");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Step Detector of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            } else if (sensorType == SensorTypeKeys.SENS_SIGNIFICANT_MOTION) {
                sensorGUID = GUIDUtil.getGUID();
                addSensorGUID(sensorGUID, sensorType, "moved");
                senMnf.setName(sensor.getName());
                senMnf.setType("" + sensor.getType());
                senMnf.setDescription("Significant Motion of a " + Build.MANUFACTURER + " " + Build.MODEL + " " + Build.USER + " Android device");
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_MNF, sCenterGUID, senMnf.getName()), new Gson().toJson(senMnf));
            }
        }
    }

    /**
     * Adds a sensors GUID to the helper map sensorMap.
     * @param GUID sensor GUID
     * @param sensorType sensor type
     * @param sensorValueName value name description (e.g. "x" or "pressure")
     */
    public void addSensorGUID(String GUID, int sensorType, String sensorValueName) {
        HashMap<String, String> dataGUIDmap = new HashMap<>();
        if (sensorMap.containsKey(sensorType)) {
            dataGUIDmap = (HashMap<String,String>) sensorMap.get(sensorType);
        }
        dataGUIDmap.put(sensorValueName, GUID);
        sensorMap.put(sensorType, dataGUIDmap);
    }

    /**
     * Getting the GUID from a already registered sensor out the helper map sensorMap.
     * @param sensorType sensor type
     * @param sensorValueName value name description (e.g. "y" or "proximity")
     * @return
     */
    public String getSensorGUID(int sensorType, String sensorValueName) {
        if (sensorMap.containsKey(sensorType)) {
            if (((HashMap<String,String>)sensorMap.get(sensorType)).containsKey(sensorValueName)) {
                return ((HashMap<String,String>)sensorMap.get(sensorType)).get(sensorValueName);
            }
        }
        return null;
    }

    /**
     * Changes latency on a sensor.
     * @param sensor sensor
     * @param latency latency value
     */
    public void setDelay(Sensor sensor, int latency) {
        if (sensor != null) {
            mSensorManager.registerListener(this, sensor, latency, latency);
        } else {
            Log.w(TAG, "No "+sensor.getName()+" found");
        }
    }

    /**
     * Sends the sensor data to the connected mobile device which publishes to the MQTT boker.
     * It filters out data from the same sensor within 200 millis, protection against overwhelming the network.
     * This method starts a executorService in the background, not blocking the application while sending the data.
     * @param sensorType sensor type
     * @param sensorGUID sensor GUID
     * @param values sensor value
     */
    public void sendSensorData(final int sensorType, final String sensorGUID, final String values) {
        long t = System.currentTimeMillis();

        long lastTimestamp = lastSensorData.get(sensorType);
        long timeAgo = t - lastTimestamp;

        if (lastTimestamp != 0) {
            if (timeAgo < 200) {
                return;
            }
        }

        lastSensorData.put(sensorType, t);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendMessageToMobile(TopicUtil.getTopic(TopicUtil.TOPIC_TYPE_DAT, sCenterGUID, sensorGUID), values);
            }
        });
    }

    /**
     * sends sensor data to the connected mobile device.
     * It is called by the executor Thread out of method sendSensorData().
     * @param topic topic name in which the data should be published
     * @param jsonObject siot.net data json
     */
    private void sendMessageToMobile(String topic, String jsonObject) {
        WearableData wearableData = new WearableData();
        wearableData.setTopic(topic);
        wearableData.setData(jsonObject);
        Wearable.MessageApi.sendMessage(googleApiClient, sGANodeId, MSG_PATH_SEND_DATA, new Gson().toJson(wearableData).getBytes());
        Log.i(TAG, "Send data to mobile: "+sGANodeId+" ; Data: " +topic);
        Log.i(TAG, "Connection status: "+googleApiClient.isConnected());
    }

    /**
     * Starts a listener of the stated sensor and registers it to siot.net.
     * @param sensor sensor
     */
    public void stopListener(Sensor sensor) {
        if (sensor != null) {
            mSensorManager.unregisterListener(this, sensor);
        } else {
            Log.w(TAG, "No "+sensor.getName()+" found");
        }
    }

    /**
     * Stops a listener of the stated sensor.
     * @param sensor sensor
     */
    public void startListener(Sensor sensor) {
        if (sensor != null) {
            registerSensor(sensor);
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.w(TAG, "No "+sensor.getName()+" found");
        }
    }
}
