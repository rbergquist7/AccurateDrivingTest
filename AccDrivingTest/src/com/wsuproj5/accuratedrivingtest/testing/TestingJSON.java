package com.wsuproj5.accuratedrivingtest.testing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TableRow;

public class TestingJSON {
	
   public static final String CATEGORY_ABBREVIATIONS = "Category Abbreviations";
	
   public static JSONObject readTestingJSONLocal(ActionBarActivity createTest) {
	   String json = null;
	   try 
	   {
		   //Load json file
		   InputStream fd = createTest.getResources().getAssets().open("testingcriteria.json");
		   int size = fd.available();
		   //Create buffer to receive json
		   byte[] buffer = new byte[size];
		   fd.read(buffer);
		   fd.close();
		   //Convert json to string
		   json = new String(buffer, "UTF-8");
	   } catch (FileNotFoundException e) {
		   Log.e("jsonFile", "file not found");
	   } catch (IOException e) {
		   Log.e("jsonFile", "ioerror");
	   }
	return parseJSON(json);
   }
   
   public static JSONObject parseJSON(String json) {
	   try {
		JSONObject jsonObject = new JSONObject(json);
		JSONObject testingCriteria = jsonObject.getJSONObject("testingCriteria");
		return testingCriteria;
	} catch (JSONException e) {
		Log.e("jsonParse", "Error parsing json");
		e.printStackTrace();
	}
	   return null;
   }
}
