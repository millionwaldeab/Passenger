package com.asmarainnovations.taxi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 9/8/2015.
 */
public class RiderLogin extends Activity{
    EditText name, tele;
    Button signup;
    String namestring = "test name", telestring = "test tele", tokenstring = "test token", codeString;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    int number;
    RegistrationResponseReceiver receiver;
    public static Context mycontext;
    final String local = Configuration_Data.local;
    final String remote = Configuration_Data.remote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        name = (EditText) findViewById(R.id.etname);
        tele = (EditText) findViewById(R.id.ettel);
        signup = (Button) findViewById(R.id.bsignup);
        TextView terms = (TextView) findViewById(R.id.tvterms);
        receiver = new RegistrationResponseReceiver(); //Instantiate the receiver first
        mycontext = getApplicationContext();
        sharedPref = getSharedPreferences("taxi", MODE_PRIVATE);
        editor = sharedPref.edit();
        //get registration id
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

        String getStatus = sharedPref.getString("register", "nil");
        //user has previously signed up
        if(getStatus.equals("true")){
            //Open this Home activity
            Intent passed = new Intent(this, MainActivity.class);
            startActivity(passed);
        }else{
            sendVerificationCode();
            Intent enterCode = new Intent(RiderLogin.this, VerificationCode.class);
            startActivity(enterCode);
        }

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent termsint = new Intent(RiderLogin.this, Terms_and_Conditions.class);
                startActivity(termsint);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(number == 0) {
            //Open the login activity and set this so that next it value is 1 then this condition will be false.
            SharedPreferences.Editor prefEditor = sharedPref.edit();
            prefEditor.putInt("isLogged",1);
            prefEditor.commit();
        } else {
            //Open this Home activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }*/

        IntentFilter filter = new IntentFilter(RegistrationResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //this will send a secret 5 digit code to the phone number provided
    private void sendVerificationCode() {
        int min = 00000, max = 99999;
        SecureRandom random = new SecureRandom();
        int generatedNumber = random.nextInt(max - min + 1) + min;
        codeString = String.valueOf(generatedNumber);
        String message = "This is your secret code, please enter it in the app now" +  codeString;
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(tele.getText().toString().trim(), null, message, null, null);
        } catch (IllegalArgumentException illexc) {
            illexc.printStackTrace();
            Log.e("Exception rl133", illexc.toString());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public class RegistrationResponseReceiver extends BroadcastReceiver {
        static final String ACTION_RESP = "com.asmarainnovations.taxi.intent.action.MESSAGE_PROCESSED";//this should match Intentservice action
        public RegistrationResponseReceiver(){ }
        GlobalValidatorClass validator = new GlobalValidatorClass();
        @Override
        public void onReceive(Context context, Intent intent) {
            final String text = intent.getStringExtra(RegistrationIntentService.PARAM_OUT);

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((text != null) && (validator.isPersonName(name, true) && validator.isPhoneNumber(tele, true))){
                        namestring = name.getText().toString();
                        telestring = tele.getText().toString();

                        SendLoginCredentials sendcreds = new SendLoginCredentials();
                        sendcreds.execute(text, namestring, telestring);
                        //this stores a string to tell that the user is already signed up
                        editor.putString("register","true");
                        editor.commit();
                            //Open this Home activity
                            Intent intent = new Intent(RiderLogin.this, MainActivity.class);
                            startActivity(intent);

                    }else {
                        Log.i("credentials!!!", "caution: empty credentials");
                        Toast.makeText(getApplicationContext(), "complete all boxes!!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            });

            //this is code from when this class was in MapActivity
			/*regbutton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        mLocationRequest = new LocationRequest();
                        mLocationRequest.setInterval(10);
                        mLocationRequest.setFastestInterval(10);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        //mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                        //mLocationRequest.setSmallestDisplacement(0.1F);

                        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) listener);
                        startReceivingLocationUpdates();


                        Location mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double latitude = mLastLocation.getLatitude();
                        double longitude = mLastLocation.getLongitude();
                        PostData poda = new PostData();
                        String la = String.valueOf(latitude);
                        String lo = String.valueOf(longitude);
                        //String[] sa = {regid, la, lo};
                        poda.execute(text, la, lo);
                    } catch (NullPointerException npo) {
                        npo.printStackTrace();
                        Log.i("crap -----", "location is null in my opinion");
                    }
                }
            });*/
        }

    }

    //async class to send credentials.
    class SendLoginCredentials extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                try {
                    URL url;
                    HttpURLConnection urlConn;
                    url = new URL (remote+"passenger_register.php");
                    urlConn = (HttpURLConnection)url.openConnection();
                    urlConn.setDoInput (true);
                    urlConn.setDoOutput (true);
                    urlConn.setUseCaches (false);
                    urlConn.setRequestProperty("Content-Type","application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    //urlConn.setChunkedStreamingMode(0);
                    urlConn.setRequestMethod("POST");
                    urlConn.connect();
                    //get google account
                    AccountManager am = AccountManager.get(getBaseContext()); // "this" references the current Context
                    Account[] accounts = am.getAccountsByType("com.google");

                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("pastoken", String.valueOf(args[0]));
                    json.put("name", String.valueOf(args[1]));
                    json.put("tele", String.valueOf(args[2]));
                    json.put("Google_account", accounts[0].name);

                    String postData=json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    Log.i("NOTIFICATION", "Data Sent");
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String msg="";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        msg += line; }
                    Log.i("msg=",""+msg);
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
    }

    public static class MyInstanceIDListenerService extends InstanceIDListenerService {
        public String getToken() {
            InstanceID instanceID = InstanceID.getInstance(mycontext);
            String token = "";
            try {
                token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
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
    }
}
