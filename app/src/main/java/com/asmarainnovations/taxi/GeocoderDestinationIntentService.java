package com.asmarainnovations.taxi;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 12/9/2015.
 */
public class GeocoderDestinationIntentService extends IntentService {

    private static final String TAG = "Mapper";
    static final int numberOptions = 10;
    String[] optionArray = new String[numberOptions];
    double lat, lon;
    LatLng convertedcoordinates;
    public static final String destinationltlngObject = "com.asmarainnovations.taxi.destinationlstlng";
    public static final String LTLNGTO_SEND = "TEST";
    LocalBroadcastManager destinationGeocoder;

    public GeocoderDestinationIntentService(){super("");}
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeocoderDestinationIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        destinationGeocoder = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                Location addressltlng = geocodeLocation(intent.getStringExtra("destination_address"));
                Intent sendltlng = new Intent();
                sendltlng.setAction(destinationltlngObject);
                sendltlng.putExtra(LTLNGTO_SEND, String.valueOf(addressltlng.getLatitude()) + ", " + String.valueOf(addressltlng.getLongitude()));
                destinationGeocoder.sendBroadcast(sendltlng);
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("null shit", ex.toString());
            }
        }
    }

    // Method to geocode location passed as string (e.g., "Pentagon"), which
    // places the corresponding latitude and longitude in the variables lat and
    // lon. This method will convert address to lat/lon

    public Location geocodeLocation(String placeName) {

        // Following adapted from Conder and Darcey, pp.321 ff.
        Geocoder gcoder = new Geocoder(this);

        // Note that the Geocoder uses synchronous network access, so in a
        // serious application
        // it would be best to put it on a background thread to prevent blocking
        // the main UI if network
        // access is slow. Here we are just giving an example of how to use it
        // so, for simplicity, we
        // don't put it on a separate thread. See the class RouteMapper in this
        // package for an example
        // of making a network access on a background thread. Geocoding is
        // implemented by a backend
        // that is not part of the core Android framework, so we use the static
        // method
        // Geocoder.isPresent() to test for presence of the required backend on
        // the given platform.

        try {
            List<Address> results = null;
            if (Geocoder.isPresent()) {
                results = gcoder.getFromLocationName(placeName, numberOptions);
            } else {
                Log.i(TAG, "No geocoder accessible on this platform");
                //return;
            }
            Iterator<Address> locations = results.iterator();
            String raw = "\nRaw String:\n";
            String country;
            int opCount = 0;
            while (locations.hasNext()) {
                Address location = locations.next();
                if (opCount == 0 && location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                }
                country = location.getCountryName();
                if (country == null) {
                    country = "";
                } else {
                    country = ", " + country;
                }
                raw += location + "\n";
                optionArray[opCount] = location.getAddressLine(0) + ", "
                        + location.getAddressLine(1) + country + "\n";
                opCount++;
            }
            Log.i(TAG, raw);
            Log.i(TAG, "\nOptions:\n");
            for (int i = 0; i < opCount; i++) {
                Log.i(TAG, "(" + (i + 1) + ") " + optionArray[i]);
            }
            Log.i(TAG, "lat=" + lat + " lon=" + lon);

        } catch (IOException e) {
            Log.e(TAG, "I/O Failure; is network available?", e);
        }
        //convertedcoordinates = new LatLng(lat, lon);
        //return convertedcoordinates;
        Location returnvalue = new Location("");
        returnvalue.setLatitude(lat);
        returnvalue.setLongitude(lon);
        return returnvalue;
    }
}
