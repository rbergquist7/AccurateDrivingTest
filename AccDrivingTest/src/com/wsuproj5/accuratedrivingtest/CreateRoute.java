package com.wsuproj5.accuratedrivingtest;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wsuproj5.accuratedrivingtest.AddRoute.DirectionsFetcher;
import com.wsuproj5.accuratedrivingtest.AddRoute.RoutePatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CreateRoute {
	
	public AddRoute addRoute;
	
	public CreateRoute(AddRoute addRoute) {
		this.addRoute = addRoute;
	}
	
	public void addWaypoint(View view) {
		String waypoint = addRoute.nextWaypoint.getText().toString();
		if (waypoint.equals("")) return;
		addRoute.waypointListStrings.add(waypoint);
		TableLayout waypointList = (TableLayout) addRoute.findViewById(R.id.waypointList);
		TableRow newRow = new TableRow(addRoute);
		waypointList.addView(newRow);

		TableLayout.LayoutParams params = (TableLayout.LayoutParams) newRow.getLayoutParams();
		params.width=TableLayout.LayoutParams.MATCH_PARENT;
		newRow.setLayoutParams(params);
		newRow.setWeightSum(15f);
		TextView newTextView = new TextView(addRoute);
		Button newButton = new Button(addRoute);
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
		
		if (addRoute.previousWaypoint == null)
			addRoute.previousWaypoint = waypoint;
		else {
			addRoute.currentWaypoint = waypoint;
			addRoute.directionsFetcher = addRoute.new DirectionsFetcher();
			addRoute.directionsFetcher.execute((URL) null);
		}
		addRoute.nextWaypoint.setText("");
	}

	public void rebuildTable() {
		Iterator<String> it = addRoute.waypointListStrings.iterator();
		TableLayout waypointList = (TableLayout) addRoute.findViewById(R.id.waypointList);
		waypointList.removeAllViews();
		String waypoint = "";
		while (it.hasNext()) {
			waypoint = it.next();
			TableRow newRow = new TableRow(addRoute);
			waypointList.addView(newRow);

			TableLayout.LayoutParams params = (TableLayout.LayoutParams) newRow.getLayoutParams();
			params.width = TableLayout.LayoutParams.MATCH_PARENT;
			params.height = TableLayout.LayoutParams.WRAP_CONTENT;
			newRow.setLayoutParams(params);
			newRow.setWeightSum(15f);
			TextView newTextView = new TextView(addRoute);
			Button newButton = new Button(addRoute);
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
			addRoute.previousWaypoint = waypoint;
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
					if (addRoute.routeListPoints.size() != 0)
						addRoute.routeListPoints.remove(0);
					removedWaypoint = addRoute.waypointListStrings.get(0);
					addRoute.waypointListStrings.remove(0);
					table.removeView(currentRow);
					addRoute.createRouteMap.rebuildMap();
				} else if (i == rowCount - 1) {
					addRoute.routeListPoints.remove(i - 1);
					removedWaypoint = addRoute.waypointListStrings.get(i);
					addRoute.waypointListStrings.remove(i);
					table.removeView(currentRow);
					addRoute.createRouteMap.rebuildMap();
				} else {
					addRoute.newOrigin = addRoute.waypointListStrings.get(i - 1);
					addRoute.newTerminus = addRoute.waypointListStrings.get(i + 1);
					addRoute.replacedPath = i - 1;
					addRoute.waypointListStrings.remove(i);
					addRoute.routeListPoints.remove(i);
					table.removeView(currentRow);
					RoutePatch routePatch = addRoute.new RoutePatch();
					routePatch.execute((URL) null);
				}
			}
			if (removedWaypoint == addRoute.previousWaypoint) {
				if (addRoute.waypointListStrings.size() != 0)
					addRoute.previousWaypoint = addRoute.waypointListStrings.get(addRoute.waypointListStrings.size() - 1);
				else
					addRoute.previousWaypoint = null;
			}
		}
	}
	
  	public void saveRoute(View v) {
 		 //save the task list to preference
       SharedPreferences prefs = addRoute.getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
       Editor editor = prefs.edit();/*
       try {
       	editor.putString("route" + workingIndex, ObjectSerializer.serialize("Test"));
       	if (workingIndex == maxRouteIndex)
       		maxRouteIndex += 1;
       } catch (IOException e) {
           e.printStackTrace();
       }*/
       String routeAsString = "";
       Iterator<String> it = addRoute.waypointListStrings.iterator();
		while (it.hasNext()) {
			String waypoint = it.next();
			if (it.hasNext()) {
				routeAsString += waypoint + AddRoute.routeDelimiter;
			} else {
				routeAsString += waypoint;
			}
		}
       editor.putString("route" + addRoute.workingIndex, routeAsString);
       if (addRoute.workingIndex == addRoute.maxRouteIndex)
    	   addRoute.maxRouteIndex += 1;
       editor.commit();
 	}
}
