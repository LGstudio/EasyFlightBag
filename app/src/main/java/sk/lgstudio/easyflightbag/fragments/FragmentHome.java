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

import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentHome extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private RelativeLayout panelTop;
    private RelativeLayout panelBottom;
    private LinearLayout fullLayout;
    private ImageButton panelTopBtn;
    private ImageButton panelBottomBtn;
    private Button startStop;
    private TextView textViewTest;

    protected MapView mapLayout;
    protected GoogleMap map;

    private boolean isPanelTop = true;
    private boolean isPanelBottom = true;
    private int isTracking = 0; // 0 - cleared | 1 - tracking | 2 - stopped
    private boolean mapReady = false;
    private boolean mapFollow = false;
    private float mapZoomLevel = 14f;
    private LatLng lastPosition = null;
    private LatLng mapTargetArea = null;
    private BitmapDescriptor mapLocationBmp = null;

    public ArrayList<Location> track;
    //public ArrayList<Location> route;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            isPanelTop = savedInstanceState.getBoolean("PTop");
            isPanelBottom = savedInstanceState.getBoolean("PBottom");
            if (mapLayout != null) mapLayout.onSaveInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("PTop", isPanelTop);
        outState.putBoolean("PBottom", isPanelBottom);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textViewTest = (TextView) view.findViewById(R.id.home_data_test);

        panelTopBtn = (ImageButton) view.findViewById(R.id.home_panel_top_btn);
        panelBottomBtn = (ImageButton) view.findViewById(R.id.home_panel_bottom_btn);
        startStop = (Button) view.findViewById(R.id.home_button_start_stop);
        setTrackingButton();

        panelTopBtn.setOnClickListener(this);
        panelBottomBtn.setOnClickListener(this);
        startStop.setOnClickListener(this);

        fullLayout = (LinearLayout) view.findViewById(R.id.home_screen);
        panelTop = (RelativeLayout) view.findViewById(R.id.home_panel_top);
        panelBottom = (RelativeLayout) view.findViewById(R.id.home_panel_bottom);

        mapLayout = (MapView) view.findViewById(R.id.home_map_view);
        mapLayout.onCreate(savedInstanceState);

        mapLayout.getMapAsync(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapLayout != null) mapLayout.onResume();
        //changePanelState(isPanelTop,isPanelBottom);
    }

    @Override
    public void onPause() {
        if (mapLayout != null) mapLayout.onPause();
        mapReady = false;
        super.onPause();
    }

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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapLayout != null) mapLayout.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_panel_top_btn:
                changePanelState(!isPanelTop, isPanelBottom);
                break;
            case R.id.home_panel_bottom_btn:
                changePanelState(isPanelTop, !isPanelBottom);
                break;
            case R.id.home_button_start_stop:
                isTracking = (isTracking + 1) % 3;
                setTrackingButton();
                break;
        }
    }

    private void setTrackingButton(){
        if (isTracking == 1) startStop.setText(getString(R.string.btn_stop));
        else if (isTracking == 2) startStop.setText(getString(R.string.btn_clear));
        else {
            track.clear();
            startStop.setText(getString(R.string.btn_start));
        }
    }

    private void changePanelState(boolean top, boolean bottom) {
        isPanelTop = top;
        isPanelBottom = bottom;

        int panels = 0;
        int open = fullLayout.getHeight() / 4;
        int close = panelTopBtn.getHeight();

        if (isPanelTop) {
            panels += open;
            panelTop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, open));
            panelTopBtn.setImageResource(R.drawable.ic_expand_up_inv);
        } else {
            panels += close;
            panelTop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, close));
            panelTopBtn.setImageResource(R.drawable.ic_expand_down_inv);
        }

        if (isPanelBottom) {
            panels += open;
            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, open));
            panelBottomBtn.setImageResource(R.drawable.ic_expand_down_inv);
        } else {
            panels += close;
            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, close));
            panelBottomBtn.setImageResource(R.drawable.ic_expand_up_inv);
        }

        int mapH = fullLayout.getHeight() - panels;
        mapLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mapH));

    }

    public void addNewLocation(Location loc) {

        lastPosition = new LatLng(loc.getLatitude(), loc.getLongitude());

        if (mapReady) {
            if (mapLocationBmp == null)
                mapLocationBmp = BitmapDescriptorFactory.fromResource(R.drawable.ic_flight);

            //map.addMarker(new MarkerOptions().position(lastPosition).draggable(false).icon(mapLocationBmp).rotation(loc.getBearing()));

            if (mapFollow) changeMapPosition(lastPosition);
            else changeMapPosition(mapTargetArea);

        }
        if (isTracking == 1) {
            track.add(loc);

            if (isPanelTop) {
                RedrawElevationGraphTask task = new RedrawElevationGraphTask();
                task.execute((Void) null);
            }

            Location l = track.get(track.size() - 1);
            String num = String.valueOf(track.size()) + ": ";
            String posit = String.valueOf(l.getLongitude()) + "/" + String.valueOf(l.getLatitude()) + " @ " + String.valueOf(l.getAltitude());
            String acc = " | A:" + String.valueOf(l.getAccuracy() + " | S:" + String.valueOf(l.getSpeed()));
            String head = " | B:" + String.valueOf(l.getBearing());

            textViewTest.setText(num + posit + acc + head);

        }
        else if (isTracking == 2){
            textViewTest.setText("Tracking stopped");
        }
        else{
            textViewTest.setText("No tracked data");
        }
    }

    private void changeMapPosition(LatLng pos){
        CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(pos).zoom(mapZoomLevel).build());
        map.moveCamera(myLoc);
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        map.setMyLocationEnabled(true);
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

    public class RedrawElevationGraphTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            // TODO: Update graph in RedrawLocationData

            return null;
        }
    }

}
