package com.wsuproj5.accuratedrivingtest.testing;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.wsuproj5.accuratedrivingtest.R;
import com.wsuproj5.accuratedrivingtest.R.id;
import com.wsuproj5.accuratedrivingtest.R.layout;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CreateTest extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_test);
		JSONObject json = TestingJSON.readTestingJSONLocal(this);
		setLists(json);
	}
	
	private class JObject extends JSONObject {
		JSONObject jsonObject;
		String objectName;
		public JObject (JSONObject j, String oN) {
			jsonObject = j;
			objectName = oN;
		}
		
	    @Override
	    public String toString(){
			return objectName;
	    }
	}

	private void setLists(JSONObject json) {
		ListView rightSide = (ListView) findViewById(R.id.all_test);
		ListView leftSide = (ListView) findViewById(R.id.selected_test);
		
		final ArrayList<JSONObject> notselected= getAllCriteria(json);
		final ArrayList<JSONObject> selected = new ArrayList<JSONObject>();
		
		final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, notselected);
			rightSide.setAdapter(adapter);
		final StableArrayAdapter secondadapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, selected);
			leftSide.setAdapter(secondadapter);

			rightSide.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		      @SuppressLint("NewApi") @Override
		      public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {
		    	  final JObject item = (JObject) parent.getItemAtPosition(position);
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

		    });
			
			leftSide.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			      @SuppressLint("NewApi") @Override
			      public void onItemClick(AdapterView<?> parent, final View view,
			          int position, long id) {
			        final JObject item = (JObject) parent.getItemAtPosition(position);
			        view.animate().setDuration(500).alpha(0)
			            .withEndAction(new Runnable() {
			              @Override
			              public void run() {
			            	notselected.add(item);
			            	selected.remove(item);
			                adapter.notifyDataSetChanged();
			                secondadapter.notifyDataSetChanged();
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
	        	//Assign our JSONObject to a new JObject that will override the tostring method
	            JObject value = new JObject((JSONObject) json.get(key), key);
	            list.add(value);
	        } catch (JSONException e) {
	            Log.e("JSON","Error retrieving test criteria.");
	        }
	    }
		
		return list;
	}
	
}
