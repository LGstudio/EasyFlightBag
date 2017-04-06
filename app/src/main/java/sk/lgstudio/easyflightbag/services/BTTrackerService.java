package sk.lgstudio.easyflightbag.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2017-04-03.
 */

public class BTTrackerService extends Service {

    private static final UUID MY_UUID =
            UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    // hardcode your server's MAC address here
    // private static String address = "34:E6:AD:5A:25:66";

    @Override
    public void onCreate() {
        super.onCreate();
        (new GetBTData()).execute();
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


    public void sendLocation(Location mLastLocation) {

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

    private class GetBTData extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            BluetoothSocket socket = null;
            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

            Log.e("BT", "Waiting...");

            try {
                BluetoothServerSocket mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("MyService", MY_UUID);
                socket = mmServerSocket.accept();
            } catch(Exception e){}

            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()
            try {

                InputStream tmpIn = null;

                // Get the BluetoothSocket input stream
                tmpIn = socket.getInputStream();

                while (true){
                    if (tmpIn != null){
                        DataInputStream mmInStream = new DataInputStream(tmpIn);

                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);

                        String[] data = readMessage.split(" ");

                        Location loc = new Location("BT");
                        loc.setAccuracy(0f);
                        loc.setAltitude(Float.parseFloat(data[2]));
                        loc.setLongitude(Float.parseFloat(data[0]));
                        loc.setLatitude(Float.parseFloat(data[1]));
                        loc.setBearing(Float.parseFloat(data[3]));
                        loc.setSpeed(Float.parseFloat(data[4]));

                        sendLocation(loc);
                    }
                }

            } catch (Exception e) {}

            Log.e("BT", "Failed. Please reset");

            return null;
        }
    }
}
