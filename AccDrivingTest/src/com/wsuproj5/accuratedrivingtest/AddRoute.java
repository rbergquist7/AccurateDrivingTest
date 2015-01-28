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
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AddRoute extends ActionBarActivity {

	MapFragment mapFragment;
	GoogleMap map;
	AutoCompleteTextView nextWaypoint;
	DirectionsFetcher directionsFetcher;
	EditRoute editRoute;
	CreateRoute createRoute;
	CreateRouteMap createRouteMap;
	String previousWaypoint = null;
	String currentWaypoint = null;
	String newOrigin = null;
	String newTerminus = null;
	int replacedPath;
	int maxRouteIndex;
	int workingIndex;
	int selectedRoute = -1;
	boolean loadingRoute = false;
	static final int VISIBLE = 0;
	static final int INVISIBLE = 4;
	
	//TODO: Compile data fragments into one fragment
	private PlaceholderFragment routeLines;
	private PlaceholderFragment tableData;
	public static String routeDelimiter = "!--ROUTEDELIMITER--!";
	ArrayList<List<LatLng>> routeListPoints = new ArrayList<List<LatLng>>();
	List<String> waypointListStrings = new ArrayList<String>();
	ArrayList<List<String>> existingRoutes = new ArrayList<List<String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_route);
		createRoute = new CreateRoute(this);
		editRoute = new EditRoute(this);
		createRouteMap = new CreateRouteMap(this);
		
	    // load routes from preference
        SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
        editRoute.getExistingRoutes(prefs);
		
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
			createRouteMap.rebuildMap();
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
			createRoute.rebuildTable();
        }
		
        editRoute.buildExistingRoutesTable();
        AutoComplete autoComplete = new AutoComplete();
		nextWaypoint = (AutoCompleteTextView) findViewById(R.id.nextWaypoint);
		nextWaypoint.setAdapter(autoComplete.new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
		if (loadingRoute)
			editRoute.viewRoutes((View) null);
	}
	
	public void viewRoutes(View v) {
		editRoute.viewRoutes(v);
	}
	
	public void returnToEdit(View v) {
		editRoute.returnToEdit(v);
	}
	
	public void saveRoute(View v) {
		createRoute.saveRoute(v);
	}
	
	public void addWaypoint(View v) {
		createRoute.addWaypoint(v);
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
		createRouteMap.rebuildMap();
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
	public class DirectionsFetcher extends AsyncTask<URL, Integer, String> {
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
				createRouteMap.addMarkersToMap(latLngs);
				previousWaypoint = currentWaypoint;
			}

		}
	
	public class RoutePatch extends AsyncTask<URL, Integer, String> {
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
	
	public class RetrieveRoute extends AsyncTask<URL, Integer, String> {
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
				createRouteMap.addMarkersToMap(latLngs);
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