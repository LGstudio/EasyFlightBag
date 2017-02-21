package sk.lgstudio.easyflightbag.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.openAIP.Airspace;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPDownloader;
import sk.lgstudio.easyflightbag.services.AirspaceDownloader;

/**
 * Created by LGstudio on 2017-02-20.
 */

public class AirspaceManager {

    public static final String countries[] = {"at", "cz", "hu", "pl", "sk"};
    public static final String filetypes[] = {"_wpt.aip", "_asp.aip", "_hot.aip", "_nav.aip"};
    public static final int fileCount = countries.length*filetypes.length;

    private SharedPreferences prefs;
    private MainActivity activity;
    private boolean started = false;

    public ArrayList<Airspace.Data> airspaces = null;

    public AirspaceManager(MainActivity a){
        activity = a;
        prefs = a.prefs;
        if (exists())
            loadData();
    }

    private void loadData(){
        airspaces = new ArrayList<>();
        for (int i = 0; i < countries.length; i++){
            String fileName = activity.airFolder.getPath()+"/"+countries[i]+filetypes[1];
            File f = new File(fileName);
            if (f.exists())
                airspaces.addAll(new Airspace.Parser().parse(f));
        }

    }

    public int airspaceStrokeColor(short category){

        switch (category){
            case Airspace.CATEGORY_RESTRICTED:
                return Color.YELLOW;
            case Airspace.CATEGORY_PROHIBITED:
                return Color.RED;
            case Airspace.CATEGORY_GLIDING:
                return Color.BLACK;
            case Airspace.CATEGORY_FIR:
                return Color.BLUE;
            case Airspace.CATEGORY_RMZ:
                return Color.GREEN;
            case Airspace.CATEGORY_TMA:
                return Color.WHITE;
            case Airspace.CATEGORY_UIR:
                return Color.GRAY;
            case Airspace.CATEGORY_WAVE:
                return Color.CYAN;
            case Airspace.CATEGORY_OTH:
                return Color.MAGENTA;
        }
        return 0;
    }

    public int airspaceFillColor(short category){

        switch (category){
            case Airspace.CATEGORY_RESTRICTED:
                return Color.argb(10, 255, 128, 0);
            case Airspace.CATEGORY_PROHIBITED:
                return Color.argb(10, 255, 20, 20);
            case Airspace.CATEGORY_GLIDING:
                return Color.argb(10, 0, 0, 0);
            case Airspace.CATEGORY_FIR:
                return Color.argb(10, 20, 20, 255);
            case Airspace.CATEGORY_RMZ:
                return Color.argb(10, 20, 255, 20);
            case Airspace.CATEGORY_TMA:
                return Color.argb(10, 255, 255, 255);
            case Airspace.CATEGORY_UIR:
                return Color.argb(10, 125, 125, 125);
            case Airspace.CATEGORY_WAVE:
                return Color.argb(10, 0, 240, 240);
            case Airspace.CATEGORY_OTH:
                return Color.argb(10, 255, 0, 240);
        }
        return 0;
    }

    public boolean exists(){
        Long saveTime = prefs.getLong(activity.getString(R.string.pref_air_last_update), 0);
        return saveTime > 0;
    }

    public String getStatus(){
        if (started)
            return activity.getString(R.string.aip_downloading);

        DecimalFormat format = new DecimalFormat("#");
        Long saveTime = prefs.getLong(activity.getString(R.string.pref_air_last_update), 0);
        if (saveTime > 0) {
            Long timeDiff = (new Date(System.currentTimeMillis()).getTime()) - saveTime;
            return String.valueOf(format.format(timeDiff / MainActivity.DAYS_IN_MILLISECONDS)) + " " + activity.getString(R.string.aip_day_ago);
        }

        return "";
    }

    private void saveSharedPref(){
        Date dt = new Date(System.currentTimeMillis());
        prefs.edit().putLong(activity.getString(R.string.pref_air_last_update), dt.getTime()).apply();
    }

    public void getUpdate(){
        if (!started) startService();
    }

    private void startService(){
        activity.startService(new Intent(activity, AirspaceDownloader.class));
        LocalBroadcastManager.getInstance(activity).registerReceiver(this.downloadedReceiver, new IntentFilter(activity.getString(R.string.service_air_download)));
        started = true;
        activity.airDataChange();
    }

    private void stopService(){
        activity.stopService(new Intent(activity, AirspaceDownloader.class));
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(this.downloadedReceiver);
        started = false;
        activity.airDataChange();
    }

    private BroadcastReceiver downloadedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            int code = intent.getIntExtra(activity.getString(R.string.intent_aip_status), -1);
            String fileCount = intent.getStringExtra(activity.getString(R.string.intent_aip_count));

            switch (code) {
                case AIPDownloader.STATUS_STARTED:
                    Log.i("openAIP: success", String.valueOf(fileCount));
                    break;
                case AIPDownloader.STATUS_ERROR:
                    Log.e("openAIP error", String.valueOf(fileCount));
                    break;
                case AIPDownloader.STATUS_FINISHED:
                    Log.i("openAIP: finished", String.valueOf(fileCount));
                    stopService();
                    saveSharedPref();
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.air_done_download), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
