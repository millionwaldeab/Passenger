package com.asmarainnovations.taxi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Million on 8/3/2016.
 */
public class VerificationCode extends Activity {
    EditText codeedittext;
    Button verificationSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verifcation_file);
        codeedittext = (EditText) findViewById(R.id.etVerificationCode);
        verificationSender = (Button) findViewById(R.id.bsend_verification_code);
        verificationSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeedittext.getText().toString().trim();
                RiderLogin loginActivity = new RiderLogin();
                if (code != null && loginActivity.codeString != null && code == loginActivity.codeString){
                    //save credentials in shared pref
                    SharedPreferences sharedPref = getSharedPreferences("taxi", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    //this stores a string to tell that the user is already signed up
                    editor.putString("register","true");
                    editor.apply();
                    //the phone verification passed
                    Intent passed = new Intent(VerificationCode.this, RiderLogin.class);
                    startActivity(passed);
                } else {
                    codeedittext.setError("Oops invalid code, please enter valid code");
                }
            }
        });
    }
}
