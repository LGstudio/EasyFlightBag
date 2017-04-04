package sk.lgstudio.easyflightbag.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2017-04-03.
 */

public class BTTrackerService extends Service {

    private Location mLastLocation = null;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private InputStream connectedInputStream;
    // Well known SPP UUID (will *probably* map to
    // RFCOMM channel 1 (default) if not in use);
    // see comments in onResume().
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // TODO ==> hardcode your server's MAC address here <==
    private static String address = "12:23:34:45:56:56";

    @Override
    public void onCreate() {
        super.onCreate();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("BT", "Bluetooth is not available.");
            sendLocation();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.e("BT", "Bluetooth is disabled.");
            sendLocation();
            return;
        }
        Log.i("BT", "ENABLED");

        // via it's MAC address.

        try {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            connectedInputStream = btSocket.getInputStream();
        } catch (IOException e) {
            Log.e("BT", "Socket creation failed.");
            sendLocation();
            return;
        }

        mBluetoothAdapter.cancelDiscovery();

        //if (connectedInputStream != null)
        //    run();
        //else
            Log.e("BT", "END");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void run(){
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = connectedInputStream.read(buffer);
                String strReceived = new String(buffer, 0, bytes);
                final String msgReceived = String.valueOf(bytes) +
                        " bytes received:\n"
                        + strReceived;

                Log.i("BT", msgReceived);


            } catch (IOException e) {
                final String msgConnectionLost = "Connection lost:\n"
                        + e.getMessage();

                Log.i("BT", msgConnectionLost);

            }
        }
    }

    public void sendLocation() {

        Context context = getApplicationContext();
        Intent intent = new Intent(context.getString(R.string.gps_intent_filter));
        if (mLastLocation != null) {

            intent.putExtra(context.getString(R.string.gps_latitude), mLastLocation.getLatitude());
            intent.putExtra(context.getString(R.string.gps_longitude), mLastLocation.getLongitude());
            intent.putExtra(context.getString(R.string.gps_accuracy), mLastLocation.getAccuracy());
            intent.putExtra(context.getString(R.string.gps_bearing), mLastLocation.getBearing());
            intent.putExtra(context.getString(R.string.gps_speed), mLastLocation.getSpeed());
            intent.putExtra(context.getString(R.string.gps_time), mLastLocation.getTime());
            intent.putExtra(context.getString(R.string.gps_altitude), mLastLocation.getAltitude());
            intent.putExtra(getString(R.string.gps_enabled), true);
        }
        else {
            intent.putExtra(getString(R.string.gps_enabled), false);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
