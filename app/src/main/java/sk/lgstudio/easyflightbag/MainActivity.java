package sk.lgstudio.easyflightbag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.services.GPSTrackerService;
import sk.lgstudio.easyflightbag.menu.TabFragmentAdapter;
import sk.lgstudio.easyflightbag.menu.TabViewPager;
import sk.lgstudio.easyflightbag.fragments.FragmentAip;
import sk.lgstudio.easyflightbag.fragments.FragmentCalc;
import sk.lgstudio.easyflightbag.fragments.FragmentChklist;
import sk.lgstudio.easyflightbag.fragments.FragmentDocs;
import sk.lgstudio.easyflightbag.fragments.FragmentHome;
import sk.lgstudio.easyflightbag.fragments.FragmentPlan;
import sk.lgstudio.easyflightbag.fragments.FragmentSettings;
import sk.lgstudio.easyflightbag.fragments.FragmentWeather;
import sk.lgstudio.easyflightbag.menu.TabMenu;

public class MainActivity extends AppCompatActivity {

    private FragmentHome fHome = new FragmentHome();
    private FragmentChklist fChk = new FragmentChklist();
    private FragmentSettings fSet  = new FragmentSettings();

    private TabMenu menu;
    private TabViewPager viewPager;

    private File rootDir;

    private ArrayList<Location> track = new ArrayList<>();
    //private ArrayList<Location> route = new ArrayList<>();

    public SharedPreferences prefs;
    private String prefChkActual;
    public boolean nightMode = false;
    public String aipLastUpdate;

    private boolean isChkTutCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // checks internal storage for existing files
        checkDirectory();

        // loads shared preferences
        loadSharedPerefs();

        setContentView(R.layout.activity_main);

        if (userAllowedLocation()) {
            this.startGPSService();
        }
        //else{
            // Request missing location permission.
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        //}

        // creates fragments
        initView();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();


        if (fChk.folderActual != null)
            prefs.edit().putString(getString(R.string.pref_chk_folder), fChk.folderActual.getPath()).apply();
        else
            prefs.edit().remove(getString(R.string.pref_chk_folder)).apply();


        stopService(new Intent(this, GPSTrackerService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsReceiver);
    }

    /**
     * Loads the saved preferences when application starts
     */
    private void loadSharedPerefs(){
        prefs = this.getSharedPreferences("sk.lgstudio.efb", Context.MODE_PRIVATE);

        // Load last opened Checklist
        if (isChkTutCreated){
            fChk.folderActual = new File(fChk.folder.getPath() + getString(R.string.folder_chklist_demo));
        }
        else {
            prefChkActual = prefs.getString(getString(R.string.pref_chk_folder), null);
            if (prefChkActual != null) {
                fChk.folderActual = new File(prefChkActual);
                if (!fChk.folderActual.exists()){
                    fChk.folderActual = null;
                }
            }
        }

        // Load selected theme
        nightMode = prefs.getBoolean(getString(R.string.pref_theme), false);
        if (nightMode) setTheme(R.style.AppThemeDark);

        // Aip last updateed time
        aipLastUpdate = prefs.getString(getString(R.string.pref_aip_last_update), "");

    }

    /**
     * Checks if directories exist on internal memory
     * Adds default data in case of first app launch or when data were deleted
     */
    private void checkDirectory(){
        rootDir = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_root));
        if(!rootDir.exists()) {
            rootDir.mkdir();
        }

        fChk.folder = new File(rootDir.getPath() + getString(R.string.folder_chklist));
        if(!fChk.folder.exists()) {
            fChk.folder.mkdir();
            File chkTutDir = new File(fChk.folder.getPath() + getString(R.string.folder_chklist_demo));
            chkTutDir.mkdir();
            if (chkTutDir.exists()){
                isChkTutCreated = true;
                int files[] = {R.string.file_chk_plane, R.string.file_chk_add_list, R.string.file_chk_edit_list};
                int texts[] = {R.string.file_chk_plane_, R.string.file_chk_add_list_, R.string.file_chk_edit_list_};

                for (int i = 0; i<files.length; i++){
                    try {
                        File tut1 = new File(chkTutDir, getString(files[i]) );
                        FileWriter writer1 = new FileWriter(tut1);
                        writer1.append(getString(texts[i]));
                        writer1.flush();
                        writer1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        File aipFolder = new File(rootDir.getPath() + getString(R.string.folder_aip));
        if (!aipFolder.exists())
            aipFolder.mkdir();
    }

    /**
     * initializes fragments and top menu on the view
     */
    private void initView(){
        final TabFragmentAdapter fA = new TabFragmentAdapter(getSupportFragmentManager());

        fHome.track = track;
        fA.addFragment(fHome);
        fA.addFragment(new FragmentAip());
        fA.addFragment(new FragmentWeather());
        fA.addFragment(fChk);
        fA.addFragment(new FragmentDocs());
        fA.addFragment(new FragmentPlan());
        fA.addFragment(new FragmentCalc());
        fSet.activity = this;
        fA.addFragment(fSet);

        viewPager = (TabViewPager) findViewById(R.id.view_fragment_pager);
        viewPager.setAdapter(fA);
        viewPager.setPagingEnabled(false);

        menu = new TabMenu(this, viewPager);
    }

    private void startGPSService() {
        startService(new Intent(this, GPSTrackerService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceiver, new IntentFilter(this.getString(R.string.gps_intent_filter))
        );
    }

    /**
     * Check if app has permissions for fine location
     */
    private boolean userAllowedLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.GPS_disabled), Toast.LENGTH_SHORT);
            return false;
        } else {
            // Location permission has been granted, continue as usual.
            return true;
        }
    }

    /**
     * Receiver of new locations from the GPS service
     */
    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Location newLoc = new Location(getString(R.string.gps_location));

            newLoc.setLatitude(Double.parseDouble(intent.getStringExtra(context.getString(R.string.gps_latitude))));
            newLoc.setLongitude(Double.parseDouble(intent.getStringExtra(context.getString(R.string.gps_longitude))));
            newLoc.setAccuracy(Float.parseFloat(intent.getStringExtra(context.getString(R.string.gps_accuracy))));
            newLoc.setBearing(Float.parseFloat(intent.getStringExtra(context.getString(R.string.gps_bearing))));
            newLoc.setSpeed(Float.parseFloat(intent.getStringExtra(context.getString(R.string.gps_speed))));
            newLoc.setTime(Long.parseLong(intent.getStringExtra(context.getString(R.string.gps_time))));
            newLoc.setAltitude(Double.parseDouble(intent.getStringExtra(context.getString(R.string.gps_altitude))));

            fHome.addNewLocation(newLoc);

        }
    };

    @Override
    public void onBackPressed() {

        if (menu.selected != 0)
            menu.change(0);
        else
            super.onBackPressed();
    }

}
