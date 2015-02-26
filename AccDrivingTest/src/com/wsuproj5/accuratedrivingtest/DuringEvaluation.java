package com.wsuproj5.accuratedrivingtest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//instead of always running all. only get MPH, get rest only when needed. when hit display 
//obd, get it one time, show pass/fail from HPH, connection = T/F,
//possibly add refresh button. removes memory issue since dont need so many loops running

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

import de.greenrobot.event.EventBus;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;

import android.app.Activity;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DuringEvaluation extends ActionBarActivity implements 
	GoogleApiClient.ConnectionCallbacks,
	GoogleApiClient.OnConnectionFailedListener,
	LocationListener,PairedDevicesDialog.PairedDeviceDialogListener{
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
	int counter = 0;
	boolean routeDrawn = false;
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

    private static final String TAG = MainElmActivity.class.getSimpleName();
    private static final String TAG_DIALOG = "dialog";
    private static final String NO_BLUETOOTH = "Oops, your device doesn't support bluetooth";
    
    // Commands
    private static final String[] INIT_COMMANDS = {"0105", "010C", "010D", "0131"};
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
    
    // Variable def
    private int commandNumber = 0;
    private int MPH = 0;
    private int RPM = 0;
    private int distTraveled = 0;
    private int highestSpeed = 0;
    private TextView mMonitor;
    private TextView mTest_progress;
    private static StringBuilder mSbCmdResp;
    private static StringBuilder mPartialResponse;
    private String mConnectedDeviceName;
    BluetoothDevice device;
    boolean deviceHolder = false;
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
//                            sendDefaultCommands();
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
                    readMessage = readMessage.toUpperCase();
                    displayLog(mConnectedDeviceName + ": " + readMessage);
                    char lastChar = readMessage.charAt(readMessage.length() - 1);
                    if (lastChar == '>')
                    {
                        parseResponse(mPartialResponse.toString() + readMessage);
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
                    displayMessage(msg.getData().getString(TOAST));
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
            }
        }

		

    };

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_during_evaluation);
        mMonitor = (TextView) findViewById(R.id.obd_data_view);
       	mMonitor.setText(getString(R.string.bt_not_available) + " attempting to reconnect...");
        mTest_progress = (TextView) findViewById(R.id.test_progress_data_view);
        mTest_progress.setText("Passing...For now!");

     //   mMonitor.setMovementMethod(new ScrollingMovementMethod());
		mConnectionStatus = (TextView) findViewById(R.id.tvConnectionStatus);
	        
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
//	    	sendOBD2CMD(INIT_COMMANDS[2]);
//	    	String response = mSbCmdResp.toString();
//	    	if(response!=""){
//	    		mMonitor.setText(showVehicleSpeed(response));	    		
//	    	}
//	    	else{
//	    		mMonitor.setText("No OBD device connected...");
//	    	}
//	    	sendOBD2CMD(INIT_COMMANDS[1]);
//	    	response = mSbCmdResp.toString();
//	    	if(response!=""){
//	    		mMonitor.setText(mMonitor.getText() + "\n" + showEngineRPM(response));
//	    	}
//	    	else{
//	    		mMonitor.setText("No OBD device connected...");
//	    	}
//	    	sendOBD2CMD(INIT_COMMANDS[3]);
//	    	response = mSbCmdResp.toString();
//	    	if(response!=""){
//	    		mMonitor.setText(mMonitor.getText() + "\n" + showDistanceTraveled(response));
//	    	}
//	    	else{
//	    		mMonitor.setText("No OBD device connected...");
//	    	}
//    	if(deviceHolder == true){
//    		this.mCMDPointer = -1;
//    	sendOBD2CMD(INIT_COMMANDS[3]);
//    	String response = mSbCmdResp.toString();
//    	mMonitor.setText(showVehicleSpeed(response));
//    	}
//    	else{
//    		mMonitor.setText(getString(R.string.bt_not_available) + " attempting to reconnect...");
//    		//mIOGateway.connect(this.device, true);
//    	}

    	
  //  	sendDefaultCommands();
    	/*
    	sendOBD2CMD(INIT_COMMANDS[0]);
    	sendOBD2CMD(INIT_COMMANDS[1]);
    	sendOBD2CMD(INIT_COMMANDS[2]);
    	sendOBD2CMD(INIT_COMMANDS[3]);
    	*/
//    	if(Integer.getInteger(response.substring(4, 6) ) > 25){
//    		mMonitor.setText("FAIL");
//    		displayLog("Fail");
//    	}
    	
//    	
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
	        Iterator<LatLng> lt = points.iterator();
	        while(lt.hasNext()){
	        	LatLng point = lt.next();
	        	map.addPolyline(new PolylineOptions()
	        	.add(point)
	        	.width(5)
	        	.color(Color.RED));
	        	
	        }
//	        Polyline line = map.addPolyline(new PolylineOptions()
//	        .addAll(points)
//	        .width(5)
//	        .color(Color.RED));
	        
	        //if route is not drawn, go into if statement, when route is loaded, there 
	        //next() will be valid and enter loop. once in loop, set routeDrawn to true
	        //drawning route once may save some memory
	       //if(routeDrawn == false){
	        
	        Iterator<List<LatLng>> it = routeListPoints.iterator();
	        	while (it.hasNext()) {
	        		List<LatLng> waypoint = it.next();
	        		map.addPolyline(new PolylineOptions()
	        		.addAll(waypoint)
	        		.width(5)
	        		.color(Color.BLUE));
	        	//	routeDrawn = true;
	        	}
//	        }else{
//	        	String notice = "loading route, please wait...";
//	            Toast.makeText(this, notice, Toast.LENGTH_SHORT).show();
//	            routeDrawn = false;
//	        }
//	        
//	        Polyline route = map.addPolyline(new PolylineOptions()
//	        .addAll(routeListPoints.get(0))
//	        .width(5)
//	        .color(Color.BLUE));
	        
	        CameraPosition cameraPosition = new CameraPosition.Builder()
	        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to new location
	        .zoom(map.getCameraPosition().zoom).zoom(25)                   // Sets the zoom
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
	    	commandNumber = 2;
	    	sendOBD2CMD(INIT_COMMANDS[2]);
//	    	String response = mSbCmdResp.toString();
//	    	if(response!=""){
//	    		mMonitor.setText("Vehicle MPH: " + this.MPH);	    		
//	    	}
//	    	else{
//	    		mMonitor.setText("No OBD device connected...");
//	    	}
	    	commandNumber = 1;
	    	sendOBD2CMD(INIT_COMMANDS[1]);
//	    	response = mSbCmdResp.toString();
//	    	if(response!=""){
//	    		mMonitor.setText(mMonitor.getText() + "\n" + "Vehicle RPM: " + this.RPM);
//	    	}
//	    	else{
//	    		mMonitor.setText("No OBD device connected...");
//	    	}
	    	commandNumber = 3;
	    	sendOBD2CMD(INIT_COMMANDS[3]);
//	    	response = mSbCmdResp.toString();
//	    	if(response!=""){
//	    		mMonitor.setText(mMonitor.getText() + "\n" + "Distance Traveled: " + this.distTraveled);
//	    	}
//	    	else{
//	    		mMonitor.setText("No OBD device connected...");
//	    	}
			//mMonitor.setText(mMonitor.getText() + " i ");
	    	//sendDefaultCommands();
	    }
	    
	    public void hideOBDDataMenu(View view) {
	    	LinearLayout obdDataMenu = (LinearLayout) findViewById(R.id.menu_OBD_data);
	    	obdDataMenu.setVisibility(INVISIBLE);
	    	mSbCmdResp.setLength(0);
	    	mMonitor.setText("");
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
	                	

	                default:
	                    // nothing at the moment
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
	    	if(item.getItemId() == R.id.action_scan){
	    		queryPairedDevices();
	                setupMonitor();
	                return true;
	    	}
	    	else if(item.getItemId() == R.id.menu_send_cmd){
	    		mCMDPointer = -1;
	    		sendDefaultCommands();
	    		return true;
	    		
	    	}
	    	else if(item.getItemId() == R.id.menu_clr_scr){
	    		mSbCmdResp.setLength(0);
	    		mMonitor.setText("");
	    		return true;
	    		
	    	}
	    	else if(item.getItemId() == R.id.menu_clear_code){
	    		sendOBD2CMD("04");
	    		return true;
	    		
	    	}

	        return super.onOptionsItemSelected(item);
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
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
        if(mIOGateway.getState() == BluetoothIOGateway.STATE_CONNECTED){
        	this.device = device;
        	deviceHolder = true;
        }
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
    	if(deviceHolder == true){ //may need to chage to this.device.getBondedState() == Bluetooth.StateConnected
    		mIOGateway.connect(this.device, true);
    		return;
    	}       
        
        String strCMD = sendMsg;
        strCMD += '\r';
        
        byte[] byteCMD = strCMD.getBytes();
        mIOGateway.write(byteCMD);
       // parseResponse(mSbCmdResp.toString());
        
    }

    private void sendDefaultCommands()
    {
        if (mCMDPointer >= INIT_COMMANDS.length)
        {
           mCMDPointer = -1;
            //return;
        }
        
        // reset pointer
        if (mCMDPointer < 0)
        {
            mCMDPointer = 0;
        }
        //remove recursion. multiple threads mixed up on stack? dont know why this would be an issue but lets try
        sendOBD2CMD(INIT_COMMANDS[mCMDPointer]);
    }
    
//    private void parseResponse(String buffer)
//    {        
//        switch (mCMDPointer)
//        {
////            case 0: // CMD: AT Z, no parse needed
////            case 1: // CMD: AT SP 0, no parse needed
////                mSbCmdResp.append("R>>");
////                mSbCmdResp.append(buffer);
////                mSbCmdResp.append("\n");
////                break;
////            
//            case 0: // CMD: 0105, Engine coolant temperature
//                int ect = showEngineCoolantTemperature(buffer);
//                mSbCmdResp.append("R>>");
//                mSbCmdResp.append(buffer);
//                mSbCmdResp.append( " (Eng. Coolant Temp is ");
//                mSbCmdResp.append(ect);
//                mSbCmdResp.append((char) 0x00B0);
//                mSbCmdResp.append("C)");
//                mSbCmdResp.append("\n");
//                break;
//
//            case 1: // CMD: 010C, EngineRPM
//                int eRPM = showEngineRPM(buffer);
//                mSbCmdResp.append("R>>");
//                mSbCmdResp.append(buffer);
//                mSbCmdResp.append( " (Eng. RPM: ");
//                mSbCmdResp.append(eRPM);
//                mSbCmdResp.append(")");
//                mSbCmdResp.append("\n");
//                break;
//
//            case 2: // CMD: 010D, Vehicle Speed
//                int vs = showVehicleSpeed(buffer);
//                mSbCmdResp.append("R>>");
//                mSbCmdResp.append(buffer);
//                mSbCmdResp.append( " (Vehicle Speed: ");
//                mSbCmdResp.append(vs);
//                mSbCmdResp.append("Km/h)");
//                mSbCmdResp.append("\n");
//                break;
//            
//            case 3: // CMD: 0131
//                int dt = showDistanceTraveled(buffer);
//                mSbCmdResp.append("R>>");
//                mSbCmdResp.append(buffer);
//                mSbCmdResp.append( " (Distance traveled since codes cleared: ");
//                mSbCmdResp.append(dt);
//                mSbCmdResp.append("Km)");
//                mSbCmdResp.append("\n");
//                break;
//            
//            default:
//                mSbCmdResp.append("R>>");
//                mSbCmdResp.append(buffer);
//                mSbCmdResp.append("\n");
//        }
//
//      //  mMonitor.setText(mSbCmdResp.toString());
//
//        if (mCMDPointer >= 0)
//        {
//            mCMDPointer++;
//            sendDefaultCommands();
//        }
//    }
    private void parseResponse(String buffer)
    {        
        switch (commandNumber)
        {
//            case 0: // CMD: AT Z, no parse needed
//            case 1: // CMD: AT SP 0, no parse needed
//                mSbCmdResp.append("R>>");
//                mSbCmdResp.append(buffer);
//                mSbCmdResp.append("\n");
//                break;
//            
            case 0: // CMD: 0105, Engine coolant temperature
                int ect = showEngineCoolantTemperature(buffer);
                mSbCmdResp.append("R>>");
                mSbCmdResp.append(buffer);
                mSbCmdResp.append( " (Eng. Coolant Temp is ");
                mSbCmdResp.append(ect);
                mSbCmdResp.append((char) 0x00B0);
                mSbCmdResp.append("C)");
                mSbCmdResp.append("\n");
                break;

            case 1: // CMD: 010C, EngineRPM
                int eRPM = showEngineRPM(buffer);
                this.RPM = eRPM;
                mSbCmdResp.append("R>>");
                mSbCmdResp.append(buffer);
                mSbCmdResp.append( " (Eng. RPM: ");
                mSbCmdResp.append(eRPM);
                mSbCmdResp.append(")");
                mSbCmdResp.append("\n");
                break;

            case 2: // CMD: 010D, Vehicle Speed
                int vs = showVehicleSpeed(buffer);
                this.MPH = vs;
                mSbCmdResp.append("R>>");
                mSbCmdResp.append(buffer);
                mSbCmdResp.append( " (Vehicle Speed: ");
                mSbCmdResp.append(vs);
                mSbCmdResp.append("Km/h)");
                mSbCmdResp.append("\n");
                break;
            
            case 3: // CMD: 0131
                int dt = showDistanceTraveled(buffer);
                this.distTraveled = dt;
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
        	buf = buf.substring(buf.indexOf("4105"));
        	
        	String temp = buf.substring(4, 6);
        	int A = Integer.valueOf(temp, 16);
        	A -= 40;
        	
        	return A;
            
        }
        
        return -1;
    }
    
    private int showEngineRPM(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);
        
        if (buf.contains("410C"))
        {
        	buf = buf.substring(buf.indexOf("410C"));
        	
        	String MSB = buf.substring(4, 6);
        	String LSB = buf.substring(6, 8);
        	int A = Integer.valueOf(MSB, 16);
        	int B = Integer.valueOf(LSB, 16);
        	
        	return  ((A * 256) + B) / 4;
            
        }
        
        return -1;
    }
    
    private int showVehicleSpeed(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);
        
        if (buf.contains("410D"))
        {
        	buf = buf.substring(buf.indexOf("410D"));
        	
        	String temp = buf.substring(4, 6);
        	
        	if(highestSpeed < Integer.valueOf(temp,16)){
        		highestSpeed = Integer.valueOf(temp, 16);
        		if (highestSpeed > 15){
        			mTest_progress.setText("Failed");
        		}
        	}
        	return Integer.valueOf(temp, 16);
            
        }
        
        return -1;
    }
    
    private int showDistanceTraveled(String buffer)
    {
        String buf = buffer;
        buf = cleanResponse(buf);
        
        if (buf.contains("4131"))
        {
        	buf = buf.substring(buf.indexOf("4131"));
        	
        	String MSB = buf.substring(4, 6);
        	String LSB = buf.substring(6, 8);
        	int A = Integer.valueOf(MSB, 16);
        	int B = Integer.valueOf(LSB, 16);
        	
        	return (A * 256) + B;
            
        }

        return -1;
    }


    private void cancelScanning()
    {        
        if (mBluetoothAdapter.isDiscovering())
        {
            mBluetoothAdapter.cancelDiscovery();

            displayLog("Scanning canceled.");
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
	
	

}

