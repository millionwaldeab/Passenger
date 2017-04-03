package com.asmarainnovations.taxi;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
//This class is for getting location updates
public abstract class MyLocationProvider implements FusedLocationProviderApi {  //added abstract
	

	@Override
	public Location getLastLocation(GoogleApiClient arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingResult<Status> removeLocationUpdates(GoogleApiClient client,
			LocationListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingResult<Status> removeLocationUpdates(GoogleApiClient client,
			PendingIntent callbackIntent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingResult<Status> requestLocationUpdates(GoogleApiClient client,
			LocationRequest request, LocationListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingResult<Status> requestLocationUpdates(GoogleApiClient client,
			LocationRequest request, PendingIntent callbackIntent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingResult<Status> requestLocationUpdates(GoogleApiClient client,
			LocationRequest request, LocationListener listener, Looper looper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingResult<Status> setMockLocation(GoogleApiClient client,
			Location mockLocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PendingResult<Status> setMockMode(GoogleApiClient client,
			boolean isMockMode) {
		// TODO Auto-generated method stub
		return null;
	}

}
