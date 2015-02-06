package com.wsuproj5.accuratedrivingtest.addroute;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.MapFragment;
import com.wsuproj5.accuratedrivingtest.GoogleMapsQuery;
import com.wsuproj5.accuratedrivingtest.R;
import com.wsuproj5.accuratedrivingtest.GoogleMapsQuery.*;
import com.wsuproj5.accuratedrivingtest.R.id;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class EditRoute {
	
	AddRoute addRoute;
	RouteState routeState;
	
	public EditRoute(AddRoute addRoute) {
		this.addRoute = addRoute;
		this.routeState = addRoute.routeState;
	}
	
	//Builds the table that exists in "View Routes" button view
	public void buildExistingRoutesTable() {
		TableLayout waypointList = (TableLayout) addRoute.findViewById(R.id.routeList);
		for (int i = 0; i < addRoute.maxRouteIndex; i++) {
			TableRow newRow = new TableRow(addRoute);
			waypointList.addView(newRow);
			TextView newTextView = new TextView(addRoute);
			newRow.addView(newTextView);
			newTextView.setText("Route " + i);
			newRow.setClickable(true);
			newRow.setTag(i);
			if (addRoute.selectedRoute == i) {
				newRow.setBackgroundColor(Color.BLUE);
			}
			newRow.setOnClickListener(new View.OnClickListener() {

			    @Override
			    public void onClick(View v) {
			         TableRow tableRow = (TableRow) v;
			         tableRow.setBackgroundColor(Color.BLUE);
			         TableLayout waypointList = (TableLayout) addRoute.findViewById(R.id.routeList);
			         if (addRoute.selectedRoute != -1)
			        	 waypointList.getChildAt(addRoute.selectedRoute).setBackgroundColor(Color.TRANSPARENT);
			         addRoute.selectedRoute = (Integer) tableRow.getTag();
			    }
			});
		}
	}
	
	public void getExistingRoutes(SharedPreferences prefs) {
		for (int i = 0; i < 1000; i++) {
			String route = prefs.getString("route" + i, null);
			String[] routeAsArray;
			if (route == null || route.equals("removed")) {
				addRoute.workingIndex = addRoute.maxRouteIndex = i;
				break;
			} else {
				List<String> newRoute = new ArrayList<String>();
				routeAsArray = route.split(AddRoute.routeDelimiter);
				for (int j = 0; j < routeAsArray.length; j++) {
					newRoute.add(routeAsArray[j]);
				}
				addRoute.existingRoutes.add(newRoute);
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
	
	public void returnToEdit(View v) {
		addRoute.loadingRoute = false;
  		RelativeLayout viewRoutes = (RelativeLayout) addRoute.findViewById(R.id.editRouteView);
    	viewRoutes.setVisibility(addRoute.INVISIBLE);
    	RelativeLayout editRoute = (RelativeLayout) addRoute.findViewById(R.id.createRouteView);
    	editRoute.setVisibility(addRoute.VISIBLE);
    	if (addRoute.selectedRoute != -1) {
    		addRoute.workingIndex = addRoute.selectedRoute;
    		addRoute.waypointListStrings = addRoute.existingRoutes.get(addRoute.selectedRoute);
    		addRoute.createRoute.rebuildTable();
    		addRoute.mapFragment = ((MapFragment) addRoute.getFragmentManager().findFragmentById(R.id.map));
    		addRoute.map = addRoute.mapFragment.getMap();
    		addRoute.map.clear();
    		addRoute.routeListPoints.clear();
            TableLayout waypointList = (TableLayout) addRoute.findViewById(R.id.routeList);
           	waypointList.getChildAt(addRoute.selectedRoute).setBackgroundColor(Color.TRANSPARENT);
    		DirectionsFetcher retrieveRoute = addRoute.googleMapsQuery.new DirectionsFetcher(GoogleMapsQuery.routeTotal, new ArrayList<String>(addRoute.waypointListStrings));
    		routeState.origin = addRoute.waypointListStrings.get(addRoute.waypointListStrings.size() - 1);
    		retrieveRoute.execute((URL) null);
    		addRoute.selectedRoute = -1;
    	}
  	}
		
	public void viewRoutes(View v) {
  		addRoute.loadingRoute = true;
  		RelativeLayout viewRoutes = (RelativeLayout) addRoute.findViewById(R.id.editRouteView);
    	viewRoutes.setVisibility(AddRoute.VISIBLE);
    	RelativeLayout editRoute = (RelativeLayout) addRoute.findViewById(R.id.createRouteView);
    	editRoute.setVisibility(AddRoute.INVISIBLE);
  	}
}
