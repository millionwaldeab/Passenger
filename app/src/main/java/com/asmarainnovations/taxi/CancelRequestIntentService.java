package com.asmarainnovations.taxi;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 9/9/2015.
 */
public class CancelRequestIntentService extends IntentService {


    public CancelRequestIntentService() { super("CancelRequestIntentService"); }
    public CancelRequestIntentService(String name) {
        super(name);
    }
    final String local = Configuration_Data.local;
    final String remote = Configuration_Data.remote;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String code = intent.getStringExtra("cancelcode");
            String drtoken = intent.getStringExtra("drid");
            sendToServer(code, drtoken);
        }catch (Exception exception){
            exception.printStackTrace();
            Log.i("received exception", exception.toString());
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }



    //Receive location updates
    private void startReceivingLocationUpdates() {

    }

    private void sendToServer(String cancelled, String usertoken) {
        // send to server in background thread. you might want to start AsyncTask here
        try {
            try {
                URL updateurl;
                HttpURLConnection urlConn;
                updateurl = new URL (remote+"cancelbypassenger.php");
                urlConn = (HttpURLConnection)updateurl.openConnection();
                urlConn.setDoInput (true);
                urlConn.setDoOutput (true);
                urlConn.setUseCaches (false);
                urlConn.setRequestProperty("Content-Type","application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                //urlConn.setChunkedStreamingMode(0);
                urlConn.setRequestMethod("POST");
                urlConn.connect();

                //Create JSONObject here
                JSONObject json = new JSONObject();
                json.put("cancelReq", cancelled);
                json.put("driver", usertoken);

                String postData=json.toString();

                // Send POST output.
                OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                os.write(postData);
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
            Log.e("ERROR", "Problem sending cancel cancelreq line 106");

        }
    }
}
