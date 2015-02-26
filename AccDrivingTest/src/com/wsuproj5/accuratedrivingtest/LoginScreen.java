package com.wsuproj5.accuratedrivingtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginScreen extends ActionBarActivity{
	protected void onCreate(Bundle savedInstanceState) {
		
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_login_screen);
	      
	      Button mButton_login = (Button)findViewById(R.id.btn_login);
	      final EditText mEdit_Evaluator_name = (EditText)findViewById(R.id.fld_Evaluator_name);
	      final EditText mEdit_password = (EditText)findViewById(R.id.fld_pwd);

	      
	      final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
	      
	   // We need an editor object to make changes
	     // final SharedPreferences.Editor edit = pref.edit();
	    
	      mButton_login.setOnClickListener(
	    		  new View.OnClickListener()
	    		  {
	    			  public void onClick(View view)
	    			  {
	    				  Log.d("evaluators_name", mEdit_password.getText().toString());
	    				  pref.put("evaluator_name", mEdit_Evaluator_name.getText().toString());
	    				//  edit.commit();
	    				  Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
	    			      startActivity(userMenu);
	    			  }
	    		  });
	      
	   }
	
	public void toUserMenu(View view) {
		Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
        startActivity(userMenu);
	}
	
	public void checkPassword(View view){
		//function will be used to check if the username and password match from backend
		
		//kaden must finish connecting to fatFractal first to pass and pull information
		 final EditText mEdit_Evaluator_name = (EditText)findViewById(R.id.fld_Evaluator_name);
	     final EditText mEdit_password = (EditText)findViewById(R.id.fld_pwd);

		 SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
	      
		   // We need an editor object to make changes
		  final SharedPreferences.Editor edit = pref.edit();
		  Log.v("EditText", mEdit_password.getText().toString());
		  edit.putString("evaluator_name", mEdit_Evaluator_name.getText().toString());
		  edit.commit();
		  Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
	      startActivity(userMenu);
	}
}
