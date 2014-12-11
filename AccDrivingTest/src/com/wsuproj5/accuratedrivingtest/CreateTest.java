package com.wsuproj5.accuratedrivingtest;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CreateTest extends ActionBarActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_test);
		setLists();
	}

	private void setLists() {
		ListView rightSide = (ListView) findViewById(R.id.all_test);
		ListView leftSide = (ListView) findViewById(R.id.selected_test);
		
		final ArrayList<String> notselected= getAllCriteria();
		final ArrayList<String> selected = new ArrayList<String>();
		
		final StableArrayAdapter adapter = new StableArrayAdapter(this,
		        android.R.layout.simple_list_item_1, notselected);
			rightSide.setAdapter(adapter);
		final StableArrayAdapter secondadapter = new StableArrayAdapter(this,
			        android.R.layout.simple_list_item_1, selected);
			leftSide.setAdapter(secondadapter);

			rightSide.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		      @SuppressLint("NewApi") @Override
		      public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {
		        final String item = (String) parent.getItemAtPosition(position);
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
			        final String item = (String) parent.getItemAtPosition(position);
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

		  
		
	
	private class StableArrayAdapter extends ArrayAdapter<String> {
	
	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
	
	    public StableArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }
	}

	private ArrayList<String> getAllCriteria() {
		
		ArrayList<String> list = new ArrayList<String>();
		
		list.add("Test Criteria 1");
		list.add("Test Criteria 2");
		list.add("Test Criteria 3");
		list.add("Test Criteria 4");
		list.add("Test Criteria 5");
		list.add("Test Criteria 6");
		list.add("Test Criteria 7");
		list.add("Test Criteria 8");
		list.add("Test Criteria 9");
		list.add("Test Criteria 10");
		list.add("Test Criteria 11");
		list.add("Test Criteria 12");
		list.add("Test Criteria 13");
		list.add("Test Criteria 14");
		list.add("Test Criteria 15");
		list.add("Test Criteria 16");
		list.add("Test Criteria 17");
		list.add("Test Criteria 18");
		list.add("Test Criteria 19");
		list.add("Test Criteria 20");
		list.add("Test Criteria 21");
		
		return list;
	}
	
}
