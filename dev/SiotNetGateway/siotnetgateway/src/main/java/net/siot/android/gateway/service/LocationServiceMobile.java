package net.siot.android.gateway.service;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import net.siot.android.gateway.connection.MQTTClient;

/**
 * Location service class is TODO
 * Created by Sathesh on 05.01.16.
 */
public class LocationServiceMobile implements LocationListener {

    private MQTTClient mqttClient;
    private String sCenterGUID;

    public LocationServiceMobile(String sCenterGUID, MQTTClient mqttClient) {

        this.sCenterGUID = sCenterGUID;
        this.mqttClient = mqttClient;

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
