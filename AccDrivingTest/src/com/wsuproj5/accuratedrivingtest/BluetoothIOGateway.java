package com.wsuproj5.accuratedrivingtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothIOGateway
{
    // Debugging
    private static final String TAG = "BluetoothIOGateway";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = TAG + "Secure";
    private static final String NAME_INSECURE = TAG + "Insecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE   = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Making default constructor private
    private BluetoothIOGateway()
    {
        mAdapter = null;
        mHandler = null;
    }
    
    public BluetoothIOGateway(Context context, Handler handler)
    {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }
    private synchronized void setState(int state)
    {
        MyLog.d(TAG, "setState() " + mState + " -> " + state);

        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(DuringEvaluation.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState()
    {
        return mState;
    }

    public synchronized void start()
    {
        MyLog.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Set state
        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null)
        {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        
        if (mInsecureAcceptThread == null)
        {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device, boolean secure)
    {
        MyLog.d(TAG, "connect to: " + device.getAddress());

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING)
        {
            if (mConnectThread != null)
            {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();

        // Set state
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType)
    {
        MyLog.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null)
        {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null)
        {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(DuringEvaluation.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DuringEvaluation.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Set state
        setState(STATE_CONNECTED);
    }

    public synchronized void stop()
    {
        MyLog.d(TAG, "stop");

        if (mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null)
        {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null)
        {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Set state
        setState(STATE_NONE);
    }

    public void write(byte[] out)
    {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        synchronized (this)
        {
            if (mState != STATE_CONNECTED)
            {
                return;
            }

            r = mConnectedThread;
        }

        // Perform the write un-synchronized
        r.write(out);
    }

    private void connectionFailed()
    {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(DuringEvaluation.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(DuringEvaluation.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothIOGateway.this.start();
    }

    private void connectionLost()
    {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(DuringEvaluation.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(DuringEvaluation.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothIOGateway.this.start();
    }

    private class AcceptThread extends Thread
    {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure)
        {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try
            {
                if (secure)
                {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
                }
                else
                {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
                }
            }
            catch (IOException e)
            {
                MyLog.e(TAG, "Listen to " + mSocketType + " socket type failed. More: ", e);
            }

            mmServerSocket = tmp;
        }

        public void run()
        {
            MyLog.d(TAG, "Socket Type: " + mSocketType + " BEGIN mAcceptThread " + this);

            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED)
            {
                try
                {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                }
                catch (IOException e)
                {
                    MyLog.e(TAG, "Socket Type: " + mSocketType + " accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null)
                {
                    synchronized (BluetoothIOGateway.this)
                    {
                        switch (mState)
                        {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(), mSocketType);
                                break;

                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try
                                {
                                    socket.close();
                                }
                                catch (IOException e)
                                {
                                    MyLog.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }

            MyLog.d(TAG, "END mAcceptThread, socket Type: " + mSocketType);
        }

        public void cancel()
        {
            MyLog.d(TAG, "Socket Type " + mSocketType + " cancel " + this);

            try
            {
                mmServerSocket.close();
            }
            catch (IOException e)
            {
                MyLog.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure)
        {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try
            {
                if (secure)
                {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                }
                else
                {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            }
            catch (IOException e)
            {
                MyLog.e(TAG, "Socket Type: " + mSocketType + " create() failed", e);
            }

            mmSocket = tmp;
        }

        public void run()
        {
            MyLog.d(TAG, "BEGIN mConnectThread SocketType: " + mSocketType);

            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try
            {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            }
            catch (IOException e)
            {
                // Close the socket
                MyLog.e(TAG, "Exception: " + e.getMessage());
                try
                {
                    mmSocket.close();
                }
                catch (IOException e2)
                {
                    MyLog.e(TAG, "unable to connect() " + mSocketType +
                            " socket during connection failure", e2);
                }

                connectionFailed();
                
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothIOGateway.this)
            {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
                MyLog.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }


    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType)
        {
            MyLog.d(TAG, "create ConnectedThread: " + socketType);

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                MyLog.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            MyLog.i(TAG, "BEGIN mConnectedThread");

            byte[] buffer = new byte[1024]; // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream while connected
            while (true)
            {
                try
                {
                    // Read from the InputStream for test
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(DuringEvaluation.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
                catch (IOException e)
                {
                    MyLog.e(TAG, "disconnected", e);

                    connectionLost();

                    // Start the service over to restart listening mode
                    BluetoothIOGateway.this.start();

                    break;
                }
            }
        }

        private int read(byte[] buffer) throws IOException
        {
            int bytes = 0;
            boolean escape = false;
            boolean prompt = false;
            byte[] readBuf;
            readBuf = new byte[1];
            while (!escape)
            {
                bytes += mmInStream.read(readBuf);
                String s = new String(readBuf);
                if (!TextUtils.isEmpty(s))
                {
                    buffer[bytes] = readBuf[0];
                    prompt = s.contains(">");
                }

                escape = prompt;
            }

            return bytes;
        }

        public void write(byte[] buffer)
        {
            try
            {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(DuringEvaluation.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            }
            catch (IOException e)
            {
                MyLog.e(TAG, "Exception during write, ", e);
            }
        }

        public void cancel()
        {
            try
            {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            }
            catch (IOException e)
            {
                MyLog.e(TAG, "close() of connect socket failed, ", e);
            }
        }
    }
}

