package sk.lgstudio.easyflightbag.fragments;

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
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import lecho.lib.hellocharts.view.LineChartView;
import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentHome extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private RelativeLayout panelBottom;
    private LinearLayout fullLayout;
    private ImageButton btnPanelBottom;
    private Button btnFlightPlan;
    private LineChartView elevationChart;
    private LinearLayout infoPanel;

    protected MapView mapLayout;
    protected GoogleMap map;

    private boolean isElevationGraphVisible = true;
    private boolean mapReady = false;
    private boolean mapFollow = false;
    private float mapZoomLevel = 14f;
    private LatLng lastPosition = null;
    private LatLng mapTargetArea = null;
    private BitmapDescriptor mapLocationBmp = null;


    /**
     * Reload view settings after fragment reopened
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            isElevationGraphVisible = savedInstanceState.getBoolean("PBottom");
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
        infoPanel = (LinearLayout) view.findViewById(R.id.home_gps_info_panel);

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

        if (isElevationGraphVisible){
            panelSize = infoPanel.getHeight();
            elevationChart.setVisibility(View.VISIBLE);
            btnPanelBottom.setImageResource(R.drawable.ic_expand_down_inv);
        }
        else {
            panelSize = (int) (fullLayout.getHeight() * 0.3);
            elevationChart.setVisibility(View.GONE);
            btnPanelBottom.setImageResource(R.drawable.ic_expand_up_inv);
        }
        panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, panelSize));
        mapLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fullLayout.getHeight() - panelSize));

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


        String str = String.valueOf(loc.getLongitude()) + "/" + String.valueOf(loc.getLatitude()) + " @ " + String.valueOf(loc.getAltitude());
        str = str + " | A:" + String.valueOf(loc.getAccuracy() + " | S:" + String.valueOf(loc.getSpeed()));
        str = str + " | B:" + String.valueOf(loc.getBearing());
        Log.d("Location", str);

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
        // TODO: fix        map.setMyLocationEnabled(true);
        map.setOnCameraMoveListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMyLocationButtonClickListener(this);
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        mapFollow = true;

        if(lastPosition != null) changeMapPosition(lastPosition);

        UiSettings settings = map.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setRotateGesturesEnabled(false);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(false);
        settings.setMapToolbarEnabled(false);

        mapReady = true;

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
