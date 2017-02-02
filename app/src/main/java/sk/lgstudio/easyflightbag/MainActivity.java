package sk.lgstudio.easyflightbag;

import android.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.managers.AIPManager;
import sk.lgstudio.easyflightbag.services.GPSTrackerService;
import sk.lgstudio.easyflightbag.menu.TabFragmentAdapter;
import sk.lgstudio.easyflightbag.menu.TabViewPager;
import sk.lgstudio.easyflightbag.fragments.FragmentAip;
import sk.lgstudio.easyflightbag.fragments.FragmentCalc;
import sk.lgstudio.easyflightbag.fragments.FragmentChklist;
import sk.lgstudio.easyflightbag.fragments.FragmentDocs;
import sk.lgstudio.easyflightbag.fragments.FragmentHome;
import sk.lgstudio.easyflightbag.fragments.FragmentSettings;
import sk.lgstudio.easyflightbag.menu.TabMenu;

public class MainActivity extends FragmentActivity {

    public final static int MENU_NAV = 0;
    public final static int MENU_CAL = 1;
    public final static int MENU_AIP = 2;
    public final static int MENU_CHKL = 3;
    public final static int MENU_DOCS = 4;
    public final static int MENU_SET = 5;


    private FragmentHome fHome = new FragmentHome();
    private FragmentCalc fCalc = new FragmentCalc();
    private FragmentChklist fChk = new FragmentChklist();
    private FragmentSettings fSet  = new FragmentSettings();
    private FragmentAip fAip = new FragmentAip();

    private TabMenu menu;

    private ArrayList<Location> track = new ArrayList<>();
    //private ArrayList<Location> route = new ArrayList<>();

    public SharedPreferences prefs;
    public boolean nightMode = false;
    public AIPManager aipManager;

    private boolean isChkTutCreated = false;
    private boolean inited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = this.getSharedPreferences(getString(R.string.app_prefs), Context.MODE_PRIVATE);

        permissionCheck();
        inited = true;

        // checks internal storage for existing files
        checkDirectory();

        // loads shared preferences
        loadSharedPerefs();

        // Load selected theme
        changeToNight();

        // create AIP manager
        aipManager = new AIPManager(this);

        setContentView(R.layout.activity_main);

        startGPSService();

        // creates fragments
        initView();

    }

    @Override
    public void onResume(){
        super.onResume();
        if (!inited)
            permissionCheck();
    }

    @Override
    public void onStop(){
        super.onStop();
        inited = false;
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

        //TODO: stop AIP downloader ???
    }

    /**
     * Android 6.0 and newer - checks for permissions
     */
    private void permissionCheck(){
        // Request missing location permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
       if (grantResults.length < 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED ){
           finish();
       }
    }

    /**
     * Loads the saved preferences when application starts
     */
    private void loadSharedPerefs(){
        // TODO: MOve to Fragemnt Chklist + extend with autorecovery feature
        // Load last opened Checklist
        if (isChkTutCreated){
            fChk.folderActual = new File(fChk.folder.getPath() + getString(R.string.folder_chklist_demo));
        }
        else {
            String prefChkActual = prefs.getString(getString(R.string.pref_chk_folder), null);
            if (prefChkActual != null) {
                fChk.folderActual = new File(fChk.folder.getPath() + prefChkActual);
                if (!fChk.folderActual.exists()){
                    fChk.folderActual = null;
                }
            }
        }
    }


    /**
     * Changes between day and night mode
     */
    public void changeToNight(){
        nightMode = prefs.getBoolean(getString(R.string.pref_theme), false);
        if (nightMode)
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
    }

    /**
     * Checks if directories exist on internal memory
     * Adds default data in case of first app launch or when data were deleted
     */
    private void checkDirectory(){
        File rootDir = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_root));
        if(!rootDir.exists()) {
            rootDir.mkdir();
        }

        fChk.folder = new File(rootDir.getPath() + getString(R.string.folder_chklist));
        if(!fChk.folder.exists()) {
            fChk.folder.mkdir();
            // TODO: move to Checklist fragment to deal with these stuff
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

        fCalc.folder = new File(rootDir.getPath() + getString(R.string.folder_airplanes));
        if (!fCalc.folder.exists())
            fCalc.folder.mkdir();

        File aipFolder = new File(rootDir.getPath() + getString(R.string.folder_aip));
        if (!aipFolder.exists())
            aipFolder.mkdir();

        //TODO: move to AIP to handle this
        fAip.folder = new File(aipFolder.getPath() + getString(R.string.folder_cz));
        if (!fAip.folder.exists())
            fAip.folder.mkdir();
    }

    /**
     * initializes fragments and top menu on the view
     */
    public void initView(){
        final TabFragmentAdapter fA = new TabFragmentAdapter(getSupportFragmentManager());

        fHome.track = track;
        fA.addFragment(fHome);
        fCalc.prefs = prefs;
        fA.addFragment(fCalc);
        fA.addFragment(fAip);
        fChk.prefs = prefs;
        fA.addFragment(fChk);
        fA.addFragment(new FragmentDocs());
        fSet.activity = this;
        fA.addFragment(fSet);

        TabViewPager viewPager = (TabViewPager) findViewById(R.id.view_fragment_pager);
        viewPager.setAdapter(fA);
        viewPager.setPagingEnabled(false);

        menu = new TabMenu(this, viewPager);
    }

    /**
     * Starts GPS service
     */
    private void startGPSService() {
        //startService(new Intent(this, GPSTrackerService.class));
        //LocalBroadcastManager.getInstance(this).registerReceiver(gpsReceiver, new IntentFilter(this.getString(R.string.gps_intent_filter)));
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

    public void aipDataChange(int c){
        if (menu.selected == MENU_SET){
            fSet.reloadAipData(c);
        }
    }


    @Override
    public void onBackPressed() {

        if (menu.selected != MENU_NAV)
            menu.change(MENU_NAV);
        else
            super.onBackPressed();
    }

}
