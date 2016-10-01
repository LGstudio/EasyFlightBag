package sk.lgstudio.easyflightbag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.service.GPSTrackerService;
import sk.lgstudio.easyflightbag.ui.CustomFragmentAdapter;
import sk.lgstudio.easyflightbag.ui.CustomViewPager;
import sk.lgstudio.easyflightbag.ui.FragmentAip;
import sk.lgstudio.easyflightbag.ui.FragmentCalc;
import sk.lgstudio.easyflightbag.ui.FragmentChklist;
import sk.lgstudio.easyflightbag.ui.FragmentDocs;
import sk.lgstudio.easyflightbag.ui.FragmentHome;
import sk.lgstudio.easyflightbag.ui.FragmentPlan;
import sk.lgstudio.easyflightbag.ui.FragmentSettings;
import sk.lgstudio.easyflightbag.ui.FragmentWeather;
import sk.lgstudio.easyflightbag.ui.TabMenu;

import static android.os.Environment.getDataDirectory;

public class MainActivity extends AppCompatActivity {

    private FragmentHome fHome = new FragmentHome();

    private TabMenu menu;
    private CustomViewPager viewPager;

    private File rootDir;
    private File chkDir;

    private ArrayList<Location> track = new ArrayList<>();
    private ArrayList<Location> route = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (userAllowedLocation()) {
            this.startGPSService();
        }
        else{
            // Request missing location permission.
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        }

        checkDirectory();
        initView();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        stopService(new Intent(this, GPSTrackerService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsReceiver);
    }

    /**
     * Checks if directories exist on internal memory
     */
    private void checkDirectory(){
        rootDir = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_root));
        if(!rootDir.exists()) {
            rootDir.mkdir();
        }

        chkDir = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_chklist));
        if(!chkDir.exists()) {
            chkDir.mkdir();
        }

    }

    /**
     * initializes fragments and top menu on the view
     */
    private void initView(){
        final CustomFragmentAdapter fA = new CustomFragmentAdapter(getSupportFragmentManager());

        fHome.track = track;

        fA.addFragment(fHome);
        fA.addFragment(new FragmentAip());
        fA.addFragment(new FragmentWeather());
        fA.addFragment(new FragmentChklist());
        fA.addFragment(new FragmentDocs());
        fA.addFragment(new FragmentPlan());
        fA.addFragment(new FragmentCalc());
        fA.addFragment(new FragmentSettings());

        viewPager = (CustomViewPager) findViewById(R.id.view_fragment_pager);
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
