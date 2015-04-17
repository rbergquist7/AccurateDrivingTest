package com.wsuproj5.accuratedrivingtest.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.wsuproj5.accuratedrivingtest.DuringEvaluation;
import com.wsuproj5.accuratedrivingtest.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TestDetailsGeneral extends Fragment {
	JSONObject testData;
	public String testName;
	public String info;
	TestDetailsGeneral o;
	CreateTest activityCreateTest;
	DuringEvaluation activityDuringEvaluation;
	public TestDetailsGeneral previousFragment;
	
	private String description = "Description";
	private String notes = "Notes";
	private String instructions = "Instructions";
	
	public TestDetailsGeneral(JSONObject jO, String tN, CreateTest a) {
		super();
		testName = tN;
		testData = jO;
		activityCreateTest = a;
	}

	public TestDetailsGeneral(String value, String objectName, CreateTest a) {
		testName = objectName;
		info = value;
		activityCreateTest = a;
	}
	
	public TestDetailsGeneral(JSONObject jO, String tN, DuringEvaluation a) {
		super();
		testName = tN;
		testData = jO;
		activityDuringEvaluation = a;
	}

	public TestDetailsGeneral(String value, String objectName, DuringEvaluation a) {
		testName = objectName;
		info = value;
		activityDuringEvaluation = a;
	}
	//This constructor converts a list of JObjects back into a JSONObject since current TestDetailsGeneral code only supports JSONObjects
	public TestDetailsGeneral(ArrayList<JObject> testDataSelected, DuringEvaluation duringEvaluation) {
		testData = new JSONObject();
		Iterator<JObject> it = testDataSelected.iterator();
		while (it.hasNext()) {
			JObject currentTest = it.next();
			try {
				testData.put(currentTest.objectName, currentTest.jsonObject);
			} catch (JSONException e) {
				System.err.println("Error converting JObject to JSON");
				e.printStackTrace();
			}
		}
		activityDuringEvaluation = duringEvaluation;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.test_details_general, container, false);
		o = this;
		if (info != null)
			setTextView(v);
		else
			setList(v);
		
		return v;
    }
	
	private void setTextView(View v) {
		TextView testInfo = (TextView) v.findViewById(R.id.test_info);
		testInfo.setText(info);
		testInfo.setVisibility(View.VISIBLE);
		v.findViewById(R.id.test_general_view).setVisibility(View.GONE);
		/*If we are in DuringEvaluation and we are not viewing description instruction, or notes. */
		if (activityDuringEvaluation != null && !description.equals(testName) && !instructions.equals(testName) && !notes.equals(testName)) {
			v.findViewById(R.id.button_fail_test).setVisibility(View.VISIBLE);
		}
	}
	
	private void setList(View v) {
		final ListView list = (ListView) v.findViewById(R.id.test_general_view);
		
		final ArrayList<JSONObject> generalCriteria = getAllCriteria(testData);
		final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, generalCriteria);
			list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		      @SuppressLint("NewApi") @Override
		      public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {
		        final JObject item = (JObject) parent.getItemAtPosition(position);
		        view.animate().setDuration(1).alpha(0)
		            .withEndAction(new Runnable() {
		              @Override
		              public void run() {
		      	        JObject temp = item;
		      	        TestDetailsGeneral fr;
		      	        if (activityDuringEvaluation == null) {
			      	        if (item.jsonObject != null)
			      	        	fr = new TestDetailsGeneral(temp.jsonObject, temp.objectName, activityCreateTest);
			      	        else 
			      	        	fr = new TestDetailsGeneral(temp.value, temp.objectName, activityCreateTest);
			      	        previousFragment = activityCreateTest.previousFragment;
			      	        activityCreateTest.previousFragment = o;
		      	        } else { // We are in DuringEvaluation
			      	        if (item.jsonObject != null)
			      	        	fr = new TestDetailsGeneral(temp.jsonObject, temp.objectName, activityDuringEvaluation);
			      	        else 
			      	        	fr = new TestDetailsGeneral(temp.value, temp.objectName, activityDuringEvaluation);
			      	        previousFragment = activityDuringEvaluation.previousFragment;
			      	        activityDuringEvaluation.previousFragment = o;
			      	        activityDuringEvaluation.currentFragment = fr;
		      	        }
				        FragmentManager fm = getFragmentManager();
				        FragmentTransaction fragmentTransaction = fm.beginTransaction();
				        if (activityDuringEvaluation == null)
				        	fragmentTransaction.add(R.id.test_view, fr);
				        else
				        	fragmentTransaction.add(R.id.menu_test_progress, fr);
				        fragmentTransaction.addToBackStack(null)
				        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
				        .hide(o)
				        .show(fr)
				        .commit();
		                view.setAlpha(1);
		              }
		            });
		      }

		    });
	}
	
	private class StableArrayAdapter extends ArrayAdapter<JSONObject> {
		
	    HashMap<JSONObject, Integer> mIdMap = new HashMap<JSONObject, Integer>();
	
	    public StableArrayAdapter(Context context, int textViewResourceId, List<JSONObject> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }
	}

	private ArrayList<JSONObject> getAllCriteria(JSONObject json) {
		
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		
		Iterator<String> iter = json.keys();
		while (iter.hasNext()) {
	        String key = iter.next();
	        try {
	        	if (!TestingJSON.CATEGORY_ABBREVIATIONS.equals(key)) {
		        	//Assign our JSONObject to a new JObject that will override the tostring method
		        	Object obj = json.get(key);
		        	JObject value = null;
		        	if (obj instanceof JSONObject) {
		        		JSONObject newValue = (JSONObject) obj;
		        		value = new JObject(newValue, key);
		        	} else if (obj instanceof String) {
		        		String newValue = (String) obj;
		        		value = new JObject(newValue, key);
		        	}
		            list.add(value);
	        	}
	        } catch (JSONException e) {
	            Log.e("JSON","Error retrieving test criteria.");
	        }
	    }
		return list;
	}
}
