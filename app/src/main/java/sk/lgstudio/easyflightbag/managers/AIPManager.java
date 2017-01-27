package sk.lgstudio.easyflightbag.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPDownloader;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPcz;

/**
 * Created by LGstudio on 2017-01-27.
 */

public class AIPManager {

    public final static int AIP_CZ = 0;
    public final static int AIP_SK = 1;
    public final static int AIP_HU = 2;

    private SharedPreferences prefs;
    private MainActivity activity;

    private int started = -1;

    private ArrayList<Integer> waiting;


    public AIPManager(MainActivity a){
        activity = a;
        prefs = a.prefs;
        waiting = new ArrayList<>();
    }

    public String getStatus(int country){

        if (started == country)
            return activity.getString(R.string.aip_downloading);

        if (!waiting.isEmpty()){
            boolean match = false;
            for (int q: waiting){
                if (q == country) match = true;
            }
            if (match)
                return activity.getString(R.string.aip_waiting);
        }

        String str = getPrefId(country);
        if (str != null){
            Long date = prefs.getLong(str, 0);
        }

        return "";
    }

    public boolean exists(int country){
        String str = getPrefId(country);
        if (str != null){
            if (prefs.getString(str, "").length() > 0){
                return true;
            }
        }

        return false;
    }

    private String getPrefId(int country){
        switch (country){
            case AIP_CZ:
                return activity.getString(R.string.pref_aip_last_update_cz);
            case AIP_SK:
                return activity.getString(R.string.pref_aip_last_update_sk);
            case AIP_HU:
                return activity.getString(R.string.pref_aip_last_update_hu);
        }
        return null;
    }

    private void saveSharedPref(){
        Date dt = new Date(System.currentTimeMillis());
        prefs.edit().putLong(getPrefId(started), dt.getTime()).apply();
    }

    public void stopUpdate(int country){
        // force stop update
    }

    public void getUpdate(int country){
        if (started < 0){
            startService(country);
        }
        else {
            boolean match = false;
            for (int q: waiting){
                if (q == country) match = true;
            }
            if (!match && started != country)
                waiting.add(country);
        }
    }

    private void stopService(){
        switch (started){
            case AIP_CZ:
                activity.stopService(new Intent(activity, AIPcz.class));
                LocalBroadcastManager.getInstance(activity).unregisterReceiver(this.aipDownloadedReceiver);
                break;
        }
        started = -1;
    }

    private void startService(int c){
        started = c;
        switch (started){
            case AIP_CZ:
                activity.startService(new Intent(activity, AIPcz.class));
                LocalBroadcastManager.getInstance(activity).registerReceiver(this.aipDownloadedReceiver, new IntentFilter(activity.getString(R.string.service_aip_download)));
                break;
        }
    }

    public BroadcastReceiver aipDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int code = intent.getIntExtra(activity.getString(R.string.intent_aip_status), -1);
            int fileCount = intent.getIntExtra(activity.getString(R.string.intent_aip_count), -1);
            int country = intent.getIntExtra(activity.getString(R.string.intent_aip_country), -1);

            switch (code){
                case AIPDownloader.STATUS_STARTED:
                    // Show/update notification
                    Log.i("AIP downloading", String.valueOf(country) + " - " + String.valueOf(fileCount));
                    break;
                case AIPDownloader.STATUS_ERROR:
                    Toast.makeText(activity.getApplicationContext(), "Error downloading AIP", Toast.LENGTH_SHORT).show();
                    break;
                case AIPDownloader.STATUS_FINISHED:
                    // Hide notification
                    saveSharedPref();
                    stopService();
                    if (!waiting.isEmpty()){
                        startService(waiting.get(0));
                        waiting.remove(0);
                    }
                    break;
            }
        }
    };

}
