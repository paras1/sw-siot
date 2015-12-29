package net.siot.android.sensorpublisher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import net.siot.android.gateway.SiotNetGatewayManagerWear;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sathesh Paramasamy on 15.12.15.
 * SensorPublisher App for android wear devices
 */
public class WearSensorPublisherActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static long CONNECTION_TIME_OUT_MS = 100;
    private final static String MSG_PATH_SEND_DATA = "sendMessage";
    private final static String MSG_PATH_CONNECT = "connectToSiot";
    private final static String MSG_PATH_DISCONNECT = "disconnectFromSiot";
    private final static String MSG_PATH_START_APP = "startPhoneSensorPublisher";

    private LinearLayout linearLayoutSensorSwitches;

    //Switches
    private Switch accelerometerSwitch;
    private Switch magneticFieldSwitch;
    private Switch gyroscopeSwitch;
    private Switch lightSwitch;
    private Switch pressureSwitch;
    private Switch proximitySwitch;
    private Switch gravitySwitch;
    private Switch linearAccelerationSwitch;
    private Switch rotationVectorSwitch;
    private Switch humiditySwitch;
    private Switch ambientTemperatureSwitch;
    private Switch magneticFieldUncalSwitch;
    private Switch gameRotationVectorSwitch;
    private Switch gyroscopeUncalSwitch;
    private Switch significantMotionSwitch;
    private Switch stepDetectorSwitch;
    private Switch stepCounterSwitch;
    private Switch geomagneticSwitch;
    private Switch heartrateSwitch;
    private Switch heartrateSamsungSwitch;
    private Switch allSensorsSwitch;

    private Button buttonConnectBroker;

    private boolean isConnected;

    private GoogleApiClient googleApiClient = null;
    private String sGAnodeId = null;

    //Sensor States
    private boolean isAccelerometerON;
    private boolean isMagneticFieldON;
    private boolean isGyroscopeON;
    private boolean isLightON;
    private boolean isPressureON;
    private boolean isProximityON;
    private boolean isGravityON;
    private boolean isLinearAccelerationON;
    private boolean isRotationVectorON;
    private boolean isHumidityON;
    private boolean isAmbientTemperatureON;
    private boolean isMagneticFieldUncalON;
    private boolean isGameRotationVectorON;
    private boolean isGyroscopeUncalON;
    private boolean isSignificantMotionON;
    private boolean isStepDetectorON;
    private boolean isStepCounterON;
    private boolean isGeomagneticON;
    private boolean isHeartrateON;
    private boolean isHeartrateSamsungON;
    private boolean isAllSensorsOn;

    public String sLicense;

    private SiotNetGatewayManagerWear sngwmgr;

    private static final String TAG = "SensorPub/WSPActivity";

    /**
     * Creates the activity. Instantiates SiotNetGateway.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_sensor_publisher);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        googleApiClient = getGoogleApiClient(this);
        retrieveMobileNodeId();

        //Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, "startPhoneSensorPublisher", null);
        sngwmgr = new SiotNetGatewayManagerWear(this, googleApiClient, sGAnodeId);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                linearLayoutSensorSwitches = (LinearLayout) stub.findViewById(R.id.linearLayoutSensorSwitches);
                linearLayoutSensorSwitches.setVisibility(linearLayoutSensorSwitches.GONE);

                buttonConnectBroker = (Button) findViewById(R.id.buttonConnectBroker);
                buttonConnectBroker.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.i(TAG, "CONNECT BUTTON on wear clicked");
                        if (!isConnected) {
                            isConnected = true;
                            buttonConnectBroker.setText("Disconnect");
                            Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_CONNECT, null);

                            //sngwmgr.connectToMobile()
                            //editTextLicense.setVisibility(editTextLicense.INVISIBLE);
                            //textViewConnectionInfo.setText(Html.fromHtml("Device connected via MQTT<br>Broker URL:<br> " + sngwmgr.getMqttBrokerUrl() + "<br>ClientId:<br> " + sLicense + "<br>Topic DAT:<br> " + TopicUtil.PREFIX_DAT + "/" + sLicense + "/#"));
                            //enableSensorSwitches();
                            //linearLayoutSensorSwitches.setVisibility(linearLayoutSensorSwitches.VISIBLE);
                        } else if (isConnected) {
                            isConnected = false;
                            buttonConnectBroker.setText("Connect to siot.net");
                            Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_DISCONNECT, null);
                            //editTextLicense.setVisibility(editTextLicense.VISIBLE);
                            disableSensorSwitches();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter your siot.net license", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    /**
     * Getting a GoogleApiClient. Used for messaging between mobile and wearable.
     * @param context GoogleApiClient builder needs the activity context
     * @return GoogleApiClient instance
     */
    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Retrieves the GoogleApiClient nodeId from mobile device.
     */
    private void retrieveMobileNodeId() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                googleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    sGAnodeId = nodes.get(0).getId();
                    Log.i(TAG, "Node ID from mobile: " + sGAnodeId);
                }
                sngwmgr.setsGAnodeId(sGAnodeId);
            }
        }).start();
    }

    /**
     * Creates switches for available sensor on device and enables them.
     */
    private void enableSensorSwitches() {

        allSensorsSwitch = (Switch) findViewById(R.id.allSwitch);
        allSensorsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAllSensorsOn = isChecked;
                    sngwmgr.getSensorService().startAllListeners();
                    Toast.makeText(getApplicationContext(), "The all sensors ON", Toast.LENGTH_SHORT).show();
                } else {
                    isAllSensorsOn = !isChecked;
                    sngwmgr.getSensorService().stopAllListeners();
                    Toast.makeText(getApplicationContext(), "The all sensors OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        accelerometerSwitch = (Switch) findViewById(R.id.accelerometerSwitch);
        if (sngwmgr.getSensorService().accelerometerSensor != null) {
            accelerometerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isAccelerometerON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().accelerometerSensor);
                        Toast.makeText(getApplicationContext(), "The accelerometer is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isAccelerometerON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().accelerometerSensor);
                        Toast.makeText(getApplicationContext(), "The accelerometer is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            accelerometerSwitch.setClickable(false);
            accelerometerSwitch.setVisibility(accelerometerSwitch.GONE);
        }

        magneticFieldSwitch = (Switch) findViewById(R.id.magneticFieldSwitch);
        if (sngwmgr.getSensorService().magneticFieldSensor != null) {


            magneticFieldSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isMagneticFieldON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().magneticFieldSensor);
                        Toast.makeText(getApplicationContext(), "The magnetic field is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isMagneticFieldON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().magneticFieldSensor);
                        Toast.makeText(getApplicationContext(), "The magnetic field is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            magneticFieldSwitch.setClickable(false);
            magneticFieldSwitch.setVisibility(magneticFieldSwitch.GONE);
        }

        gyroscopeSwitch = (Switch) findViewById(R.id.gyroscopeSwitch);
        if (sngwmgr.getSensorService().gyroscopeSensor != null) {
            gyroscopeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isGyroscopeON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().gyroscopeSensor);
                        Toast.makeText(getApplicationContext(), "The gyroscope is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isGyroscopeON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().gyroscopeSensor);
                        Toast.makeText(getApplicationContext(), "The gyroscope is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            gyroscopeSwitch.setClickable(false);
            gyroscopeSwitch.setVisibility(gyroscopeSwitch.GONE);
        }

        lightSwitch = (Switch) findViewById(R.id.lightSwitch);
        if (sngwmgr.getSensorService().lightSensor != null) {
            lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isLightON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().lightSensor);
                        Toast.makeText(getApplicationContext(), "The light sensor is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isLightON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().lightSensor);
                        Toast.makeText(getApplicationContext(), "The light sensor is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            lightSwitch.setClickable(false);
            lightSwitch.setVisibility(lightSwitch.GONE);
        }

        pressureSwitch = (Switch) findViewById(R.id.pressureSwitch);
        if (sngwmgr.getSensorService().pressureSensor != null) {
            pressureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isPressureON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().pressureSensor);
                        Toast.makeText(getApplicationContext(), "The pressure sensor is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isPressureON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().pressureSensor);
                        Toast.makeText(getApplicationContext(), "The pressure sensor is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            pressureSwitch.setClickable(false);
            pressureSwitch.setVisibility(pressureSwitch.GONE);
        }

        proximitySwitch = (Switch) findViewById(R.id.proximitySwitch);
        if (sngwmgr.getSensorService().proximitySensor != null) {
            proximitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isProximityON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().proximitySensor);
                        Toast.makeText(getApplicationContext(), "The proximity sensor is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isProximityON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().proximitySensor);
                        Toast.makeText(getApplicationContext(), "The proximity sensor is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            proximitySwitch.setClickable(false);
            proximitySwitch.setVisibility(proximitySwitch.GONE);
        }

        gravitySwitch = (Switch) findViewById(R.id.gravitySwitch);
        if (sngwmgr.getSensorService().gravitySensor != null) {
            gravitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isGravityON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().gravitySensor);
                        Toast.makeText(getApplicationContext(), "The gravity sensor is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isGravityON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().gravitySensor);
                        Toast.makeText(getApplicationContext(), "The gravity sensor is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            gravitySwitch.setClickable(false);
            gravitySwitch.setVisibility(gravitySwitch.GONE);
        }

        linearAccelerationSwitch = (Switch) findViewById(R.id.linearAccelerationSwitch);
        if (sngwmgr.getSensorService().linearAccelerationSensor != null) {

            linearAccelerationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isLinearAccelerationON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().linearAccelerationSensor);
                        Toast.makeText(getApplicationContext(), "The linear accelerometer is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isLinearAccelerationON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().linearAccelerationSensor);
                        Toast.makeText(getApplicationContext(), "The linear accelerometer is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            linearAccelerationSwitch.setClickable(false);
            linearAccelerationSwitch.setVisibility(linearAccelerationSwitch.GONE);
        }

        rotationVectorSwitch = (Switch) findViewById(R.id.rotationVectorSwitch);
        if (sngwmgr.getSensorService().rotationVectorSensor != null) {
            rotationVectorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isRotationVectorON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().rotationVectorSensor);
                        Toast.makeText(getApplicationContext(), "The rotation vector is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isRotationVectorON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().rotationVectorSensor);
                        Toast.makeText(getApplicationContext(), "The rotation vector is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            rotationVectorSwitch.setClickable(false);
            rotationVectorSwitch.setVisibility(rotationVectorSwitch.GONE);
        }

        humiditySwitch = (Switch) findViewById(R.id.humiditySwitch);
        if (sngwmgr.getSensorService().humiditySensor != null) {
            humiditySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isHumidityON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().humiditySensor);
                        Toast.makeText(getApplicationContext(), "The humidity sensor is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isHumidityON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().humiditySensor);
                        Toast.makeText(getApplicationContext(), "The humidity sensor is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            humiditySwitch.setClickable(false);
            humiditySwitch.setVisibility(humiditySwitch.GONE);
        }

        ambientTemperatureSwitch = (Switch) findViewById(R.id.ambientTemperatureSwitch);
        if (sngwmgr.getSensorService().ambientTemperatureSensor != null) {
            ambientTemperatureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isAccelerometerON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().ambientTemperatureSensor);
                        Toast.makeText(getApplicationContext(), "The ambient thermometer is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isAccelerometerON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().ambientTemperatureSensor);
                        Toast.makeText(getApplicationContext(), "The ambient thermometer is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ambientTemperatureSwitch.setClickable(false);
            ambientTemperatureSwitch.setVisibility(ambientTemperatureSwitch.GONE);
        }

        magneticFieldUncalSwitch = (Switch) findViewById(R.id.magneticFieldUncalSwitch);
        if (sngwmgr.getSensorService().magneticFieldUncalibratedSensor != null) {
            magneticFieldUncalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isMagneticFieldUncalON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().magneticFieldUncalibratedSensor);
                        Toast.makeText(getApplicationContext(), "The magnetic field uncalibrated is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isMagneticFieldUncalON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().magneticFieldUncalibratedSensor);
                        Toast.makeText(getApplicationContext(), "The magnetic field uncalibrated is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            magneticFieldUncalSwitch.setClickable(false);
            magneticFieldUncalSwitch.setVisibility(magneticFieldUncalSwitch.GONE);
        }

        gameRotationVectorSwitch = (Switch) findViewById(R.id.gameRotationVectorSwitch);
        if (sngwmgr.getSensorService().gameRotationVectorSensor != null) {
            gameRotationVectorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isGameRotationVectorON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().gameRotationVectorSensor);
                        Toast.makeText(getApplicationContext(), "The game rotation vector is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isGameRotationVectorON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().gameRotationVectorSensor);
                        Toast.makeText(getApplicationContext(), "The game rotation vector is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            gameRotationVectorSwitch.setClickable(false);
            accelerometerSwitch.setVisibility(gameRotationVectorSwitch.GONE);
        }

        gyroscopeUncalSwitch = (Switch) findViewById(R.id.gyroscopeUncalSwitch);
        if (sngwmgr.getSensorService().gyroscopeUncalibratedSensor != null) {
            gyroscopeUncalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isGyroscopeUncalON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().gyroscopeUncalibratedSensor);
                        Toast.makeText(getApplicationContext(), "The gyroscope uncalibrated is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isGyroscopeUncalON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().gyroscopeUncalibratedSensor);
                        Toast.makeText(getApplicationContext(), "The gyroscope uncalibrated is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            gyroscopeUncalSwitch.setClickable(false);
            gyroscopeUncalSwitch.setVisibility(gyroscopeUncalSwitch.GONE);
        }

        significantMotionSwitch = (Switch) findViewById(R.id.significantMotionSwitch);
        if (sngwmgr.getSensorService().significantMotionSensor != null) {
            significantMotionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isSignificantMotionON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().significantMotionSensor);
                        Toast.makeText(getApplicationContext(), "The significant motion sensor is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isSignificantMotionON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().significantMotionSensor);
                        Toast.makeText(getApplicationContext(), "The significant motion sensor is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            significantMotionSwitch.setClickable(false);
            significantMotionSwitch.setVisibility(significantMotionSwitch.GONE);
        }

        stepDetectorSwitch = (Switch) findViewById(R.id.stepDetectorSwitch);
        if (sngwmgr.getSensorService().stepDetectorSensor != null) {
            stepDetectorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isStepDetectorON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().stepDetectorSensor);
                        Toast.makeText(getApplicationContext(), "The step detector is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isStepDetectorON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().stepDetectorSensor);
                        Toast.makeText(getApplicationContext(), "The step detector is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            stepDetectorSwitch.setClickable(false);
            stepDetectorSwitch.setVisibility(stepDetectorSwitch.GONE);
        }

        stepCounterSwitch = (Switch) findViewById(R.id.stepCounterSwitch);
        if (sngwmgr.getSensorService().stepCounterSensor != null) {
            stepCounterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isStepCounterON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().stepCounterSensor);
                        Toast.makeText(getApplicationContext(), "The step counter is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isStepCounterON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().stepCounterSensor);
                        Toast.makeText(getApplicationContext(), "The step counter is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            stepCounterSwitch.setClickable(false);
            stepCounterSwitch.setVisibility(stepCounterSwitch.GONE);
        }

        geomagneticSwitch = (Switch) findViewById(R.id.geomagneticSwitch);
        if (sngwmgr.getSensorService().geomagneticSensor != null) {
            geomagneticSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isGeomagneticON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().geomagneticSensor);
                        Toast.makeText(getApplicationContext(), "The geomagnetic sensor is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isGeomagneticON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().geomagneticSensor);
                        Toast.makeText(getApplicationContext(), "The geomagnetic sensor is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            geomagneticSwitch.setClickable(false);
            geomagneticSwitch.setVisibility(geomagneticSwitch.GONE);
        }

        heartrateSwitch = (Switch) findViewById(R.id.heartrateSwitch);
        if (sngwmgr.getSensorService().heartrateSensor != null) {
            heartrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isHeartrateON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().heartrateSensor);
                        Toast.makeText(getApplicationContext(), "The heart rate measurement is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isHeartrateON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().heartrateSensor);
                        Toast.makeText(getApplicationContext(), "The heart rate measurement is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            heartrateSwitch.setClickable(false);
            heartrateSwitch.setVisibility(heartrateSwitch.GONE);
        }

        heartrateSamsungSwitch = (Switch) findViewById(R.id.heartrateSamsungSwitch);
        if (sngwmgr.getSensorService().heartrateSamsungSensor != null) {
            heartrateSamsungSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        isHeartrateSamsungON = isChecked;
                        sngwmgr.getSensorService().startListener(sngwmgr.getSensorService().heartrateSamsungSensor);
                        Toast.makeText(getApplicationContext(), "The SAMSUNG heart rate measurement is ON", Toast.LENGTH_SHORT).show();
                    } else {
                        isHeartrateSamsungON = !isChecked;
                        sngwmgr.getSensorService().stopListener(sngwmgr.getSensorService().heartrateSamsungSensor);
                        Toast.makeText(getApplicationContext(), "The SAMSUNG heart rate measurement is OFF", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            heartrateSamsungSwitch.setClickable(false);
            heartrateSamsungSwitch.setVisibility(heartrateSamsungSwitch.GONE);
        }
        linearLayoutSensorSwitches.setVisibility(linearLayoutSensorSwitches.VISIBLE);
    }

    /**
     * Disable all sensor switches for defined situations (e.g. disconnected from siot.net)
     */
    private void disableSensorSwitches() {
        linearLayoutSensorSwitches.setVisibility(linearLayoutSensorSwitches.GONE);
    }

    /**
     * On starting the GoogleApiClient thread the connection will be established
     */
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();

    }

    /**
     * When the GoogleApiClient thread is stopped connection will be closed
     */
    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * When the wearable device is connected to the MessageApi to the mobile. Send start trigger to the PhoneSensorPublisher activity.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "-Connected to Mobile-");
        Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_START_APP, null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Inner class. MessageReceiver for message from mobile device.
     */
    public class MessageReceiver extends BroadcastReceiver {

        /**
         * Action which will be done when a message is received.</br>
         * Paths:</br>
         *     sendMessage : triggers the action to generate a toast which says that data is sent from wearable device</br>
         *     connectToSiot : triggers an action which connects the android wear device to the mobile. That is connected to siot.net MQTT broker.
         *     disconnectFromSiot: trigger an action which disables all sensor on the wearable. Shows the login screen.
         * @param context activity context
         * @param intent received message intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Message onReceive: " + intent.getStringExtra("value"));

            String path = intent.getStringExtra("path");

            if (path.equals(MSG_PATH_CONNECT)) {
                //String value = intent.getStringExtra("value");
                Log.i(TAG, "connect wear sensor to mobile gateway");
                //Toast.makeText(getApplicationContext(), "Connecting to siot.net", Toast.LENGTH_SHORT).show();
                sngwmgr.connectToMobile(intent.getStringExtra("value"));
                enableSensorSwitches();
                Log.i(TAG, "Source NodeId: " + intent.getStringExtra("nodeId"));

            } else if (path.equals(MSG_PATH_DISCONNECT)) {
                isConnected = false;
                buttonConnectBroker.setText("Connect to siot.net");
                disableSensorSwitches();
            } else if (path.equals(MSG_PATH_SEND_DATA)) {

                Toast.makeText(getApplicationContext(), "Send pass-through data to mobile gateway", Toast.LENGTH_SHORT).show();
                Log.i(TAG, intent.getStringExtra("value"));
            }
        }
    }
}
