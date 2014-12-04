package com.wsuproj5.accuratedrivingtest;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class SplashScreen extends ActionBarActivity {
	OnTouchListener listener = new OnTouchListener() {      
        @SuppressLint("ClickableViewAccessibility") @Override
        public boolean onTouch(View v, MotionEvent event) {
        	toUserMenu(v);
        	return true;
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash__screen);
		
		TextView splashScreen = (TextView) findViewById(R.id.splash_screen);
		splashScreen.setOnTouchListener(listener);
		
		new CountDownTimer(5000, 1000) {

		     public void onTick(long millisUntilFinished) {
		     }

		     public void onFinish() {
		    	 Intent userMenu = new Intent(SplashScreen.this,UserMenu.class);                               
		         startActivity(userMenu);
		     }
		  }.start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void toUserMenu(View view) {
		Intent userMenu = new Intent(SplashScreen.this,UserMenu.class);                               
        startActivity(userMenu);
	}
}
