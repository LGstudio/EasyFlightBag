package sk.lgstudio.easyflightbag.managers;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Flight Plan manager - Opens and saves .json flight plans and manages the plan edits
 */

public class FlightPlanManager {

    private final static String KEY_NAME = "name";
    private final static String KEY_LAT = "lat";
    private final static String KEY_LON = "lon";
    private final static String KEY_EDITABLE = "editable";

    public boolean loaded = false;

    private File file;
    public ArrayList<Point> plan;
    public ArrayList<Point> editedPlan;

    /**
     * Constructor - reads the .json file
     * @param f
     */
    public FlightPlanManager(File f){
        plan = new ArrayList<>();

        if (f == null) return;

        file = f;

        try {
            FileInputStream is = new FileInputStream(file);
            String str = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while(line != null) {
                str = str + line;
                line = reader.readLine();
            }

            JSONArray json = new JSONArray(str);

            for (int i = 0; i < json.length(); i++){
                JSONObject item = json.getJSONObject(i);

                Point p = new Point();
                p.name = item.getString(KEY_NAME);
                p.location = new LatLng(item.getDouble(KEY_LAT), item.getDouble(KEY_LON));
                p.editeble = item.getBoolean(KEY_EDITABLE);

                plan.add(p);

            }

            loaded = true;
        } catch (JSONException | IOException e) {
            loaded = false;
        }
    }

    /**
     * Saves the edited plan
     * @return
     */
    public boolean saveEditedPlan(){

        if (editedPlan == null)
            return false;

        try{
            JSONArray json = new JSONArray();

            for (Point p: editedPlan){
                JSONObject o = new JSONObject();
                o.put(KEY_NAME, p.name);
                o.put(KEY_LAT, p.location.latitude);
                o.put(KEY_LON, p.location.longitude);
                o.put(KEY_EDITABLE, p.editeble);
                json.put(o);
            }

            FileWriter writer = new FileWriter(file);
            writer.append(json.toString());
            writer.flush();
            writer.close();

        } catch (JSONException | IOException e1) {
            return false;
        }

        plan = editedPlan;

        return true;
    }

    /**
     * Adds new free point to the editor
     */
    public void addNewPoint(LatLng loc){
        Point p = new Point();
        p.editeble = true;
        p.location = loc;
        p.name = String.valueOf(editedPlan.size());
        editedPlan.add(p);
    }

    /**
     * Adds new point to the editor with name and not draggable
     */
    public void addNewPoint(LatLng loc, String name){
        Point p = new Point();
        p.editeble = false;
        p.location = loc;
        p.name = name;
        editedPlan.add(p);
    }

    /**
     * Calculates the length of the route in km
     * @return
     */
    public float getRoutLength(){
        float length = 0f;

        for (int i = 1; i < plan.size(); i++){
            Point p1 = plan.get(i-1);
            Point p2 = plan.get(i);

            float[] results = new float[1];
            Location.distanceBetween(p1.location.latitude, p1.location.longitude, p2.location.latitude, p2.location.longitude, results);
            length += results[0]/1000;
        }

        return length;
    }

    /**
     * Holds a plan route point
     */
    public static class Point{
        public String name;
        public LatLng location;
        public boolean editeble;
    }

}
