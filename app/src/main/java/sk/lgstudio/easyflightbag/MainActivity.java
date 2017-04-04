package sk.lgstudio.easyflightbag;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.dialogs.SplashDialog;
import sk.lgstudio.easyflightbag.managers.AIPManager;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;
import sk.lgstudio.easyflightbag.services.AirspaceDownloader;
import sk.lgstudio.easyflightbag.services.BTTrackerService;
import sk.lgstudio.easyflightbag.services.GPSTrackerService;
import sk.lgstudio.easyflightbag.fragments.FragmentAip;
import sk.lgstudio.easyflightbag.fragments.FragmentCalc;
import sk.lgstudio.easyflightbag.fragments.FragmentChklist;
import sk.lgstudio.easyflightbag.fragments.FragmentDocs;
import sk.lgstudio.easyflightbag.fragments.FragmentHome;
import sk.lgstudio.easyflightbag.fragments.FragmentSettings;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    // static values
    public final static int MENU_NAV = 0;
    public final static int MENU_CAL = 1;
    public final static int MENU_AIP = 2;
    public final static int MENU_CHKL = 3;
    public final static int MENU_DOCS = 4;
    public final static int MENU_SET = 5;

    public final static int NOTIFICATION_AIRSAPCE = 0;
    public final static int NOTIFICATION_AIP = 1;

    public final static int DAYS_IN_MILLISECONDS = 24*60*60*1000;

    // menu button ids
    private final int[] tabIds = {
            R.id.tab_home,
            R.id.tab_calc,
            R.id.tab_aip,
            R.id.tab_chklist,
            R.id.tab_docs,
            R.id.tab_set
    };

    // framnets
    private FragmentHome fHome = new FragmentHome();
    private FragmentCalc fCalc = new FragmentCalc();
    private FragmentChklist fChk = new FragmentChklist();
    private FragmentSettings fSet  = new FragmentSettings();
    private FragmentAip fAip = new FragmentAip();
    private FragmentDocs fDocs = new FragmentDocs();

    // menu paging related
    public ArrayList<ImageButton> menuTabs = new ArrayList<>();
    private int selectedTab = -1;

    // public variables for fragments
    public SharedPreferences prefs;
    public boolean nightMode = false;
    public boolean gpsViaBt = false;

    // random private variables needed for some reason
    private boolean inited = false;
    private MainActivity activity;
    private SplashDialog splash;

    /**
     * ------------------------------------------
     * Lifecycle
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        splash = new SplashDialog(this, R.style.FullScreenDialog);
        splash.setContentView(R.layout.dialog_splash);
        splash.setCancelable(false);
        splash.show();

        (new LoadContentTask()).execute((Void) null);
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

        if (fHome.lastPosition != null) {
            prefs.edit().putFloat(getString(R.string.gps_latitude), (float) fHome.lastPosition.latitude).apply();
            prefs.edit().putFloat(getString(R.string.gps_longitude), (float) fHome.lastPosition.longitude).apply();
        }
        if (fChk.folderActual != null) prefs.edit().putString(getString(R.string.pref_chk_folder), fChk.folderActual.getPath()).apply();
        else prefs.edit().remove(getString(R.string.pref_chk_folder)).apply();

        if (gpsViaBt) stopService(new Intent(this, BTTrackerService.class));
        else stopService(new Intent(this, GPSTrackerService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.gpsReceiver);

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
            if (checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, 0);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 0);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 0);
            }
        }
    }

    /**
     * Permission check result handling
     * Close if not all permissions are granted
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
       if (grantResults.length < 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED ){
           finish();
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
    private void loadIntoFragments(){

        // init all folders
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
                prefs.edit().putString(getString(R.string.pref_chk_folder), chkTutDir.getPath()).apply();
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

        fDocs.folder = new File(rootDir.getPath() + getString(R.string.folder_docs));
        if (!fDocs.folder.exists())
            fDocs.folder.mkdir();

        //TODO: move to AIP to handle this
        fAip.folder = new File(aipFolder.getPath() + getString(R.string.folder_cz));
        if (!fAip.folder.exists())
            fAip.folder.mkdir();

        File fldr = new File(rootDir.getPath() + getString(R.string.folder_airspace));
        if (!fldr.exists())
            fldr.mkdir();

        // init managers
        MapOverlayManager mapOverlayManager = new MapOverlayManager(activity, fldr);
        AIPManager aipManager = new AIPManager(activity);

        fHome.plansFolder = new File(rootDir.getPath() + getString(R.string.folder_plans));
        if (!fHome.plansFolder.exists())
            fHome.plansFolder.mkdir();

        // load values to fragments
        fHome.lastPosition = new LatLng(prefs.getFloat(getString(R.string.gps_latitude), 0), prefs.getFloat(getString(R.string.gps_longitude), 0));
        fHome.activity = activity;
        fHome.mapOverlayManager = mapOverlayManager;
        fCalc.prefs = prefs;
        fChk.prefs = prefs;
        fSet.activity = activity;
        fSet.aipManager = aipManager;
        fSet.mapOverlayManager = mapOverlayManager;
    }

    /**
     * Initializes the view for the first use
     */
    public void initView(){

        FragmentTransaction fragmentView = getSupportFragmentManager().beginTransaction();
        fragmentView.add(R.id.view_fragment, fHome);
        fragmentView.addToBackStack(null);
        fragmentView.commit();

        for (int id : tabIds)
            activity.menuTabs.add((ImageButton) findViewById(id));

        for (ImageButton ib : activity.menuTabs)
            ib.setOnClickListener(this);

        selectedTab = 0;
    }

    /**
     * Click listener only for the menu items
     * @param v
     */
    @Override
    public void onClick(View v) {
        int clicked = 0;
        for (int i = 0; i < tabIds.length; i++)
            if (tabIds[i] == v.getId())
                clicked = i;

        if (selectedTab != clicked)
            changeTab(clicked);
    }

    /**
     * Changes the opened tab int the viewPager
     * @param clicked
     */
    public void changeTab(int clicked){
        activity.menuTabs.get(selectedTab).setBackgroundResource(R.drawable.bck_transparent);
        activity.menuTabs.get(selectedTab).setAlpha((float) 0.5);
        activity.menuTabs.get(clicked).setBackgroundResource(R.drawable.bck_tab_selected);
        activity.menuTabs.get(clicked).setAlpha((float) 1.0);

        FragmentTransaction fragmentView = getSupportFragmentManager().beginTransaction();

        switch (clicked){
            case 0:
                fragmentView.replace(R.id.view_fragment,  fHome);
                break;
            case 1:
                fragmentView.replace(R.id.view_fragment,  fCalc);
                break;
            case 2:
                fragmentView.replace(R.id.view_fragment,  fAip);
                break;
            case 3:
                fragmentView.replace(R.id.view_fragment,  fChk);
                break;
            case 4:
                fragmentView.replace(R.id.view_fragment,  fDocs);
                break;
            case 5:
                fragmentView.replace(R.id.view_fragment,  fSet);
                break;
        }

        fragmentView.addToBackStack(null);
        fragmentView.commit();

        selectedTab = clicked;
    }

    /**
     * Called to turn on/off GPS simulation via Bt
     * @param toBt
     */
    public void switchLocationService(boolean toBt){
        prefs.edit().putBoolean(getString(R.string.pref_gps_bt),toBt).apply();
        gpsViaBt = toBt;
        if (toBt) {
            stopService(new Intent(this, GPSTrackerService.class));
            startService(new Intent(this, BTTrackerService.class));
        }
        else {
            stopService(new Intent(this, BTTrackerService.class));
            startService(new Intent(this, GPSTrackerService.class));
        }
    }

    /**
     * Receiver of new locations from the GPS service
     */
    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (selectedTab == MENU_NAV){
                boolean isEnabled = intent.getBooleanExtra(context.getString(R.string.gps_enabled), false);

                if (isEnabled) {
                    Location newLoc = new Location(getString(R.string.gps_location));
                    newLoc.setLatitude(intent.getDoubleExtra(context.getString(R.string.gps_latitude), 0.0));
                    newLoc.setLongitude(intent.getDoubleExtra(context.getString(R.string.gps_longitude), 0.0));
                    newLoc.setAccuracy(intent.getFloatExtra(context.getString(R.string.gps_accuracy), 0f));
                    newLoc.setBearing(intent.getFloatExtra(context.getString(R.string.gps_bearing), 0f));
                    newLoc.setSpeed(intent.getFloatExtra(context.getString(R.string.gps_speed), 0f));
                    newLoc.setTime(intent.getLongExtra(context.getString(R.string.gps_time), 0));
                    newLoc.setAltitude(intent.getDoubleExtra(context.getString(R.string.gps_altitude), 0.0));

                    fHome.addNewLocation(isEnabled, newLoc);
                }
                else {
                    fHome.addNewLocation(isEnabled, null);
                }
            }
        }
    };

    /**
     * reloads AIP data status in settings
     * @param c
     */
    public void aipDataChange(int c){
        if (selectedTab == MENU_SET){
            fSet.reloadAipData(c);
        }
    }

    /**
     * reloads Airspace data status in settings
     */
    public void airDataChange(){
        if (selectedTab == MENU_SET){
            fSet.reloadMapOverlayData();
        }
    }

    @Override
    public void onBackPressed() {
        if (selectedTab != MENU_NAV)
            changeTab(MENU_NAV);
    }

    /**
     * Background task to initialize the application and load content
     */
    private class LoadContentTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            prefs = getSharedPreferences(getString(R.string.app_prefs), Context.MODE_PRIVATE);

            // check permissions
            permissionCheck();
            inited = true;

            // Load selected theme
            changeToNight();

            // checks internal storage for existing files
            loadIntoFragments();

            // start gps service
            gpsViaBt = prefs.getBoolean(getString(R.string.pref_gps_bt), false);
            if (gpsViaBt)
                startService(new Intent(activity, BTTrackerService.class));
            else
                startService(new Intent(activity, GPSTrackerService.class));
            LocalBroadcastManager.getInstance(activity).registerReceiver(gpsReceiver, new IntentFilter(activity.getString(R.string.gps_intent_filter)));

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            // creates fragments
            setContentView(R.layout.activity_main);
            initView();

            // show the screen
            splash.dismiss();
        }
    }

}
