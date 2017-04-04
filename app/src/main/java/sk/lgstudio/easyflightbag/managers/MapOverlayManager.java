package sk.lgstudio.easyflightbag.managers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.openAIP.Airport;
import sk.lgstudio.easyflightbag.openAIP.Airspace;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPDownloader;
import sk.lgstudio.easyflightbag.services.AirspaceDownloader;

/**
 * Map overlay manager
 *  - reads the map overlays from .aip files
 *  - makes layout property calculations
 *  - specifies layout icons and colors based on
 */

public class MapOverlayManager {

    // Static values
    public static final String countries[] = {"at", "cz", "hu", "pl", "sk"};
    public static final String filetypes[] = {"_wpt.aip", "_asp.aip", "_hot.aip", "_nav.aip"};
    public static final int fileCount = countries.length*filetypes.length;

    // Private variables
    private SharedPreferences prefs;
    private MainActivity activity;
    private boolean started = false;
    public int country = 0;

    // List of content
    public ArrayList<Airspace.Data> airspaces = null;
    public ArrayList<Airport.Data> airports = null;
    public boolean isLoading = false;
    private File folder;

    /**
     * Constructor - loads data from .aip files
     * @param a - MainActivity
     */
    public MapOverlayManager(MainActivity a, File f){
        activity = a;
        prefs = a.prefs;
        folder = f;

        country = prefs.getInt(activity.getString(R.string.pref_overlay_selected), 0);

        if (exists())
            loadData();

    }

    /**
     * Change the country to be showed
     * @param i
     */
    public void changeCountry(int i){
        country = i;
        prefs.edit().putInt(activity.getString(R.string.pref_overlay_selected), i).apply();
        isLoading = true;
        (new LoadData()).execute();
    }

    /**
     * Loading the data from the .aip files
     */
    private void loadData(){
        airspaces = new ArrayList<>();
        airports = new ArrayList<>();

        for (int i = 0; i < countries.length; i++){

            if (country != 0 && country != i+1) continue;

            // airspaces
            String fileName = folder.getPath()+"/"+countries[i]+filetypes[1];
            File f = new File(fileName);
            if (f.exists())
                airspaces.addAll(new Airspace.Parser().parse(f));

            // airports
            fileName = folder.getPath()+"/"+countries[i]+filetypes[0];
            f = new File(fileName);
            if (f.exists()) {
                airports.addAll(new Airport.Parser().parse(f));
                for (Airport.Data d: airports)
                    d.icon = getAirportIcon(d);
            }
        }
    }

    /**
     * Looks for the airport ICAO at position
     */
    public String getAirportICAO(LatLng latLng){
        for (Airport.Data d: airports){
            if (latLng.latitude == d.location.latitude && latLng.longitude == d.location.longitude){
                return d.icao;
            }
        }

        return "";
    }

    /**
     * Map coloring based on airspace category
     * @param category
     * @return
     */
    public static int airspaceStrokeColor(short category){
        switch (category){
            case Airspace.CATEGORY_F:
                return Color.argb(100, 0, 0, 200); // !
            case Airspace.CATEGORY_G:
                return Color.rgb(128, 0, 0); // !
            case Airspace.CATEGORY_DANGER:
            case Airspace.CATEGORY_RESTRICTED:
                return Color.rgb(240, 40, 0); // !
            case Airspace.CATEGORY_PROHIBITED:
                return Color.rgb(190, 0, 0); // !
            case Airspace.CATEGORY_GLIDING:
            case Airspace.CATEGORY_WAVE:
                return Color.rgb(128, 0, 0); // !
            case Airspace.CATEGORY_OTH:
                return Color.rgb(0, 0, 0);
            case Airspace.CATEGORY_TMZ:
                return Color.rgb(180,180,180); // !;
            default: // A, B, C, D, E, FIR, CTR, UIR, TMZ, RMZ
                return Color.rgb( 0, 0, 200); // !
        }
    }

    /**
     * Map fill color based on airspace category
     * @param category
     * @return
     */
    public static int airspaceFillColor(short category){

        switch (category){
            case Airspace.CATEGORY_CTR:
                return Color.argb(40, 200, 0, 0); // !
            case Airspace.CATEGORY_G:
                return Color.argb(15, 220, 220, 0); //!
            case Airspace.CATEGORY_DANGER:
                return Color.argb(40, 240, 40, 0); // !
            case Airspace.CATEGORY_PROHIBITED:
                return Color.argb(50, 190, 0, 0); // !
            case Airspace.CATEGORY_GLIDING:
            case Airspace.CATEGORY_WAVE:
                return Color.argb(30, 250, 250, 0); // !
            case Airspace.CATEGORY_OTH:
                return Color.argb(30, 0, 0, 0);
            default: // A, B, C, D, E, F, TMZ, FIR, RESTRICTED, UIR, RMZ
                return Color.argb(0,0,0,0);
        }
    }

    public ArrayList<Airport.Data> getAirportsCloseBy(LatLng point){
        ArrayList<Airport.Data> data = new ArrayList<>();
        Airport.Data closest = null;
        float close = Float.MAX_VALUE;

        for (Airport.Data d: airports){
            float[] results = new float[1];
            Location.distanceBetween(d.location.latitude, d.location.longitude, point.latitude, point.longitude, results);
            if (results[0] < close) {
                closest = d;
                close = results[0];
            }
            if (results[0] < 10000){ // 25Km around
                data.add(d);
            }
        }
        if (!data.contains(closest)){
            data.add(closest);
        }

        return data;
    }

    /**
     * Returns airspaces that contain the given coordinate
     * @param point
     * @return
     */
    public ArrayList<Airspace.Data> getAirspacesAt(LatLng point){
        ArrayList<Airspace.Data> data = new ArrayList<>();

        for (Airspace.Data d: airspaces)
            if (pointInPolygon(point, d.polygon))
                data.add(d);

        return data;
    }

    public Bitmap getAirportIcon(Airport.Data ap){

        VectorDrawable vectorType = (VectorDrawable) activity.getDrawable(R.drawable.airport_circle_general);
        VectorDrawable vectorRwy = null;

        int h = vectorType.getIntrinsicHeight();
        int w = vectorType.getIntrinsicWidth();

        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        boolean isHeli = false;

        switch(ap.type) {
            case Airport.APT_TYPE_INT:
                vectorType = (VectorDrawable) activity.getDrawable(R.drawable.airport_circle_international);
                break;
            case Airport.APT_TYPE_HELI_CIVIL:
            case Airport.APT_TYPE_HELI_MIL:
                vectorType = (VectorDrawable) activity.getDrawable(R.drawable.airport_heli);
                isHeli = true;
                break;
            case Airport.APT_TYPE_GLIDING:
            case Airport.APT_TYPE_LIGHT:
            case Airport.APT_TYPE_WATER:
                vectorType = (VectorDrawable) activity.getDrawable(R.drawable.empty_vector);
                break;
        }

        vectorType.setBounds(0, 0, w, h);
        vectorType.draw(canvas);

        if (!isHeli){
            for (Airport.Runway rwy: ap.runways){
                if (rwy.operations != Airport.RWY_CLOSED){
                    short sfc = rwy.sfc;
                    switch (sfc){
                        case Airport.RWY_SFC_ASPH:
                        case Airport.RWY_SFC_CONC:
                            vectorRwy = (VectorDrawable) activity.getDrawable(R.drawable.airport_rwy_asph);
                            break;
                        default:
                            vectorRwy = (VectorDrawable) activity.getDrawable(R.drawable.airport_rwy_other);
                    }
                    vectorRwy.setBounds(0, 0, w, h);
                    float tc = 0f;
                    for (Airport.Direction dir: rwy.directions){
                        tc = dir.tc;
                    }
                    canvas.rotate(tc, w/2, h/2);
                    vectorRwy.draw(canvas);
                    canvas.rotate(-tc, w/2, h/2);
                }
            }
        }
        return bm;
    }

    /**
     * Returns if polygon contains a given point
     * @param point
     * @param path
     * @return
     */
    private boolean pointInPolygon(LatLng point, ArrayList<LatLng> path) {
        // ray casting alogrithm
        int crossings = 0;

        // for each edge
        for (int i=0; i < path.size(); i++) {
            LatLng a = path.get(i);
            int j = i + 1;
            //to close the last edge, you have to take the first point of your polygon
            if (j >= path.size()) {
                j = 0;
            }
            LatLng b = path.get(j);
            if (rayCrossesSegment(point, a, b)) {
                crossings++;
            }
        }

        // odd number of crossings?
        return (crossings % 2 == 1);
    }

    /**
     * Checking segment crossings for point in polygon
     * @param point
     * @param a
     * @param b
     * @return
     */
    private boolean rayCrossesSegment(LatLng point, LatLng a, LatLng b) {
        double px = point.longitude,
                py = point.latitude,
                ax = a.longitude,
                ay = a.latitude,
                bx = b.longitude,
                by = b.latitude;
        if (ay > by) {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }
        // alter longitude to cater for 180 degree crossings
        if (px < 0 || ax <0 || bx <0) { px += 360; ax+=360; bx+=360; }
        // if the point has the same latitude as a or b, increase slightly py
        if (py == ay || py == by) py += 0.00000001;


        // if the point is above, below or to the right of the segment, it returns false
        if ((py > by || py < ay) || (px > Math.max(ax, bx))){
            return false;
        }
        // if the point is not above, below or to the right and is to the left, return true
        else if (px < Math.min(ax, bx)){
            return true;
        }
        // if the two above conditions are not met, you have to compare the slope of segment [a,b] (the red one here) and segment [a,p] (the blue one here) to see if your point is to the left of segment [a,b] or not
        else {
            double red = (ax != bx) ? ((by - ay) / (bx - ax)) : Double.POSITIVE_INFINITY;
            double blue = (ax != px) ? ((py - ay) / (px - ax)) : Double.POSITIVE_INFINITY;
            return (blue >= red);
        }

    }

    public static BitmapDescriptor getBitmapDescriptor(int id, Activity a) {

        VectorDrawable vectorDrawable = (VectorDrawable) a.getDrawable(id);

        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();

        vectorDrawable.setBounds(0, 0, 2*w, 2*h);

        Bitmap bm = Bitmap.createBitmap(2*w, 2*h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    /**
     * Checks if there are downloaded .aip files
     * @return
     */
    public boolean exists(){
        Long saveTime = prefs.getLong(activity.getString(R.string.pref_air_last_update), 0);
        return saveTime > 0;
    }

    /**
     * Returns the status of the .aip files
     * @return
     */
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

    /**
     * Saves shared preferences when .aip files updated
     */
    private void saveSharedPref(){
        Date dt = new Date(System.currentTimeMillis());
        prefs.edit().putLong(activity.getString(R.string.pref_air_last_update), dt.getTime()).apply();
    }

    /**
     * Requests .aip file downloading if already not started
     */
    public void getUpdate(){
        if (!started) startService();
    }

    /**
     * Starts downloading service
     */
    private void startService(){
        activity.startService(new Intent(activity, AirspaceDownloader.class));
        LocalBroadcastManager.getInstance(activity).registerReceiver(this.downloadedReceiver, new IntentFilter(activity.getString(R.string.service_air_download)));
        started = true;
        activity.airDataChange();
    }

    /**
     * Finishes downloading service
     */
    private void stopService(){
        activity.stopService(new Intent(activity, AirspaceDownloader.class));
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(this.downloadedReceiver);
        started = false;
        activity.airDataChange();
    }

    /**
     * Receiver for downloader service
     */
    private BroadcastReceiver downloadedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            int code = intent.getIntExtra(activity.getString(R.string.intent_aip_status), -1);
            String fileCount = intent.getStringExtra(activity.getString(R.string.service_air_name));

            switch (code) {
                case AIPDownloader.STATUS_STARTED:
                    //Log.i("openAIP: success", String.valueOf(fileCount));
                    break;
                case AIPDownloader.STATUS_ERROR:
                    //Log.e("openAIP error", String.valueOf(fileCount));
                    break;
                case AIPDownloader.STATUS_FINISHED:
                    //Log.i("openAIP: finished", String.valueOf(fileCount));
                    stopService();
                    saveSharedPref();
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.air_done_download), Toast.LENGTH_SHORT).show();
                    loadData();
                    break;
            }
        }
    };

    // ---------------------------------------------------------------
    // Async Tasks
    // ---------------------------------------------------------------

    public class LoadData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            loadData();
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            isLoading = false;
        }
    }

}
