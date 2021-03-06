package com.wsuproj5.accuratedrivingtest;

import java.util.ArrayList;
import java.util.List;

import com.wsuproj5.accuratedrivingtest.addroute.*;
import com.wsuproj5.accuratedrivingtest.testing.CreateTest;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


public class UserMenu extends ActionBarActivity {	  
	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_user_menu);
	      fillRoutes();
	   }
	   
	   @Override
	   public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	   }
	   
	   private void fillRoutes() {
			SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
			List<String> spinnerArray =  new ArrayList<String>();
			for (int i = 0; i < 1000; i++) {
				String route = prefs.getString("route" + i, null);
				if (route == null || route.equals("removed")) {
					break;
				}
				spinnerArray.add("route " + i);
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			    this, android.R.layout.simple_spinner_item, spinnerArray);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			Spinner sItems = (Spinner) findViewById(R.id.route_spinner);
			sItems.setAdapter(adapter);
		}
	   
	   public void toBeginEvaluation(View view) {
			//TODO: Change this intent to go to the pre-evaluation screen 
		   Intent beginEvaluation = new Intent(UserMenu.this,BeginEvaluation.class);
		   startActivity(beginEvaluation);
	   }
	   public void toAddRoute(View view) {
		   Intent addRoute = new Intent(UserMenu.this, AddRoute.class);
		   startActivity(addRoute);
	   }
	   
	   public void toCreateTest(View view) {
		   Intent createTest = new Intent(UserMenu.this, CreateTest.class);
		   startActivity(createTest);
	   }
	   
	   public void toELMmain(View view) {
		   Intent obdMain = new Intent(UserMenu.this, MainElmActivity.class);
		   startActivity(obdMain);
	   }
	   public void toReviewEvaluation(View view){
		     startActivity(new Intent(UserMenu.this,ReviewEvaluation.class));   

	   }
	}
