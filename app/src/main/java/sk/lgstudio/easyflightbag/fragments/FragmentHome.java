package sk.lgstudio.easyflightbag.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


import java.util.List;

import lecho.lib.hellocharts.view.LineChartView;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;
import sk.lgstudio.easyflightbag.managers.AirspaceManager;
import sk.lgstudio.easyflightbag.openAIP.Airspace;

/**
 *
 */
public class FragmentHome extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private RelativeLayout panelBottom;
    private RelativeLayout panelMap;
    private LinearLayout fullLayout;
    private ImageButton btnPanelBottom;
    private Button btnFlightPlan;
    private LineChartView elevationChart;
    private LinearLayout panelInfo;

    protected MapView mapLayout = null;
    protected GoogleMap map = null;

    private boolean isElevationGraphVisible = true;
    private boolean mapReady = false;
    private boolean mapFollow = false;
    private float mapZoomLevel = 14f;
    private LatLng lastPosition = null;
    private LatLng mapTargetArea = null;
    private BitmapDescriptor mapLocationBmp = null;

    public AirspaceManager airspaceManager = null;

    /**
     * Reload view settings after fragment reopened
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //isElevationGraphVisible = savedInstanceState.getBoolean("PBottom");
            //changePanelState(isElevationGraphVisible);
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
        btnFlightPlan = (Button) view.findViewById(R.id.home_fl_plan_btn);

        btnPanelBottom.setOnClickListener(this);
        btnFlightPlan.setOnClickListener(this);

        fullLayout = (LinearLayout) view.findViewById(R.id.home_screen);
        panelBottom = (RelativeLayout) view.findViewById(R.id.home_panel_bottom);
        panelInfo = (LinearLayout) view.findViewById(R.id.home_gps_info_panel);
        panelMap = (RelativeLayout) view.findViewById(R.id.home_map_layout);

        elevationChart = (LineChartView) view.findViewById(R.id.home_elevation_graph);

        mapLayout = (MapView) view.findViewById(R.id.home_map_view);
        mapLayout.onCreate(savedInstanceState);

        mapLayout.getMapAsync(this);

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
     * Low mamory google map action
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapLayout != null) mapLayout.onLowMemory();
    }

    /**
     * Button click listener
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_panel_bottom_btn:
                changePanelState(!isElevationGraphVisible);
                break;
            case R.id.home_fl_plan_btn:
                openFlightPlans();
                break;

        }
    }

    /**
     * Create flight plan chooser dialog
     */
    private void openFlightPlans(){
        // todo: create flight plan selector
    }

    /**
     * Show/Hide elevaton graph on the bottom
     * @param isGraph
     */
    private void changePanelState(boolean isGraph) {

            isElevationGraphVisible = isGraph;

            int panelSize = 0;

            if (isElevationGraphVisible) {
                panelSize = (int) (fullLayout.getHeight() * 0.3);
                elevationChart.setVisibility(View.VISIBLE);
                btnPanelBottom.setImageResource(R.drawable.ic_expand_down_inv);
            } else {
                panelSize = panelInfo.getHeight();
                elevationChart.setVisibility(View.GONE);
                btnPanelBottom.setImageResource(R.drawable.ic_expand_up_inv);
            }

            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, panelSize));
            panelMap.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fullLayout.getHeight() - panelSize));
    }

    /**
     * Called from activity to handle new location
     * @param loc
     */
    public void addNewLocation(Location loc) {

        lastPosition = new LatLng(loc.getLatitude(), loc.getLongitude());

        if (mapReady) {
            if (mapLocationBmp == null)
                mapLocationBmp = BitmapDescriptorFactory.fromResource(R.drawable.ic_flight);

            //map.addMarker(new MarkerOptions().position(lastPosition).draggable(false).icon(mapLocationBmp).rotation(loc.getBearing()));

            if (mapFollow) changeMapPosition(lastPosition);
            else changeMapPosition(mapTargetArea);

        }


        RedrawElevationGraphTask task = new RedrawElevationGraphTask();
        task.execute((Void) null);


        //String str = String.valueOf(loc.getLongitude()) + "/" + String.valueOf(loc.getLatitude()) + " @ " + String.valueOf(loc.getAltitude());
        //str = str + " | A:" + String.valueOf(loc.getAccuracy() + " | S:" + String.valueOf(loc.getSpeed()));
        //str = str + " | B:" + String.valueOf(loc.getBearing());
        //Log.d("Location", str);

    }

    /**
     * Jump to position on the map
     * @param pos
     */
    private void changeMapPosition(LatLng pos){
        CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(pos).zoom(mapZoomLevel).build());
        map.moveCamera(myLoc);
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMyLocationEnabled(true); // TODO: change to airplane icon
        map.setOnCameraMoveListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMyLocationButtonClickListener(this);
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);

        mapFollow = true;

        if(lastPosition != null) changeMapPosition(lastPosition);

        UiSettings settings = map.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setRotateGesturesEnabled(false);
        settings.setZoomControlsEnabled(false);
        settings.setZoomGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);

        mapReady = true;

        if (airspaceManager != null) loadOverlays();

        // TODO: Night mode
        //if (isNightMode) googleMap.setMapStyle(new MapStyleOptions(getString(R.string.map_style_night)));
        //else googleMap.setMapStyle(new MapStyleOptions(getString(R.string.map_style_day)));

    }

    @Override
    public void onCameraMove() {
        mapZoomLevel = map.getCameraPosition().zoom;
        mapTargetArea = map.getCameraPosition().target;
        mapFollow = false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mapFollow = true;
        changeMapPosition(lastPosition);
        return true;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    /**
     * Load map overlays from airspaceManager
     */
    private void loadOverlays(){
        if (airspaceManager.airspaces != null){
            for (Airspace.Data d: airspaceManager.airspaces){
                Polygon p = map.addPolygon(new PolygonOptions()
                    .addAll(d.polygon)
                    .strokeColor(Color.CYAN)
                    .strokeWidth(4));
            }
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
