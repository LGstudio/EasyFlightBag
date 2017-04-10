package sk.lgstudio.easyflightbag.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

import sk.lgstudio.easyflightbag.R;

/**
 * Service to recieve GPS data from a Bluetooth source
 */

public class BTTrackerService extends Service {

    // Bluetooth service identification
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    private BTThread thread;

    @Override
    public void onCreate() {
        super.onCreate();

        thread = new BTThread();
        thread.start();
    }

    @Override
    public void onDestroy() {
        thread.run = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Send the location via intent for the activity
     * @param data [lon, lat, alt. bearing, speed]
     */
    private void sendLocation(String[] data) {

        Context context = getApplicationContext();
        Intent intent = new Intent(context.getString(R.string.gps_intent_filter));
        if (data.length == 5) {

            intent.putExtra(context.getString(R.string.gps_latitude), Double.parseDouble(data[1]));
            intent.putExtra(context.getString(R.string.gps_longitude), Double.parseDouble(data[0]));
            intent.putExtra(context.getString(R.string.gps_accuracy), 0f);
            intent.putExtra(context.getString(R.string.gps_bearing), Float.parseFloat(data[3]));
            intent.putExtra(context.getString(R.string.gps_speed), Float.parseFloat(data[4]));
            intent.putExtra(context.getString(R.string.gps_time), (long) 0);
            intent.putExtra(context.getString(R.string.gps_altitude), Double.parseDouble(data[2]));
            intent.putExtra(getString(R.string.gps_enabled), true);
        }
        else {
            intent.putExtra(getString(R.string.gps_enabled), false);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private class BTThread extends Thread {

        boolean run = true;

        @Override
        public void run() {
            while (true){
                BluetoothSocket socket = null;
                BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

                try {
                    BluetoothServerSocket mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("EFB", MY_UUID);
                    socket = mmServerSocket.accept();
                } catch(Exception e){
                    return;
                }

                byte[] buffer = new byte[256];  // buffer store for the stream
                int bytes; // bytes returned from read()
                try {

                    // Get the BluetoothSocket input stream
                    InputStream tmpIn = socket.getInputStream();

                    while (run){
                        if (tmpIn != null){
                            DataInputStream mmInStream = new DataInputStream(tmpIn);

                            // Read from the InputStream
                            bytes = mmInStream.read(buffer);
                            String readMessage = new String(buffer, 0, bytes);
                            sendLocation(readMessage.split(" "));
                        }
                    }

                } catch (Exception e) {}

            }
        }
    }
}
