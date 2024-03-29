package sk.lgstudio.easyflightbag.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPDownloader;


/**
 * Created by LGstudio on 2017-02-20.
 */

public class OpenAIPDownloader extends IntentService {

    private String webUrl;
    private File folder;
    private int fileCount = 0;
    private NotificationCompat.Builder builder = null;
    private NotificationManager notificationManager;

    public OpenAIPDownloader() {
        super("Airspace");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        webUrl = getString(R.string.url_open_aip);
        folder = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_root) + getString(R.string.folder_airspace));
        if (!folder.exists()){
            folder.mkdir();
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.air_progress_download))
                .setContentText(String.valueOf(fileCount)+ "/" + String.valueOf(MapOverlayManager.fileCount))
                .setSmallIcon(R.drawable.ic_download)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true);

        notificationManager.notify(MainActivity.NOTIFICATION_AIRSAPCE ,builder.build());

        for(int i = 0; i < MapOverlayManager.countries.length; i++){
            for (int j = 0; j < MapOverlayManager.filetypes.length; j++){
                fileCount += 1;
                String name = MapOverlayManager.countries[i]+ MapOverlayManager.filetypes[j];
                if (downloadFile(name))
                    sendReport(name, AIPDownloader.STATUS_STARTED);
                else{
                    sendReport(name, AIPDownloader.STATUS_ERROR);
                }
            }
        }

        sendReport("done", AIPDownloader.STATUS_FINISHED);

    }

    private void sendReport(String name, int status) {
        switch (status) {
            case AIPDownloader.STATUS_STARTED:
                if (builder != null){
                    builder.setContentText(String.valueOf(fileCount)+ "/" + String.valueOf(MapOverlayManager.fileCount));
                    notificationManager.notify(MainActivity.NOTIFICATION_AIRSAPCE ,builder.build());
                }
                break;
            case AIPDownloader.STATUS_FINISHED:
                notificationManager.cancel(MainActivity.NOTIFICATION_AIRSAPCE);
            case AIPDownloader.STATUS_ERROR:
                Context context = getApplicationContext();
                Intent intent = new Intent(context.getString(R.string.service_air_download));

                intent.putExtra(context.getString(R.string.intent_aip_status), status);
                intent.putExtra(context.getString(R.string.service_air_name), name);

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private boolean downloadFile(String fileName){
        try {
            // connection to the url of the file
            URL url = new URL(webUrl+fileName);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            File file = new File(folder, fileName);

            //this will be used to write the downloaded data into the file we created
            FileOutputStream fileOutput = new FileOutputStream(file);

            //this will be used in reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0; //used to store a temporary size of the buffer

            //now, read through the input buffer and write the contents to the file
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card
                fileOutput.write(buffer, 0, bufferLength);
            }
            //close the output stream when done
            fileOutput.close();
            urlConnection.disconnect();

            return true;
        } catch (IOException e) {
            Log.e("Exception on file", fileName);
        }
        return false;
    }
}
