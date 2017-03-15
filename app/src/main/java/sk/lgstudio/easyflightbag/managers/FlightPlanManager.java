package sk.lgstudio.easyflightbag.managers;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.MainActivity;

/**
 * Created by LGstudio on 2017-03-15.
 */

public class FlightPlanManager {

    public File folder;
    public File selectedFile;
    public ArrayList<Point> plan;

    private SharedPreferences prefs;
    private MainActivity activity;

    public FlightPlanManager(MainActivity a){
        activity = a;
        prefs = activity.prefs;
    }

    public void loadPlan(File f){
        if (f == null){
            plan = null;
        }
        else {

        }
    }


    public static class Point{
        public String name;
        public LatLng locaton;
    }

}
