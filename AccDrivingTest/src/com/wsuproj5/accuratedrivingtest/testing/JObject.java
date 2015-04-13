package com.wsuproj5.accuratedrivingtest.testing;

import org.json.JSONArray;
import org.json.JSONObject;

public class JObject extends JSONObject {
	public JSONObject jsonObject;
	public String objectName;
	public String value;
	public JSONArray jsonArray;
	
	public JObject (JSONObject j, String oN) {
		jsonObject = j;
		objectName = oN;
	}
	
	public JObject(JSONArray j, String oN) {
		jsonArray = j;
		objectName = oN;
	}
	
	public JObject(String newValue, String key) {
		value = newValue;
		objectName = key;
	}
	
    @Override
    public String toString(){
		return objectName;
    }
}