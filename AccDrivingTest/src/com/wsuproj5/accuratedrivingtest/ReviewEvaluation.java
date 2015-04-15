package com.wsuproj5.accuratedrivingtest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.view.View;
import android.widget.TextView;

import com.fatfractal.ffef.FFException;
import com.fatfractal.ffef.FatFractal;
import com.fatfractal.ffef.impl.FatFractalHttpImpl;
import com.fatfractal.ffef.json.FFObjectMapper;

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
		final TextView LeftSide = (TextView)findViewById(R.id.editText1);
		final TextView RightSide = (TextView)findViewById(R.id.editText2);
		final TextView license = (TextView)findViewById(R.id.editText3);
		
	    final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
	    
//	    String Pass_Fail = "True";
//	    String EvaluatorsName = pref.getString("evaluator_name");
	    String driversLicense = license.getText().toString();
//	    String AvgMPH = pref.getString("average_MPH");
//	    int A_MPH = Integer.parseInt(AvgMPH);
//	    
//	    LeftSide.setText("Pass/Fail: " + Pass_Fail + 
//	    		"\n\nEvaluators Name: " + EvaluatorsName + 
//	    		"\n\nDrivers License Number: " + driversLicense + 
//	    		"\n\nAvg MPH: " + A_MPH);
	    
	    
		try {
			ff.login("r.bergquist7@gmail.com", "23Mar917457");
		
			List<Driver> list = ff.getArrayFromUri("/allDrivers");	
		

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
              //  FFObjectMapper.registerClassNameForClazz(User.class.getName(), "User");
                FFObjectMapper.registerClassNameForClazz(Driver.class.getName(), "Driver");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return ff;
    }

}
