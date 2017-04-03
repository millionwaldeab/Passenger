package com.asmarainnovations.taxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.asmarainnovations.taxi.FetchAddressIntentService.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.asmarainnovations.taxi.MarkerAnimation.animateMarkerToGB;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener, OnClickListener {
    // to test try GCA 2013/2014 in youtube and Jenkins or UI animator, Espresso
    boolean mMapIsTouched;

    GoogleMap map;
    AutoCompleteTextView from, to;
    Marker marker, cabMarker, customMarker;
    double driverlatitude, driverlongitude;
    private Driver driverObj;
    ArrayList<Driver> driverList;
    ArrayList<Marker> myMarkers;
    UtilityClass utility;

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private MediaPlayer mpCancelled;
    Location fusedLocation, GPSLoc, cabLocation, bestLocation;
    Button sendlocation, buttoncancel, buttondestination;
    String type = "Regular sedan", TAG, TAGGPS, inidriverlat, inidriverlon, inidriver = "", dtoken = "", inicab_type = "type",
            phone = "number", isConnected, estimatedarriaval = "";
    private String tok = "passenger token";
    static AutoCompleteTextView acrilocation, destinationText;
    GoogleApiClient mGoogleApiClient;
    Context context;
    private Connection conn;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    TextToSpeech tts;
    boolean isLocationUpdating = false, isTextEntered = false;
    LocationListener listener;
    LocationManager locationManager;
    LocationRequest mLocationRequest; // to request high priority to low priority
    // updates from the fusedlocaprovi
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_GCM_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    // Server Url absolute url where php files are placed.
    static final String PHP_SERVER_URL = Configuration_Data.PHP_SERVER_URL;
    String SENDER_ID = Configuration_Data.SENDER_ID; //this is the project number
    String DRIVER_SENDER_ID = Configuration_Data.DRIVER_SENDER_ID;
    static final String GCM_TAG = "GCMDemo"; //log messages

    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    final String local = Configuration_Data.local;
    final String remote = Configuration_Data.remote;
    private static final String API_KEY = Configuration_Data.API_KEY; //this is android key 3

    // public static Location mLastLocation = new
    // Location(LocationManager.GPS_PROVIDER);
    public AddressResultReceiver mAddressResultReceiver;
    boolean mAddressRequested;

    Toast avlblCabToast;
    // private static LatLngInterpolator latLngInterpolator;
    private static LatLng riderLocationCoordinate, myLocation;

    DrawerLayout mdrawerLayout;
    ListView mMenuList;
    ImageView appImage;
    TextView TitleText, etaCounter, hcView, vanView, regularView;
    String[] MenuTitles = new String[]{"Promotions", "Legal", "Contact Us", "Misc", "ContactDriver"};
    Toolbar toolbar;
    private ActionBarDrawerToggle drlistener;
    private LinearLayout menuList;
    private boolean isMarkerAlive = false;
    private HashMap<String, Marker> visibleMarkers;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //prevents the screen from rotating
        sendlocation = (Button) findViewById(R.id.bsendmylocation);
        acrilocation = (AutoCompleteTextView) findViewById(R.id.actvriderlocation);
        destinationText = (AutoCompleteTextView) findViewById(R.id.actvDestination);
        mAddressResultReceiver = new AddressResultReceiver(new Handler());
        buttoncancel = (Button) findViewById(R.id.bcancelRequest);
        buttondestination = (Button) findViewById(R.id.bDestination);
        etaCounter = (TextView) findViewById(R.id.tvarrival_time_counter);
        regularView = (TextView) findViewById(R.id.tvRegular);
        vanView = (TextView) findViewById(R.id.tvVan);
        hcView = (TextView) findViewById(R.id.tvHandicapped);
        context = getApplicationContext();
        menuList = (LinearLayout) findViewById(R.id.menuLayout);
        tok = getRegistrationToken();
        gcm = GoogleCloudMessaging.getInstance(this);
        driverList = new ArrayList<>();
        myMarkers = new ArrayList<Marker>();
        utility = new UtilityClass(context);
        visibleMarkers = new HashMap<String, Marker>();
        avlblCabToast = avlblCabToast.makeText(getApplicationContext(), getResources().getString(R.string.no_requested_cabs), Toast.LENGTH_LONG);
        avlblCabToast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);

        //If there is no internet connection notify the user and close
        conn = new Connection(context);
        if (!conn.isNetworkAvailable()) {
            utility.showToast(10000, "No internet connection, please try again later");
            finish();
        }

        if (checkPlayServices()) {

            //menuList.setVisibility(View.INVISIBLE); //hide the list temporarily until further decision
            destinationText.setVisibility(View.INVISIBLE);
            // This prevents my Nexus 7 running Android 4.4.2 from opening
            // the soft keyboard when the app is launched rather than when
            // an input field is selected.
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
            if (map == null) {
                // Try to obtain the map from the SupportMapFragment.
                map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
                // Check if we were successful in obtaining the map.
                if (map != null) {
                    map.setMyLocationEnabled(true);
                    map.getUiSettings().isCompassEnabled();
                    map.getUiSettings().setMyLocationButtonEnabled(true);
                    map.getUiSettings().isZoomControlsEnabled();
                    //map.setPadding(0, 100, 0, 0);
                }
            }
            //change camera if user pans map to select different pick up location
            updateMapFromMarkerLocation(true);

            if(cabMarker != null) {
                if (!isMarkerAlive) {
                    cabMarker.remove();
                } else {
                    cabMarker.isVisible();
                }
            }
        } else {
            Log.e(TAG, "No valid Google Play Services APK found.");
        }
        avlblCabToast = avlblCabToast.makeText(getApplicationContext(), getResources().getString(R.string.no_requested_cabs), Toast.LENGTH_LONG);
        avlblCabToast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 10);

        regularView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "Regular sedan";
                //String rs = "Regular sedan"; String vn = "Van"; String wa = "Wheelchair accessible";
                //type = rs = vn = wa;
                regularView.setBackgroundResource(R.drawable.rounded_button_selected);
                vanView.setBackgroundResource(R.drawable.rounded_button);
                hcView.setBackgroundResource(R.drawable.rounded_button);
                if (myMarkers.size() == 0) {
                    avlblCabToast.show();
                } else {
                    //make all types of cabs visible
                    myMarkers.add(cabMarker);
                    for (Marker m : myMarkers) {
                        m.setVisible(true);
                    }
                }
            }
        });

        vanView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "Van";
                vanView.setBackgroundResource(R.drawable.rounded_button_selected);
                regularView.setBackgroundResource(R.drawable.rounded_button);
                hcView.setBackgroundResource(R.drawable.rounded_button);
                if (myMarkers.size() != 0) {
                    for (Marker m : myMarkers) {
                        if (myMarkers.contains(m) && m.getTitle().equalsIgnoreCase("Van")) {
                            if (m.getTitle().equalsIgnoreCase("Van")) {
                                //make Vans visible
                                m.setVisible(true);
                                //driverList.remove(driverList.indexOf(driverObj));
                                //cabMarker.setVisible(true);
                                //driverList.add(driverObj);
                            } else {
                                //hide
                                m.setVisible(false);
                            }
                        } else {
                            //there are no vans
                            //avlblCabToast.show();
                            m.setVisible(false);
                        }
                    }
                } else {
                    //no cabs of all types
                    avlblCabToast.show();
                }
            }
        });

        hcView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "Wheelchair accessible";
                hcView.setBackgroundResource(R.drawable.rounded_button_selected);
                regularView.setBackgroundResource(R.drawable.rounded_button);
                vanView.setBackgroundResource(R.drawable.rounded_button);
                if (myMarkers.size() != 0) {
                    for (Marker m : myMarkers) {
                        if (myMarkers.contains(m) && m.getTitle().equalsIgnoreCase("Wheelchair accessible")) {
                            if (m.getTitle().equalsIgnoreCase("Wheelchair accessible")) {
                                //make handicap accessible visible
                                m.setVisible(true);

                                //driverList.remove(driverList.indexOf(driverObj));
                                //cabMarker.setVisible(true);
                                //driverList.add(driverObj);
                            } else {
                                //hide
                                m.setVisible(false);
                            }
                        } else {
                            //there are no handicap accessible
                            //avlblCabToast.show();
                            m.setVisible(false);
                        }
                    }
                } else {
                    //no cabs of all types
                    avlblCabToast.show();
                }
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //this all to stop autocomplete from showing places at the beginning which is unsuccessful so far
        setSelectedPlace(true);
        //acrilocation.setThreshold(50);
        acrilocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //acrilocation.setThreshold(3);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //setSelectedPlace(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        destinationText.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.search_results_list_item, R.id.tvSearchResultItem, true));
        destinationText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String destinPlace = (String) parent.getItemAtPosition(position);
                if (destinPlace != null && !"".equals(destinPlace.length()) && GlobalValidatorClass.isAddress(destinationText, true)) {
                    //convert address to LaLng object
                    Intent destinationConverter = new Intent(MapActivity.this, GeocoderDestinationIntentService.class);
                    destinationConverter.setAction("com.asmarainnovations.taxi.GeocoderDestinationIntentService");
                    destinationConverter.putExtra("destination_address", destinationText.getText().toString());
                    startService(destinationConverter);
                } else {
                    Toast.makeText(context, "Invalid Address, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() { //intialize text to speech
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.getDefault());
                } else {
                    Log.e("TexSpeech", "Error Text_to_speech");
                }
            }
        });
        //navigationdrawer specific UI design
        mdrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuList = (ListView) findViewById(R.id.MenuList);
        appImage = (ImageView) findViewById(android.R.id.home);

        TitleText = (TextView) findViewById(android.R.id.title);

        // Check device for Play Services APK. If this check succeeds, proceed with normal processing.
        // Otherwise, prompt user to get valid Play Services APK.

        if (null != map.getMyLocation()) {
            bestLocation = map.getMyLocation();
        } else {
            bestLocation = getCurrentLocation();
        }

        //send user location as soon as the user opens the app
        get_All_Available_Cabs();
        // Get the address from the geocoder class and display it on the
        // autocomplatetextview
        fetchAddressButtonHandler(acrilocation);

        //the custom layout is to be able to change the sliding panel background and text color...
        mMenuList.setAdapter(new ArrayAdapter(this, R.layout.custom_layout_for_listview, MenuTitles));
        drlistener = new ActionBarDrawerToggle(this, mdrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(getTitle());
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(getTitle());
            }
        };

        // Set the drawer toggle as the DrawerListener
        mdrawerLayout.setDrawerListener(drlistener);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); //this lines are hiding the navigation drawer action bar
        //getActionBar().setCustomView(R.layout.custon_actionbar_title);

        mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemPosition = parent.getItemAtPosition(position).toString();
                switch (itemPosition) {
                    case "Promotions":
                        Intent promotion = new Intent(MapActivity.this, Promotions.class);
                        startActivity(promotion);
                        break;
                    case "Legal":
                        Intent legal = new Intent(MapActivity.this, Legal.class);
                        startActivity(legal);
                        break;
                    case "Contact Us":
                        Intent contact = new Intent(MapActivity.this, ContactUs.class);
                        startActivity(contact);
                        break;
                    case "Misc":
                        Intent miscel = new Intent(MapActivity.this, Miscellaneous.class);
                        startActivity(miscel);
                        break;
                    case "ContactDriver":
                        Intent messagedriver = new Intent(MapActivity.this, ContactDriver.class);
                        startActivity(messagedriver);
                        break;
                }
            }
        });

        if (cabMarker == null) {
            //avlblCabToast.show();
        }

        sendlocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                destinationText.setVisibility(View.VISIBLE);
                //final double passengerlatitude = bestLocation.getLatitude();
                //final double passengerlongitude = bestLocation.getLongitude();
                Location currentLo = utility.geocodeLocation(acrilocation.getText().toString());
                final double passengerlatitude = currentLo.getLatitude();
                final double passengerlongitude = currentLo.getLongitude();
                final String requrl = remote + "request.php";

                dtoken = getClosestDriver(type, currentLo);

                PostData poda = new PostData();
                final String la = String.valueOf(passengerlatitude);
                final String lo = String.valueOf(passengerlongitude);
                poda.execute(requrl, tok, la, lo, dtoken);
            }

        });


        buttoncancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //cancel passenger's request
                String cancel = "cancelcode";

                CancelRequest canreq = new CancelRequest();
                canreq.execute(cancel, tok, dtoken);
            }
        });

        buttondestination.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String validAddress = destinationText.getText().toString();
                if (validAddress != null && !"".equals(validAddress.length()) && GlobalValidatorClass.isAddress(destinationText, true)) {
                    //convert address to LaLng object
                    Intent destinationConverter = new Intent(MapActivity.this, GeocoderDestinationIntentService.class);
                    destinationConverter.setAction("com.asmarainnovations.taxi.GeocoderDestinationIntentService");
                    destinationConverter.putExtra("destination_address", destinationText.getText().toString());
                    startService(destinationConverter);
                } else {
                    Toast.makeText(context, "Invalid Address, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //send location to populate the map with cabs oncreate and when driver signin after signing out
    private void get_All_Available_Cabs() {
        InitialPostData poda = new InitialPostData();
        if (null != bestLocation) {
            poda.execute(tok, String.valueOf(bestLocation.getLatitude()), String.valueOf(bestLocation.getLongitude()));
        }else if(null == bestLocation){
            Log.e("loacation ma411", "location is null");
        }
    }

    //anonymous class created by android studio
    private String getClosestDriver(String cabT, Location passengerlocation) {
        //Location passengerlocation = new Location("");
        //passengerlocation.setLatitude(bestLocation.getLatitude());
        //passengerlocation.setLongitude(bestLocation.getLongitude());
        float distance = 0;

        //loop through all available drivers
        for (int driv = 0; driv < driverList.size(); driv++) {
            //if there is/are cab type the passenger is looking for
            if (cabT != "Regular sedan" && cabT.equals(driverList.get(driv).cab_type)) {
                //get the distance between passenger and cab
                distance = passengerlocation.distanceTo(driverList.get(driv).l);
                //collect the distance of all cabs that match the passenger cab choice
                List<Float> alldistances = new ArrayList<Float>(); //generic type arguments must be objects, thus Float because primitive types (float) don't extend object
                alldistances.add(distance);
                //sort all the distances in a descending order
                Collections.sort(alldistances);
                for (int i = 0; i < alldistances.size(); i++) {
                    if (distance == alldistances.get(0)) {
                        //get the closest driver's token
                        return driverList.get(driv).token;
                    }
                }
            } else if (cabT == "Regular sedan") {
                distance = passengerlocation.distanceTo(driverList.get(driv).l);
                List<Float> alldistances = new ArrayList<Float>(); //generic type arguments must be objects, thus Float because primitive types (float) don't extend object
                alldistances.add(distance);
                Collections.sort(alldistances);
                for (int i = 0; i < alldistances.size(); i++) {
                    if (distance == alldistances.get(0)) {
                        return driverList.get(driv).token;
                    }
                }
            } else {
                //the requested type of cab is not available
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_requested_cabs), Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }

    public void setSelectedPlace(boolean state) {
        if (state == false) {
            return;
        }
        acrilocation.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.search_results_list_item, R.id.tvSearchResultItem, state));
        acrilocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selectedPlace = (String) parent.getItemAtPosition(position);
                acrilocation.setText(selectedPlace);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drlistener.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    //When user wants different location than their current location
    void updateMapFromMarkerLocation(boolean isDriverAvailable) {
        if(isDriverAvailable) {
            map.setOnCameraChangeListener(new OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    try {
                        LatLng newll = cameraPosition.target;
                        bestLocation.setLatitude(newll.latitude);
                        bestLocation.setLongitude(newll.longitude);
                        Location timeLocation = new Location("");
                        timeLocation.setLatitude(driverlatitude);
                        timeLocation.setLongitude(driverlongitude);
                        Intent myintent = new Intent(MapActivity.this, FetchAddressIntentService.class);
                        myintent.putExtra(Constants.RECEIVER, mAddressResultReceiver);
                        myintent.putExtra(Constants.LOCATION_DATA_EXTRA, bestLocation);
                        startService(myintent);
                        //send user location as soon as the user changes their pickup location
                        InitialPostData poda = new InitialPostData();
                        if (null != bestLocation) {
                            poda.execute(tok, String.valueOf(bestLocation.getLatitude()), String.valueOf(bestLocation.getLongitude()));
                        } else if (null == bestLocation) {
                            Log.e("location ma606", "location is null");
                        }
                        //show the estimated arrival time of driver
                        etaCounter.setText(estimatedarriaval);
                    } catch (NullPointerException nuex) {
                        nuex.printStackTrace();
                    } catch (Exception gexce) {
                        Log.e("exception ma624", gexce.toString());
                        gexce.printStackTrace();
                    }
                }
            });
        }
    }

    //Note that the type "Items" will be whatever type of object you're adding markers for so you'll
//likely want to create a List of whatever type of items you're trying to add to the map and edit this appropriately
//Your "Item" class will need at least a unique id, latitude and longitude.
    private void addDriverMarkersToMap(List<Driver> drivers, LatLng Locationll, float bearing) {
        if(this.map != null){
            //This is the current user-viewable region of the map
            LatLngBounds bounds = this.map.getProjection().getVisibleRegion().latLngBounds;

            //Loop through all the items that are available to be placed on the map
            for(Driver driver : drivers) {
                //If the item is within the the bounds of the screen
                if (bounds.contains(new LatLng(driver.getL().getLatitude(), driver.getL().getLongitude()))) {
                    //If the item isn't already being displayed
                    if (!visibleMarkers.containsKey(driver.getToken())) {
                        //Add the Marker to the Map and keep track of it with the HashMap
                        //getMarkerForItem just returns a MarkerOptions object
                        this.visibleMarkers.put(driver.getToken(), this.map.addMarker(getMarkerForItem(driver, Locationll, bearing)));
                    }

                //If the marker is off screen
                }else {
                    removeDrivers(driver);
                    //If the course was previously on screen
                    /*if(visibleMarkers.containsKey(driver.getToken())) {
                        //1. Remove the Marker from the GoogleMap
                        visibleMarkers.get(driver.getToken()).remove();

                        //2. Remove the reference to the Marker from the HashMap
                        visibleMarkers.remove(driver.getToken());
                    }*/
                }
            }
        }
    }

    private MarkerOptions getMarkerForItem(Driver dr, LatLng lctionll, float brng){
        isMarkerAlive = true;
        MarkerOptions mo = new MarkerOptions().icon((BitmapDescriptorFactory.fromResource(R.drawable.movingcab)))
                .anchor(0.5f, 0.5f) //so marker rotates around the center
                .position(lctionll)
                .rotation(brng)
                .visible(true)
                .flat(true);

        //marker.setIcon(BitmapDescriptorFactory.fromBitmap( BitmapFactory.decodeResource(getResources(), R.drawable.movingcab)));
        /*new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap( BitmapFactory.decodeResource(getResources(), R.drawable.movingcab)))
                .anchor(0.5f, 0.5f) //so marker rotates around the center
                .position(lctionll)
                .rotation(brng)
                .flat(true);*/
        return mo;
       /*mo.icon((BitmapDescriptorFactory.fromResource(R.drawable.movingcab)))
               .anchor(0.5f, 0.5f) //so marker rotates around the center
               .position(lctionll)
               .rotation(brng)
               .flat(true);
        return mo;*/
    }

    private void removeDrivers(Driver dr){
        //If the course was previously on screen
        if(null != dr && visibleMarkers.containsKey(dr.getToken())) {
            //1. Remove the Marker from the GoogleMap
            visibleMarkers.get(dr.getToken()).remove();
            //acrilocation.setText("");
            etaCounter.setText("");
            //2. Remove the reference to the Marker from the HashMap
            visibleMarkers.remove(dr.getToken());
            //3. Destroy the driver object completely
            dr.destroyDriver();
            isMarkerAlive = false;
        }
    }

    private void addDriver(Driver dri){
        isMarkerAlive = true;
        visibleMarkers.put(dri.getToken(), cabMarker);
    }

    public Location getCurrentLocation() {
        Location mGPSLocation = null;
        Location mNetworkLocation = null;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        //mLocationRequest.setSmallestDisplacement(0.1F);

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) listener);
        startReceivingLocationUpdates();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return TODO;
            utility.showToast(3000, String.valueOf(R.string.enable_location)); //I added this one
        }
        mNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (isBetterLocation(mGPSLocation, mNetworkLocation)) ;
        return mGPSLocation;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        } else if (currentBestLocation == null && location == null) {
            Toast.makeText(getApplicationContext(), "Sorry, no valid network or GPS location found. " +
                    "Please change location and try again.", Toast.LENGTH_SHORT).show();


            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    //this is to get the initial all drivers location updates when the passenger app starts
    private BroadcastReceiver initialreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(MyGcmListenerService.INITIAL_LOCATION);
            if (text != null && !text.isEmpty()) {
                String[] separated = text.split(",");
                inidriverlat = separated[0].trim();
                inidriverlon = separated[1].trim();
                Log.e("updatingLocation ma720", inidriverlat + inidriverlon);
                inidriver = separated[2].trim();
                inicab_type = separated[3].trim();
                phone = separated[4].trim();
                isConnected = separated[5].trim();
                estimatedarriaval = separated[6].trim();
                Log.e("connectcode", isConnected);
                driverlatitude = Double.parseDouble(inidriverlat);
                driverlongitude = Double.parseDouble(inidriverlon);
                Location drilo = new Location("");
                drilo.setLatitude(driverlatitude);
                drilo.setLongitude(driverlongitude);
                //make driver object
                driverObj = new Driver(drilo, inidriver, inicab_type, phone);
                driverList.add(driverObj);

                try {
                    //cabLocation.setLatitude(lati);
                    //cabLocation.setLongitude(longi);
                    LatLng updatedcabloc = new LatLng(driverlatitude, driverlongitude);
                    //draw the cab and update it's location periodically
                    draw_All_Available_Cabs(updatedcabloc, inicab_type);
                    //driverObj.makeMarker(drilo, inicab_type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    //update passenger when the driver cancelles trip
    private BroadcastReceiver cancellationreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(MyGcmListenerService.CANCELLED);
            String driver_cancelled = intent.getStringExtra(MyGcmListenerService.DRIVER_CANCELLED);
            if (text != null && !text.isEmpty()) {
                //user has cancelled so update UI
                play_notification_sound();
                acrilocation.setText("Sorry, the driver has cancelled your request.");
                etaCounter.setText("");
                for (int dr = 0; dr < driverList.size();  dr++){
                    if (driverList.get(dr).getToken().equals(driver_cancelled)) {
                        driverList.remove(dr);
                        driverList.get(dr).cMarker.remove();
                        get_All_Available_Cabs(); //remove the driver that has cancelled and start all over
                    }
                }
            }
        }
    };

    private BroadcastReceiver locationupdatesreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messagestr = intent.getStringExtra(MyGcmListenerService.LOCATION);
            double lati, longi;
            if (messagestr != null && !messagestr.isEmpty()) {
                String[] separated = messagestr.split(",");
                try {
                    lati = Double.parseDouble(separated[0].trim());
                    longi = Double.parseDouble(separated[1].trim());
                    //cabLocation.setLatitude(lati);
                    //cabLocation.setLongitude(longi);
                    LatLng updatedcabloc = new LatLng(lati, longi);
                    //draw the cab and update it's location periodically
                    drawLocationUpdatesonMap(updatedcabloc);
                    //if driver arrives or is close enough notify passenger
                    float distance = 0;
                    Location cablocation = new Location("");
                    cablocation.setLatitude(updatedcabloc.latitude);
                    cablocation.setLongitude(updatedcabloc.longitude);
                    distance = bestLocation.distanceTo(cablocation) * (float) 0.000621371192; //convert from meter to miles
                    if (distance <= 0.00621371) {    //if cab is 10 meters or closer then notify passenger
                        notifyCabArrival();
                    }
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        }
    };

    //this receives the destination latlng object from the converter service and sends it to driver
    private BroadcastReceiver latLngReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String latlng = intent.getStringExtra(GeocoderDestinationIntentService.LTLNGTO_SEND);
            String desturl = remote + "destination.php";
            if (latlng != null) {
                String[] coordinate = latlng.split(",");
                double lat = Double.parseDouble(coordinate[0]);
                double lon = Double.parseDouble(coordinate[1]);
                PostData senddestination = new PostData();
                senddestination.execute(desturl, tok, String.valueOf(lat), String.valueOf(lon), dtoken);
            }
        }
    };

    //this receives the driver unavailable/available code
    private BroadcastReceiver unavailableCabReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String unavailabledriver = intent.getStringExtra(MyGcmListenerService.DRIVER_UNAVAILABLE);
            if (!unavailabledriver.equals(null)) {
                String[] isAvlbl = unavailabledriver.split(",");
                String drvr = isAvlbl[0];
                String availability = isAvlbl[1];
                Driver driver = new Driver(drvr);
                if(availability.equals("unavailable")){
                    //stop location updates from the driver that is unavailable and remove
                    //removeDrivers(driver);
                    //start over and look for closest driver
                    for (int dr = 0; dr < driverList.size();  dr++){
                        if (driverList.get(dr).getToken().equals(drvr) && driverList.get(dr).cMarker == cabMarker && cabMarker != null) {
                            driverList.remove(dr);
                            etaCounter.setText("");
                            cabMarker.remove();
                            updateMapFromMarkerLocation(false);
                            get_All_Available_Cabs(); //remove the driver that has cancelled and start all over
                            //this get all cabs method should make sure the driver availability is true
                        }else if(cabMarker == null){
                            Log.e("markerispossiblynull", "markernull");
                        }
                    }
                } else {
                    driver.setDriver(drvr);
                    addDriver(driver);
                    get_All_Available_Cabs();
                }
            }
        }
    };

    private void destroyDriver(String stringDriver){
        if(driverObj.token == stringDriver && driverObj.cMarker == cabMarker)
            cabMarker.remove();
            driverObj.destroyDriver();
    }

    //All available cabs in their immediate areas to show to any passenger when they open their app
    private void draw_All_Available_Cabs(LatLng initialLocation, String typeTitle) {
        Location inidirection = new Location("");
        inidirection.setLatitude(initialLocation.latitude);
        inidirection.setLongitude(initialLocation.longitude);
        if (locationManager.getProvider(LocationManager.GPS_PROVIDER).supportsBearing()) {
            Location older = getCurrentLocation();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location calculatedLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //this list is to get the earliest possible location to animate bearing of the cab
            ArrayList<Location> updts = new ArrayList<>();
            updts.add(older);
            updts.add(calculatedLoc);

            Collections.sort(updts, new Comparator<Location>() {
                @Override
                public int compare(Location lhs, Location rhs) {
                    return lhs.getClass().getName().compareTo(rhs.getClass().getName());
                }
            });

            float bearing = updts.get(0).bearingTo(inidirection);
            /*if(cabMarker != null && isConnected != "connected"){
                cabMarker.remove();
                driverObj.destroyDriver();
            }*/

            addDriverMarkersToMap(driverList, initialLocation, bearing);
            /*cabMarker = map.addMarker(new MarkerOptions()
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.movingcab))
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) this draws red location icon
                    .anchor(0.5f, 0.5f) //so marker rotates around the center
                    .position(initialLocation)
                    .rotation(bearing)
                    .flat(true));*/
            //show the estimated arrival time of driver
            etaCounter.setText(estimatedarriaval);
            //setTime(inidirection, calculatedLoc);
            cabMarker.setTitle(typeTitle);
            myMarkers.add(cabMarker);
            //animateMarker(inidirection, cabMarker);
            LatLngInterpolator interpolator = new LatLngInterpolator.LinearFixed();
            animateMarkerToGB(cabMarker, initialLocation, interpolator);
            //consider deleting this in the future
            /*if(isConnected != "connected" && cabMarker != null) {
                cabMarker.remove();
                driverObj.destroyDriver();
            }*/
        }
    }

    private void drawLocationUpdatesonMap(LatLng newlocatioin) {
        Location direction = new Location("test");
        direction.setLatitude(newlocatioin.latitude);
        direction.setLongitude(newlocatioin.longitude);
        if (locationManager.getProvider(LocationManager.GPS_PROVIDER).supportsBearing()) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location calculatedLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            float bearing = calculatedLoc.bearingTo(direction);
            Toast.makeText(getApplicationContext(), String.valueOf(bearing), Toast.LENGTH_SHORT).show();
            if (cabMarker != null) {
                cabMarker.remove();
            } else {
                cabMarker = map.addMarker(new MarkerOptions()
                        .visible(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.movingcab))
                                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) this draws red location icon
                        .anchor(0.5f, 0.5f) //so marker rotates around the center
                        .position(newlocatioin)
                        .rotation(bearing)
                        .flat(true));
                long cab_eta = direction.getTime(); //time till cab arrival
                etaCounter.setText(String.valueOf(cab_eta));
                //animateMarker(direction, cabMarker);
                LatLngInterpolator lngitpltr = new LatLngInterpolator.LinearFixed();
                LatLng intpltrltlng = new LatLng(direction.getLatitude(), direction.getLongitude());
                animateMarkerToGB(cabMarker, intpltrltlng, lngitpltr);
            }
        }
    }

    //text to speech to notify passenger that their cab has arrived
    private void notifyCabArrival() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(String.valueOf(R.string.cab_arrived));
        } else {
            ttsUnder20(String.valueOf(R.string.cab_arrived));
        }
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    //notification sound played to notify cancellation of trip.....
    private void play_notification_sound() {
        SoundPool soup;
        int spSoundId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soup = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(attributes)
                    .build();
            spSoundId = soup.load(MapActivity.this, R.raw.notifyme, 1);
        }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            soup = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
            spSoundId = soup.load(MapActivity.this, R.raw.notifyme, 1);
        }

    }

	public String getRegistrationToken() {
        //retrieve token from sharedpreferences
        SharedPreferences prefs = getSharedPreferences(QuickstartPreferences.MY_PREFS_NAME, MODE_PRIVATE);
            String retrievedtoken = prefs.getString("savedtoken", "No name defined");//"No name defined" is the default value.
            return retrievedtoken;
    }

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
                if (dialog != null) {
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            if (ConnectionResult.SERVICE_INVALID == resultCode) finish();
                        }
                    });
                    return false;
                }
            }
            Toast.makeText(context, getString(R.string.update_googleplay), Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
	}

	@Override
	public void onMapReady(final GoogleMap gm) {

	}

	public void fetchAddressButtonHandler(View view) {
		// Only start the service to fetch the address if GoogleApiClient is
		// connected.
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && bestLocation != null) {
                    startGeocoderService(bestLocation);
		}
		// If GoogleApiClient isn't connected, process the user's request by
		// setting mAddressRequested to true. Later, when GoogleApiClient
		// connects,
		// launch the service to fetch the address. As far as the user is
		// concerned, pressing the Fetch Address button
		// immediately kicks off the process of getting the address.
		mAddressRequested = true;
	}


	protected void startGeocoderService(final Location lo) {
        try {
            LatLng lalng = new LatLng(lo.getLatitude(), lo.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lalng, 15));
            //LatLng newloatlng = map.getCameraPosition().target;
            //lo.setLatitude(newloatlng.latitude);
            //lo.setLongitude(newloatlng.longitude);

                Intent myintent = new Intent(MapActivity.this, FetchAddressIntentService.class);
                myintent.putExtra(Constants.RECEIVER, mAddressResultReceiver);
                myintent.putExtra(Constants.LOCATION_DATA_EXTRA, lo);
                startService(myintent);

            }catch (NullPointerException nullexception){
                nullexception.printStackTrace();
            }
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
        LocalBroadcastManager.getInstance(this).unregisterReceiver(initialreceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationupdatesreceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(cancellationreceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(latLngReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(unavailableCabReceiver);
        stopLocationUpdates();
        if(tts !=null){  //close the text to speech
            tts.stop();
            tts.shutdown();
        }
        isLocationUpdating = false;
        super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (!checkPlayServices()){
			Toast.makeText(getApplicationContext(), "No valid google play services found!! Download it from google play.", Toast.LENGTH_SHORT).show();
		}
        isLocationUpdating = true;
        LocalBroadcastManager.getInstance(this).registerReceiver((initialreceiver),
                new IntentFilter(MyGcmListenerService.INITIAL_COORDINATES));
        LocalBroadcastManager.getInstance(this).registerReceiver((locationupdatesreceiver),
                new IntentFilter(MyGcmListenerService.UPDATE_COORDINATES));
        LocalBroadcastManager.getInstance(this).registerReceiver((cancellationreceiver),
                new IntentFilter(MyGcmListenerService.CANCEL));
        LocalBroadcastManager.getInstance(this).registerReceiver((latLngReceiver),
                new IntentFilter(GeocoderDestinationIntentService.destinationltlngObject));
        LocalBroadcastManager.getInstance(this).registerReceiver((unavailableCabReceiver),
                new IntentFilter(MyGcmListenerService.UNAVAILABLE));
        startLocationUpdates();
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
	}

    protected void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);  //throws npe
        } else {
            mGoogleApiClient.connect();
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        } else {
            mGoogleApiClient.connect();
        }
    }

	@Override
	public void onBackPressed() {
        //close navigation drawer
        if (this.mdrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mdrawerLayout.closeDrawer(GravityCompat.START);
        }else {
            //this will close and exit the app
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);
        }
	}

	//Receive location updates
	private void startReceivingLocationUpdates() {
        /*if(locationManager == null) {
            try {
                locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 0F,
                        (android.location.LocationListener) listener);
                //if (listener != null) listener.showGpsOnScreenIndicator(false);
            } catch (SecurityException ex) {
                Log.e(TAG + "ma1131", "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.e(TAG + "ma1133", "provider does not exist " + ex.getMessage());
            }
        }*/
        if (locationManager == null) {
			locationManager = (android.location.LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		}

		if (locationManager != null) {
			try {locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000, 0F,
					(android.location.LocationListener) listener);
                isLocationUpdating = true;
			}
			catch (SecurityException ex)
			{
				Log.e(TAG+"ma1269", "fail to request location update, ignore", ex);
			}

			catch (IllegalArgumentException ex)
			{
				Log.d(TAG + "ma1152", "provider does not exist " + ex.getMessage());
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
        /*LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 15);
        map.animateCamera(cameraUpdate);
        centeredMap(current)*/;
	}

	@Override
	public void onConnectionFailed(ConnectionResult cf) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), cf.toString(),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnected(Bundle bundl) {
		// TODO Auto-generated method stub
		// Location mLastLocation;
		// Gets the best and most recent location currently available,
		// which may be null in rare cases when a location is not available.

        bestLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (bestLocation != null) {
            /*LatLng current = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 15);
            map.animateCamera(cameraUpdate);
            centeredMap(current);*/

			map.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker mymarker) {
					// Display pricing, cab number and other details here
                    //animateMarker(mymarker, myLocation, mAddressRequested);
					return false;
				}
			});

			// Determine whether a Geocoder is available.
			if (!Geocoder.isPresent()) {
				Toast.makeText(this, R.string.no_geocoder_available,
						Toast.LENGTH_LONG).show();
				return;
			}

			if (mAddressRequested) {
				startGeocoderService(bestLocation);
			}
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		// TODO Auto-generated method stub

	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drlistener.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drlistener.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {

    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = new ArrayList();
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:"+Locale.getDefault().getCountry()); //replaced us by Locale.getDefault() so it pulls local addresses of user
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException mue) {
            //Error processing Places API URL
            Log.e("TAG", mue.toString());
            mue.printStackTrace();
            return resultList;
        } catch (IOException ioe) {
            //Error cannecting to Places API
            Log.e("TAG", ioe.toString());
            ioe.printStackTrace();
            return resultList;
        } catch (Exception ex){
            ex.printStackTrace();
            Log.e("TAG", ex.toString());
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException je) {
            //cannot process JSON results
            Log.e("TAG", je.toString());
            je.printStackTrace();
        } catch (Exception exe){
            Log.e("TAG", exe.toString());
            exe.printStackTrace();
        }
        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {

        private ArrayList resultList;
        //private ArrayList<HashMap<String, Place>> results = new ArrayList<>();

        public GooglePlacesAutocompleteAdapter(Context context, int list, int textViewResourceId, boolean condition) {
            super(context, list, textViewResourceId);
            if(false == condition){
                return;
            }
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index).toString();
        }

        //@Override
        //public HashMap<String, Place>  getItem(int index) {return results.get(index);}

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());
                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    //this sends passenger token as soon as the app is opened
    class InitialPostData extends AsyncTask<String, Void, String> {

        boolean failure = false;

        @Override
        protected String doInBackground(String... args) {
            try {
                try {
                    URL url;
                    HttpURLConnection urlConn;
                    url = new URL(remote+"index.php");
                    urlConn = (HttpURLConnection) url.openConnection();
                    //System.setProperty("http.keepAlive", "true");
                    //final int responseCode = urlConn.getResponseCode();  //to check http status
                    //if (responseCode == HttpURLConnection.HTTP_OK) {
                        //Log.e("status", String.valueOf(responseCode));
                    //urlConn.setDoInput(true);  //used for get request
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches(false);
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    urlConn.setRequestMethod("HEAD");  //this will use the same connection the entire time
                    urlConn.setRequestMethod("POST");
                    //urlConn.connect();

                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("pastoken", args[0]);
                    json.put("paslat", args[1]);
                    json.put("paslon", args[2]);
                    String postData = json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    Log.e("INItialJSON", "SENT" + postData);
                    os.close();
                    //urlConn.disconnect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String msg = "";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        msg += line;
                    }
                    Log.i("msg=", "" + msg);

                    //} else if (responseCode == HttpURLConnection.HTTP_BAD_GATEWAY) {
                        //Log.e("status", String.valueOf(responseCode));
                    //}

                } catch (JSONException jsex){
                    jsex.printStackTrace();
                } catch (MalformedURLException muex) {
                    // TODO Auto-generated catch block
                    muex.printStackTrace();
                } catch (IOException ioex){
                    ioex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR", "There is error in this code");
            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

    }

    //async class to make request.
	class PostData extends AsyncTask<String, Void, String> {

		boolean failure = false;

		@Override
		protected String doInBackground(String... args) {
			try {
	        	try {
	        		URL url;
	        		HttpURLConnection urlConn;
	    			url = new URL (args[0]);
	        		urlConn = (HttpURLConnection)url.openConnection();
                    System.setProperty("http.keepAlive", "true");
//                    final int responseCode = urlConn.getResponseCode();  //to check http status
//                    if (responseCode == HttpURLConnection.HTTP_OK) {
//                        Log.e("status", String.valueOf(responseCode));
	    			urlConn.setDoInput (true);
	    			urlConn.setDoOutput (true);
	    			urlConn.setUseCaches (false);
                    urlConn.setInstanceFollowRedirects(false);
	    			urlConn.setRequestProperty("Content-Type","application/json");
	    			urlConn.setRequestProperty("Accept", "application/json");
	    			urlConn.setRequestMethod("POST");
	    			urlConn.connect();
	    			//get google account
	    			AccountManager am = AccountManager.get(getBaseContext()); // "this" references the current Context
	    		    Account[] accounts = am.getAccountsByType("com.google");

	    			//Create JSONObject here
	    			JSONObject json = new JSONObject();
	    			json.put("pastoken", String.valueOf(args[1]));
                    json.put("paslat", String.valueOf(args[2]));
                    json.put("paslon", String.valueOf(args[3]));
                    json.put("dritoken", String.valueOf(args[4]));
	    			json.put("Google_account", accounts[0].name);

	    			String postData=json.toString();

	    		    // Send POST output.
	    			OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    Log.i("NOTIFICATION", "Data Sent");
                    os.flush();
                    os.close();

	    		    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
	    		    String msg="";
	    		    String line = "";
	    		    while ((line = reader.readLine()) != null) {
	    		    	msg += line; }
	    		    Log.i("msg=",""+msg);

//                    } else if (responseCode == HttpURLConnection.HTTP_BAD_GATEWAY) {
//                        Log.e("status", String.valueOf(responseCode));
//                    }
	    		} catch (MalformedURLException muex) {
	    			// TODO Auto-generated catch block
	    			muex.printStackTrace();
	    		} catch (IOException ioex){
	    			ioex.printStackTrace();
	    		}
	        } catch (Exception e) {
	            e.printStackTrace();
	            Log.e("ERROR", "There is error in this code line ma 1610");

	        }
			return null;

		}

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    //async class to send the cancel code.
    class CancelRequest extends AsyncTask<String, Void, String> {

        boolean failure = false;

        @Override
        protected String doInBackground(String... args) {
            try {
                try {
                    URL updateurl;
                    HttpURLConnection urlConn;
                    updateurl = new URL(remote + "cancelbypassenger.php");
                    urlConn = (HttpURLConnection) updateurl.openConnection();
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches(false);
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    urlConn.setRequestMethod("POST");
                    urlConn.connect();

                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("cancelReq", args[0]);
                    json.put("passengtok", args[1]);
                    json.put("dritok", args[2]);

                    String postData = json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String msg = "";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        msg += line;
                    }
                    Log.i("msg=", "" + msg);
                } catch (NullPointerException npe){
                    npe.printStackTrace();
                    Log.e("nullexcep ma1558", npe.toString());
                } catch (MalformedURLException muex) {
                    // TODO Auto-generated catch block
                    muex.printStackTrace();
                } catch (IOException ioex){
                    ioex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR", "Problem sending cancel cancelreq line 1514");

            }
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

	@SuppressLint("ParcelCreator") //delete this line and solve the CREATOR field shit before production
    public class AddressResultReceiver extends ResultReceiver {

		ResultReceiver mReceiver;

		public AddressResultReceiver(Handler handler) {
			super(handler);
		}

		public void setReceiver(ResultReceiver receiver) {
			mReceiver = receiver;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			// mReceiver.onReceiveResult(resultCode, resultData);
			if (resultCode == Constants.FAILURE_RESULT) {
				// Toast.makeText(getApplicationContext(),
				// "ADDRESS LOOKUP UNSUCCESSFUL", Toast.LENGTH_SHORT).show();
			} else if (resultCode == Constants.SUCCESS_RESULT) {
				// Display the address string
				// or an error message sent from the intent service
				final String mAddressOutput = resultData
						.getString(Constants.RESULT_DATA_KEY);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mAddressOutput != null)
							MapActivity.showaddress(mAddressOutput);
					}
				});
				// Show a toast message if an address was found.
				if (resultCode == Constants.SUCCESS_RESULT) {
					// Toast.makeText(getApplicationContext(),
					// "SUCCESS",Toast.LENGTH_SHORT).show();
				}
			}
		}

	}

	protected static void showaddress(String mAddressOutput) {
		// TODO Auto-generated method stub
		acrilocation.setText(String.valueOf(mAddressOutput));
	}

    //I think this class needs to be deleted. Comment first and delete
	public class TouchableWrapper extends FrameLayout {

		public TouchableWrapper(Context context) {
			super(context);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {

			switch (ev.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMapIsTouched = true;
					break;
				case MotionEvent.ACTION_UP:
					mMapIsTouched = false;
					break;
			}

			return super.dispatchTouchEvent(ev);

		}

	}

	/*public class MyInstanceIDListenerService extends InstanceIDListenerService{

		public String getToken() {
			InstanceID instanceID = InstanceID.getInstance(this);
			String token = null;
			try {
				token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
						GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
			return token;
		}
		@Override
		public void onTokenRefresh() {
			// Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
			Intent intent = new Intent(this, RegistrationIntentService.class);
			startService(intent);

		}
	}*/

    //this class needs to be static since it is nested class
	public static class MyGcmListenerService extends GcmListenerService {
        public static final String INITIAL_COORDINATES = "com.asmarainnovations.taxi.initialcoordinates";
		public static final String UPDATE_COORDINATES = "com.asmarainnovations.taxi.newcoordinate";
        public static final String CANCEL = "com.asmarainnovations.taxi.cancel";
        public static final String UNAVAILABLE = "com.asmarainnovations.taxi.unavailable";
        public static final String INITIAL_LOCATION = "";
        public static final String LOCATION = "";
        public static final String CANCELLED = "";
        public static final String DRIVER_CANCELLED = "";
        public static final String DRIVER_UNAVAILABLE = "";
        public static final String AVAILABILITY = "";
        LocalBroadcastManager coordintesupdater, cancelNotifier, initialdrawer, unavailableNotifier;

	    public MyGcmListenerService() { super(); }

        @Override
        public void onCreate() {
            //this will broadcast downstream messages to update the UI
            initialdrawer = LocalBroadcastManager.getInstance(this);
            coordintesupdater = LocalBroadcastManager.getInstance(this);
            cancelNotifier = LocalBroadcastManager.getInstance(this);
            unavailableNotifier = LocalBroadcastManager.getInstance(this);
            super.onCreate();
        }

        @Override
		public void onMessageSent(String msgId) {
			super.onMessageSent(msgId);
		}

		@Override
		public void onDeletedMessages() {
			super.onDeletedMessages();
		}

		@Override
		public void onSendError(String msgId, String error) {
			Log.e("GCMsendError", error);
            super.onSendError(msgId, error);
		}

		@Override
		public void onMessageReceived(String from, Bundle data) {
            if (data != null && !data.isEmpty()) {
                //to draw initial driver location on passenger map
                if(data.containsKey("inilati") && data.containsKey("inilongi") && data.containsKey("inidri")
                        && data.containsKey("cabtype") && data.containsKey("phone") && data.containsKey("isConnected")
                        && data.containsKey("eta")) {
                    String lati = data.getString("inilati");
                    String longi = data.getString("inilongi");
                    String driver = data.getString("inidri");
                    String cab_type = data.getString("cabtype");
                    String phoneNumber = data.getString("phone");
                    String connect = data.getString("isConnected");
                    String earrival = data.getString("eta");
                    Intent iniIntent = new Intent();
                    iniIntent.putExtra(INITIAL_LOCATION, lati + "," + longi + "," + driver + "," + cab_type + "," + phoneNumber
                            + "," + connect + "," + earrival);
                    iniIntent.setAction(INITIAL_COORDINATES); //should match the receiver intent filter at the registering
                    initialdrawer.sendBroadcast(iniIntent);
                }//to draw driver location on passenger map
                else if(data.containsKey("updlati") && data.containsKey("updlongi") && data.containsKey("upddri")) {
                    String lati = data.getString("updlati");
                    String longi = data.getString("updlongi");
                    Intent updIntent = new Intent();
                    updIntent.putExtra(LOCATION, lati + "," + longi);
                    updIntent.setAction(UPDATE_COORDINATES); //should match the receiver intent filter at the registering
                    coordintesupdater.sendBroadcast(updIntent);

                }else if(data.containsKey("cancelcode") && data.containsKey("dri")){ //to cancel request based on driver's request
                    String cancelco = data.getString("cancelcode");
                    String cancelleddriver = data.getString("dri");
                    Intent cancelInt = new Intent();
                    cancelInt.putExtra(CANCELLED, cancelco);
                    cancelInt.putExtra(DRIVER_CANCELLED, cancelleddriver);
                    cancelInt.setAction(CANCEL);
                    cancelNotifier.sendBroadcast(cancelInt);
                } else if(data.containsKey("driver") && data.containsKey("isAvailable")){
                    String drv = data.getString("driver");
                    String availability = data.getString("isAvailable");
                    Intent unavailable = new Intent();
                    unavailable.putExtra(DRIVER_UNAVAILABLE, drv + "," + availability);
                    unavailable.setAction(UNAVAILABLE); //should match the receiver intent filter at the registering
                    unavailableNotifier.sendBroadcast(unavailable);
                }
            } else {
				Log.e("Received", "empty message ma 1872");
			}
		}
	}
}

