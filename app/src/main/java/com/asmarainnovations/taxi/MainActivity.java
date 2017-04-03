package com.asmarainnovations.taxi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
public class MainActivity extends MapActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {

				Intent i = new Intent(MainActivity.this, MapActivity.class);
				startActivity(i);
			}
		}, 10000);
	}
}
