package com.wsuproj5.accuratedrivingtest;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.content.Intent;
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
		
	    final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);

		mButton_login.setOnClickListener(
				new View.OnClickListener()
				{
					public void onClick(View view)
					{
						if( (mEdit_Evaluator_name.getText().toString().equals("") == true) ||
							(mEdit_password.getText().toString().equals("") == true) ){
							Context context = getApplicationContext();
							CharSequence text = "Empty Fields. Try again";
							int duration = Toast.LENGTH_SHORT;
							
							Toast toast = Toast.makeText(context, text, duration);
							toast.show();
							return;
						}
						
						try {
				        	ff.login("accuratedrivingtest@gmail.com", "AccurateDrivingT3st");

							Log.d("yes","login worked!");
							User x = null;
							if(mEdit_Evaluator_name.getText().toString().equalsIgnoreCase("ryan")){
								x = ff.getObjFromUri("/ff/resources/evaluator/jz0aB2v8cTecsbv_3k2CT7");

							}
							else if(mEdit_Evaluator_name.getText().toString().equalsIgnoreCase("parker")){
								x = ff.getObjFromUri("/ff/resources/evaluator/IcegzoKiYsaaRpvhclJNu6");
								
							}
							else {
								//new User
								pref.put("evaluator_name", mEdit_Evaluator_name.getText().toString());
								
								User evaluator = new User();
								evaluator.setusername(mEdit_Evaluator_name.getText().toString());
								evaluator.setpassword(mEdit_password.getText().toString());
								ff.createObjAtUri(evaluator, "/evaluator");
								
								Intent userMenu = new Intent(LoginScreen.this,UserMenu.class);                               
								startActivity(userMenu);
							}
							
							if(checkPassword(x,mEdit_password.getText().toString()) == true){
								pref.put("evaluator_name", mEdit_Evaluator_name.getText().toString());
								Intent userMenus = new Intent(LoginScreen.this,UserMenu.class);                               
								startActivity(userMenus);
							}
							else{
								//toast error message of incorrect password
								Context context = getApplicationContext();
								CharSequence text = "Incorrent Password. Try again";
								int duration = Toast.LENGTH_SHORT;
								
								Toast toast = Toast.makeText(context, text, duration);
								toast.show();
								
								//dont need to check other users passwords
							}

						} catch (FFException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

	    			  }

					
	    		  });
	      
	   }
	
	   @Override
	   public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
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
	            String baseUrl = "http://accuratedrivingtest.fatfractal.com/accuratedrivingtest";
	            String sslUrl = "https://accuratedrivingtest.fatfractal.com/accuratedrivingtest";
	            try {
	                ff = FatFractal.getInstance(new URI(baseUrl), new URI(sslUrl));
	                FatFractalHttpImpl.addTrustedHost("accuratedrivingtest.fatfractal.com");
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

}
