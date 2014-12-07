package com.wsuproj5.accuratedrivingtest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pig.impl.util.ObjectSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.maps.android.PolyUtil;

import android.support.v7.app.ActionBarActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AddRoute extends ActionBarActivity {

	MapFragment mapFragment;
	GoogleMap map;
	AutoCompleteTextView nextWaypoint;
	DirectionsFetcher directionsFetcher;
	String previousWaypoint = null;
	String currentWaypoint = null;
	String newOrigin = null;
	String newTerminus = null;
	int replacedPath;
	int maxRouteIndex;
	int workingIndex;
	int selectedRoute = -1;
	boolean loadingRoute = false;
	private static final int VISIBLE = 0;
	private static final int INVISIBLE = 4;
	
	//TODO: Compile data fragments into one fragment
	private PlaceholderFragment routeLines;
	private PlaceholderFragment tableData;
	private String routeDelimiter = "!--ROUTEDELIMITER--!";
	ArrayList<List<LatLng>> routeListPoints = new ArrayList<List<LatLng>>();
	List<String> waypointListStrings = new ArrayList<String>();
	ArrayList<List<String>> existingRoutes = new ArrayList<List<String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_route);
		
	   ///      load tasks from preference
        SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
        getExistingRoutes(prefs);
        /*
        try {
            existingWaypoints = (ArrayList<List<String>>) ObjectSerializer.deserialize(prefs.getString("existingWayPoints", ObjectSerializer.serialize(new ArrayList<List<String>>())));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
		
		 // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        routeLines = (PlaceholderFragment) fm.findFragmentByTag("routeLines");
        tableData = (PlaceholderFragment) fm.findFragmentByTag("tableData");

        // create the fragment and data the first time
        if (routeLines == null) {
            // add the fragment
            routeLines = new PlaceholderFragment();
            fm.beginTransaction().add(routeLines, "routeLines").commit();
            // load the data from the web
            //routeLines.setData(loadMyData());
        }
        else {
	        // the data is available in dataFragment.getData()
			routeListPoints = routeLines.getRouteList();
			selectedRoute = routeLines.getSelectedRoute();
			loadingRoute = routeLines.isLoadingRoute();
			rebuildMap();
        }
        // create the fragment and data the first time
        if (tableData == null) {
            // add the fragment
            tableData = new PlaceholderFragment();
            fm.beginTransaction().add(tableData, "tableData").commit();
            // load the data from the web
            //routeLines.setData(loadMyData());
        }
        else {
	        // the data is available in dataFragment.getData()
			waypointListStrings = tableData.getWaypointList();
			rebuildTable();
        }
		
        buildExistingRoutesTable();
        
		nextWaypoint = (AutoCompleteTextView) findViewById(R.id.nextWaypoint);
		nextWaypoint.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
		if (loadingRoute)
			viewRoutes((View) null);
	}
	
	private void buildExistingRoutesTable() { //TODO: Restore selection if one existed
		TableLayout waypointList = (TableLayout) findViewById(R.id.routeList);
		for (int i = 0; i < maxRouteIndex; i++) {
			TableRow newRow = new TableRow(this);
			waypointList.addView(newRow);
			TextView newTextView = new TextView(this);
			newRow.addView(newTextView);
			newTextView.setText("Route " + i);
			newRow.setClickable(true);
			newRow.setTag(i);
			if (selectedRoute == i) {
				newRow.setBackgroundColor(Color.BLUE);
			}
			newRow.setOnClickListener(new View.OnClickListener() {

			    @Override
			    public void onClick(View v) {
			         TableRow tableRow = (TableRow) v;
			         tableRow.setBackgroundColor(Color.BLUE);
			         TableLayout waypointList = (TableLayout) findViewById(R.id.routeList);
			         if (selectedRoute != -1)
			        	 waypointList.getChildAt(selectedRoute).setBackgroundColor(Color.TRANSPARENT);
			         selectedRoute = (Integer) tableRow.getTag();
			    }
			});
		}
	}

	private void getExistingRoutes(SharedPreferences prefs) {
		for (int i = 0; i < 1000; i++) {
			String route = prefs.getString("route" + i, null);
			String[] routeAsArray;
			if (route == null || route.equals("removed")) {
				workingIndex = maxRouteIndex = i;
				break;
			} else {
				List<String> newRoute = new ArrayList<String>();
				routeAsArray = route.split(routeDelimiter);
				for (int j = 0; j < routeAsArray.length; j++) {
					newRoute.add(routeAsArray[j]);
				}
				existingRoutes.add(newRoute);
				/*
				try {
					existingRoutes.add((List<String>) ObjectSerializer.deserialize(route));
				} catch (IOException e) {
					e.printStackTrace();
				}
				*/
			}
		}
		
	}

	public void rebuildTable() {
		Iterator<String> it = waypointListStrings.iterator();
		TableLayout waypointList = (TableLayout) findViewById(R.id.waypointList);
		waypointList.removeAllViews();
		String waypoint = "";
		while (it.hasNext()) {
			waypoint = it.next();
			TableRow newRow = new TableRow(this);
			waypointList.addView(newRow);

			TableLayout.LayoutParams params = (TableLayout.LayoutParams) newRow.getLayoutParams();
			params.width = TableLayout.LayoutParams.MATCH_PARENT;
			params.height = TableLayout.LayoutParams.WRAP_CONTENT;
			newRow.setLayoutParams(params);
			newRow.setWeightSum(15f);
			TextView newTextView = new TextView(this);
			Button newButton = new Button(this);
			newRow.addView(newTextView);
			TableRow.LayoutParams paramsTextView = new TableRow.LayoutParams(
				    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				paramsTextView.weight = 10.0f;
				newTextView.setLayoutParams(paramsTextView);
			newRow.addView(newButton);
			TableRow.LayoutParams paramsButton = new TableRow.LayoutParams(
				    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				paramsButton.weight = 5.0f;
				newButton.setLayoutParams(paramsButton);
			newButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                removeWaypoint(v);
	            }
	        });
			newButton.setText("X");
			newTextView.setText(waypoint);
		}
		if (waypoint != "")
			this.previousWaypoint = waypoint;
	}
	
	public void rebuildMap() {
		mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
		map = mapFragment.getMap();
		map.clear();
		Iterator<List<LatLng>> it = routeListPoints.iterator();
		while (it.hasNext()) {
		    List<LatLng> waypoint = it.next();
		    map.addPolyline(new PolylineOptions()
			.addAll(waypoint)
			.width(5)
			.color(Color.GREEN));
		}
	}
	
  @Override
    public void onDestroy() {
        super.onDestroy();
        // store the data in the fragment
        routeLines.setData(routeListPoints);
        tableData.setWaypoint(waypointListStrings);
        routeLines.setSelectedRoute(selectedRoute);
        routeLines.setLoadingRoute(loadingRoute);
    }
  
  	public void replacePath(List<LatLng> newPath) {
		routeListPoints.set(replacedPath, newPath);
		rebuildMap();
  	}
  	
  	public void saveRoute(View v) {
  		 //save the task list to preference
        SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
        Editor editor = prefs.edit();/*
        try {
        	editor.putString("route" + workingIndex, ObjectSerializer.serialize("Test"));
        	if (workingIndex == maxRouteIndex)
        		maxRouteIndex += 1;
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        String routeAsString = "";
        Iterator<String> it = waypointListStrings.iterator();
		while (it.hasNext()) {
			String waypoint = it.next();
			if (it.hasNext()) {
				routeAsString += waypoint + routeDelimiter;
			} else {
				routeAsString += waypoint;
			}
		}
        editor.putString("route" + workingIndex, routeAsString);
        if (workingIndex == maxRouteIndex)
    		maxRouteIndex += 1;
        editor.commit();
  	}
  	
  	public void viewRoutes(View v) {
  		loadingRoute = true;
  		RelativeLayout viewRoutes = (RelativeLayout) findViewById(R.id.editRouteView);
    	viewRoutes.setVisibility(VISIBLE);
    	RelativeLayout editRoute = (RelativeLayout) findViewById(R.id.createRouteView);
    	editRoute.setVisibility(INVISIBLE);
  	}
  	
  	public void returnToEdit(View v) {
  		loadingRoute = false;
  		RelativeLayout viewRoutes = (RelativeLayout) findViewById(R.id.editRouteView);
    	viewRoutes.setVisibility(INVISIBLE);
    	RelativeLayout editRoute = (RelativeLayout) findViewById(R.id.createRouteView);
    	editRoute.setVisibility(VISIBLE);
    	if (selectedRoute != -1) {
    		workingIndex = selectedRoute;
    		waypointListStrings = existingRoutes.get(selectedRoute);
    		rebuildTable();
    		mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
    		map = mapFragment.getMap();
    		map.clear();
    		routeListPoints.clear();
            TableLayout waypointList = (TableLayout) findViewById(R.id.routeList);
           	waypointList.getChildAt(selectedRoute).setBackgroundColor(Color.TRANSPARENT);
    		RetrieveRoute retrieveRoute = new RetrieveRoute(new ArrayList<String>(waypointListStrings));
    		retrieveRoute.execute((URL) null);
    		selectedRoute = -1;
    	}
  	}

	public void addWaypoint(View view) {
		String waypoint = nextWaypoint.getText().toString();
		if (waypoint.equals("")) return;
		waypointListStrings.add(waypoint);
		TableLayout waypointList = (TableLayout) findViewById(R.id.waypointList);
		TableRow newRow = new TableRow(this);
		waypointList.addView(newRow);

		TableLayout.LayoutParams params = (TableLayout.LayoutParams) newRow.getLayoutParams();
		params.width=TableLayout.LayoutParams.MATCH_PARENT;
		newRow.setLayoutParams(params);
		newRow.setWeightSum(15f);
		TextView newTextView = new TextView(this);
		Button newButton = new Button(this);
		newRow.addView(newTextView);
		TableRow.LayoutParams paramsTextView = new TableRow.LayoutParams(
			    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
			paramsTextView.weight = 14.0f;
			newTextView.setLayoutParams(paramsTextView);
		newRow.addView(newButton);
		TableRow.LayoutParams paramsButton = new TableRow.LayoutParams(
			    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
			paramsButton.weight = 1.0f;
			newButton.setLayoutParams(paramsButton);
		newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                removeWaypoint(v);
            }
        });
		newButton.setText("X");
		newTextView.setText(waypoint);
		
		if (previousWaypoint == null)
			previousWaypoint = waypoint;
		else {
			currentWaypoint = waypoint;
			directionsFetcher = new DirectionsFetcher();
			directionsFetcher.execute((URL) null);
		}
		nextWaypoint.setText("");
	}
	
	private void addMarkersToMap(List<LatLng> route) {
		routeListPoints.add(route);
		mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
		map = mapFragment.getMap();
		map.addPolyline(new PolylineOptions()
			.addAll(route)
			.width(5)
			.color(Color.GREEN));
		    
		CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(route.get(0)) // Sets the center of the map to new location
			.zoom(10) // Sets the zoom
			//.zoom(map.getCameraPosition().zoom) // Sets the zoom
			.bearing(0) // Sets the orientation of the camera to east
			.tilt(0) // Sets the tilt of the camera to 30 degrees
			.build(); // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	public void removeWaypoint(View view) {
		ViewParent parent = view.getParent();
		ViewParent grandParent = parent.getParent();
		TableLayout table = (TableLayout) grandParent;
		//if (parent instanceof TableRow)
		TableRow parentRow = (TableRow) parent;
		
		int rowCount = table.getChildCount();
		String removedWaypoint = "";
		for (int i = 0; i < rowCount; i++) {
			TableRow currentRow = (TableRow) table.getChildAt(i);
			if (currentRow == parentRow) {
				if (i == 0) {
					if (routeListPoints.size() != 0)
						routeListPoints.remove(0);
					removedWaypoint = waypointListStrings.get(0);
					waypointListStrings.remove(0);
					table.removeView(currentRow);
					rebuildMap();
				} else if (i == rowCount - 1) {
					routeListPoints.remove(i - 1);
					removedWaypoint = waypointListStrings.get(i);
					waypointListStrings.remove(i);
					table.removeView(currentRow);
					rebuildMap();
				} else {
					newOrigin = waypointListStrings.get(i - 1);
					newTerminus = waypointListStrings.get(i + 1);
					replacedPath = i - 1;
					waypointListStrings.remove(i);
					routeListPoints.remove(i);
					table.removeView(currentRow);
					RoutePatch routePatch = new RoutePatch();
					routePatch.execute((URL) null);
				}
			}
			if (removedWaypoint == previousWaypoint) {
				if (waypointListStrings.size() != 0)
					previousWaypoint = waypointListStrings.get(waypointListStrings.size() - 1);
				else
					previousWaypoint = null;
			}
		}
		
	}
	//TODO: Put all AutoComplete code in a different class file
	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
		private ArrayList<String> resultList;
		
		public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}
		
		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (false) {
					//if (constraint != null) {
						// Retrieve the autocomplete results.
						resultList = autocomplete(constraint.toString());
						
						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					}
					else {
						notifyDataSetInvalidated();
					}
				}};
			return filter;
		}
	}
	
	private static final String LOG_TAG = "ExampleApp";

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";

	private static final String API_KEY = "AIzaSyBfa2SpUqCAqB_T5MjDl9h0ePdnfSLZmQ8";

	private ArrayList<String> autocomplete(String input) {
	    ArrayList<String> resultList = null;

	    HttpURLConnection conn = null;
	    StringBuilder jsonResults = new StringBuilder();
	    try {
	        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
	        sb.append("?key=" + API_KEY);
	        sb.append("&components=country:uk");
	        sb.append("&input=" + URLEncoder.encode(input, "utf8"));

	        URL url = new URL(sb.toString());
	        conn = (HttpURLConnection) url.openConnection();
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());

	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = in.read(buff)) != -1) {
	            jsonResults.append(buff, 0, read);
	        }
	    } catch (MalformedURLException e) {
	        Log.e(LOG_TAG, "Error processing Places API URL", e);
	        return resultList;
	    } catch (IOException e) {
	        Log.e(LOG_TAG, "Error connecting to Places API", e);
	        return resultList;
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }

	    try {
	        // Create a JSON object hierarchy from the results
	        JSONObject jsonObj = new JSONObject(jsonResults.toString());
	        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

	        // Extract the Place descriptions from the results
	        resultList = new ArrayList<String>(predsJsonArray.length());
	        for (int i = 0; i < predsJsonArray.length(); i++) {
	            resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
	        }
	    } catch (JSONException e) {
	        Log.e(LOG_TAG, "Cannot process JSON results", e);
	    }

	    return resultList;
	}
	
	public static class DirectionsResult {
		@Key("routes")
		public List<Route> routes;
	}

	public static class Route {
		@Key("overview_polyline")
		public OverviewPolyLine overviewPolyLine;
	}

	public static class OverviewPolyLine {
		@Key("points")
		public String points;
	}
	
	static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
	static final JacksonFactory JSON_FACTORY = new JacksonFactory();
	//TODO: Compile the three different direction fetchers into one
	private class DirectionsFetcher extends AsyncTask<URL, Integer, String> {
		private List<LatLng> latLngs = new ArrayList<LatLng>();
		@Override
		protected String doInBackground(URL... params) {
			try {
				HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
				request.setParser(new JsonObjectParser(JSON_FACTORY));
				}
				});

				GenericUrl url = new GenericUrl("http://maps.googleapis.com/maps/api/directions/json");
				url.put("origin", previousWaypoint);
				url.put("destination", currentWaypoint);
				url.put("sensor",false);

				HttpRequest request = requestFactory.buildGetRequest(url);
				HttpResponse httpResponse = request.execute();
				DirectionsResult directionsResult = httpResponse.parseAs(DirectionsResult.class);
				String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
				latLngs = PolyUtil.decode(encodedPoints);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
			}

			protected void onProgressUpdate(Integer... progress) {
			}

			protected void onPostExecute(String result) {
				//clearMarkers();
				addMarkersToMap(latLngs);
				previousWaypoint = currentWaypoint;
			}

		}
	
	private class RoutePatch extends AsyncTask<URL, Integer, String> {
		private List<LatLng> latLngs = new ArrayList<LatLng>();
		@Override
		protected String doInBackground(URL... params) {
			try {
				HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
				request.setParser(new JsonObjectParser(JSON_FACTORY));
				}
				});

				GenericUrl url = new GenericUrl("http://maps.googleapis.com/maps/api/directions/json");
				url.put("origin", newOrigin);
				url.put("destination", newTerminus);
				url.put("sensor",false);

				HttpRequest request = requestFactory.buildGetRequest(url);
				HttpResponse httpResponse = request.execute();
				DirectionsResult directionsResult = httpResponse.parseAs(DirectionsResult.class);
				String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
				latLngs = PolyUtil.decode(encodedPoints);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
			}

			protected void onProgressUpdate(Integer... progress) {
			}

			protected void onPostExecute(String result) {
				//clearMarkers();
				replacePath(latLngs);
			}

		}
	
	private class RetrieveRoute extends AsyncTask<URL, Integer, String> {
		private List<LatLng> latLngs = new ArrayList<LatLng>();
		private List<String> currentRoutes;
		private String origin;
		private String destination;
		
		public RetrieveRoute(List<String> routes) {
	        super();
	        currentRoutes = routes;
	        origin = currentRoutes.remove(0);
	        destination = currentRoutes.get(0);
	    }
		
		@Override
		protected String doInBackground(URL... params) {
			try {
				HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
				request.setParser(new JsonObjectParser(JSON_FACTORY));
				}
				});

				GenericUrl url = new GenericUrl("http://maps.googleapis.com/maps/api/directions/json");
				url.put("origin", origin);
				url.put("destination", destination);
				url.put("sensor",false);

				HttpRequest request = requestFactory.buildGetRequest(url);
				HttpResponse httpResponse = request.execute();
				DirectionsResult directionsResult = httpResponse.parseAs(DirectionsResult.class);
				String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
				latLngs = PolyUtil.decode(encodedPoints);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
			}

			protected void onProgressUpdate(Integer... progress) {
			}

			protected void onPostExecute(String result) {
				//clearMarkers();
				addMarkersToMap(latLngs);
				currentWaypoint = destination;
				previousWaypoint = origin;
				if (currentRoutes.size() > 1) {
					RetrieveRoute retrieveRoute = new RetrieveRoute(currentRoutes);
		    		retrieveRoute.execute((URL) null);
				} else {
					previousWaypoint = currentWaypoint;
				}
			}

		}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		
		 // data objects we want to retain
	    private ArrayList<List<LatLng>> latLngList;
	    private List<String> waypointList;
	    private int selectedRoute;
	    private boolean loadingRoute;
		
		public PlaceholderFragment() {
		}
		
	   public List<String> getWaypointList() {
			return waypointList;
		}

		// this method is only called once for this fragment
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        // retain this fragment
	        setRetainInstance(true);
	        
	    }
	    
	    public void setData(ArrayList<List<LatLng>> data) {
	        this.latLngList = data;
	    }
	    
	    public void setWaypoint(List<String> data) {
	        this.waypointList = data;
	    }
	    
	    public void setSelectedRoute(int selectedRoute) {
	    	this.selectedRoute = selectedRoute;
	    }
	    
	    public int getSelectedRoute() {
	    	return this.selectedRoute;
	    }
	    
	    public ArrayList<List<LatLng>> getRouteList() {
	    	return this.latLngList;
	    }
	    
	    public boolean isLoadingRoute() {
	    	return this.loadingRoute;
	    }
	    
	    public void setLoadingRoute(boolean loadingRoute) {
	    	this.loadingRoute = loadingRoute;
	    }
		
	}
	
}