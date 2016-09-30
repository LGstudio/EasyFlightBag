package sk.lgstudio.easyflightbag.ui;

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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentHome extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private RelativeLayout panelTop;
    private RelativeLayout panelBottom;
    private LinearLayout fullLayout;
    private ImageButton panelTopBtn;
    private ImageButton panelBottomBtn;
    private Button startStop;
    private TextView textViewTest;

    protected MapView mapLayout;

    private boolean isPanelTop = true;
    private boolean isPanelBottom = true;
    private int isTracking = 0; // 0 - cleared | 1 - tracking | 2 - stopped

    public ArrayList<Location> track;
    public ArrayList<Location> route;

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

        panelTopBtn.setOnClickListener(this);
        panelBottomBtn.setOnClickListener(this);
        startStop.setOnClickListener(this);

        fullLayout = (LinearLayout) view.findViewById(R.id.home_screen);
        panelTop = (RelativeLayout) view.findViewById(R.id.home_panel_top);
        panelBottom = (RelativeLayout) view.findViewById(R.id.home_panel_bottom);

        mapLayout = (MapView) view.findViewById(R.id.home_map_view);
        mapLayout.onCreate(savedInstanceState);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapLayout != null) mapLayout.onResume();

    }

    @Override
    public void onPause() {
        if (mapLayout != null) mapLayout.onPause();

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
        switch (v.getId()){
            case R.id.home_panel_top_btn:
                changePanelState(!isPanelTop, isPanelBottom);
                break;
            case R.id.home_panel_bottom_btn:
                changePanelState(isPanelTop, !isPanelBottom);
                break;
            case  R.id.home_button_start_stop:
                isTracking = (isTracking + 1) % 3;
                if (isTracking == 1) startStop.setText(getString(R.string.btn_stop));
                else if (isTracking == 2) startStop.setText(getString(R.string.btn_clear));
                else {
                    track.clear();
                    startStop.setText(getString(R.string.btn_start));
                }
                break;
        }
    }

    private void changePanelState(boolean top, boolean bottom){
        isPanelTop = top;
        isPanelBottom = bottom;

        int panels = 0;
        int open = fullLayout.getHeight() / 4;
        int close = panelTopBtn.getHeight();

        if (isPanelTop){
            panels += open;
            panelTop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, open));
            panelTopBtn.setImageResource(R.drawable.ic_expand_up_inv);
        }
        else {
            panels += close;
            panelTop.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, close));
            panelTopBtn.setImageResource(R.drawable.ic_expand_down_inv);
        }

        if (isPanelBottom){
            panels += open;
            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, open));
            panelBottomBtn.setImageResource(R.drawable.ic_expand_down_inv);
        }
        else {
            panels += close;
            panelBottom.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, close));
            panelBottomBtn.setImageResource(R.drawable.ic_expand_up_inv);
        }

        int mapH = fullLayout.getHeight() - panels;
        mapLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mapH));

    }

    public void addNewLocation(Location loc){
        if (isTracking == 1){
            track.add(loc);

            if (isPanelTop){
                RedrawElevationGraphTask task = new RedrawElevationGraphTask();
                task.execute((Void) null);
            }

            Location l = track.get(track.size()-1);
            String num = String.valueOf(track.size()) + ": ";
            String pos = String.valueOf(l.getLongitude()) + "/" + String.valueOf(l.getLatitude()) + " @ " + String.valueOf(l.getAltitude());
            String acc = " | A:" + String.valueOf(l.getAccuracy() + " | S:" + String.valueOf(l.getSpeed()));
            String head = " | B:" + String.valueOf(l.getBearing());

            textViewTest.setText(num + pos + acc + head);

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public class RedrawElevationGraphTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            // TODO: Update graph in RedrawLocationData

            return null;
        }
    }

}
