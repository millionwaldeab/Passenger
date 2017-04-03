package com.asmarainnovations.taxi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.location.LocationListener;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */

public class LocationListenerClass extends BroadcastReceiver implements LocationListener {

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		 try {
	            if (location != null) {
//	                Data.CURENT_LATITUDE = location.getLatitude();
//	                Log.v(ConstantLib.LOG, "LOCATION CHANGED" + " latitude : "
//	                        + Data.CURENT_LATITUDE);
//	                longitude = location.getLongitude();
//	                Data.CURENT_LONGITUDE = location.getLongitude();
//	                Log.v(ConstantLib.LOG, "LOCATION CHANGED" + " longitude : "
//	                        + Data.CURENT_LONGITUDE);
	            }
	        } catch (Exception e) {

	        }

		
	}
	
	public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}
}


