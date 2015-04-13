package com.wsuproj5.accuratedrivingtest.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.wsuproj5.accuratedrivingtest.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LoadTest extends Fragment {
	ArrayList<List<String>> existingTests;
	List<String> testList = new ArrayList<String>();
	CreateTest parentActivity;
	
	public LoadTest (CreateTest p, ArrayList<List<String>> eT) {
		existingTests = eT;
		parentActivity = p;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.load_test, container, false);
		countTests();
		loadTests(v);
		return v;
    }
	
	public void countTests() {
		for (int i = 0; i < existingTests.size(); i++) {
			testList.add("Test " + i);
		}
	}
	
	public void loadTests(View v) {
		final ListView list = (ListView) v.findViewById(R.id.test_list);
		
		final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, testList);
			list.setAdapter(adapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		      @SuppressLint("NewApi") @Override
		      public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {
		        parentActivity.indexTestWorking = position;
		        ((CreateTest)getActivity()).loadTest(existingTests.get(position));
		        parentActivity.onBackPressed();
		      }
		    });
		}	
	
	private class StableArrayAdapter extends ArrayAdapter<String> {
		
	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
	
	    public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }
	}
	
}
