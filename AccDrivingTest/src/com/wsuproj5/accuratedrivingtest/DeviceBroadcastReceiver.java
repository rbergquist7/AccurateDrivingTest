package com.wsuproj5.accuratedrivingtest;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class DeviceBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "DeviceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            displayLog("Device found: " + device.getName() + " (" + device.getAddress() + ")");
        
        }
    }

    private void displayLog(String msg)
    {
        MyLog.d(TAG, msg);
    }
}
