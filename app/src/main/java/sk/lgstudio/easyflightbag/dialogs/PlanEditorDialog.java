package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.FlightPlanManager;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;
import sk.lgstudio.easyflightbag.openAIP.Airport;
import sk.lgstudio.easyflightbag.openAIP.Airspace;

/**
 * Dialog with map to edit flight plan
 */

public class PlanEditorDialog extends Dialog implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    protected MapView mapLayout = null;
    protected GoogleMap map = null;

    private LayoutInflater inflater;
    private boolean nightMode = false;
    private float fuelRange;
    private LatLng lastPosition = null;
    private FlightPlanManager flightPlanManager;
    private MapOverlayManager mapOverlayManager;
    private Bundle savedInstanceState;

    private HorizontalScrollView planScrollView;
    private LinearLayout planList;
    private ImageButton btnBack;
    private ImageButton btnSave;
    private TextView txthint;

    private ArrayList<Marker> planMarkers = new ArrayList<>();
    private Polyline planLine;
    private BitmapDescriptor mapPointIcon;

    public PlanEditorDialog(Context context, int themeResId) {
        super(context, themeResId);
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_prefs), Context.MODE_PRIVATE);
        nightMode = prefs.getBoolean(context.getString(R.string.pref_theme), false);
        inflater = getLayoutInflater();

        VectorDrawable vectorDrawable = (VectorDrawable) context.getDrawable(R.drawable.map_marker);
        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        mapPointIcon = BitmapDescriptorFactory.fromBitmap(bm);
    }

    public void loadContent(LatLng mypos, float range, FlightPlanManager flPlanMan, MapOverlayManager overlay, Bundle b){
        lastPosition = mypos;
        flightPlanManager = flPlanMan;
        fuelRange = range;
        mapOverlayManager = overlay;
        savedInstanceState = b;
    }

    @Override
    public void show(){
        super.show();

        mapLayout = (MapView) findViewById(R.id.plan_editor_map);
        mapLayout.onCreate(savedInstanceState);
        mapLayout.getMapAsync(this);
        mapLayout.onResume();

        planScrollView = (HorizontalScrollView) findViewById(R.id.plan_editor_scroll);
        planList = (LinearLayout) findViewById(R.id.plan_editor_list);
        btnBack = (ImageButton) findViewById(R.id.plan_editor_bck);
        btnSave = (ImageButton) findViewById(R.id.plan_editor_save);
        btnSave.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        txthint = (TextView) findViewById(R.id.plan_editor_hint);

        TextView title = (TextView) findViewById(R.id.plan_editor_name);
        title.setText(flightPlanManager.getPlanName());

        loadFlightPlan();
    }

    @Override
    public void dismiss(){
        mapLayout.onDestroy();
        super.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("MAP", "ready");
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setBuildingsEnabled(false);
        map.setTrafficEnabled(false);
        map.setOnMarkerClickListener(this);

        if (nightMode)
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_dark));
        else
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style_light));


        if(lastPosition != null){
            CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(lastPosition).zoom(12f).build());
            map.moveCamera(myLoc);
        }

        UiSettings settings = map.getUiSettings();
        settings.setCompassEnabled(false);
        settings.setMyLocationButtonEnabled(false);
        settings.setRotateGesturesEnabled(false);
        settings.setZoomControlsEnabled(false);
        settings.setZoomGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);


        if (mapOverlayManager != null){
            loadOverlays();
        }

        loadPlanMarkers();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        flightPlanManager.addNewPoint(latLng);
        refillPlanListEdit();
        loadPlanMarkers();
        planScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        LatLng latLng = marker.getPosition();
        String name = mapOverlayManager.getAirportICAO(latLng);

        if (name.length() == 0) flightPlanManager.addNewPoint(latLng);
        else flightPlanManager.addNewPoint(latLng, name);

        refillPlanListEdit();
        loadPlanMarkers();
        planScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);

        return false;
    }

    /**
     * Fills the plan list with editor items
     */
    private void refillPlanListEdit(){
        planList.removeAllViews();

        int i = 0;
        for (FlightPlanManager.Point p: flightPlanManager.editedPlan){

            if (i > 0)
                addPlanArrow();

            LinearLayout elem = (LinearLayout) inflater.inflate(R.layout.item_edit_h, null);

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

        if (i == 0) txthint.setVisibility(View.VISIBLE);
        else txthint.setVisibility(View.GONE);

    }

    /**
     * Saves the edidted route from the list
     */
    private void saveEditedRoute(){
        if (flightPlanManager.saveEditedPlan()){
            Toast.makeText(getContext(), getContext().getString(R.string.plan_warning_saved), Toast.LENGTH_SHORT).show();
            dismiss();
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
        refillPlanListEdit();
        loadPlanMarkers();
    }

    /**
     * Loads flight plan into the list
     */
    private void loadFlightPlan(){
        if (flightPlanManager != null)
            if (flightPlanManager.editedPlan == null)
                flightPlanManager.editedPlan = new ArrayList<>(flightPlanManager.plan);
            refillPlanListEdit();
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
     * Reloads/creates plan markers for them map.
     */
    private void loadPlanMarkers() {

        for (Marker m : planMarkers) {
            m.remove();
        }

        planMarkers.clear();
        if (planLine != null)
            planLine.remove();

        if (flightPlanManager != null) {
            PolylineOptions po = new PolylineOptions().geodesic(true).clickable(false).width(7f).color(Color.MAGENTA);

            for (FlightPlanManager.Point p : flightPlanManager.editedPlan) {
                MarkerOptions o = new MarkerOptions().position(p.location).draggable(p.editeble).icon(mapPointIcon).anchor(0.5f, 0.5f);
                Marker m = map.addMarker(o);
                planMarkers.add(m);
                po.add(p.location);
            }

            planLine = map.addPolyline(po);
        }
    }

    /**
     * Load map overlays from mapOverlayManager
     */
    public void loadOverlays(){

        if (mapOverlayManager.isLoading)
            return;

        for (Airspace.Data d : mapOverlayManager.getAirspaces()) {
            PolygonOptions options = new PolygonOptions()
                    .addAll(d.polygon)
                    .strokeWidth(4)
                    .strokeColor(MapOverlayManager.airspaceStrokeColor(d.category))
                    .fillColor(MapOverlayManager.airspaceFillColor(d.category));

            map.addPolygon(options);
        }

        for (Airport.Data d: mapOverlayManager.getAirports()){
            MarkerOptions options = new MarkerOptions()
                    .position(d.location)
                    .icon(BitmapDescriptorFactory.fromBitmap(d.icon))
                    .flat(true);

            map.addMarker(options).setAnchor(0.5f, 0.5f);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.plan_editor_bck:
                dismiss();
                break;
            case R.id.plan_editor_save:
                saveEditedRoute();
                break;
        }
    }
}
