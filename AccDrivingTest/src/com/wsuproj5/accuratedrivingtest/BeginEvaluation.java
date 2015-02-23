package com.wsuproj5.accuratedrivingtest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/*OBD connection will be done at beginning of during Evaluation and exit 
 * to main menu if unconnected. this will save the challenge of trying to pass
 * the object that holds the bluetooth connection. 
 */

public class BeginEvaluation extends ActionBarActivity{
	protected void onCreate(Bundle savedInstanceState) {
		
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_begin_evaluation);
	      fillRoutes();
	      fillTests();
	      fillEvaluationsName();
		   }
		   
		   private void fillEvaluationsName() {
		/*evaluators name is a text field, so get Evaluators name from login screen
		 * and fill the text field after variable is set
		 */
	}
		   private void getDriversName(){
			   /*get drivers name from the text field*/
		   }
		   private void toDuringEvaluation(View view){
			   /*check if all fields have been filled
			    * check obd connection onCreate of duringEvaluation()
			    */
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
				SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
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
		 
}



