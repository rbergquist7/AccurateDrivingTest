package com.wsuproj5.accuratedrivingtest;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
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
import com.wsuproj5.accuratedrivingtest.addroute.AddRoute;
import com.wsuproj5.accuratedrivingtest.addroute.CreateRouteMap;

public class GoogleMapsQuery {
	
	AddRoute addRoute;
	DuringEvaluation duringEvaluation;
	private CreateRouteMap map;
	public static final int routeSegment = 0;
	public static final int routePatch = 1;
	public static final int routeTotal = 2;
	
	public GoogleMapsQuery(AddRoute addRoute) {
		this.addRoute = addRoute;
		map = addRoute.createRouteMap;
	}
	
	public GoogleMapsQuery(DuringEvaluation duringEvaluation) {
		this.duringEvaluation = duringEvaluation;
		map = duringEvaluation.createRouteMap;
		//map = duringEvaluation.createRouteMap;
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
		private List<String> currentRoutes;
		private String origin;
		private String terminus;
		int choice;
		
		public DirectionsFetcher (int choice, String origin, String terminus) {
			this.choice = choice;
			this.origin = origin;
			this.terminus = terminus;
		}
		
		public DirectionsFetcher(int choice, List<String> routes) {
			if (choice == routeTotal) {
				this.choice = choice;
				currentRoutes = routes;
		        origin = currentRoutes.remove(0);
		        terminus = currentRoutes.get(0);
			}
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
				url.put("destination", terminus);
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
				if (latLngs.size() == 0) { //if user entered invalid location
					if (addRoute.waypointListStrings.size() == 2) {
						addRoute.waypointListStrings.remove(0);
						addRoute.waypointListStrings.remove(0);
						while (addRoute.routeListPoints.size() != 0)
							addRoute.routeListPoints.remove(0);
						addRoute.routeState.origin = null;
						addRoute.routeState.terminus = null;
					} else {
						addRoute.waypointListStrings.remove(addRoute.waypointListStrings.size() - 1);
						addRoute.routeState.terminus = addRoute.waypointListStrings.get(addRoute.waypointListStrings.size() - 1);
					}
					addRoute.createRoute.rebuildTable();
				} else if (choice == routePatch)
					addRoute.replacePath(latLngs);
				else if (choice == routeSegment) {
					map.addMarkersToMap(latLngs);
				} else if (choice == routeTotal) {
					map.addMarkersToMap(latLngs);
					//if there are still routes left to retrieve
					if (currentRoutes.size() > 1) {
						DirectionsFetcher retrieveRoute = new DirectionsFetcher(routeTotal, currentRoutes);
			    		retrieveRoute.execute((URL) null);
					} 
				}
			}

	}
}
