package com.wsuproj5.accuratedrivingtest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.widget.TextView;

import com.fatfractal.ffef.FatFractal;

public class ReviewEvaluation  extends ActionBarActivity{
	public static FatFractal ff = null;
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review_evaluation);
		final TextView LeftSide = (TextView)findViewById(R.id.editText1);
		final TextView RightSide = (TextView)findViewById(R.id.editText2);
		
	    final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
	    
	    String Pass_Fail = "True";
	    String EvaluatorsName = pref.getString("evaluator_name");
	    String driversLicense = pref.getString("drivers_licence_number");
	    String AvgMPH = pref.getString("average_MPH");
	    int A_MPH = Integer.parseInt(AvgMPH);
	    
	    LeftSide.setText("Pass/Fail: " + Pass_Fail + 
	    		"\nEvaluators Name: " + EvaluatorsName + 
	    		"\nDrivers License Number: " + driversLicense + 
	    		"\nAvg MPH: " + A_MPH);
	    
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

}
