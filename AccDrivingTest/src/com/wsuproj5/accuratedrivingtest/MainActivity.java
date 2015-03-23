package com.wsuproj5.accuratedrivingtest;


import android.content.Intent;

import android.os.Bundle;
import android.app.Activity;
import java.net.URI;
import java.net.URISyntaxException;
/*import com.fatfractal.ffef.FFException;
import com.fatfractal.ffef.FatFractal;
import com.fatfractal.ffef.impl.FatFractalHttpImpl;
import com.fatfractal.ffef.json.FFObjectMapper;*/

public class MainActivity extends Activity {
	
	//public static FatFractal ff = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent splashScreen = new Intent(MainActivity.this,SplashScreen.class);                               
        startActivity(splashScreen);   
    }
  /*
    public static FatFractal getFF() {
    	//initialize instance of fatfractal
        if (ff == null) {
            String baseUrl = "http://adt.fatfractal.com/accuratedrivingtest";
            String sslUrl = "https://adt.fatfractal.com/accuratedrivingtest";
            try {
                ff = FatFractal.getInstance(new URI(baseUrl), new URI(sslUrl));
                FatFractalHttpImpl.addTrustedHost("adt.fatfractal.com");
                //declare object collections here
                FFObjectMapper.registerClassNameForClazz(User.class.getName(), "User");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return ff;
    }*/
}
