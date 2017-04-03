package com.asmarainnovations.taxi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Million on 3/3/2016.
 */
public class ContactDriver extends Activity {
    MapActivity mpacti;
    UtilityClass utility;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_driver);
        mpacti = new MapActivity();
        utility = new UtilityClass(getApplicationContext());
        final EditText message_to_driver = (EditText) findViewById(R.id.etMessage);
        Button sendMessage = (Button) findViewById(R.id.bsendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GlobalValidatorClass.isAlphaNumeric(message_to_driver, true)) {
                    utility.sendTextMessage(mpacti.phone, message_to_driver.getText().toString());
                }else{
                    Toast.makeText(getApplicationContext(), "number not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
