package com.wsuproj5.accuratedrivingtest;

import java.util.Set;


import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;


public class UserMenu extends ActionBarActivity {	  
	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_user_menu);
	   }
	   
	   public void toBeginEvaluation(View view) {
		   //TODO: Change this intent to go to the pre-evaluation screen
		   Intent beginEvaluation = new Intent(UserMenu.this,DuringEvaluation.class);                               
		   startActivity(beginEvaluation);  
	   }
	   
	   public void toAddRoute(View view) {
		   Intent addRoute = new Intent(UserMenu.this, AddRoute.class);
		   startActivity(addRoute);
	   }
	}
