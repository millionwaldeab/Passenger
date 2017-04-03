package com.asmarainnovations.taxi;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Closeable;
import java.util.ArrayList;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 10/14/2015.
 */
public class Driver implements LocationListener{
    Location l;
    String token;
    String cab_type;
    Marker cMarker;
    String phone;

    public Driver(){
        super();
    }

    public Driver(String mytoken){
        this.token = mytoken;
    }

    public Driver(Location location, String mytok){
        this.l = location;
        this.token = mytok;
    }

    public Driver(Location location, String mytoken, String type, String number){
        this.l = location;
        this.token = mytoken;
        this.cab_type = type;
        this.phone = number;
    }

    public void setDriver(Marker m){
        this.cMarker = m;
    }

    public Location getL() {
        return l;
    }

    public String getToken() {
        return token;
    }

    public String getCab_type() {
        return cab_type;
    }

    public Marker getMarker() {
        return cMarker;
    }

    public void setDriver(String driverToken){
        this.token = driverToken;
    }

    public void setDriver(Location loc, String tok, String Ctype){
        this.l = loc;
        this.token = tok;
        this.cab_type = Ctype;
    }

    public void destroyDriver(){
        Driver drivr = new Driver();
        drivr = null;
    }

    //create marker that will identify each cab by type
    public Marker makeMarker(Location cabL, String cabTyp){
        MapActivity ma = new MapActivity();
        LatLng cabltln = new LatLng(cabL.getLatitude(), cabL.getLongitude());
        Marker cabM = ma.map.addMarker(new MarkerOptions()
                .position(cabltln)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.movingcab))
                .anchor(0.5f, 0.5f) //so marker rotates around the center
                .flat(true)
                .title(cabTyp));

        long etatime = cabL.getTime();
        //etaCounter.setText(String.valueOf(etatime));
        cMarker = cabM;
        return cabM;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.l = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
