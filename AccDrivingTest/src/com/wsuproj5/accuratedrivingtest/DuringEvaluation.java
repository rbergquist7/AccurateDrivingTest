package com.wsuproj5.accuratedrivingtest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wsuproj5.accuratedrivingtest.GoogleMapsQuery.DirectionsFetcher;
import com.wsuproj5.accuratedrivingtest.addroute.AddRoute;
import com.wsuproj5.accuratedrivingtest.addroute.CreateRouteMap;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DuringEvaluation extends ActionBarActivity implements 
	GoogleApiClient.ConnectionCallbacks,
	GoogleApiClient.OnConnectionFailedListener,
	LocationListener {
	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final long UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final long FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
	

    GoogleApiClient mLocationClient;
    // Global variable to hold the current location
    Location mCurrentLocation;
    
    boolean mUpdatesRequested;
    
    SharedPreferences mPrefs;
    
    GoogleMapsQuery googleMapsQuery;
    CreateRouteMap createRouteMap;
    MapFragment mapFragment;
    GoogleMap map;
    
    Editor mEditor;
	
	public final static String EXTRA_MESSAGE = "com.wsuproj5.accuratedrivingtest.MESSAGE";
	
    List<LatLng>points = new ArrayList<LatLng>();
	public ArrayList<List<LatLng>> routeListPoints = new ArrayList<List<LatLng>>();
	private static final int VISIBLE = 0;
	private static final int INVISIBLE = 4;
	private String routeToLoad = "";
	List<String> newRoute = new ArrayList<String>();
	
	private PlaceholderFragment routeLines;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_during_evaluation);
		
		//Order must not change
		createRouteMap = new CreateRouteMap(this);
		googleMapsQuery = new GoogleMapsQuery(this);
		
		// Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences",
                Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */

        mLocationClient = new GoogleApiClient.Builder(this)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
        
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        
        // Start with updates turned off
        mUpdatesRequested = false;
        
        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        routeLines = (PlaceholderFragment) fm.findFragmentByTag("routeLines");

        // create the fragment and data the first time
        if (routeLines == null) {
            // add the fragment
            routeLines = new PlaceholderFragment();
            fm.beginTransaction().add(routeLines, "routeLines").commit();
            // load the data from the web
       //     routeLines.setData(loadMyData());
        }
        else {
	        // the data is available in dataFragment.getData()
			routeListPoints = routeLines.getRouteList();
			points = routeLines.getGPSPoints();
        }
		
	    Bundle extras = getIntent().getExtras();
	    if (routeListPoints.size() == 0) {
		    routeToLoad = extras.getString("route");
		    loadRoute();
	    }
	}
	
	// Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
    	DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	Date date = new Date(location.getTime());
    	String formatted = format.format(date);
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()) + "," + formatted;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    	Log.d("Location Update:",
                "Location Updated.");
        
        if (map == null) {
        	mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        	map = mapFragment.getMap();
        	CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to new location
            .zoom(25)                   // Sets the zoom
            .bearing(0)        
            .tilt(0)            
            .build();                  
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
	        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
	        
	        map.clear();
	        MarkerOptions mp = new MarkerOptions();
	        
	        mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
	
	        mp.title("Me ");
	
	        map.addMarker(mp);
	        
	        Polyline line = map.addPolyline(new PolylineOptions()
	        .addAll(points)
	        .width(5)
	        .color(Color.RED));
	        
	        Polyline route = map.addPolyline(new PolylineOptions()
	        .addAll(routeListPoints.get(0))
	        .width(5)
	        .color(Color.BLUE));
	        
	        CameraPosition cameraPosition = new CameraPosition.Builder()
	        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to new location
	        .zoom(map.getCameraPosition().zoom)                   // Sets the zoom
	        .bearing(0)                // Sets the orientation of the camera to east
	        .tilt(0)                   // Sets the tilt of the camera to 30 degrees
	        .build();                   // Creates a CameraPosition from the builder
	        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
    
    @Override
    protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // store the data in the fragment
        routeLines.setData(routeListPoints);
        routeLines.setGPSPoints(points);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

        // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
    }
    
    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
     //   mCurrentLocation = mLocationClient.getLastLocation();
        Log.d("Location Update:",
                "We have connected location client.");
    }
    
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
	
	private void loadRoute() {
		SharedPreferences prefs = getSharedPreferences("existingRoutes", Context.MODE_PRIVATE);
		for (int i = 0; i < 1000; i++) {
			String route = prefs.getString("route" + i, null);
			String[] routeAsArray;
			if (route == null || route.equals("removed")) {
				break;
			} else if (!routeToLoad.equals("route " + i)) {
				continue;
			} else {
				routeAsArray = route.split(AddRoute.routeDelimiter);
				for (int j = 0; j < routeAsArray.length; j++) {
					newRoute.add(routeAsArray[j]);
				}
				/*
				try {
					existingRoutes.add((List<String>) ObjectSerializer.deserialize(route));
				} catch (IOException e) {
					e.printStackTrace();
				}
				*/
			}
		}
		DirectionsFetcher directionsFetcher = googleMapsQuery.new DirectionsFetcher(GoogleMapsQuery.routeTotal, newRoute);
		directionsFetcher.execute();
	}
	
	/*
	 * Called by Location Services when the request to connect the
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {

	    mLocationRequest = LocationRequest.create();
	    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	    mLocationRequest.setInterval(1000); // Update location every second //TODO: Change this from hardcode
	
	    LocationServices.FusedLocationApi.requestLocationUpdates(
	        mLocationClient, mLocationRequest, this);
	}
	
	@Override
	public void onConnectionSuspended(int arg) {
		Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
	}

	 public void extendCommentMenu(View view) {
	    	LinearLayout commentMenu = (LinearLayout) findViewById(R.id.menu_comments);
	    	commentMenu.setVisibility(VISIBLE);
//	    	Intent intent = new Intent(this, DisplayMessageActivity.class);
//	    	EditText editText = (EditText) findViewById(R.id.edit_message);
//	    	String message = editText.getText().toString();
//	    	intent.putExtra(EXTRA_MESSAGE, message);
//	    	startActivity(intent);
	    }
	    
	    public void hideCommentMenu(View view) {
	    	LinearLayout commentMenu = (LinearLayout) findViewById(R.id.menu_comments);
	    	commentMenu.setVisibility(INVISIBLE);
	    }
	    
	    public void revealOBDDataMenu(View view) {
	    	LinearLayout obdDataMenu = (LinearLayout) findViewById(R.id.menu_OBD_data);
	    	obdDataMenu.setVisibility(VISIBLE);
	    }
	    
	    public void hideOBDDataMenu(View view) {
	    	LinearLayout obdDataMenu = (LinearLayout) findViewById(R.id.menu_OBD_data);
	    	obdDataMenu.setVisibility(INVISIBLE);
	    }
	    
	    public void revealRouteProgress(View view) {
	    	LinearLayout routeProgress = (LinearLayout) findViewById(R.id.menu_route_progress);
	    	routeProgress.setVisibility(VISIBLE);
	    }
	    
	    public void hideRouteProgress(View view) {
	    	LinearLayout routeProgress = (LinearLayout) findViewById(R.id.menu_route_progress);
	    	routeProgress.setVisibility(INVISIBLE);
	    }
	    
	    public void revealTestProgress(View view) {
	    	LinearLayout testProgress = (LinearLayout) findViewById(R.id.menu_test_progress);
	    	testProgress.setVisibility(VISIBLE);
	    }
	    
	    public void hideTestProgress(View view) {
	    	LinearLayout testProgress = (LinearLayout) findViewById(R.id.menu_test_progress);
	    	testProgress.setVisibility(INVISIBLE);
	    }
	    
	    // Define a DialogFragment that displays the error dialog
	    public static class ErrorDialogFragment extends DialogFragment {
	        // Global field to contain the error dialog
	        private Dialog mDialog;
	        // Default constructor. Sets the dialog field to null
	        public ErrorDialogFragment() {
	            super();
	            mDialog = null;
	        }
	        // Set the dialog to display
	        public void setDialog(Dialog dialog) {
	            mDialog = dialog;
	        }
	        // Return a Dialog to the DialogFragment.
	        @Override
	        public Dialog onCreateDialog(Bundle savedInstanceState) {
	            return mDialog;
	        }
	    }

	    /*
	     * Handle results returned to the FragmentActivity
	     * by Google Play services
	     */
	    @Override
	    protected void onActivityResult(
	            int requestCode, int resultCode, Intent data) {
	        // Decide what to do based on the original request code
	        switch (requestCode) {

	            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
	            /*
	             * If the result code is Activity.RESULT_OK, try
	             * to connect again
	             */
	                switch (resultCode) {
	                    case Activity.RESULT_OK :
	                    /*
	                     * Try the request again
	                     */

	                    break;
	                }

	        }
	     }

	    private boolean servicesConnected() {
	        // Check that Google Play services is available
	        int resultCode =
	                GooglePlayServicesUtil.
	                        isGooglePlayServicesAvailable(this);
	        // If Google Play services is available
	        if (ConnectionResult.SUCCESS == resultCode) {
	            // In debug mode, log the status
	            Log.d("Location Updates",
	                    "Google Play services is available.");
	            // Continue
	            return true;
	        // Google Play services was not available for some reason.
	        // resultCode holds the error code.
	        } else {
	            // Get the error dialog from Google Play services
	            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
	                    resultCode,
	                    this,
	                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

	            // If Google Play services can provide an error dialog
	            if (errorDialog != null) {
	                // Create a new DialogFragment for the error dialog
	                ErrorDialogFragment errorFragment =
	                        new ErrorDialogFragment();
	                // Set the dialog in the DialogFragment
	                errorFragment.setDialog(errorDialog);
	                // Show the error dialog in the DialogFragment
	                //errorFragment.show(getSupportFragmentManager(),
	                 //       "Location Updates");
	            }
	        }
	        return false;
	    }


	    /*
	     * Called by Location Services if the attempt to
	     * Location Services fails.
	     */
	    @Override
	    public void onConnectionFailed(ConnectionResult connectionResult) {
	        /*
	         * Google Play services can resolve some errors it detects.
	         * If the error has a resolution, try sending an Intent to
	         * start a Google Play services activity that can resolve
	         * error.
	         */
	        if (connectionResult.hasResolution()) {
	            try {
	                // Start an Activity that tries to resolve the error
	                connectionResult.startResolutionForResult(
	                        this,
	                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
	                /*
	                 * Thrown if Google Play services canceled the original
	                 * PendingIntent
	                 */
	            } catch (IntentSender.SendIntentException e) {
	                // Log the error
	                e.printStackTrace();
	            }
	        } else {
	            /*
	             * If no resolution is available, display a dialog to the
	             * user with the error.
	             */
	            //showErrorDialog(connectionResult.getErrorCode());
	        }
	        
	    }
	   
		public static class PlaceholderFragment extends Fragment {
			
			 // data objects we want to retain
		    private ArrayList<List<LatLng>> latLngList;
		    private List<String> waypointList;
		    private List<LatLng> gpsPoints;
		    private int selectedRoute;
		    private boolean loadingRoute;
			
			public PlaceholderFragment() {
			}
			
		   public void setGPSPoints(List<LatLng> points) {
				this.gpsPoints = points;
			}
		   
		   public List<LatLng> getGPSPoints() {
			   return this.gpsPoints;
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
