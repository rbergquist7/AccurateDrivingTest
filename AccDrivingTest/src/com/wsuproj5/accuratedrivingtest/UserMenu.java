package com.wsuproj5.accuratedrivingtest;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.content.Intent;
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
	   
	   public void toCreateTest(View view) {
		   Intent createTest = new Intent(UserMenu.this, CreateTest.class);
		   startActivity(createTest);
	   }
	}
