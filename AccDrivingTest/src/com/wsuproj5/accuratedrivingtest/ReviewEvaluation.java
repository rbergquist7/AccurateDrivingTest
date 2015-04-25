package com.wsuproj5.accuratedrivingtest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.view.View;
import android.widget.TextView;

import com.fatfractal.ffef.FFException;
import com.fatfractal.ffef.FatFractal;
import com.fatfractal.ffef.impl.FatFractalHttpImpl;
import com.fatfractal.ffef.json.FFObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ReviewEvaluation  extends ActionBarActivity{
	public static FatFractal ff = null;
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		ff = getFF();
		setContentView(R.layout.activity_review_evaluation);
//		final TextView LeftSide = (TextView)findViewById(R.id.editText1);
//		final TextView RightSide = (TextView)findViewById(R.id.editText2);
//		
//	    final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
//	    
////	    String Pass_Fail = "True";
////	    String EvaluatorsName = pref.getString("evaluator_name");
//	    String driversLicense = pref.getString("drivers_licence_number");
////	    String AvgMPH = pref.getString("average_MPH");
////	    int A_MPH = Integer.parseInt(AvgMPH);
////	    
////	    LeftSide.setText("Pass/Fail: " + Pass_Fail + 
////	    		"\n\nEvaluators Name: " + EvaluatorsName + 
////	    		"\n\nDrivers License Number: " + driversLicense + 
////	    		"\n\nAvg MPH: " + A_MPH);
//	    
//	    
//		try {
//			ff.login("r.bergquist7@gmail.com", "23Mar917457");
//		
//			List<Driver> list = ff.getArrayFromUri("/driver");	
//		
//
//			for (Driver temp : list) { //finds driver information based on drivers license
//				if(temp.getdriversLicense().equalsIgnoreCase(driversLicense)){
//					 String Pass_Fail = "True";
//					    String EvaluatorsName = temp.getEvaluatorsName();
//					    int A_MPH = temp.getAvgMPH();
//					    
//					    LeftSide.setText("Pass/Fail: " + Pass_Fail + 
//					    		"\n\nEvaluators Name: " + EvaluatorsName + 
//					    		"\n\nDrivers License Number: " + driversLicense + 
//					    		"\n\nAvg MPH: " + A_MPH);
//					LeftSide.append("\n\nComments:\n" + temp.getcomments());
//				}
//			}
//		} catch (FFException e) {
//			e.printStackTrace();
//		}
//
//		RightSide.setText("Section in progress");
//	    /* Left side
//	     * 
//	     * Pass/Fail
//	     * Average Speed
//	     * Distance Traveled
//	     * 
//	     * Test Score percent
//	     * Test Score Total Points
//	     * 
//	     * Trouble Areas:
//	     * 
//	     * Role: Driver/eval
//	     * Eval name
//	     * driver license
//	     * 
//	     * ...
//	     */
//	    
//	    /*
//	     * Right Side
//	     * 
//	     * Test Criteria # : pass/fail
//	     * 	Eval comment
//	     * 	description
//	     * 
//	     * ...
//	     * 
//	     */
	    
	}
	
	public void searchLicenseNumber(View view){

		findViewById(R.id.please_wait).setVisibility(View.VISIBLE);
		findViewById(R.id.review_container).setVisibility(View.GONE);
		
		final TextView LeftSide = (TextView)findViewById(R.id.editText1);
		final TextView RightSide = (TextView)findViewById(R.id.editText2);
		final TextView license = (TextView)findViewById(R.id.editText3);
	    final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
	    
	    String driversLicense = license.getText().toString();
	    
		try {
        	ff.login("accuratedrivingtest@gmail.com", "AccurateDrivingT3st");

			List<Driver> list = ff.getArrayFromUri("/driver");	
		

			for (Driver temp : list) { //finds driver information based on drivers license
				if(temp.getdriversLicense().equalsIgnoreCase(driversLicense)){
					 	String Pass_Fail = temp.getPass_Fail();
					    String EvaluatorsName = temp.getEvaluatorsName();
					    int A_MPH = temp.getAvgMPH();
					    
					    LeftSide.setText("Pass/Fail: " + Pass_Fail + 
					    		"\n\nEvaluators Name: " + EvaluatorsName + 
					    		"\n\nDrivers License Number: " + driversLicense + 
					    		"\n\nAvg MPH: " + A_MPH);
					LeftSide.append("\n\nComments:\n" + temp.getcomments());
					recreateMap(temp);
				}
			}
		} catch (FFException e) {
			e.printStackTrace();
		}

		RightSide.setText("Section in progress");
	    /* Left side
	     * 
	     * Pass/Fail
	     * Average Speed
	     * Distance Traveled
	     * 
	     * Test Score percent
	     * Test Score Total Points
	     * 
	     * Trouble Areas:
	     * 
	     * Role: Driver/eval
	     * Eval name
	     * driver license
	     * 
	     * ...
	     */
	    
	    /*
	     * Right Side
	     * 
	     * Test Criteria # : pass/fail
	     * 	Eval comment
	     * 	description
	     * 
	     * ...
	     * 
	     */
		findViewById(R.id.please_wait).setVisibility(View.GONE);
		findViewById(R.id.review_container).setVisibility(View.VISIBLE);
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
                FFObjectMapper.registerClassNameForClazz(Driver.class.getName(), "Driver");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return ff;
    }

	GoogleMap map;
	public void recreateMap(Driver driver) {
		MapFragment m = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		map = m.getMap();
		addCommentsToMap(driver.getcomments()); // Add all comments back to the map
		addDesignatedRouteToMap(null); // Add the route that was intended to be taken to the map. This is the blue line in during evaluation.
		addDrivenRouteToMap(driver.getLatLon()); // Add the route that was actually taken to the map. This is the route that was driven during the evaluation and is the red line in DuringEvaluatio
		addFailsToMap(driver.getFails(), driver.getFailsTypes(), driver.getFailsLatLng());
	}
	
	public void addCommentsToMap(String c) {
		String[] cAndL = c.split("\n");
		for (int i = 0; i < cAndL.length; i += 2) {
			String[] pS = cAndL[i+1].replace(")","").replace("(", "").split(","); // Position as string
			LatLng p = new LatLng(Double.parseDouble(pS[0]), Double.parseDouble(pS[1]));
			map.addMarker(new MarkerOptions()
				.title("Comment:")
				.snippet(cAndL[i])
				.position(p)
			);
		}
	}
	
	public void addDesignatedRouteToMap(String r) {
		
	}
	
	public void addDrivenRouteToMap(String r) {
		String[] pairs = r.split("\n");
		ArrayList<LatLng> latLng = new ArrayList<LatLng>();
		for (int i = 0; i < pairs.length; i++) {
			String[] points = pairs[i].split(",");
			latLng.add( // Construct route by converting string to doubles and creating new latlng points
					new LatLng(Double.parseDouble(points[0]), Double.parseDouble(points[1])) 
					);
		}
		map.addPolyline(new PolylineOptions()
					.addAll(latLng)
					.width(5)
					.color(Color.RED)
				);
		CameraPosition cameraPosition = new CameraPosition.Builder()
        .target(latLng.get(0))      // Sets the center of the map the start of the route
        .zoom(25)                   // Sets the zoom
        .bearing(0)        
        .tilt(0)            
        .build();                  
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	public void addFailsToMap(String f, String fT, String fP) {
		String[] fails = f.split(DuringEvaluation.delimiter);
		String[] failsTypes = fT.split(DuringEvaluation.delimiter);
		String[] failsPoints = fP.split("\n");
		ArrayList<LatLng> latLng = new ArrayList<LatLng>();
		for (int i = 0; i < failsPoints.length; i=i+2)
			latLng.add(new LatLng(
					Double.parseDouble(failsPoints[i]), 
					Double.parseDouble(failsPoints[i+1])
					)
			);
		for (int i = 0; i < fails.length; i++)
			map.addMarker(new MarkerOptions()
					.snippet(fails[i])
					.title(failsTypes[i])
					.position(latLng.get(i))
					);
	}
}
