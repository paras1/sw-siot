package net.siot.android.sensorpublisher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import net.siot.android.gateway.SiotNetGatewayManagerMobile;
import net.siot.android.gateway.messagetypes.WearableData;
import net.siot.android.gateway.util.TopicUtil;

/**
 * Created by Sathesh Paramasamy on 20.11.15.
 * SensorPublisher App for android mobile devices
 */
public class PhoneSensorPublisherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "SensorPub/PSPActivity";
    public static final String PREFS_NAME = "siotnetPrefs";

    public static final String MSG_PATH_SEND_DATA = "sendMessage";
    public static final String MSG_PATH_CONNECT = "connectToSiot";
    public static final String MSG_PATH_DISCONNECT = "disconnectFromSiot";
    public static final String MSG_PATH_START_APP = "startPhoneSensorPublisher";

    private String sLicense;

    private GoogleApiClient googleApiClient = null;
    private String sGAnodeId = null;

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

    private LinearLayout linearLayoutSensorSwitches;

    private EditText editTextLicense;

    private Button buttonConnectBroker;

    private TextView textViewConnectionInfo;

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
    private boolean isConnected;

    private boolean isRunOnBackground;

    private SiotNetGatewayManagerMobile sngwmgr;

    /**
     * Creates the activity. Instantiates SiotNetGateway.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_sensor_publisher);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        sLicense = settings.getString("license", "");

        googleApiClient = getGoogleApiClient(this);

        sngwmgr = new SiotNetGatewayManagerMobile(this);

        linearLayoutSensorSwitches = (LinearLayout) findViewById(R.id.linearLayoutSensorSwitches);
        linearLayoutSensorSwitches.setVisibility(linearLayoutSensorSwitches.GONE);

        editTextLicense = (EditText) findViewById(R.id.editTextLicense);
        textViewConnectionInfo = (TextView) findViewById(R.id.textViewConnectionInfo);

        editTextLicense.setText(sLicense);

        buttonConnectBroker = (Button) findViewById(R.id.buttonConnectBroker);
        buttonConnectBroker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "CONNECT BUTTON clicked: " + editTextLicense.getText().toString());
                sLicense = editTextLicense.getText().toString();
                if (!isConnected && (sLicense != null && !sLicense.equals(""))) {
                    Toast.makeText(getApplicationContext(), "Connecting to siot.net", Toast.LENGTH_SHORT).show();
                    // try to connect to siot.net (URL service and broker)
                    runOnUiThread(new Runnable() {
                        public void run() {
                            isConnected = sngwmgr.connectToSiotNet(sLicense);
                            if (isConnected) {
                                buttonConnectBroker.setText("Disconnect");
                                editTextLicense.setVisibility(editTextLicense.INVISIBLE);
                                textViewConnectionInfo.setText(Html.fromHtml("Device connected via MQTT<br>Broker URL:<br> " + sngwmgr.getMqttBrokerUrl() + "<br>Topic DAT:<br> " + TopicUtil.PREFIX_DAT + "/" + sLicense + "/#"));
                                enableSensorSwitches();
                                linearLayoutSensorSwitches.setVisibility(linearLayoutSensorSwitches.VISIBLE);
                                if (sGAnodeId != null && !sGAnodeId.equals("")) {
                                    Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_CONNECT, sLicense.getBytes());
                                }
                                Toast.makeText(getApplicationContext(), "Android Wear device connected.", Toast.LENGTH_SHORT).show();
                                // save siot.net license to the preferences file
                                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("license", sLicense);
                                editor.commit();
                            }
                        }
                    });

                } else if(isConnected) {
                    isConnected = sngwmgr.disconnectFromSiotNet();
                    buttonConnectBroker.setText("Connect");
                    editTextLicense.setVisibility(editTextLicense.VISIBLE);
                    disableSensorSwitches();
                    if (sGAnodeId != null && !sGAnodeId.equals("")) {
                        Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_DISCONNECT, null);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter your siot.net license", Toast.LENGTH_SHORT).show();
                    if (sGAnodeId != null && !sGAnodeId.equals("")) {
                        Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_DISCONNECT, null);
                    }
                }
            }
        });

    }

    /**
     * Menu creator.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_phone_sensor_publisher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Action to do when activity resumes.
     * Checks if the connection is still established when it was created.
     * Unless it tries to reconnect.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(sngwmgr.getMqttClient() != null) {
            if (isConnected && !sngwmgr.getMqttClient().isConnected())
                isConnected = sngwmgr.connectToSiotNet(sLicense);
        }

    }

    /**
     * Nothing specially to do when activity pauses.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * On starting the GoogleApiClient thread the connection will be established.
     */
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    /**
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Action to do when activity is destroyed.
     * Stops all sensor listeners and closes the connection to siot.net
     * When the GoogleApiClient thread is stopped connection will be closed.
     * Message to Wearable will be sent to inform that the connection to siot.net has closed.
     */
    @Override
    protected void onDestroy() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            if (sGAnodeId != null && !sGAnodeId.equals("")) {
                Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_DISCONNECT, null);
            }
            googleApiClient.disconnect();
        }
        if (isConnected) {
            sngwmgr.getSensorService().stopAllListeners();
            sngwmgr.disconnectFromSiotNet();
        }
        super.onDestroy();
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
     * Creates switches for available sensor on device and enables them.
     */
    private void enableSensorSwitches() {

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
                        Toast.makeText(getApplicationContext(), "The magnetic field is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The gyroscope is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The light sensor is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The pressure sensor is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The proximity sensor is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The gravity sensor is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The linear accelerometer is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The rotation vector is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The humidity sensor is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The ambient thermometer is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The magnetic field uncalibrated is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The game rotation vector is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The gyroscope uncalibrated is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The significant motion sensor is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The step detector is OFF",Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "The step counter is OFF",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }  else {
            stepCounterSwitch.setClickable(false);
            stepCounterSwitch.setVisibility(stepCounterSwitch.GONE);
        }
/*
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
                        Toast.makeText(getApplicationContext(), "The geomagnetic sensor is OFF",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }  else {
            geomagneticSwitch.setClickable(false);
            geomagneticSwitch.setVisibility(geomagneticSwitch.GONE);
        }
*/
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
                    Toast.makeText(getApplicationContext(), "The all sensors OFF",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Disable all sensor switches for defined situations (e.g. disconnected from siot.net)
     */
    private void disableSensorSwitches() {
        linearLayoutSensorSwitches.setVisibility(linearLayoutSensorSwitches.GONE);
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (sGAnodeId != null && !sGAnodeId.equals("")) {
            Wearable.MessageApi.sendMessage(googleApiClient, sGAnodeId, MSG_PATH_DISCONNECT, null);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Inner class. MessageReceiver for message from wearables.
     */
    public class MessageReceiver extends BroadcastReceiver {

        /**
         * Action which will be done when a message is received.</br>
         * Paths:</br>
         *     sendMessage : triggers the action to publish a message to the MQTT broker with the given topic and JSON formatted string</br>
         *     connectToSiot : triggers an action which connects the mobile device to the siot.net MQTT broker and sends the license code as a message to the wearable
         * @param context activity context
         * @param intent received message intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Message onReceive: "+intent.getStringExtra("value"));

            String path = intent.getStringExtra("path");

            if (path.equals(MSG_PATH_CONNECT)) {
                //String value = intent.getStringExtra("value");
                Log.i(TAG, "connect mobile gateway to siot.net");
                //Toast.makeText(getApplicationContext(), "Connecting to siot.net", Toast.LENGTH_SHORT).show();
                if(!isConnected)
                    buttonConnectBroker.performClick();

                Log.i(TAG, "Source NodeId: " + intent.getStringExtra("nodeId"));
                sGAnodeId = intent.getStringExtra("nodeId");


            } else if (path.equals(MSG_PATH_DISCONNECT)) {
                Toast.makeText(getApplicationContext(), "Android Wear device disconnected.", Toast.LENGTH_SHORT).show();
            } else if (path.equals(MSG_PATH_SEND_DATA)) {

                //Toast.makeText(getApplicationContext(), "Send data to siot.net", Toast.LENGTH_SHORT).show();
                if (sngwmgr.getMqttClient() != null &&sngwmgr.getMqttClient().isConnected()) {
                    WearableData wearableData = new Gson().fromJson(intent.getStringExtra("value"), WearableData.class);
                    sngwmgr.getMqttClient().publishData(wearableData.getTopic(), wearableData.getData());
                    Log.i(TAG, "DATA to send: " + intent.getStringExtra("value"));
                } else {
                    Log.i(TAG, "Not connected to MQTT broker");
                }
            }
        }
    }
}
