package com.wsuproj5.accuratedrivingtest;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OBDinfo extends Activity {

	   private static final int REQUEST_ENABLE_BT = 1;
	   private Button onBtn;
	   private Button offBtn;
	   private Button listBtn;
	   private Button findBtn;
	   private TextView text;
	   private BluetoothAdapter myBluetoothAdapter;
	   private Set<BluetoothDevice> pairedDevices;
	   private ListView myListView;
	   private ArrayAdapter<String> BTArrayAdapter;
	  
	   @Override
	   protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.activity_obd_info);
	      
	      // take an instance of BluetoothAdapter - Bluetooth radio
	      myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	      if(myBluetoothAdapter == null) {
	    	  onBtn.setEnabled(false);
	    	  offBtn.setEnabled(false);
	    	  listBtn.setEnabled(false);
	    	  findBtn.setEnabled(false);
	    	  text.setText("Status: not supported");
	    	  
	    	  Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
	         		 Toast.LENGTH_LONG).show();
	      } else {
		      text = (TextView) findViewById(R.id.obd);
		      onBtn = (Button)findViewById(R.id.turnOn);
		      onBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					on(v);
				}
		      });
		      
		      offBtn = (Button)findViewById(R.id.turnOff);
		      offBtn.setOnClickListener(new OnClickListener() {
		  		
		  		@Override
		  		public void onClick(View v) {
		  			// TODO Auto-generated method stub
		  			off(v);
		  		}
		      });
		      
		      listBtn = (Button)findViewById(R.id.pair);
		      listBtn.setOnClickListener(new OnClickListener() {
		  		
		  		@Override
		  		public void onClick(View v) {
		  			// TODO Auto-generated method stub
		  			list(v);
		  		}
		      });
		      
		      
		    
		      myListView = (ListView)findViewById(R.id.listView1);
		
		      // create the arrayAdapter that contains the BTDevices, and set it to the ListView
		      BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		      myListView.setAdapter(BTArrayAdapter);
	      }
	   }

	   public void on(View view){
	      if (!myBluetoothAdapter.isEnabled()) {
	         Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	         startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

	         Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
	        		 Toast.LENGTH_LONG).show();
	      }
	      else{
	         Toast.makeText(getApplicationContext(),"Bluetooth is already on",
	        		 Toast.LENGTH_LONG).show();
	      }
	   }
	   
	   @Override
	   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		   // TODO Auto-generated method stub
		   if(requestCode == REQUEST_ENABLE_BT){
			   if(myBluetoothAdapter.isEnabled()) {
				   text.setText("Status: Enabled");
			   } else {   
				   text.setText("Status: Disabled");
			   }
		   }
	   }
	   
	   public void list(View view){
		  // get paired devices
	      pairedDevices = myBluetoothAdapter.getBondedDevices();
	      final ArrayList devices = new ArrayList();
	      ArrayList deviceStrs = new ArrayList();
	      BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter(); 
	      // put it's one to the adapter
	      for(BluetoothDevice device : pairedDevices){
	    	    deviceStrs.add(device.getName()+ "\n" + device.getAddress());
	      		devices.add(device.getAddress());
	      }

	      final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
	      ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, deviceStrs.toArray(new String[deviceStrs.size()]));
	      
	      alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener()
	      {
	      @Override
	      	public void onClick(DialogInterface dialog, int which) {
	    	  	dialog.dismiss();
	    	  	int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
	    	  	String deviceAddress = (String) devices.get(position);
	                             // TODO save deviceAddress
	    	  	BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	    	  	BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress); 
	    	  	UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	    	  	BluetoothSocket socket;
	    	  	try {
					socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
					alertDialog.setTitle("Choose Bluetooth device");
					alertDialog.show();
					socket.connect();
					socket.getInputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	  	
	      	} 
	      }
	      );
	      
	      alertDialog.setTitle("Choose Bluetooth device");
	      alertDialog.show();

	      
	   }
//	      Toast.makeText(getApplicationContext(),"Show Paired Devices",
//	    		  Toast.LENGTH_SHORT).show();
//	      pairedDevices = myBluetoothAdapter.getBondedDevices();
//	      ArrayList deviceStrs = new ArrayList();
//	      final ArrayList devices = new ArrayList();
//	      BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter(); 
//	      pairedDevices = btAdapter.getBondedDevices();
//	      if (pairedDevices.size() > 0) {
//	    	  for (BluetoothDevice device : pairedDevices){
//	    		  deviceStrs.add(device.getName() + "\n" + device.getAddress());
//	      ￼￼￼￼￼			devices.add(device.getAddress());
//	    	  }
//	      }
//	      // show list
//	      final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//	      ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, deviceStrs.toArray(new String[deviceStrs.size()]));
//	      
//	      alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener()
//	      {
//	      @Override
//	      public void onClick(DialogInterface dialog, int which) {
//	      dialog.dismiss();
//	      int position = ((AlertDialog)
//	      dialog).getListView().getCheckedItemPosition();
//	      String deviceAddress = (String) devices.get(position);
//	                             // TODO save deviceAddress
//	      } });
//	                             alertDialog.setTitle("Choose Bluetooth device");
//	                             alertDialog.show();
//
//	      
//	   }
	  // }
	   
	   final BroadcastReceiver bReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		             // Get the BluetoothDevice object from the Intent
		        	 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		        	 // add the name and the MAC address of the object to the arrayAdapter
		             BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		             BTArrayAdapter.notifyDataSetChanged();
		        }
		    }
		};
		
	   public void find(View view) {
		   if (myBluetoothAdapter.isDiscovering()) {
			   // the button is pressed when it discovers, so cancel the discovery
			   myBluetoothAdapter.cancelDiscovery();
			   
		   }
		   else {
				BTArrayAdapter.clear();
				myBluetoothAdapter.startDiscovery();
				
				registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));	
			}    
	   }
	   
	   public void off(View view){
		  myBluetoothAdapter.disable();
		  text.setText("Status: Disconnected");
		  
	      Toast.makeText(getApplicationContext(),"Bluetooth turned off",
	    		  Toast.LENGTH_LONG).show();
	   }
	   
	   @Override
	   protected void onDestroy() {
		   // TODO Auto-generated method stub
		   super.onDestroy();
		   unregisterReceiver(bReceiver);
	   }
			
	}
