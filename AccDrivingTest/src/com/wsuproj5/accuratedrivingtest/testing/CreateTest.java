package com.wsuproj5.accuratedrivingtest.testing;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.location.LocationServices;
import com.wsuproj5.accuratedrivingtest.R;


import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CreateTest extends ActionBarActivity {
	
	private boolean viewingTestInfo = false;
	public TestDetailsGeneral previousFragment;
	private CreateTest activity;
	ArrayList<JObject> selected;
	ArrayList<JObject> notselected;
	StableArrayAdapter adapter;
	StableArrayAdapter secondadapter;
	ArrayList<List<String>> existingTests = new ArrayList<List<String>>();
	public static final String testDelimiter = "!--TEST--!";
	int indexTestWorking = 0;
	int indexTestMax = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_test);
		JSONObject json = TestingJSON.readTestingJSONLocal(this);
		activity = this;
		getExistingTests();
		setLists(json);
	}
	
	@Override
	public void onBackPressed()
	{
		if (previousFragment != null) {
			getFragmentManager().popBackStack(); // remove fragment
			getFragmentManager() 
			.beginTransaction()
			.remove(previousFragment)
			.commit();
			getFragmentManager() //show previous fragment
			.beginTransaction()
			.add(R.id.test_view, previousFragment)
			.commit();
			previousFragment = previousFragment.previousFragment;
			return;
		}
		
	    if (viewingTestInfo) { //Case: Last fragment on the stack
	    	viewingTestInfo = false;
	    	findViewById(R.id.layout_lists).setVisibility(View.VISIBLE);
	        findViewById(R.id.layout_buttons).setVisibility(View.VISIBLE);
	    	getFragmentManager().popBackStack(); // remove fragment
	        return;
	    }
	    super.onBackPressed();
	}

	private void setLists(JSONObject json) {
		final ListView rightSide = (ListView) findViewById(R.id.all_test);
		final ListView leftSide = (ListView) findViewById(R.id.selected_test);
		notselected = getAllCriteria(json);
		selected = new ArrayList<JObject>();
		
		adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, notselected);
			rightSide.setAdapter(adapter);
		secondadapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, selected);
			leftSide.setAdapter(secondadapter);
		
		rightSide.setOnTouchListener(new AddSwipeListener(this) {
			@Override
		    public void onSwipeLeft(int x, int y) {
		        System.out.println("swipe left");
		        int position = rightSide.pointToPosition(x, y);
		        JObject temp = (JObject) adapter.getItem((position));
		        findViewById(R.id.layout_lists).setVisibility(View.GONE);
		        findViewById(R.id.layout_buttons).setVisibility(View.GONE);
		        viewingTestInfo = true;
		        TestDetailsGeneral fr = new TestDetailsGeneral(temp.jsonObject, temp.objectName, activity);
		        FragmentManager fm = getFragmentManager();
		        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        		fragmentTransaction.add(R.id.test_view, fr)
        		.addToBackStack(null)
		        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
		        .show(fr)
		        .commit();
		    }
			@Override
		    public void onSwipeRight(int x, int y) {
		        System.out.println("swipe right");
		        int position = rightSide.pointToPosition(x, y);
		        JObject temp = (JObject) adapter.getItem((position));
		        findViewById(R.id.layout_lists).setVisibility(View.GONE);
		        findViewById(R.id.layout_buttons).setVisibility(View.GONE);
		        viewingTestInfo = true;
		        TestDetailsGeneral fr = new TestDetailsGeneral(temp.jsonObject, temp.objectName, activity);
		        FragmentManager fm = getFragmentManager();
		        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        		fragmentTransaction.add(R.id.test_view, fr)
        		.addToBackStack(null)
		        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
		        .show(fr)
		        .commit();
		    }
		});
		
		leftSide.setOnTouchListener(new AddSwipeListener(this) {
			@Override
		    public void onSwipeLeft(int x, int y) {
		        System.out.println("swipe left");
		        int position = rightSide.pointToPosition(x, y);
		        JObject temp = (JObject) adapter.getItem((position));
		        findViewById(R.id.layout_lists).setVisibility(View.GONE);
		        findViewById(R.id.layout_buttons).setVisibility(View.GONE);
		        viewingTestInfo = true;
		        TestDetailsGeneral fr = new TestDetailsGeneral(temp.jsonObject, temp.objectName, activity);
		        FragmentManager fm = getFragmentManager();
		        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        		fragmentTransaction.add(R.id.test_view, fr)
        		.addToBackStack(null)
		        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
		        .show(fr)
		        .commit();
		    }
			@Override
		    public void onSwipeRight(int x, int y) {
		        System.out.println("swipe right");
		        int position = rightSide.pointToPosition(x, y);
		        JObject temp = (JObject) adapter.getItem((position));
		        findViewById(R.id.layout_lists).setVisibility(View.GONE);
		        findViewById(R.id.layout_buttons).setVisibility(View.GONE);
		        viewingTestInfo = true;
		        TestDetailsGeneral fr = new TestDetailsGeneral(temp.jsonObject, temp.objectName, activity);
		        FragmentManager fm = getFragmentManager();
		        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        		fragmentTransaction.add(R.id.test_view, fr)
        		.addToBackStack(null)
		        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
		        .show(fr)
		        .commit();
		    }
		});
		
			rightSide.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		      @SuppressLint("NewApi") @Override
		      public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {
		    	  final JObject item = (JObject) parent.getItemAtPosition(position);
		    	  if (position == -1) {
	    		  view.animate().setDuration(1).alpha(0)
		            .withEndAction(new Runnable() {
		              @Override
		              public void run() {
		                adapter.notifyDataSetChanged();
		                secondadapter.notifyDataSetChanged();
		                view.setAlpha(1);
		              }
		            }); 
		    	  } else {
			        view.animate().setDuration(500).alpha(0)
			            .withEndAction(new Runnable() {
			              @Override
			              public void run() {
			            	selected.add(item);
			            	notselected.remove(item);
			                adapter.notifyDataSetChanged();
			                secondadapter.notifyDataSetChanged();
			                view.setAlpha(1);
			              }
			            });
		    	  }
		      }
		    });
			
			leftSide.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@SuppressLint("NewApi") @Override
			      public void onItemClick(AdapterView<?> parent, final View view,
			          int position, long id) {
			    	  final JObject item = (JObject) parent.getItemAtPosition(position);
			    	  if (position == -1) {
		    		  view.animate().setDuration(1).alpha(0)
			            .withEndAction(new Runnable() {
			              @Override
			              public void run() {
			                adapter.notifyDataSetChanged();
			                secondadapter.notifyDataSetChanged();
			                view.setAlpha(1);
			              }
			            }); 
			    	  } else {
				        view.animate().setDuration(500).alpha(0)
				            .withEndAction(new Runnable() {
				              @Override
				              public void run() {
				            	selected.remove(item);
				            	notselected.add(item);
				                adapter.notifyDataSetChanged();
				                secondadapter.notifyDataSetChanged();
				                view.setAlpha(1);
				              }
				            });
			    	  }
			      }
			    });
	}

	private class StableArrayAdapter extends ArrayAdapter<JObject> {
	
	    HashMap<JSONObject, Integer> mIdMap = new HashMap<JSONObject, Integer>();
	
	    public StableArrayAdapter(Context context, int textViewResourceId, List<JObject> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }
	}

	private ArrayList<JObject> getAllCriteria(JSONObject json) {
		
		ArrayList<JObject> list = new ArrayList<JObject>();
		
		Iterator<String> iter = json.keys();
		while (iter.hasNext()) {
	        String key = iter.next();
	        try {
	        	if (!TestingJSON.CATEGORY_ABBREVIATIONS.equals(key)) {
		        	//Assign our JSONObject to a new JObject that will override the tostring method
		            JObject value = new JObject((JSONObject) json.get(key), key);
		            list.add(value);
	        	}
	        } catch (JSONException e) {
	            Log.e("JSON","Error retrieving test criteria.");
	        }
	    }
		
		return list;
	}
	
	public void saveCurrentTest(View v) {
	   //save the task list to preference
	   SharedPreferences prefs = getSharedPreferences("existingTests", Context.MODE_PRIVATE);
	   Editor editor = prefs.edit();
	   String testAsString = "";
	   Iterator<JObject> it = selected.iterator();
	   while (it.hasNext()) {
			JObject testData = it.next();
			String test = testData.objectName;
			if (it.hasNext()) {
				testAsString += test + testDelimiter;
			} else {
				testAsString += test;
			}
		}
		editor.putString("test" + indexTestWorking, testAsString); 
        editor.commit();
	 }
	
	private void getExistingTests() {
		SharedPreferences prefs = getSharedPreferences("existingTests", Context.MODE_PRIVATE);
		for (int i = 0; i < 1000; i++) {
			String test = prefs.getString("test" + i, null);
			String[] testAsArray;
			if (test == null) { //Case: We have reached the end of our existing tests
				indexTestWorking = indexTestMax = i;
				break;
			} else { //Case: We have a test that exists and we need to add it
				List<String> newTest = new ArrayList<String>();
				testAsArray = test.split(testDelimiter);
				for (int j = 0; j < testAsArray.length; j++) {
					newTest.add(testAsArray[j]);
				}
				existingTests.add(newTest);
			}
		}
	}
	
	public void loadTestFragment(View v) {
		findViewById(R.id.layout_lists).setVisibility(View.GONE);
        findViewById(R.id.layout_buttons).setVisibility(View.GONE);
        viewingTestInfo = true;
        previousFragment = null;
        LoadTest fr = new LoadTest(this, existingTests);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.add(R.id.test_view, fr)
		.addToBackStack(null)
        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        .show(fr)
        .commit();
	}

	public void loadTest(List<String> list) {
		/* Remove all tests from the left side that were selected */
		int selectedSize = selected.size();
		for (int i = 0; i < selectedSize; i++) {
			notselected.add(selected.remove(0));
			adapter.notifyDataSetChanged();
            secondadapter.notifyDataSetChanged();
		}
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			String testName = it.next();
			for (int i = 0; i < notselected.size(); i++) {
				JObject temp = notselected.get(i);
				if (testName.equals(temp.objectName)) { //TODO: Need to remove tests from the left side if they are not in the list of selected tests. Currently this only moves items from the right side.
					selected.add(temp);
	            	notselected.remove(temp);
	            	adapter.notifyDataSetChanged();
	                secondadapter.notifyDataSetChanged();
				}
			}
		}
		ListView rightSide = (ListView) findViewById(R.id.all_test);
		ListView leftSide = (ListView) findViewById(R.id.selected_test);
		if (rightSide.getChildAt(0) != null) //force a click so the view refreshes
			rightSide.performItemClick(rightSide.getChildAt(0), -1, rightSide.getItemIdAtPosition(-1));
		if (leftSide.getChildAt(0) != null) //force a click so the view refreshes
			leftSide.performItemClick(leftSide.getChildAt(0), -1, leftSide.getItemIdAtPosition(-1));
	}
}
