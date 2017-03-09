package sk.lgstudio.easyflightbag.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPDownloader;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPat;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPcz;

/**
 * Created by LGstudio on 2017-01-27.
 */

public class AIPManager {

    public final static int AIP_AT = 0;
    public final static int AIP_CZ = 1;
    public final static int AIP_HU = 2;
    public final static int AIP_PL = 3;
    public final static int AIP_SK = 4;

    private SharedPreferences prefs;
    private MainActivity activity;

    private int started = -1;

    private ArrayList<Integer> waiting;


    public AIPManager(MainActivity a){
        activity = a;
        prefs = a.prefs;
        waiting = new ArrayList<>();

        //TODO: check if every AIP exists in internal memory && data.txt line count = file count-1
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
            DecimalFormat format = new DecimalFormat("#");
            Long saveTime = prefs.getLong(str, 0);
            if (saveTime > 0) {
                Long timeDiff = (new Date(System.currentTimeMillis()).getTime()) - saveTime;
                return String.valueOf(format.format(timeDiff / MainActivity.DAYS_IN_MILLISECONDS)) + " " + activity.getString(R.string.aip_day_ago);
            }
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
            case AIP_AT:
                return activity.getString(R.string.pref_aip_last_update_at);
            case AIP_CZ:
                return activity.getString(R.string.pref_aip_last_update_cz);
            case AIP_SK:
                return activity.getString(R.string.pref_aip_last_update_sk);
            case AIP_HU:
                return activity.getString(R.string.pref_aip_last_update_hu);
            case AIP_PL:
                return activity.getString(R.string.pref_aip_last_update_pl);
        }
        return null;
    }

    private void saveSharedPref(){
        Date dt = new Date(System.currentTimeMillis());
        prefs.edit().putLong(getPrefId(started), dt.getTime()).apply();
    }

    public void stopUpdate(int country){
        // TODO: force stop update
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

    private void stopService(int c){
        if (started == c)
            switch (started){
                case AIP_AT:
                    activity.stopService(new Intent(activity, AIPat.class));
                    LocalBroadcastManager.getInstance(activity).unregisterReceiver(this.aipDownloadedReceiver);
                    activity.aipDataChange(started);
                    break;
                case AIP_CZ:
                    activity.stopService(new Intent(activity, AIPcz.class));
                    LocalBroadcastManager.getInstance(activity).unregisterReceiver(this.aipDownloadedReceiver);
                    activity.aipDataChange(started);
                    break;
            }
        started = -1;

    }

    private void startService(int c){
        started = c;
        switch (started){
            case AIP_AT:
                activity.startService(new Intent(activity, AIPat.class));
                LocalBroadcastManager.getInstance(activity).registerReceiver(this.aipDownloadedReceiver, new IntentFilter(activity.getString(R.string.service_aip_download)));
                activity.aipDataChange(started);
                break;
            case AIP_CZ:
                activity.startService(new Intent(activity, AIPcz.class));
                LocalBroadcastManager.getInstance(activity).registerReceiver(this.aipDownloadedReceiver, new IntentFilter(activity.getString(R.string.service_aip_download)));
                activity.aipDataChange(started);
                break;
            default:
                stopService(c);
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
                    Log.i("AIP downloading", String.valueOf(country) + " - " + String.valueOf(fileCount));
                    break;
                case AIPDownloader.STATUS_ERROR:
                case AIPDownloader.STATUS_FINISHED:
                    if (code == AIPDownloader.STATUS_FINISHED) {
                        saveSharedPref();
                        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.aip_done_download), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.aip_error_download), Toast.LENGTH_SHORT).show();
                    }

                    stopService(country);
                    if (!waiting.isEmpty()){
                        startService(waiting.get(0));
                        waiting.remove(0);
                    }
                    break;
            }
        }
    };

}
