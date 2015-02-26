package com.wsuproj5.accuratedrivingtest;

import java.util.ArrayList;
import java.util.List;

import com.wsuproj5.accuratedrivingtest.addroute.AddRoute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/*OBD connection will be done at beginning of during Evaluation and exit 
 * to main menu if unconnected. this will save the challenge of trying to pass
 * the object that holds the bluetooth connection. 
 */
public class BeginEvaluation extends ActionBarActivity{
	protected void onCreate(Bundle savedInstanceState) {
		
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_begin_evaluation);
	      fillEvaluatorsName();
	      fillRoutes();
	//      fillTests();
	      Button begin_evaluation = (Button)findViewById(R.id.btn_begin_evaluation);
	     begin_evaluation.setOnClickListener(
	    		  new View.OnClickListener()
	    		  {
	    			  public void onClick(View view)
	    			  {
	    				  toDuringEvaluation();
	    			  }
	    		  });
		   }
		   
		   private void fillEvaluatorsName() {
		/*evaluators name is a text field, so get Evaluators name from login screen
		 * and fill the text field after variable is set
		 */
			 //  SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
			      final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
 
			   String username = pref.getString("evaluator_name");
			   Log.d("username", username);
			   
			   final TextView evaluator_name = (TextView)findViewById(R.id.evaluators2_name);
			   evaluator_name.setText(username);
	}
		   private void getDriversName(){
			   /*get drivers name from the text field*/
			   EditText drivers_name = (EditText)findViewById(R.id.enter_drivers_name);
			   
			 //  SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
			      final SecurePreferences pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
		//	   final SharedPreferences.Editor edit = pref.edit();
			   pref.put("drivers_name",drivers_name.getText().toString());
		//	   edit.commit();
			 //  Log.d("driver name", drivers_name.getText().toString());


		   }
		   
		private void toDuringEvaluation(){
			   /*check if all fields have been filled
			    * check obd connection onCreate of duringEvaluation()
			    */
			   getDriversName();
			   Intent duringEvaluation = new Intent(BeginEvaluation.this,DuringEvaluation.class);  
				Spinner sItems = (Spinner) findViewById(R.id.route_spinner1 );
				String selected = sItems.getSelectedItem().toString();
				duringEvaluation.putExtra("route", selected);
		        startActivity(duringEvaluation);  
		   }

		private void fillRoutes() {
				SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
				List<String> spinnerArray =  new ArrayList<String>();
				for (int i = 0; i < 1000; i++) {
					String route = prefs.getString("route" + i, null);
					if (route == null || route.equals("removed")) {
						break;
					}
					spinnerArray.add("route " + i);
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				    this, android.R.layout.simple_spinner_item, spinnerArray);

				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				Spinner sItems = (Spinner) findViewById(R.id.route_spinner1);
				sItems.setAdapter(adapter);
			}
		   
		   private void fillTests() {
				SharedPreferences prefs = getSharedPreferences("existingTests", Context.MODE_PRIVATE);
				List<String> spinnerArray =  new ArrayList<String>();
				for (int i = 0; i < 1000; i++) {
					String test = prefs.getString("test" + i, null);
					if (test == null || test.equals("removed")) {
						break;
					}
					spinnerArray.add("test " + i);
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				    this, android.R.layout.simple_spinner_item, spinnerArray);

				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				Spinner sItems = (Spinner) findViewById(R.id.test_spinner);
				sItems.setAdapter(adapter);
			}
		   public void toAddRoute(View view) {
			   Intent addRoute = new Intent(BeginEvaluation.this, AddRoute.class);
			   startActivity(addRoute);
		   }
		   
		   public void toCreateTest(View view) {
			   Intent createTest = new Intent(BeginEvaluation.this, CreateTest.class);
			   startActivity(createTest);
		   }
		 
}