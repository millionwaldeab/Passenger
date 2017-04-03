package com.asmarainnovations.taxi;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
public class MyMapActivity extends SupportMapFragment implements OnMapReadyCallback {
	MapView mMapView;
	private GoogleMap googleMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	    // inflat and return the layout
	    View v = inflater.inflate(R.layout.activity_main, container,
	            false);
	    mMapView = (MapView) v.findViewById(R.id.map);
	    mMapView.onCreate(savedInstanceState);
	    mMapView.setEnabled(true);
	    mMapView.onResume();// needed to get the map to display immediately
	 // Gets to GoogleMap from the MapView and does initialization stuff
	    googleMap = mMapView.getMap();
	    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
	    googleMap.getUiSettings().isCompassEnabled();
	    googleMap.getUiSettings().isZoomControlsEnabled();
	    //googleMap.setMyLocationEnabled(true);

	    try {
	        MapsInitializer.initialize(getActivity().getApplicationContext());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    /*try {
			if (googleMap != null)
				googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			LatLng latLng = new LatLng(13.05241, 80.25082);
			googleMap.addMarker(new MarkerOptions().position(latLng).title("Your current location"));
		} catch (Exception x) {
			x.printStackTrace();
		}

	    googleMap = mMapView.getMap();*/
	    
	    // latitude and longitude
	    double latitude = 17.385044;
	    double longitude = 78.486671;

	    // create marker
	    MarkerOptions marker = new MarkerOptions().position(
	            new LatLng(latitude, longitude)).title("Your corrent location");

	    // Changing marker icon
	    marker.icon(BitmapDescriptorFactory
	            .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

	    // adding marker
	    googleMap.addMarker(marker);
	    CameraPosition cameraPosition = new CameraPosition.Builder()
	            .target(new LatLng(17.385044, 78.486671)).zoom(12).build();
	    googleMap.animateCamera(CameraUpdateFactory
	            .newCameraPosition(cameraPosition));

	    // Perform any camera updates here
	    return v;
	}

	@Override
	public void onResume() {
	    super.onResume();
	    mMapView.onResume();
	}

	@Override
	public void onPause() {
	    super.onPause();
	    mMapView.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
	    super.onLowMemory();
	    mMapView.onLowMemory();
	}

	@Override
	public void onMapReady(GoogleMap arg0) {
		// TODO Auto-generated method stub
		
	}

}

