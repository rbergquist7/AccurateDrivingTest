package com.wsuproj5.accuratedrivingtest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fatfractal.ffef.FFException;
import com.fatfractal.ffef.FatFractal;
import com.fatfractal.ffef.impl.FatFractalHttpImpl;
import com.fatfractal.ffef.json.FFObjectMapper;

public class LoginScreen extends ActionBarActivity{
	public static FatFractal ff = null;
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_screen);
		LoginScreen.ff = getFF();
		Button mButton_login = (Button)findViewById(R.id.btn_login);
		final EditText mEdit_Evaluator_name = (EditText)findViewById(R.id.fld_Evaluator_name);
		final EditText mEdit_password = (EditText)findViewById(R.id.fld_pwd);
		
	      
//		final SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
	    final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
	      
//	    // We need an editor object to make changes
//		final SharedPreferences.Editor edit = pref.edit();
//	      
		
	   // We need an editor object to make changes
	     // final SharedPreferences.Editor edit = pref.edit();
		mButton_login.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
						try {
							ff.login("r.bergquist7@gmail.com", "23Mar917457");
							Log.d("yes","login worked!");
							
							List<User> list = ff.getArrayFromUri("/evaluator");	
							
							//if no evaluators exist, create new one with the given password and name
							if(list.size() == 1){ 
								pref.put("evaluator_name", mEdit_Evaluator_name.getText().toString());
								
								User evaluator = new User();
								evaluator.setusername(mEdit_Evaluator_name.getText().toString());
								evaluator.setpassword(mEdit_password.getText().toString());
								ff.createObjAtUri(evaluator, "/evaluator");
								
								Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
								startActivity(userMenu);
							}
							
							String name = mEdit_Evaluator_name.getText().toString();
							//check through all evaluators							
							for (User temp : list) {
								String temp_name = temp.getUserName();
								
								if(name.equals( temp_name ) ){
									//if password enterend equals password saved to user start intent
									if(checkPassword(temp,mEdit_password.getText().toString()) == true){
										 pref.put("evaluator_name", mEdit_Evaluator_name.getText().toString());
										 Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
					    			     startActivity(userMenu);
									}
									else{
										//toast error message of incorrect password
										Context context = getApplicationContext();
										CharSequence text = "Incorrent Password. Try again";
										int duration = Toast.LENGTH_SHORT;

										Toast toast = Toast.makeText(context, text, duration);
										toast.show();
										
										//dont need to check other users passwords
										break;
									}
								}
							}

						} catch (FFException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

//	    				  Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
//	    			      startActivity(userMenu);
	    			  }

					
	    		  });
	      
	   }
	 protected boolean checkPassword(User temp, String string) {
		 if(temp.getPassword().equals(string)){
			 return true;
		 }
		 return false;
	}
	public static FatFractal getFF() {
	    	//initialize instance of fatfractal
	        if (ff == null) {
	            String baseUrl = "http://accuratedriving.fatfractal.com/AccDrivingTest";
	            String sslUrl = "https://accuratedriving.fatfractal.com/AccDrivingTest";
	            try {
	                ff = FatFractal.getInstance(new URI(baseUrl), new URI(sslUrl));
	                FatFractalHttpImpl.addTrustedHost("accuratedriving.fatfractal.com");
	                //declare object collections here
	                FFObjectMapper.registerClassNameForClazz(User.class.getName(), "User");
	            } catch (URISyntaxException e) {
	                e.printStackTrace();
	            }
	        }
	        return ff;
	    }
	public void toUserMenu(View view) {
		Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
        startActivity(userMenu);
	}
	
//	public void checkPassword(View view){
//		//function will be used to check if the username and password match from backend
//		
//		//kaden must finish connecting to fatFractal first to pass and pull information
//		 final EditText mEdit_Evaluator_name = (EditText)findViewById(R.id.fld_Evaluator_name);
//	     final EditText mEdit_password = (EditText)findViewById(R.id.fld_pwd);
//
//		 SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//	      
//		   // We need an editor object to make changes
//		  final SharedPreferences.Editor edit = pref.edit();
//		  Log.v("EditText", mEdit_password.getText().toString());
//		  edit.putString("evaluator_name", mEdit_Evaluator_name.getText().toString());
//		  edit.commit();
//		  Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
//	      startActivity(userMenu);
//	}
}
