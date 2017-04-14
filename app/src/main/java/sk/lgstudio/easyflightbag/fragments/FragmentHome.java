package sk.lgstudio.easyflightbag.fragments;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import sk.lgstudio.easyflightbag.dialogs.OverlayDetailDialog;
import sk.lgstudio.easyflightbag.dialogs.SelectorDialog;
import sk.lgstudio.easyflightbag.managers.FlightPlanManager;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;
import sk.lgstudio.easyflightbag.openAIP.Airport;
import sk.lgstudio.easyflightbag.openAIP.Airspace;

/**
 * The 1st fragment
 * Features:
 *  - Flight planning
 *  - Flight navigation
 *  - Shows gps data
 */
public class FragmentHome extends Fragment implements
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        DialogInterface.OnCancelListener,
        DialogInterface.OnDismissListener{

    public final static float M_TO_FT = 3.2808410892388f;
    public final static float KMPH_TO_KNOT = 0.539957f;
    public final static float MPS_TO_KNOT = 1.94384f;
    public final static float MPS_TO_KMPH = 3.6f;

    private RelativeLayout panelMap;
    private LinearLayout panelInfo;
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

    private boolean editing = false;
    private ArrayList<Marker> planMarkers = new ArrayList<>();
    private Polyline planLine;

    private SelectorDialog dialogPlans = null;

    public MapOverlayManager mapOverlayManager = null;
    public FlightPlanManager flightPlanManager = null;
    public MainActivity activity;
    private LayoutInflater inflater;

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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnFlightPlan = (Button) view.findViewById(R.id.home_button_plan);
        btnFlightPlanLeft = (ImageButton) view.findViewById(R.id.home_button_plan_left);
        btnFlightPlanRight = (ImageButton) view.findViewById(R.id.home_button_plan_right);

        btnFlightPlan.setOnClickListener(this);
        btnFlightPlanLeft.setOnClickListener(this);
        btnFlightPlanRight.setOnClickListener(this);

        planScrollView = (HorizontalScrollView) view.findViewById(R.id.home_plan_scrollview);
        planList = (LinearLayout) view.findViewById(R.id.home_plan_list);

        panelInfo = (LinearLayout) view.findViewById(R.id.home_gps_info_panel);
        panelMap = (RelativeLayout) view.findViewById(R.id.home_map_layout);

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
        if (mapLayout != null) mapLayout.onResume();
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
            if (mapLayout != null) mapLayout.onSaveInstanceState(savedInstanceState);
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

        if (isEnabled && !editing){
            lastPosition = new LatLng(loc.getLatitude(), loc.getLongitude());
            bearing = loc.getBearing();
            speed = loc.getSpeed();
            alt = loc.getAltitude();
            acc = loc.getAccuracy();

            float b = 0f;
            if (mapNorthUp) b = bearing;

            locaionMarkerOptions.position(lastPosition).rotation(b);
            if (mapReady) {
                if (locationMarker != null) locationMarker.remove();
                locationMarker = map.addMarker(locaionMarkerOptions);
                if (mapFollow) changeMapPosition();
            }

            showPositionValues();

            txtNoGps.setVisibility(View.GONE);
        }

        else if (lastPosition != null && !editing) {
            lastPosition = null;
            if (mapReady) {
                if (locationMarker != null) {
                    locationMarker.remove();
                    locationMarker = null;
                }
                mapFollow = false;
            }

            txtNoGps.setVisibility(View.VISIBLE);
            txtAccuracy.setText("-");
            txtSpeed.setText("-");
            txtAlt.setText("-");
            txtBearing.setText("-");
        }
        else if (mapReady) {
            if (locationMarker != null) {
                locationMarker.remove();
                locationMarker = null;
            }
            mapFollow = false;
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
        settings.setCompassEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setRotateGesturesEnabled(false);
        settings.setZoomControlsEnabled(false);
        settings.setZoomGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);

        mapReady = true;

        if (mapOverlayManager != null){
            loadOverlays();
        }

        if (planMarkers.size() > 0){
            changeLayoutPanels();
            loadFlightPlan();
            loadPlanMarkers();
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
        if (editing){
            flightPlanManager.addNewPoint(latLng);
            refillPlanListEdit();
            planScrollView.smoothScrollTo(HorizontalScrollView.FOCUS_RIGHT, 0);
            loadPlanMarkers();
        }
        else (new GetPOITask()).execute((LatLng) latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (editing){
            LatLng latLng = marker.getPosition();
            String name = mapOverlayManager.getAirportICAO(latLng);

            if (name.length() == 0) flightPlanManager.addNewPoint(latLng);
            else flightPlanManager.addNewPoint(latLng, name);

            refillPlanListEdit();
            planScrollView.smoothScrollTo(HorizontalScrollView.FOCUS_RIGHT, 0);
            loadPlanMarkers();
        }
        else (new GetPOITask()).execute(marker.getPosition());

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
            PolylineOptions po = new PolylineOptions().geodesic(true).clickable(false).width(7f).color(Color.MAGENTA);
            if (editing){
                for (FlightPlanManager.Point p: flightPlanManager.editedPlan) {
                    MarkerOptions o = new MarkerOptions().position(p.location).draggable(p.editeble).icon(mapPointIcon).anchor(0.5f, 0.5f);
                    Marker m = map.addMarker(o);
                    planMarkers.add(m);
                    po.add(p.location);
                }
            }
            else {
                for (FlightPlanManager.Point p: flightPlanManager.plan){
                    MarkerOptions o = new MarkerOptions().position(p.location).draggable(false).icon(mapPointIcon).anchor(0.5f,0.5f);
                    Marker m = map.addMarker(o);
                    planMarkers.add(m);
                    po.add(p.location);
                }
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
                if (editing) saveEditedRoute();
                else cancelRoute();
                break;
            case R.id.home_button_plan_left:
                if (editing) cancelRoute();
                else showPlanInfo();
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
        locaionMarkerOptions = new MarkerOptions().draggable(false).icon(MapOverlayManager.getBitmapDescriptor(R.drawable.ic_plane_map, activity)).anchor(0.5f, 0.5f);

        VectorDrawable vectorDrawable = (VectorDrawable) activity.getDrawable(R.drawable.map_marker);
        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        mapPointIcon = BitmapDescriptorFactory.fromBitmap(bm);
    }

    // TODO: FIX WRONG bOTTOM PANEL SIZES
    /**
     * Show/Hide elevaton graph on the bottom
     */
    private void changeLayoutPanels() {

        int bottomSize = 0;

        if (!editing){
            if (flightPlanManager == null){
                planScrollView.setVisibility(View.GONE);
                btnFlightPlanLeft.setVisibility(View.GONE);
                btnFlightPlanRight.setVisibility(View.GONE);
                btnFlightPlan.setVisibility(View.VISIBLE);
            }
            else {
                planScrollView.setVisibility(View.VISIBLE);
                bottomSize += planScrollView.getHeight();
                btnFlightPlanLeft.setVisibility(View.VISIBLE);
                btnFlightPlanLeft.setImageResource(R.drawable.ic_info_inv);
                btnFlightPlanRight.setVisibility(View.VISIBLE);
                btnFlightPlanRight.setImageResource(R.drawable.ic_clear_inv);
                btnFlightPlan.setVisibility(View.GONE);
            }
            panelInfo.setVisibility(View.VISIBLE);
            bottomSize += panelInfo.getHeight();

        }
        else{
            panelInfo.setVisibility(View.GONE);
            btnFlightPlan.setVisibility(View.GONE);
            planScrollView.setVisibility(View.VISIBLE);
            bottomSize += planScrollView.getHeight();
            btnFlightPlanLeft.setVisibility(View.VISIBLE);
            btnFlightPlanLeft.setImageResource(R.drawable.ic_arrow_back_inv);
            btnFlightPlanRight.setVisibility(View.VISIBLE);
            btnFlightPlanRight.setImageResource(R.drawable.ic_done_inv);
        }

        Log.e("Bottom panel", bottomSize + "px");
        panelMap.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getView().getHeight() - bottomSize));
    }

    // ---------------------------------------------------------------
    // Flight plans
    // ---------------------------------------------------------------

    /**
     * Create flight plan chooser dialog
     */
    private void openFlightPlans(){
        dialogPlans = new SelectorDialog(getContext());
        dialogPlans.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPlans.setContentView(R.layout.dialog_selector);
        dialogPlans.loadContent(plansFolder, plansFolder, true, SelectorDialog.TYPE_FLIGHTPLAN);
        dialogPlans.setOnCancelListener(this);
        dialogPlans.setOnDismissListener(this);
        dialogPlans.show();
    }

    // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    /**
     * Opens plan info dialog
     */
    private void showPlanInfo(){

    }

    /**
     * Plan seletor Dialog on cancel handler
     * @param dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        // no new plan was selected
        if (dialogPlans.selected == null) {
            flightPlanManager = null;
            editing = false;
            loadFlightPlan();
            loadPlanMarkers();
        }
        dialogPlans = null;
    }

    /**
     * Plan selector Dialog dismiss handler
     * @param dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog) {

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (dialogPlans != null){
            if (dialogPlans.selected != null){
                flightPlanManager = new FlightPlanManager(dialogPlans.selected);
                editing = dialogPlans.edit;
            }
            else {
                flightPlanManager = null;
            }

            dialogPlans = null;
            loadFlightPlan();
            loadPlanMarkers();
            changeLayoutPanels();
        }
    }

    /**
     * Loads flight plan into the list
     */
    private void loadFlightPlan(){
        if (flightPlanManager != null)
            if (editing){
                if (flightPlanManager.editedPlan == null)
                    flightPlanManager.editedPlan = new ArrayList<>(flightPlanManager.plan);
                refillPlanListEdit();
            }
            else{
                refillPlanList();
            }
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

            TextView txt = (TextView) inflater.inflate(R.layout.list_text_h_item, null);
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
     * Fills the plan list with editor items
     */
    private void refillPlanListEdit(){
        planList.removeAllViews();

        int i = 0;
        for (FlightPlanManager.Point p: flightPlanManager.editedPlan){
            LinearLayout elem = (LinearLayout) inflater.inflate(R.layout.list_edit_h_item, null);

            TextView txt = (TextView) elem.findViewById(R.id.list_edit_text);
            txt.setText(p.name);
            ImageButton btn = (ImageButton) elem.findViewById(R.id.list_edit_delete);
            final int finalI = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePlanPoint(finalI);
                }
            });

            planList.addView(elem);

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
        editing = false;
        flightPlanManager = null;
        changeLayoutPanels();
        loadPlanMarkers();
    }

    /**
     * Saves the edidted route from the list
     */
    private void saveEditedRoute(){
       if (flightPlanManager.saveEditedPlan()){
           editing = false;
           flightPlanManager.editedPlan = null;
           changeLayoutPanels();
           loadPlanMarkers();
           Toast.makeText(getContext(), getContext().getString(R.string.plan_warning_saved), Toast.LENGTH_SHORT).show();
       }
       else
           Toast.makeText(getContext(), getContext().getString(R.string.plan_warning_save_error), Toast.LENGTH_SHORT).show();
    }

    /**
     * Removes data point from the list when in editing mode
     * @param posiotion
     */
    private void removePlanPoint(int posiotion){
        flightPlanManager.editedPlan.remove(posiotion);
        planList.removeViewAt(2*posiotion); // position
        if (posiotion > 0) planList.removeViewAt(2*posiotion - 1); // arrow
        loadPlanMarkers();
    }

    /**
     * Handle Flight plan list item click
     * @param position
     */
    public void planItemClick(int position) {
        if (!editing && flightPlanManager != null){
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
