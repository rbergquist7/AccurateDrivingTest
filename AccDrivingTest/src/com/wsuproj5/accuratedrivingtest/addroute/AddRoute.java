package com.wsuproj5.accuratedrivingtest.addroute;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.wsuproj5.accuratedrivingtest.GoogleMapsQuery;
import com.wsuproj5.accuratedrivingtest.R;
import com.wsuproj5.accuratedrivingtest.R.id;
import com.wsuproj5.accuratedrivingtest.R.layout;
import com.wsuproj5.accuratedrivingtest.addroute.AutoComplete.PlacesAutoCompleteAdapter;


import android.support.v7.app.ActionBarActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class AddRoute extends ActionBarActivity {

	MapFragment mapFragment;
	GoogleMap map;
	AutoCompleteTextView nextWaypoint;
	EditRoute editRoute;
	CreateRoute createRoute;
	public CreateRouteMap createRouteMap;
	GoogleMapsQuery googleMapsQuery;
	RouteState routeState;
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
	    routeState = new RouteState();
		 // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        routeLines = (PlaceholderFragment) fm.findFragmentByTag("routeLines");
        tableData = (PlaceholderFragment) fm.findFragmentByTag("tableData");

        // create the fragment and data the first time
        if (routeLines == null) {
            // add the fragment
            routeLines = new PlaceholderFragment();
            fm.beginTransaction().add(routeLines, "routeLines").commit();
        }
        else {
			routeListPoints = routeLines.getRouteList();
			selectedRoute = routeLines.getSelectedRoute();
			loadingRoute = routeLines.isLoadingRoute();
        }
		
        //order must remain this way
		createRoute = new CreateRoute(this);
		editRoute = new EditRoute(this);
		createRouteMap = new CreateRouteMap(this);
		googleMapsQuery = new GoogleMapsQuery(this);
		
		createRouteMap.rebuildMap();
		
	    // load routes from preference
        SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
        editRoute.getExistingRoutes(prefs);
		
		
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