package com.wsuproj5.comments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.wsuproj5.accuratedrivingtest.testing.JObject;

public class CommentTemplates extends Fragment{
	CommentsFragment ancestor;
	CommentTemplates that = this;
	public CommentTemplates parent;
	JSONObject dataObject;
	JSONArray dataArray;
	
	public CommentTemplates(CommentsFragment commentsFragment) {
		ancestor= commentsFragment;
	}
	
	public CommentTemplates(CommentTemplates parent, JSONObject dO, CommentsFragment anc) {
		ancestor = anc;
		this.parent = parent;
		dataObject = dO;
	}

	public CommentTemplates(CommentTemplates that2, JSONArray jsonArray, CommentsFragment ancestor2) {
		ancestor = ancestor2;
		parent = that2;
		dataArray = jsonArray;
	}

	@SuppressWarnings("unchecked")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.comment_template, container, false);
		if (parent == null) {//if we're loading the comment templates for the first time
			ArrayList<JObject> cTemps = null;
			try {
				cTemps = convertJSONToJObject(readCommentJSON());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fillTemplateList(cTemps, v);
		} else {
			Object cTemps = null;
			try {
				if (dataObject != null)
					cTemps = convertJSONToJObject(dataObject); //this may come back as a string or an ArrayList of JSON Objects
				else if (dataArray != null)
					cTemps = convertJSONToJObject(dataArray);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (cTemps instanceof String) {
				cTemps = (String) cTemps;
			} else if (cTemps instanceof ArrayList<?>) {
				ArrayList<JObject> temp = (ArrayList<JObject>) cTemps;
				fillTemplateList(temp, v);
			} 
		}
		return v;
    }
	
	private Object convertJSONToJObject(JSONArray cTemplates) { //Only accepts arrays of strings
		ArrayList<JObject> cTemps = new ArrayList<JObject>();
		for (int i = 0; i < cTemplates.length(); i++) {
			Object temp = null;
			try {
				temp = cTemplates.get(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			cTemps.add(new JObject((String)temp, (String)temp));
		}
		return cTemps;
	}

	private JSONObject readCommentJSON() {
	   String json = null;
	   try 
	   {
		   //Load json file
		   InputStream fd = getActivity().getResources().getAssets().open("commentTemplates.json");
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
	   JSONObject temp = null;
	   try {
		   temp = new JSONObject(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	   return temp;
	}
	
	private void fillTemplateList(ArrayList<JObject> cTemps, View v) {
		//Now we have our list of jobjects and we are ready to add them to our adapter view
		ListView lV = (ListView) v.findViewById(R.id.template_list);
		StableArrayAdapter adapter = new StableArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, cTemps);
		lV.setAdapter(adapter);
		lV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		      @SuppressLint("NewApi") @Override
		      public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {
		        final JObject item = (JObject) parent.getItemAtPosition(position);
	  	        JObject temp = item;
	  	        CommentTemplates fr = null;
	  	        if (item.value != null) { //Case: we have selected a comment value and we need to return it up to
	  	        	ancestor.returnTemplate = true;
	  	        	ancestor.commentTemplate = item.value;
	  	        	getActivity().onBackPressed();
	  	        	return;
	  	        }
	  	        else if (temp.jsonObject != null)
	  	        	fr = new CommentTemplates(that, temp.jsonObject, ancestor);
	  	        else if (temp.jsonArray != null)
	  	        	fr = new CommentTemplates(that, temp.jsonArray, ancestor);
	  	        //TODO: Add a case for strings
	  	        ancestor.currentFragment = fr;
		        FragmentManager fm = getFragmentManager();
		        FragmentTransaction fragmentTransaction = fm.beginTransaction();
		        fragmentTransaction.add(R.id.menu_comments, fr);
		        fragmentTransaction.addToBackStack(null)
		        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
		        .hide(that)
		        .show(fr)
		        .commit();
		      }

		    });
	}
	
	private ArrayList<JObject> convertJSONToJObject(JSONObject cTemplates) throws JSONException {
		ArrayList<JObject> cTemps = new ArrayList<JObject>();
		Iterator<String> it = cTemplates.keys();
		while (it.hasNext()) {
			String key = it.next();
			Object temp = cTemplates.get(key);
			if (temp instanceof JSONObject)
				cTemps.add(new JObject((JSONObject)temp, key));
			else if (temp instanceof JSONArray)
				cTemps.add(new JObject((JSONArray) temp, key));
			else if (temp instanceof String)
				cTemps.add(new JObject((String) temp, key));
		}
		return cTemps;
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
}
