package com.wsuproj5.accuratedrivingtest;


import android.content.Intent;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent splashScreen = new Intent(MainActivity.this,SplashScreen.class);                               
        startActivity(splashScreen);   
    }
  
}
