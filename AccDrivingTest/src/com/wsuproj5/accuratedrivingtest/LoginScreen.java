package com.wsuproj5.accuratedrivingtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class LoginScreen extends ActionBarActivity{
	protected void onCreate(Bundle savedInstanceState) {
		
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_login_screen);
	      
	      
	   }
	
	public void toUserMenu(View view) {
		Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
        startActivity(userMenu);
	}
	
	public void checkPassword(View view){
		//function will be used to check if the username and password match from backend
		
		//kaden must finish connecting to fatFractal first to pass and pull information
		Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
        startActivity(userMenu);
	}
}
