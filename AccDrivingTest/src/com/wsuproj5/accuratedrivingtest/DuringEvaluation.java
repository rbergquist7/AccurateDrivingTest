package com.wsuproj5.accuratedrivingtest;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONObject;

//instead of always running all. only get MPH, get rest only when needed. when hit display 
//obd, get it one time, show pass/fail from HPH, connection = T/F,
//possibly add refresh button. removes memory issue since dont need so many loops running

import com.fatfractal.ffef.FFException;
import com.fatfractal.ffef.FatFractal;
import com.fatfractal.ffef.impl.FatFractalHttpImpl;
import com.fatfractal.ffef.json.FFObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wsuproj5.accuratedrivingtest.GoogleMapsQuery.DirectionsFetcher;
import com.wsuproj5.accuratedrivingtest.addroute.AddRoute;
import com.wsuproj5.accuratedrivingtest.addroute.CreateRouteMap;
import com.wsuproj5.accuratedrivingtest.testing.JObject;
import com.wsuproj5.accuratedrivingtest.testing.TestDetailsGeneral;
import com.wsuproj5.accuratedrivingtest.testing.TestingJSON;
import com.wsuproj5.comments.CommentTemplates;
import com.wsuproj5.comments.CommentsFragment;

import de.greenrobot.event.EventBus;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DuringEvaluation extends ActionBarActivity implements 
	GoogleApiClient.ConnectionCallbacks,
	GoogleApiClient.OnConnectionFailedListener,
	LocationListener,PairedDevicesDialog.PairedDeviceDialogListener, OnMarkerClickListener{
	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    SecurePreferences pref;
    private Driver driver;
    private static FatFractal ff;
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    
    private String titleComment = "Comment:";
	
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final long UPDATE_INTERVAL_IN_SECONDS = 3;
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
	int counter = 0;
	boolean routeDrawn = false;
    GoogleApiClient mLocationClient;
    // Global variable to hold the current location
    Location mCurrentLocation;
    boolean mUpdatesRequested;
    View v;
    
    //Used for removing fragments properly on onBackPressed()
    private boolean viewingTestInfo = false;
	public TestDetailsGeneral previousFragment;
    
    SharedPreferences mPrefs;
    DuringEvaluation that = this;
    
    //Various objects used by DuringEvaluation
    GoogleMapsQuery googleMapsQuery;
    CreateRouteMap createRouteMap;
    MapFragment mapFragment;
    GoogleMap map;
    
    //List of test data that applies to the current evaluation
    ArrayList<JObject> testDataSelected;
    //Used for adding comments
    ArrayList<Marker> commentAll = new ArrayList<Marker>();
    boolean commentSet = false;
    String comment = "";
    Marker markerCurrent;
    
    Editor mEditor;
	
	public final static String EXTRA_MESSAGE = "com.wsuproj5.accuratedrivingtest.MESSAGE";
	
    List<LatLng>points = new ArrayList<LatLng>();
	public ArrayList<List<LatLng>> routeListPoints = new ArrayList<List<LatLng>>();
	private static final int VISIBLE = 0;
	private static final int INVISIBLE = 4;
	private String routeToLoad = "";
	private CommentsFragment cF;
	List<String> newRoute = new ArrayList<String>();
	
	private PlaceholderFragment routeLines;

	private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_DIALOG = "dialog";
    private static final String NO_BLUETOOTH = "Oops, your device doesn't support bluetooth";
    
    // Commands
    private static final String[] INIT_COMMANDS = {"AT Z", "AT SP 0", "0105", "010C", "010D", "0131"};
    private int mCMDPointer = -1;

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 101;
   
    // Message types accessed from the BluetoothIOGateway Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names accesses from the BluetoothIOGateway Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast_message";

    // Bluetooth
    private BluetoothIOGateway mIOGateway;
    private static BluetoothAdapter mBluetoothAdapter;
    private DeviceBroadcastReceiver mReceiver;
    private PairedDevicesDialog dialog;
    private List<BluetoothDevice> mDeviceList;

    // Widgets
    private TextView mConnectionStatus;
    private TextView mMonitor;
    
    // Variable def
    boolean sendAll = false;
    int commandNumber = 0;
    private int tracker = 1;
    private int AvgMPH = 0;
    private int MPH = 0;
    private int RPM = 0;
    private int distanceTraveled = 0;
    private static StringBuilder mSbCmdResp;
    private static StringBuilder mPartialResponse;
    private String mConnectedDeviceName;
    private final Handler mMsgHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1)
                    {
                        case BluetoothIOGateway.STATE_CONNECTING:
                            mConnectionStatus.setText(getString(R.string.BT_connecting));
                            mConnectionStatus.setBackgroundColor(Color.YELLOW);
                            break;

                        case BluetoothIOGateway.STATE_CONNECTED:
                            mConnectionStatus.setText(getString(R.string.BT_status_connected_to) + " " + mConnectedDeviceName);
                            mConnectionStatus.setBackgroundColor(Color.GREEN);
                           // sendDefaultCommands();
                            break;

                        case BluetoothIOGateway.STATE_LISTEN:
                        case BluetoothIOGateway.STATE_NONE:
                            mConnectionStatus.setText(getString(R.string.BT_status_not_connected));
                            mConnectionStatus.setBackgroundColor(Color.RED);
                            break;

                        default:
                            break;
                    }
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    readMessage = readMessage.trim();
                    readMessage = readMessage.toUpperCase(Locale.US);
                    displayLog(mConnectedDeviceName + ": " + readMessage);
                    //int temp = readMessage.length();
                    if(readMessage.length() == 0){
                    	displayLog("breaking, length of read message was zero");
                    	break;
                    }
                    char lastChar = readMessage.charAt(readMessage.length() - 1);
                    if (lastChar == '>')
                    {
                        if(sendAll == true) {
                        	parseResponse(mPartialResponse.toString() + readMessage);
                        }
                        else{
                        	parseResponse_single_command(mPartialResponse.toString() + readMessage);
                        }
                        mPartialResponse.setLength(0);
                    }
                    else 
                    {
                        mPartialResponse.append(readMessage);
                    }
                    break;

                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    displayLog("Me: " + writeMessage);
                    mSbCmdResp.append("W>>");
                    mSbCmdResp.append(writeMessage);
                    mSbCmdResp.append("\n");
                    mMonitor.setText(mSbCmdResp.toString());
                    break;

                case MESSAGE_TOAST:
                    //displayMessage(msg.getData().getString(TOAST));
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
            }
        }

    };
    
    @Override
	public void onBackPressed()
	{
		if (previousFragment != null) {
			getFragmentManager().popBackStack(); // remove fragment
			getFragmentManager() 
			.beginTransaction()
			.remove(previousFragment)
			.commit();
			getFragmentManager() //show previous fragment
			.beginTransaction()
			.add(R.id.menu_test_progress, previousFragment)
			.commit();
			previousFragment = previousFragment.previousFragment;
			return;
		}
		
	    if (viewingTestInfo) { //Case: Last fragment on the stack
	    	viewingTestInfo = false;
	    	v.setVisibility(View.INVISIBLE);
	    	getFragmentManager().popBackStack(); // remove fragment
	        return;
	    }
	    if (findViewById(R.id.menu_comments).getVisibility() == View.VISIBLE) { //if our comments fragment is visible, hide it and pop it
	    	while (!cF.currentView && cF.returnTemplate) {
	    		if (cF.currentFragment != null) {
		    		getFragmentManager().popBackStack(); // remove fragment
		    		getFragmentManager()
		    		.beginTransaction()
		    		.remove(cF.currentFragment)
		    		.commit();
	    		} else {
	    			//getFragmentManager().popBackStack(); // remove fragment
		    		getFragmentManager()
		    		.beginTransaction()
		    		.remove(cF)
		    		.add(R.id.menu_comments, cF)
		    		.commit();
	    			cF.returnTemplate = false;
	    			cF.currentView = true;
	    			cF.setCommentContent();
	    			return;
	    		}
	    		cF.currentFragment = cF.currentFragment.parent;
	    	}
	    	if (!cF.currentView)
	    		cF.currentFragment = cF.currentFragment.parent;
	    	if (!cF.currentView && cF.currentFragment != null) {
	    		getFragmentManager().popBackStack(); // remove fragment
	    		getFragmentManager()
	    		.beginTransaction()
	    		.remove(cF.currentFragment)
	    		.add(R.id.menu_comments, cF.currentFragment)
	    		.commit();
	    	} else if (!cF.currentView){
	    		getFragmentManager().popBackStack(); // remove fragment
	    		getFragmentManager()
	    		.beginTransaction()
	    		.remove(cF)
	    		.add(R.id.menu_comments, cF)
	    		.commit();
	    		cF.currentView = true; //clear cF to indicate that we are on the comments menu 	
	    		return;
		    } 
	    	if (cF.currentView) {
		    	findViewById(R.id.menu_comments).setVisibility(View.INVISIBLE);
		    	getFragmentManager().popBackStack(); // remove fragment
//		    	mSbCmdResp.setLength(0); //TODO: Parker moved this. Looks like placeholder for storing a comment
//	            mMonitor.setText("");
	            comment = cF.commentTemplate;
	            driver.setPass_Fail("False"); //if template was used, you failed
	            commentSet = true;
		    }
	    	return;
	    }
	    	super.onBackPressed();
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_during_evaluation);
		 pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
		 driver = new Driver();
        mMonitor = (TextView) findViewById(R.id.obd_data_view);
       	mMonitor.setText(getString(R.string.bt_not_available) + " attempting to connect...");
       // mTest_progress = (TextView) findViewById(R.id.test_progress_data_view);
       // mTest_progress.setText("Passing...For now!");
       	/* Stop screen from resting */
       	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mConnectionStatus = (TextView) findViewById(R.id.tvConnectionStatus);
	        
		 driver.setdriversLicense(pref.getString("drivers_licence_number"));

	        // make sure user has Bluetooth hardware
	        displayLog("Try to check hardware...");
	        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (mBluetoothAdapter == null)
	        {
	            // Device does not support Bluetooth
	            displayMessage(NO_BLUETOOTH);
	            displayLog(NO_BLUETOOTH);
	            
	            DuringEvaluation.this.finish();
	        }
	        // log
	        displayLog("Bluetooth found.");
	        
	        // Init variables
	        mSbCmdResp = new StringBuilder();
	        mPartialResponse = new StringBuilder();
	        mIOGateway = new BluetoothIOGateway(this, mMsgHandler);
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
			//driver.setM_drive_route(points.toString());
        }
		
	    Bundle extras = getIntent().getExtras();
	    loadTest(extras.getString("test"));
	    if (routeListPoints.size() == 0) {
		    routeToLoad = extras.getString("route");
		    loadRoute();
	    }
	    
	    mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
	    mapFragment.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				if (that.commentSet){
					MarkerOptions marker = new MarkerOptions().position(
	                        new LatLng(point.latitude, point.longitude))
	                        .snippet(that.comment)
	                        .title(titleComment);
	                saveComment(point.latitude,point.longitude,that.comment);

					that.markerCurrent = map.addMarker(marker);
	                that.commentSet = false;
	                that.comment = "";
	                that.commentAll.add(markerCurrent);
	                findViewById(R.id.comment_instructions).setVisibility(View.INVISIBLE);
				}
			}
		});
	    mapFragment.getMap().setOnMarkerClickListener(this);
	    
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker.equals(markerCurrent)) { //If someone tapped a marker being displayed, remove it as the current marker
			markerCurrent.hideInfoWindow();
			markerCurrent = null;
			System.out.println("markerCurrent is null");
		} else {
			System.out.println("markerCurrent is set to something else");
			markerCurrent = marker;
			markerCurrent.showInfoWindow();
		}
		return false;
	}
	
	// Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {

    	mSbCmdResp.setLength(0);
        mMonitor.setText("");
    	commandNumber = 1;
    	sendOBD2CMD("AT SP 0");
    	
    	// Report to the UI that the location was updated
    	DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US);
    	Date date = new Date(location.getTime());
    	String formatted = format.format(date);
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude()) + "," + formatted;

       // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        
        //store lat/long points driven
        if(driver.getLatLon() == null){
        	driver.setLatLon(Double.toString(location.getLatitude()) + "," +
                    Double.toString(location.getLongitude()));
        }
        else{
        	driver.setLatLon(driver.getLatLon() + "\n" + Double.toString(location.getLatitude()) + "," +
        			Double.toString(location.getLongitude()));
        	
        }
        
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
	        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.car);
	        mp.icon(bitmap);
	        mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
	      
	        mp.title("Me ");
	
	        map.addMarker(mp);
	       if(points != null){
	    	   map.addPolyline(new PolylineOptions()
	    	   .addAll(points)
	    	   .width(5)
	    	   .color(Color.RED));
	    	   
	       }
    	   for (int i = 0; i < commentAll.size(); i++) {
    		   Marker m = commentAll.get(i);
				if (commentAll.get(i).equals(markerCurrent)) {
				   Marker temp = map.addMarker(new MarkerOptions()
				   .position(m.getPosition())
				   .title(m.getTitle())
				   .snippet(m.getSnippet())
				   );
				   temp.showInfoWindow();
				   commentAll.remove(i);
				   commentAll.add(i,temp);
				   markerCurrent = temp;
				}
    		   else {
    			   Marker temp = map.addMarker(new MarkerOptions()
    			   .position(m.getPosition())
    			   .title(titleComment)
    			   .snippet(m.getSnippet())
    			   );
    			   commentAll.remove(i);
    			   commentAll.add(i, temp);
    		   }
    	   }
	       if(routeListPoints != null && routeListPoints.size() > 0){
	    	   map.addPolyline(new PolylineOptions()
	    	   .addAll(routeListPoints.get(0))
	    	   .width(5)
	    	   .color(Color.BLUE));
	    	   
	    	   
	       }
	        
	        CameraPosition cameraPosition = new CameraPosition.Builder()
	        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to new location
	        .zoom(map.getCameraPosition().zoom)                   // Sets the zoom
	        .bearing(0)                // Sets the orientation of the camera
	        .tilt(0)                   // Sets the tilt of the camera to
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

        // Unregister EventBus
        EventBus.getDefault().unregister(this);    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
        // Un register receiver
        if (mReceiver != null)
        {
            unregisterReceiver(mReceiver);
        }

        // Stop scanning if is in progress
        cancelScanning();

        // Stop mIOGateway
        if (mIOGateway != null)
        {
            mIOGateway.stop();
        }
        
        // Clear StringBuilder
        if (mSbCmdResp.length() > 0)
        {
            mSbCmdResp.setLength(0);
        }        // store the data in the fragment
        routeLines.setData(routeListPoints);
        routeLines.setGPSPoints(points);
    }
    
   

	@Override
    protected void onResume() {
    	super.onResume();
        // Register EventBus
        EventBus.getDefault().register(this);
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

        if (mBluetoothAdapter == null)
        {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        // make sure Bluetooth is enabled
        displayLog("Try to check availability...");
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            displayLog("Bluetooth is available");

            queryPairedDevices();
            setupMonitor();
        }        // Connect the client.
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
        DuringEvaluation.ff = getFF();

        try {
        	ff.login("accuratedrivingtest@gmail.com", "AccurateDrivingT3st");
        	pref = new SecurePreferences(getBaseContext(),"MyPrefs", "cs421encrypt", true);
        	driver.setAvgMPH(AvgMPH);
        	driver.setEvaluatorsName(pref.getString("evaluator_name"));
        	driver.setM_drive_route(this.routeListPoints.toString());
        	
			ff.createObjAtUri(driver, "/driver");
			
		} catch (FFException e) {
			e.printStackTrace();
		}
        
		// pref.put("average_MPH", Integer.toString(AvgMPH));
		 
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
	    mLocationRequest.setInterval(1000); // Update location every second
	    //TODO: Change this from hard code
	
	    LocationServices.FusedLocationApi.requestLocationUpdates(
	        mLocationClient, mLocationRequest, this);
	}
	
	@Override
	public void onConnectionSuspended(int arg) {
		//Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
	}
	public void saveComment(double latitude, double longitude, String Comment_To_Add){

		String current_comments = driver.getcomments();
		
		if(current_comments == null){
			current_comments = "-" + Comment_To_Add + " \n( " + String.valueOf(latitude) + " , " + String.valueOf(longitude) + " )";
		}
		else{
			current_comments = current_comments + "\n-" + Comment_To_Add + " \n( " + String.valueOf(latitude) + " , " + String.valueOf(longitude) + " )";
		}
		driver.setcomments(current_comments);

	}
	 public void extendCommentMenu(View view) {
	    	LinearLayout commentMenu = (LinearLayout) findViewById(R.id.menu_comments);
	    	commentMenu.setVisibility(VISIBLE);
	    	CommentsFragment fr = new CommentsFragment();
	    	cF = fr;
	    	cF.currentView = true;
	    	FragmentManager fm = getFragmentManager();
	    	fm.beginTransaction()
	    	.add(R.id.menu_comments, fr)
	    	.addToBackStack(null)
	    	.commit();
	    }
	    
	    public void hideCommentMenu(View view) {
	    	LinearLayout commentMenu = (LinearLayout) findViewById(R.id.menu_comments);
	    	commentMenu.setVisibility(INVISIBLE);

	    }
	    
		public void viewTemplates(View v) {
			cF.currentView = false;
			Fragment fr = new CommentTemplates(cF);
			cF.currentFragment = (CommentTemplates) fr;
			getFragmentManager()
			.beginTransaction()
			.hide(cF)
			.add(R.id.menu_comments, fr)
			.addToBackStack(null)
			.commit();
		}
		
		public void addComment(View v) {
			findViewById(R.id.comment_instructions).setVisibility(View.VISIBLE);
			EditText temp = (EditText) findViewById(R.id.Field_Comment);
			if (cF.commentTemplate == null)
				cF.commentTemplate = temp.getText().toString();
			onBackPressed();
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
	    	v = testProgress;
	    	testProgress.setVisibility(VISIBLE);
	    	//inflate new test details fragment
	    	viewingTestInfo = true;
	        TestDetailsGeneral fr = new TestDetailsGeneral(testDataSelected, this);
	        FragmentManager fm = getFragmentManager();
	        android.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
    		fragmentTransaction.add(R.id.menu_test_progress, fr)
    		.addToBackStack(null)
	        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
	        .show(fr)
	        .commit();
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
		@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_scan:
                queryPairedDevices();
                setupMonitor();
                return true;
            
            case R.id.menu_send_cmd:
                mCMDPointer = -1;
                sendDefaultCommands();
                return true;
            
            case R.id.menu_clr_scr:
                mSbCmdResp.setLength(0);
                mMonitor.setText("");
                return true;
           
            case R.id.menu_clear_code:
                sendOBD2CMD("04");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED)
                {
                    displayMessage("Bluetooth not enabled :(");
                    displayLog("Bluetooth not enabled :(");
                    return;
                }

                if (resultCode == RESULT_OK)
                {
                    displayLog("Bluetooth enabled");

                    queryPairedDevices();
                    setupMonitor();
                }

                break;

            default:
                // nothing at the moment
        }
    }
    
    private void setupMonitor()
    {
        // Start mIOGateway
        if (mIOGateway == null)
        {
            mIOGateway = new BluetoothIOGateway(this, mMsgHandler);
        }

        // Only if the state is STATE_NONE, do we know that we haven't started already
        if (mIOGateway.getState() == BluetoothIOGateway.STATE_NONE)
        {
            // Start the Bluetooth chat services
            mIOGateway.start();
        }

        // clear string builder if contains data
        if (mSbCmdResp.length() > 0)
        {
            mSbCmdResp.setLength(0);
        }
        
    }

    private void queryPairedDevices()
    {
        displayLog("Try to query paired devices...");
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0)
        {
            PairedDevicesDialog dialog = new PairedDevicesDialog();
            dialog.setAdapter(new PairedListAdapter(this, pairedDevices), false);
            showChooserDialog(dialog);
        }
        else
        {
            displayLog("No paired device found");

            scanAroundDevices();
        }
    }

    private void showChooserDialog(DialogFragment dialogFragment)
    {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_DIALOG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(ft, "dialog");
    }

    private void scanAroundDevices()
    {
        displayLog("Try to scan around devices...");

        if (mReceiver == null)
        {
            // Register the BroadcastReceiver
            mReceiver = new DeviceBroadcastReceiver();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
        }

        // Start scanning
        mBluetoothAdapter.startDiscovery();
    }

    private void cancelScanning()
    {        
        if (mBluetoothAdapter.isDiscovering())
        {
            mBluetoothAdapter.cancelDiscovery();

            displayLog("Scanning canceled.");
        }
    }

    /**
     * Callback method for once a new device detected.
     *
     * @param device BluetoothDevice
     */
    public void onEvent(BluetoothDevice device)
    {
        if (mDeviceList == null)
        {
            mDeviceList = new ArrayList<BluetoothDevice>(10);
        }

        mDeviceList.add(device);

        // create dialog
        final android.support.v4.app.Fragment fragment = this.getSupportFragmentManager().findFragmentByTag(TAG_DIALOG);
        if (fragment != null && fragment instanceof PairedDevicesDialog)
        {
            PairedListAdapter adapter = dialog.getAdapter();
            adapter.notifyDataSetChanged();
        }
        else
        {
            dialog = new PairedDevicesDialog();
            dialog.setAdapter(new PairedListAdapter(this, new HashSet<BluetoothDevice>(mDeviceList)), true);
            showChooserDialog(dialog);
        }
    }

    private void displayMessage(String msg)
    {
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void displayLog(String msg)
    {
        Log.d(TAG, msg);
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device)
    {
        cancelScanning();

        displayLog("Selected device: " + device.getName() + " (" + device.getAddress() + ")");
        
        // Attempt to connect to the device
        mIOGateway.connect(device, true);
    }

    @Override
    public void onSearchAroundDevicesRequested()
    {
        scanAroundDevices();
    }

    @Override
    public void onCancelScanningRequested()
    {
        cancelScanning();
    }

    

    private void sendOBD2CMD(String sendMsg)
    {
        if (mIOGateway.getState() != BluetoothIOGateway.STATE_CONNECTED)
        {
            displayMessage(getString(R.string.bt_not_available));
            return;
        }
        
        String strCMD = sendMsg;
        strCMD += '\r';
        
        byte[] byteCMD = strCMD.getBytes();
        mIOGateway.write(byteCMD);
    }

    private void sendDefaultCommands()
    {
    	
        if(sendAll == false){
        	//choose what parser to use
        	sendAll = true;
        	
        	//clear out cmd response and clear monitor
        	mSbCmdResp.setLength(0);
            mMonitor.setText("");
        }
        if (mCMDPointer >= INIT_COMMANDS.length)
        {
        	sendAll = false;
            mCMDPointer = -1;
            
            mMonitor.append("\n\n MPH: " + getMPH());
            mMonitor.append("\n RPM: " + getRPM());
            return;
        }
        
        // reset pointer
        if (mCMDPointer < 0)
        {
            mCMDPointer = 0;
        }
        
        sendOBD2CMD(INIT_COMMANDS[mCMDPointer]);
    }
    
    private void parseResponse(String buffer)
    {        
    	switch (mCMDPointer)
    	{
    	case 0: // CMD: AT Z, no parse needed
    	case 1: // CMD: AT SP 0, no parse needed
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append("\n");
    		break;
    		
    	case 2: // CMD: 0105, Engine coolant temperature
    		int ect = showEngineCoolantTemperature(buffer);
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Eng. Coolant Temp is ");
    		mSbCmdResp.append(ect);
    		mSbCmdResp.append((char) 0x00B0);
    		mSbCmdResp.append("C)");
    		mSbCmdResp.append("\n");
    		break;
    		
    	case 3: // CMD: 010C, EngineRPM
    		int eRPM = showEngineRPM(buffer);
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Eng. RPM: ");
    		mSbCmdResp.append(eRPM);
    		mSbCmdResp.append(")");
    		mSbCmdResp.append("\n");
    		break;
    		
    	case 4: // CMD: 010D, Vehicle Speed
    		int vs = showVehicleSpeed(buffer);
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Vehicle Speed: ");
    		mSbCmdResp.append(vs);
    		mSbCmdResp.append("Km/h)");
    		mSbCmdResp.append("\n");
    		break;
    		
    	case 5: // CMD: 0131
    		int dt = showDistanceTraveled(buffer);
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Distance traveled since codes cleared: ");
    		mSbCmdResp.append(dt);
    		mSbCmdResp.append("Km)");
    		mSbCmdResp.append("\n");
    		break;
    		
    	default:
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append("\n");
    	}
    	
    	
    	mMonitor.setText(mSbCmdResp.toString());
    	
    	if (mCMDPointer >= 0)
    	{
    		mCMDPointer++;
    		sendDefaultCommands();
    	}
    }
    private void parseResponse_single_command(String buffer)
    {        
    	switch (commandNumber)
    	{
    	case 0: // CMD: AT Z, no parse needed
    	case 1: // CMD: AT SP 0, no parse needed
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append("\n");
    		commandNumber = 3;
    		sendOBD2CMD("010C");
    		break;
    		
    	case 2: // CMD: 0105, Engine coolant temperature
    		int ect = showEngineCoolantTemperature(buffer);
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Eng. Coolant Temp is ");
    		mSbCmdResp.append(ect);
    		mSbCmdResp.append((char) 0x00B0);
    		mSbCmdResp.append("C)");
    		mSbCmdResp.append("\n");
    		break;
    		
    	case 3: // CMD: 010C, EngineRPM
    		int eRPM = showEngineRPM(buffer);
    		if(eRPM != -1) {
    			setRPM(eRPM);
    		}
    		else{
        	//	sendOBD2CMD("010C");
        	}
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Eng. RPM: ");
    		mSbCmdResp.append(eRPM);
    		mSbCmdResp.append(")");
    		mSbCmdResp.append("\n");
    		commandNumber = 4;
    		sendOBD2CMD("010D");
    		break;
    		
    	case 4: // CMD: 010D, Vehicle Speed
    		int vs = showVehicleSpeed(buffer);
    		if(vs != -1){
    			setMPH(vs);
    		}
    		else{
        	//	sendOBD2CMD("010D");
    		}
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Vehicle Speed: ");
    		mSbCmdResp.append(vs);
    		mSbCmdResp.append("Km/h)");
    		mSbCmdResp.append("\n");
    		commandNumber = -1;
    		break;
    		
    	case 5: // CMD: 0131
    		int dt = showDistanceTraveled(buffer);
    		if(dt != -1){
    			setDistanceTraveled(dt);
    		}
    		else{
        	//	sendOBD2CMD("0131");
    		}
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append( " (Distance traveled since codes cleared: ");
    		mSbCmdResp.append(dt);
    		mSbCmdResp.append("Km)");
    		mSbCmdResp.append("\n");
    		break;
    		
    	default:
    		mSbCmdResp.append("R>>");
    		mSbCmdResp.append(buffer);
    		mSbCmdResp.append("\n");
    	}
    	
    	
    	mMonitor.setText(mSbCmdResp.toString());
    	if(commandNumber == -1){
    		mMonitor.append("\n\n MPH: " + getMPH());
            mMonitor.append("\n RPM: " + getRPM());
    	}
    	
    }
    
    private String cleanResponse(String text)
    {
        text = text.trim();
        text = text.replace("\t", "");
        text = text.replace(" ", "");
        text = text.replace(">", ""); 
        
        return text;
    }
    
    private int showEngineCoolantTemperature(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);
        
        if (buf.contains("4105"))
        {
            try
            {
                buf = buf.substring(buf.indexOf("4105"));

                String temp = buf.substring(4, 6);
                int A = Integer.valueOf(temp, 16);
                A -= 40;

                return A;
            }
            catch (IndexOutOfBoundsException | NumberFormatException e)
            {
                MyLog.e(TAG, e.getMessage());
            }
        }
        
        return -1;
    }
    
    private int showEngineRPM(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);
        
        if (buf.contains("410C"))
        {
            try
            {
                buf = buf.substring(buf.indexOf("410C"));
                
                String MSB = buf.substring(4, 6);
                String LSB = buf.substring(6, 8);
                int A = Integer.valueOf(MSB, 16);
                int B = Integer.valueOf(LSB, 16);
                
                return  ((A * 256) + B) / 4;
            }
            catch (IndexOutOfBoundsException | NumberFormatException e)
            {
                MyLog.e(TAG, e.getMessage());
            }
        }
        
        return -1;
    }
    
    private int showVehicleSpeed(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);
        
        if (buf.contains("410D"))
        {
            try
            {
                buf = buf.substring(buf.indexOf("410D"));

                String temp = buf.substring(4, 6);

                return (int)(Integer.valueOf(temp, 16) / 1.609344);
            }
            catch (IndexOutOfBoundsException | NumberFormatException e)
            {
                MyLog.e(TAG, e.getMessage());
            }
        }
        
        return -1;
    }
    
    private int showDistanceTraveled(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);
        
        if (buf.contains("4131"))
        {
            try
            {
                buf = buf.substring(buf.indexOf("4131"));

                String MSB = buf.substring(4, 6);
                String LSB = buf.substring(6, 8);
                int A = Integer.valueOf(MSB, 16);
                int B = Integer.valueOf(LSB, 16);

                return (A * 256) + B;
            }
            catch (IndexOutOfBoundsException | NumberFormatException e)
            {
                MyLog.e(TAG, e.getMessage());
            }
        }

        return -1;
    }

	public int getMPH() {
		return MPH;
	}

	public void setMPH(int mPH) {
		if( mPH > (2 * MPH) ){
			return;
		}
		MPH = mPH;
		AvgMPH = AvgMPH + MPH / tracker;
		tracker ++;
	}

	public int getRPM() {
		return RPM;
	}

	public void setRPM(int rPM) {
		RPM = rPM;
	}

	public int getDistanceTraveled() {
		return distanceTraveled;
	}

	public void setDistanceTraveled(int distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}
	
	public void loadTest(String t) {
		int selectedTest = Integer.parseInt(t);
		JSONObject json = TestingJSON.readTestingJSONLocal(this); //read testing json in
		ArrayList<JObject> testDataAll = DuringEvaluationLoadTest.getTestingData(json); //convert json into JObjects so they are ready to appear in list view
		ArrayList<List<String>> existingTests = DuringEvaluationLoadTest.getExistingTests(new ArrayList<List<String>>(), getSharedPreferences("existingTests", Context.MODE_PRIVATE)); //get all existing tests as strings
		List<String> selectedTests = existingTests.get(selectedTest);//Load the test selected from the spinner
		testDataSelected = DuringEvaluationLoadTest.loadTest(selectedTests, testDataAll); //extract the test data we want from the list of all test data
	}

	public static FatFractal getFF() {
    	//initialize instance of fatfractal
        if (ff == null) {
            String baseUrl = "http://accuratedrivingtest.fatfractal.com/accuratedrivingtest";
            String sslUrl = "https://accuratedrivingtest.fatfractal.com/accuratedrivingtest";
//            String baseUrl = "http://accuratedriving.fatfractal.com/AccDrivingTest";
//            String sslUrl = "https://accuratedriving.fatfractal.com/AccDrivingTest";
            try {
                ff = FatFractal.getInstance(new URI(baseUrl), new URI(sslUrl));
                FatFractalHttpImpl.addTrustedHost("accuratedrivingtest.fatfractal.com");
//                FatFractalHttpImpl.addTrustedHost("accuratedriving.fatfractal.com");
                //declare object collections here
                FFObjectMapper.registerClassNameForClazz(Driver.class.getName(), "Driver");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return ff;
    }
	
}



