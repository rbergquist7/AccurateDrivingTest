package com.wsuproj5.accuratedrivingtest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.wsuproj5.accuratedrivingtest.testing.CreateTest;
import com.wsuproj5.accuratedrivingtest.testing.JObject;

public class DuringEvaluationLoadTest {

	public static ArrayList<JObject> getTestingData(JSONObject json) {
		
		ArrayList<JObject> list = new ArrayList<JObject>();
		
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
	
	public static ArrayList<List<String>> getExistingTests(ArrayList<List<String>> existingTests, SharedPreferences prefs) {
		for (int i = 0; i < 1000; i++) {
			String test = prefs.getString("test" + i, null);
			String[] testAsArray;
			if (test == null) { //Case: We have reached the end of our existing tests;
				break;
			} else { //Case: We have a test that exists and we need to add it
				List<String> newTest = new ArrayList<String>();
				testAsArray = test.split(CreateTest.testDelimiter);
				for (int j = 0; j < testAsArray.length; j++) {
					newTest.add(testAsArray[j]);
				}
				existingTests.add(newTest);
			}
		}
		return existingTests;
	}
	
	public static ArrayList<JObject> loadTest(List<String> list, ArrayList<JObject> json) { //list is the list of selected tests by name, json contains the actual data objects with those names
		ArrayList<JObject> selected = new ArrayList<JObject>();
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			String testName = it.next();
			for (int i = 0; i < json.size(); i++) {
				if (json.get(i).objectName.equals(testName)) {
					selected.add(json.get(i));
					break;
				}
			}
		}
		return selected;
	}
}
