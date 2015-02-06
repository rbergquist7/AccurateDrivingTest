package com.wsuproj5.accuratedrivingtest.addroute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.FragmentManager;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wsuproj5.accuratedrivingtest.DuringEvaluation;
import com.wsuproj5.accuratedrivingtest.R;
import com.wsuproj5.accuratedrivingtest.R.id;

public class CreateRouteMap {

	private AddRoute addRoute;
	private DuringEvaluation duringEvaluation;
	private GoogleMap map;
	private FragmentManager fragmentManager;
	private MapFragment mapFragment;
	private ArrayList<List<LatLng>> routeListPoints;

	public CreateRouteMap(AddRoute addRoute) {
		this.addRoute = addRoute;
		this.map = addRoute.map;
		this.mapFragment = ((MapFragment) addRoute.getFragmentManager().findFragmentById(R.id.map));
		if (this.mapFragment == null)
			System.err.println("ERROR: CreateRouteMap was unable to find map fragment with id 'map'");
		if (this.map == null)
			this.map = mapFragment.getMap();
		this.routeListPoints = addRoute.routeListPoints;
	}
	
	public CreateRouteMap(DuringEvaluation duringEvaluation) {
		this.duringEvaluation = duringEvaluation;
		this.mapFragment = ((MapFragment) duringEvaluation.getFragmentManager().findFragmentById(R.id.map));
		if (this.mapFragment == null)
			System.err.println("ERROR: CreateRouteMap was unable to find map fragment with id 'map'");
		if (this.map == null)
			this.map = mapFragment.getMap();
		this.routeListPoints = duringEvaluation.routeListPoints;
	}
	
	public void addMarkersToMap(List<LatLng> route) {
		routeListPoints.add(route);
		map.addPolyline(new PolylineOptions()
			.addAll(route)
			.width(5)
			.color(Color.BLUE));
		    
		CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(route.get(0)) // Sets the center of the map to new location
			.zoom(10) // Sets the zoom
			//.zoom(map.getCameraPosition().zoom) // Sets the zoom
			.bearing(0) // Sets the orientation of the camera to east
			.tilt(0) // Sets the tilt of the camera to 30 degrees
			.build(); // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	public void rebuildMap() {
		map.clear();
		Iterator<List<LatLng>> it = routeListPoints.iterator();
		while (it.hasNext()) {
		    List<LatLng> waypoint = it.next();
		    map.addPolyline(new PolylineOptions()
			.addAll(waypoint)
			.width(5)
			.color(Color.BLUE));
		}
	}
}
