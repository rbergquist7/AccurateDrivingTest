package com.wsuproj5.accuratedrivingtest;

import java.util.Iterator;
import java.util.List;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class CreateRouteMap {

	private AddRoute addRoute;

	public CreateRouteMap(AddRoute addRoute) {
		this.addRoute = addRoute;
	}
	
	public void addMarkersToMap(List<LatLng> route) {
		addRoute.routeListPoints.add(route);
		addRoute.mapFragment = ((MapFragment) addRoute.getFragmentManager().findFragmentById(R.id.map));
		addRoute.map = addRoute.mapFragment.getMap();
		addRoute.map.addPolyline(new PolylineOptions()
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
		addRoute.map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	public void rebuildMap() {
		addRoute.mapFragment = ((MapFragment) addRoute.getFragmentManager().findFragmentById(R.id.map));
		addRoute.map = addRoute.mapFragment.getMap();
		addRoute.map.clear();
		Iterator<List<LatLng>> it = addRoute.routeListPoints.iterator();
		while (it.hasNext()) {
		    List<LatLng> waypoint = it.next();
		    addRoute.map.addPolyline(new PolylineOptions()
			.addAll(waypoint)
			.width(5)
			.color(Color.BLUE));
		}
	}
}
