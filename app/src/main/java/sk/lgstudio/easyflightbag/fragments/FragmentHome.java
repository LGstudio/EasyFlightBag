package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
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

import lecho.lib.hellocharts.view.LineChartView;
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
        DialogInterface.OnDismissListener,
        AdapterView.OnItemClickListener {

    private RelativeLayout panelBottom;
    private RelativeLayout panelMap;

    private ImageButton btnPanelBottom;
    private Button btnFlightPlanTop;
    private Button btnFlightPlanBottom;
    private ListView listFlightPlan;
    private int planWidth = 0;

    private LineChartView elevationChart;
    private LinearLayout panelInfo;
    private LinearLayout panelChart;

    private TextView txtAccuracy;
    private TextView txtSpeed;
    private TextView txtAlt;
    private TextView txtBearing;

    private TextView txtNoGps;
    private TextView txtNoNet;

    protected MapView mapLayout = null;
    protected GoogleMap map = null;

    public File plansFolder;

    private boolean isElevationGraphVisible = true;
    private boolean mapReady = false;
    private boolean mapNorthUp = true;
    private boolean mapFollow = true;
    private float mapZoomLevel = 14f;
    private float bearing = 0f;
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnPanelBottom = (ImageButton) view.findViewById(R.id.home_panel_bottom_btn);
        btnFlightPlanTop = (Button) view.findViewById(R.id.home_button_plan_top);
        btnFlightPlanBottom = (Button) view.findViewById(R.id.home_button_plan_bottom);

        btnPanelBottom.setOnClickListener(this);
        btnFlightPlanTop.setOnClickListener(this);
        btnFlightPlanBottom.setOnClickListener(this);

        panelBottom = (RelativeLayout) view.findViewById(R.id.home_panel_bottom);
        panelInfo = (LinearLayout) view.findViewById(R.id.home_gps_info_panel);
        panelMap = (RelativeLayout) view.findViewById(R.id.home_map_layout);
        listFlightPlan = (ListView) view.findViewById(R.id.home_plan_list);
        listFlightPlan.setOnItemClickListener(this);

        txtAccuracy = (TextView) view.findViewById(R.id.home_data_accuracy);
        txtSpeed = (TextView) view.findViewById(R.id.home_data_speed);
        txtAlt = (TextView) view.findViewById(R.id.home_data_altitude);
        txtBearing = (TextView) view.findViewById(R.id.home_data_bearing);

        txtNoGps = (TextView) view.findViewById(R.id.home_nogps);
        txtNoNet = (TextView) view.findViewById(R.id.home_nonet);

        ImageButton btnCenterMap = (ImageButton) view.findViewById(R.id.home_map_center);
        ImageButton btnRotateMap = (ImageButton) view.findViewById(R.id.home_map_rotate);
        btnCenterMap.setOnClickListener(this);
        btnRotateMap.setOnClickListener(this);

        panelChart = (LinearLayout) view.findViewById(R.id.home_elevation_graph_panel);
        elevationChart = (LineChartView) view.findViewById(R.id.home_elevation_graph);
        NumberPicker chartDistance = (NumberPicker) view.findViewById(R.id.home_elevation_graph_picker);
        chartDistance.setMinValue(1);
        chartDistance.setMaxValue(10);
        chartDistance.setValue(10);

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
            isElevationGraphVisible = savedInstanceState.getBoolean("PBottom");
            changeLayoutPanels(isElevationGraphVisible);
            if (mapLayout != null) mapLayout.onSaveInstanceState(savedInstanceState);
        }
    }

    /**
     * Save view settings befor closing fragment
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("PBottom", isElevationGraphVisible);

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

            locaionMarkerOptions.position(lastPosition).rotation(loc.getBearing());
            if (mapReady) {
                if (locationMarker != null) locationMarker.remove();
                locationMarker = map.addMarker(locaionMarkerOptions);
                if (mapFollow) changeMapPosition();
            }

            (new RedrawElevationGraphTask()).execute((Void) null);

            txtAccuracy.setText(new DecimalFormat("#.#").format(loc.getAccuracy()));
            txtSpeed.setText(new DecimalFormat("#.#").format(loc.getSpeed()));
            txtAlt.setText(new DecimalFormat("#").format(loc.getAltitude()));
            txtBearing.setText(new DecimalFormat("#").format(loc.getBearing()));
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

            (new RedrawElevationGraphTask()).execute((Void) null);

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
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);
        map.setOnMarkerClickListener(this);

        boolean success;
        if (activity.nightMode)
            success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_dark));
        else
            success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_dark));

        if (!success) Log.w("Map style", "Failed to load from RAW file");

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

        if (mapOverlayManager != null) loadOverlays();

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
            listFlightPlan.setAdapter(new PlanEditorAdapter(getContext(), R.layout.list_item_edit, flightPlanManager.editedPlan));
            listFlightPlan.setSelection(flightPlanManager.editedPlan.size()-1);
            reloadPlanMarkers();
        }
        else (new GetPOITask()).execute((LatLng) latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (editing){
            LatLng latLng = marker.getPosition();
            String name = "";
            for (Airport.Data d: mapOverlayManager.airports){
                if (latLng.latitude == d.location.latitude && latLng.longitude == d.location.longitude){
                    name = d.icao;
                    break;
                }
            }

            flightPlanManager.addNewPoint(latLng, name);
            listFlightPlan.setAdapter(new PlanEditorAdapter(getContext(), R.layout.list_item_edit, flightPlanManager.editedPlan));
            listFlightPlan.setSelection(flightPlanManager.editedPlan.size()-1);

            reloadPlanMarkers();
        }
        else (new GetPOITask()).execute(marker.getPosition());

        return false;
    }

    /**
     * Load map overlays from mapOverlayManager
     */
    private void loadOverlays(){
        if (mapOverlayManager.airspaces != null) {
            for (Airspace.Data d : mapOverlayManager.airspaces) {
                PolygonOptions options = new PolygonOptions()
                        .addAll(d.polygon)
                        .strokeWidth(4)
                        .strokeColor(mapOverlayManager.airspaceStrokeColor(d.category))
                        .fillColor(mapOverlayManager.airspaceFillColor(d.category));

                map.addPolygon(options);
            }
        }
        if (mapOverlayManager.airports != null){
            for (Airport.Data d: mapOverlayManager.airports){
                MarkerOptions options = new MarkerOptions()
                        .position(d.location)
                        .icon(BitmapDescriptorFactory.fromBitmap(d.icon));

                map.addMarker(options).setAnchor(0.5f, 0.5f);
            }
        }
    }

    /**
     * Reloads/creates plan markers for them map.
     */
    private void reloadPlanMarkers(){

        for (Marker m: planMarkers){
            m.remove();
        }
        planMarkers.clear();
        if (planLine != null)
            planLine.remove();

        if (flightPlanManager != null) {
            PolylineOptions po = new PolylineOptions().geodesic(true).clickable(false).width(4f).color(Color.MAGENTA);
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
            case R.id.home_panel_bottom_btn:
                changeLayoutPanels(!isElevationGraphVisible);
                break;
            case R.id.home_button_plan_top:
                if (editing) saveEditedRoute();
                else openFlightPlans();
                break;
            case R.id.home_button_plan_bottom:
                cancelRoute();
                break;
            case R.id.home_map_center:
                mapFollow = true;
                changeMapPosition();
                break;
            case R.id.home_map_rotate:
                mapNorthUp = !mapNorthUp;
                changeMapPosition();
                break;
        }
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

    /**
     * Show/Hide elevaton graph on the bottom
     * @param isGraph
     */
    private void changeLayoutPanels(boolean isGraph) {

        if (planWidth == 0) {
            btnFlightPlanTop.setVisibility(View.VISIBLE);
            planWidth = btnFlightPlanTop.getWidth();
        }

        int h = getView().getHeight();

        isElevationGraphVisible = isGraph;

        int panelSize = 0;

        btnFlightPlanTop.getLayoutParams().width = planWidth;
        btnFlightPlanBottom.getLayoutParams().width = planWidth;
        listFlightPlan.getLayoutParams().width = planWidth;

        if (!editing){
            if (isElevationGraphVisible) {
                panelChart.setVisibility(View.VISIBLE);
                btnPanelBottom.setImageResource(R.drawable.ic_expand_down_inv);
                panelSize = (int) (h * 0.3);
            } else {
                panelSize = panelInfo.getHeight();
                panelChart.setVisibility(View.GONE);
                btnPanelBottom.setImageResource(R.drawable.ic_expand_up_inv);
                btnPanelBottom.setEnabled(!editing);
            }

            if (flightPlanManager == null){
                listFlightPlan.setVisibility(View.GONE);
                btnFlightPlanBottom.setVisibility(View.GONE);
                btnFlightPlanTop.setText(R.string.home_fl_plan);
                btnFlightPlanTop.setEnabled(true);
            }
            else {
                listFlightPlan.setVisibility(View.VISIBLE);
                btnFlightPlanBottom.setVisibility(View.VISIBLE);
                btnFlightPlanBottom.setText(R.string.btn_stop);
                btnFlightPlanTop.setEnabled(false);
                btnFlightPlanTop.setText(new DecimalFormat("#.#").format(flightPlanManager.getRoutLength()) + " " + getString(R.string.calc_unit_km));
            }

            panelBottom.setVisibility(View.VISIBLE);
            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, panelSize));

        }
        else{
            listFlightPlan.setVisibility(View.VISIBLE);
            btnFlightPlanBottom.setVisibility(View.VISIBLE);
            panelBottom.setVisibility(View.GONE);
            btnFlightPlanTop.setText(R.string.btn_save);
            btnFlightPlanTop.setEnabled(true);
            btnFlightPlanBottom.setText(R.string.btn_cancel);
        }

        panelMap.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h - panelSize));

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
        dialogPlans.loadContent(plansFolder, plansFolder, true, R.string.manage_plans, R.string.flight_plan_add);
        dialogPlans.setOnCancelListener(this);
        dialogPlans.setOnDismissListener(this);
        dialogPlans.show();
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
            reloadPlanMarkers();
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
            reloadPlanMarkers();
            changeLayoutPanels(isElevationGraphVisible);
        }
    }

    /**
     * Loads flight plan into the list
     */
    private void loadFlightPlan(){
        if (flightPlanManager != null)
            if (editing){
                flightPlanManager.editedPlan = new ArrayList<>(flightPlanManager.plan);
                listFlightPlan.setAdapter(new PlanEditorAdapter(getContext(), R.layout.list_item_edit, flightPlanManager.editedPlan));
            }
            else{
                listFlightPlan.setAdapter(new PlanAdapter(getContext(), R.layout.list_text_item, flightPlanManager.plan));
            }
    }

    /**
     * On planning cancel button press
     */
    private void cancelRoute(){
        if (editing){
            editing = false;
            changeLayoutPanels(isElevationGraphVisible);
            loadFlightPlan();
        }
        else{
            flightPlanManager = null;
            changeLayoutPanels(isElevationGraphVisible);
        }

        reloadPlanMarkers();

    }

    /**
     * Saves the edidted route from the list
     */
    private void saveEditedRoute(){
        // TODO: save from text editors into editPlan so the user can edit the names

       if (flightPlanManager.saveEditedPlan()){
           flightPlanManager.editedPlan = null;
           editing = false;
           changeLayoutPanels(isElevationGraphVisible);
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
        int pos = listFlightPlan.getFirstVisiblePosition();
        listFlightPlan.setAdapter(new PlanEditorAdapter(getContext(), R.layout.list_item_edit, flightPlanManager.editedPlan));
        listFlightPlan.scrollListBy(pos);
        reloadPlanMarkers();
    }

    /**
     * Handle Flight plan list item click listener
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!editing && flightPlanManager != null){
            (new GetPOITask()).execute((LatLng) flightPlanManager.plan.get(position).location);
        }
    }

    // ---------------------------------------------------------------
    // List Adapters
    // ---------------------------------------------------------------

    /**
     * List afrapter for flight plan
     */
    private class PlanAdapter extends ArrayAdapter<FlightPlanManager.Point> {

        private ArrayList<FlightPlanManager.Point> data;
        private Context context;
        private int layoutId;

        public PlanAdapter(Context ctx, int resource, ArrayList<FlightPlanManager.Point> objects) {
            super(ctx, resource, objects);
            data = objects;
            context = ctx;
            layoutId = resource;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            TextView line = null;

            if(row == null){
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutId, parent, false);

                line = (TextView) row.findViewById(R.id.list_text);
                row.setTag(line);
            }
            else {
                line = (TextView) row.getTag();
            }

            line.setText(data.get(position).name);

            return row;
        }

    }

    /**
     * List adrapter for flight plan editing
     */
    private class PlanEditorAdapter extends ArrayAdapter<FlightPlanManager.Point> {

        private ArrayList<FlightPlanManager.Point> data;
        private Context context;
        private int layoutId;

        public PlanEditorAdapter(Context ctx, int resource, ArrayList<FlightPlanManager.Point> objects) {
            super(ctx, resource, objects);
            data = objects;
            context = ctx;
            layoutId = resource;
        }

        @Override
        public View getView(final int position, View row, ViewGroup parent) {

            EditText line = null;
            ImageButton button = null;

            if(row == null){
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutId, parent, false);

                line = (EditText) row.findViewById(R.id.list_edit_text);
                row.setTag(line);
            }
            else {
                line = (EditText) row.getTag();
            }

            line.setText(data.get(position).name);
            if (!data.get(position).editeble) line.setEnabled(false);

            button = (ImageButton) row.findViewById(R.id.list_edit_delete);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePlanPoint(position);
                }
            });

            line.setText(data.get(position).name);

            return row;
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

        @Override
        protected Void doInBackground(LatLng... params) {

            LatLng coord = params[0];
            airspaces = mapOverlayManager.getAirspacesAt(coord);
            airports = mapOverlayManager.getAirportsCloseBy(coord);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            OverlayDetailDialog d = new OverlayDetailDialog(getContext());
            d.setContentView(R.layout.dialog_overlay_detail);
            d.loadContent(lastPosition, airspaces, airports);
            d.show();
        }
    }

    /**
     * Redraw elevation graph
     */
    public class RedrawElevationGraphTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            // TODO: Update graph in RedrawLocationData

            return null;
        }
    }

}
