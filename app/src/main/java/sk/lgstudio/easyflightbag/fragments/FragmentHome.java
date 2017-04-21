package sk.lgstudio.easyflightbag.fragments;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.FlightPlanDialog;
import sk.lgstudio.easyflightbag.dialogs.FlightPlanInfoDialog;
import sk.lgstudio.easyflightbag.dialogs.OverlayDetailDialog;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;
import sk.lgstudio.easyflightbag.managers.FlightPlanManager;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;
import sk.lgstudio.easyflightbag.openAIP.Airport;
import sk.lgstudio.easyflightbag.openAIP.Airspace;

/**
 * The 1st fragment
 * Features:
 *  - Opens Flight Planning
 *  - Flight navigation
 *  - Shows gps data
 */
public class FragmentHome extends Fragment implements
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        DialogInterface.OnDismissListener{

    public final static float M_TO_FT = 3.2808410892388f;
    public final static float KMPH_TO_KNOT = 0.539957f;
    public final static float MPS_TO_KNOT = 1.94384f;
    public final static float MPS_TO_KMPH = 3.6f;

    private HorizontalScrollView planScrollView;
    private LinearLayout planList;
    private Button btnFlightPlan;
    private ImageButton btnFlightPlanLeft;
    private ImageButton btnFlightPlanRight;

    private TextView txtAccuracy;
    private TextView txtSpeed;
    private TextView txtAlt;
    private TextView txtBearing;
    private TextView txtSpeedUnit;
    private TextView txtAltUnit;
    private boolean isSpeedKnots = true;
    private boolean isAltMeter = true;

    private TextView txtNoGps;
    private TextView txtNoNet;

    protected MapView mapLayout = null;
    protected GoogleMap map = null;

    public File plansFolder;
    public File airplanesFolder;

    private boolean mapReady = false;
    private boolean mapNorthUp = true;
    private boolean mapFollow = true;
    private float mapZoomLevel = 14f;
    private float bearing = 0f;
    private float speed = 0f;
    private double alt = 0f;
    private float acc = 0f;
    public LatLng lastPosition = null;
    private LatLng mapTargetArea = null;
    private MarkerOptions locaionMarkerOptions;
    private Marker locationMarker = null;
    private BitmapDescriptor mapPointIcon;
    private TypedValue routeColor;

    //private boolean editing = false;
    private ArrayList<Marker> planMarkers = new ArrayList<>();
    private Polyline planLine;

    //private SelectorDialog dialogPlans = null;
    private FlightPlanDialog flightPlanDialog = null;

    public MapOverlayManager mapOverlayManager = null;
    public FlightPlanManager flightPlanManager = null;
    public AirplaneManager airplaneManager = null;
    public MainActivity activity;
    private LayoutInflater inflater;
    private Bundle instance;

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    /**
     * Create layout and load default settings for the layout
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        instance = savedInstanceState;
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Resources.Theme theme = getContext().getTheme();
        routeColor = new TypedValue();
        theme.resolveAttribute(R.attr.pinkColor, routeColor, true);

        btnFlightPlan = (Button) view.findViewById(R.id.home_button_plan);
        btnFlightPlanLeft = (ImageButton) view.findViewById(R.id.home_button_plan_left);
        btnFlightPlanRight = (ImageButton) view.findViewById(R.id.home_button_plan_right);

        btnFlightPlan.setOnClickListener(this);
        btnFlightPlanLeft.setOnClickListener(this);
        btnFlightPlanRight.setOnClickListener(this);

        planScrollView = (HorizontalScrollView) view.findViewById(R.id.home_plan_scrollview);
        planList = (LinearLayout) view.findViewById(R.id.home_plan_list);

        txtAccuracy = (TextView) view.findViewById(R.id.home_data_accuracy);
        txtSpeed = (TextView) view.findViewById(R.id.home_data_speed);
        txtAlt = (TextView) view.findViewById(R.id.home_data_altitude);
        txtBearing = (TextView) view.findViewById(R.id.home_data_bearing);
        txtSpeedUnit = (TextView) view.findViewById(R.id.home_data_speed_unit);
        txtAltUnit = (TextView) view.findViewById(R.id.home_data_altitude_unit);
        txtSpeed.setOnClickListener(this);
        txtAlt.setOnClickListener(this);

        txtNoGps = (TextView) view.findViewById(R.id.home_nogps);
        txtNoNet = (TextView) view.findViewById(R.id.home_nonet);

        ImageButton btnCenterMap = (ImageButton) view.findViewById(R.id.home_map_center);
        ImageButton btnRotateMap = (ImageButton) view.findViewById(R.id.home_map_rotate);
        btnCenterMap.setOnClickListener(this);
        btnRotateMap.setOnClickListener(this);

        mapLayout = (MapView) view.findViewById(R.id.home_map_view);
        mapLayout.onCreate(savedInstanceState);

        mapLayout.getMapAsync(this);

        initMapDrawables();

        return view;
    }

    /**
     * Fragment resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mapLayout != null)
            mapLayout.onResume();
        if (!airplaneManager.loaded)
            flightPlanManager = null;
        changeLayoutPanels();
        mapFollow = true;
    }

    /**
     * Fragment paused
     */
    @Override
    public void onPause() {
        if (mapLayout != null) mapLayout.onPause();
        mapReady = false;
        super.onPause();
    }

    /**
     * Destroy fragment
     */
    @Override
    public void onDestroy() {
        if (mapLayout != null) {
            try {
                mapLayout.onDestroy();
            } catch (NullPointerException e) {
                Log.e("MAP", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    /**
     * Reload view settings after fragment reopened
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            if (mapLayout != null) {
                mapLayout.onSaveInstanceState(savedInstanceState);
                instance = savedInstanceState;
            }
        }
    }

    /**
     * Save view settings befor closing fragment
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // ---------------------------------------------------------------
    // Map
    // ---------------------------------------------------------------

    /**
     * Called from activity to handle new location
     * @param loc
     */
    public void addNewLocation(boolean isEnabled, Location loc) {

        if (isEnabled){
            lastPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
            bearing = loc.getBearing();
            speed = loc.getSpeed();
            alt = loc.getAltitude();
            acc = loc.getAccuracy();

            locaionMarkerOptions.position(lastPosition).rotation(bearing);
            if (mapReady) {
                if (locationMarker != null) locationMarker.remove();
                locationMarker = map.addMarker(locaionMarkerOptions);
                if (mapFollow) changeMapPosition();
            }

            showPositionValues();
            txtNoGps.setVisibility(View.GONE);
        }
        else {
            lastPosition = null;
            if (mapReady) {
                if (locationMarker != null) {
                    locationMarker.remove();
                    locationMarker = null;
                }
            }

            txtNoGps.setVisibility(View.VISIBLE);
            txtAccuracy.setText("-");
            txtSpeed.setText("-");
            txtAlt.setText("-");
            txtBearing.setText("-");
        }
    }

    /**
     * Shows the real position data based on source and set unit
     */
    private void showPositionValues(){

        if (activity.gpsViaBt){ // bt
            if (isAltMeter)
                txtAlt.setText(new DecimalFormat("#").format(alt));
            else // m -> knot
                txtAlt.setText(new DecimalFormat("#").format(alt*M_TO_FT));

            if (isSpeedKnots) // m/s -> knot
                txtSpeed.setText(new DecimalFormat("#.#").format(speed*MPS_TO_KNOT));
            else // m/s -> km/h
                txtSpeed.setText(new DecimalFormat("#.#").format(speed*MPS_TO_KMPH));

        }
        else {  // gps
            if (isAltMeter)
                txtAlt.setText(new DecimalFormat("#").format(alt));
            else // m -> ft
                txtAlt.setText(new DecimalFormat("#").format(alt*M_TO_FT));

            if (isSpeedKnots) // km/h -> knot
                txtSpeed.setText(new DecimalFormat("#.#").format(speed*KMPH_TO_KNOT));
            else
                txtSpeed.setText(new DecimalFormat("#.#").format(speed));
        }

        txtAccuracy.setText(new DecimalFormat("#.#").format(acc));
        txtBearing.setText(new DecimalFormat("#").format(bearing) + "°");
    }

    /**
     * Jump to position on the map based on the follow and bearing settings
     */
    private void changeMapPosition(){
        float b;
        if (mapNorthUp)
            b = 0f;
        else
            b = bearing;

        LatLng pos;
        if (mapFollow)
            pos = lastPosition;
        else
            pos = mapTargetArea;

        CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(pos).zoom(mapZoomLevel).bearing(b).build());
        map.moveCamera(myLoc);

    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setOnCameraMoveListener(this);
        map.setOnMapClickListener(this);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);
        map.setOnMarkerClickListener(this);

        if (activity.nightMode)
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_dark));
        else
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_light));

        mapFollow = true;

        if(lastPosition != null) changeMapPosition();

        UiSettings settings = map.getUiSettings();
        settings.setCompassEnabled(false);
        settings.setMyLocationButtonEnabled(false);
        settings.setRotateGesturesEnabled(false);
        settings.setZoomControlsEnabled(false);
        settings.setZoomGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);

        mapReady = true;

        if (mapOverlayManager != null){
            loadOverlays();
        }

        if (planMarkers.size() > 0){
            loadFlightPlan();
        }

    }

    @Override
    public void onCameraMove() {
        mapZoomLevel = map.getCameraPosition().zoom;
        mapTargetArea = map.getCameraPosition().target;
        mapFollow = false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        (new GetPOITask()).execute((LatLng) latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        (new GetPOITask()).execute(marker.getPosition());
        return false;
    }

    /**
     * Load map overlays from mapOverlayManager
     */
    public void loadOverlays(){

        if (mapOverlayManager.isLoading || !mapReady)
            return;

        for (Airspace.Data d : mapOverlayManager.getAirspaces()) {
            PolygonOptions options = new PolygonOptions()
                    .addAll(d.polygon)
                    .strokeWidth(4)
                    .strokeColor(MapOverlayManager.airspaceStrokeColor(d.category))
                    .fillColor(MapOverlayManager.airspaceFillColor(d.category));

            map.addPolygon(options);
        }

        float r = 0f;
        if (!mapNorthUp) r = -bearing;

        for (Airport.Data d: mapOverlayManager.getAirports()){
            MarkerOptions options = new MarkerOptions()
                    .position(d.location)
                    .icon(BitmapDescriptorFactory.fromBitmap(d.icon))
                    .flat(true)
                    .rotation(r);

            map.addMarker(options).setAnchor(0.5f, 0.5f);
        }

    }

    /**
     * Reloads/creates plan markers for them map.
     */
    private void loadPlanMarkers(){

        for (Marker m: planMarkers){
            m.remove();
        }
        planMarkers.clear();
        if (planLine != null)
            planLine.remove();

        if (flightPlanManager != null) {
            PolylineOptions po = new PolylineOptions().geodesic(true).clickable(false).width(7f).color(routeColor.data);

            for (FlightPlanManager.Point p: flightPlanManager.plan){
                MarkerOptions o = new MarkerOptions().position(p.location).draggable(false).icon(mapPointIcon).anchor(0.5f,0.5f);
                Marker m = map.addMarker(o);
                planMarkers.add(m);
                po.add(p.location);
            }

            planLine = map.addPolyline(po);
        }
    }

    /**
     * Low mamory google map action
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapLayout != null) mapLayout.onLowMemory();
    }

    // ---------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------

    /**
     * Button click listener
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_button_plan_right:
                cancelRoute();
                break;
            case R.id.home_button_plan_left:
                showPlanInfo();
                break;
            case R.id.home_button_plan:
                openFlightPlans();
                break;
            case R.id.home_map_center:
                mapFollow = true;
                changeMapPosition();
                break;
            case R.id.home_map_rotate:
                mapNorthUp = !mapNorthUp;
                changeMapPosition();
                break;
            case R.id.home_data_speed:
                switchSpeedUnit();
                break;
            case R.id.home_data_altitude:
                switchAltUnit();
                break;
        }
    }

    /**
     * Switch between knots and km/h speed
     */
    private void switchSpeedUnit(){
        isSpeedKnots = !isSpeedKnots;
        if (isSpeedKnots) txtSpeedUnit.setText(getString(R.string.calc_unit_kn));
        else txtSpeedUnit.setText(getString(R.string.calc_unit_kmh));
        showPositionValues();
    }

    /**
     * Switch between m and ft altitude
     */
    private void switchAltUnit(){
        isAltMeter = !isAltMeter;
        if (isAltMeter) txtAltUnit.setText(getString(R.string.calc_unit_m));
        else txtAltUnit.setText(getString(R.string.calc_unit_ft));
        showPositionValues();
    }

    /**
     * Initializes the icons that will be put on map
     */
    private void initMapDrawables(){
        locaionMarkerOptions = new MarkerOptions().draggable(false).flat(true).icon(MapOverlayManager.getBitmapDescriptor(R.drawable.ic_plane_map, activity)).anchor(0.5f, 0.5f);

        VectorDrawable vectorDrawable = (VectorDrawable) activity.getDrawable(R.drawable.map_marker);
        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        mapPointIcon = BitmapDescriptorFactory.fromBitmap(bm);
    }

    /**
     * Show/Hide elevaton graph on the bottom
     */
    private void changeLayoutPanels() {

        if (flightPlanManager == null){
            planScrollView.setVisibility(View.GONE);
            btnFlightPlanLeft.setVisibility(View.GONE);
            btnFlightPlanRight.setVisibility(View.GONE);
            btnFlightPlan.setVisibility(View.VISIBLE);
        }
        else {
            planScrollView.setVisibility(View.VISIBLE);
            btnFlightPlanLeft.setVisibility(View.VISIBLE);
            btnFlightPlanRight.setVisibility(View.VISIBLE);
            btnFlightPlan.setVisibility(View.GONE);
        }
    }

    // ---------------------------------------------------------------
    // Flight plans
    // ---------------------------------------------------------------

    /**
     * Opens plan info dialog
     */
    private void showPlanInfo(){
        FlightPlanInfoDialog d = new FlightPlanInfoDialog(getContext(), airplaneManager, flightPlanManager);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_plan_info);
        d.show();
    }

    /**
     * Create flight plan chooser dialog
     */
    private void openFlightPlans(){
        //mapLayout.onDestroy();
        flightPlanDialog = new FlightPlanDialog(getContext(), R.style.FullScreenDialog, airplaneManager, mapOverlayManager, lastPosition, plansFolder, airplanesFolder, instance);
        flightPlanDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        flightPlanDialog.setContentView(R.layout.dialog_plan);
        flightPlanDialog.setOnDismissListener(this);
        flightPlanDialog.show();
    }

    /**
     * Plan selector Dialog dismiss handler
     * @param dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (flightPlanDialog != null){
            flightPlanManager = flightPlanDialog.flightPlanManager;
            flightPlanDialog = null;
            loadFlightPlan();
            changeLayoutPanels();
        }
        if (flightPlanManager == null)
            airplaneManager.isRoute = false;
        else
            airplaneManager.isRoute = true;
    }

    /**
     * Loads flight plan into the list
     */
    private void loadFlightPlan(){
        if (flightPlanManager != null)
            refillPlanList();

        loadPlanMarkers();
    }

    /**
     * Fills the plan list with items
     */
    private void refillPlanList(){

        planList.removeAllViews();

        int i = 0;
        for (FlightPlanManager.Point p: flightPlanManager.plan){
            if (i > 0)
                addPlanArrow();

            TextView txt = (TextView) inflater.inflate(R.layout.item_text_h, null);
            txt.setText(p.name);
            final int finalI = i;
            txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    planItemClick(finalI);
                }
            });

            planList.addView(txt);

            i++;
        }
    }

    /**
     * Adds a simple arrow icon to the plan list
     */
    private void addPlanArrow(){
        ImageView div = new ImageView(getContext());
        div.setImageResource(R.drawable.ic_expand_right_inv);
        div.setScaleX(0.5f);
        div.setScaleY(0.5f);
        planList.addView(div);
    }

    /**
     * On planning cancel button press
     */
    private void cancelRoute(){
        flightPlanManager = null;
        airplaneManager.isRoute = false;
        changeLayoutPanels();
        loadPlanMarkers();
    }

    /**
     * Handle Flight plan list item click
     * @param position
     */
    public void planItemClick(int position) {
        if (flightPlanManager != null){
            (new GetPOITask()).execute((LatLng) flightPlanManager.plan.get(position).location);
        }
    }

    // ---------------------------------------------------------------
    // Async Tasks
    // ---------------------------------------------------------------

    /**
     * Get POIs at map coordinates
     */
    public class GetPOITask extends AsyncTask<LatLng, Void, Void>{

        ArrayList<Airport.Data> airports;
        ArrayList<Airspace.Data> airspaces;

        LatLng coord;

        @Override
        protected Void doInBackground(LatLng... params) {

            coord = params[0];
            airspaces = mapOverlayManager.getAirspacesAt(coord);
            airports = mapOverlayManager.getAirportsCloseBy(coord);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (airspaces.size() > 0){
                final OverlayDetailDialog d = new OverlayDetailDialog(getContext());
                d.setContentView(R.layout.dialog_overlay_detail);
                d.loadContent(lastPosition, coord, airspaces, airports);
                d.show();
            }
        }
    }

}
