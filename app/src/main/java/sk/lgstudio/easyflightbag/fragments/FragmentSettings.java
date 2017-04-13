package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AIPManager;
import sk.lgstudio.easyflightbag.managers.FlightPlanManager;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;

/**
 * Settings Fragment -
 * - Switch Night/Day mode
 * - Switch GPS mode (GPS/BT)
 * - Map overlay data refresh
 * - AIP downloaders
 */
public class FragmentSettings extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final static int apt_chk_box_id[] = {
            R.id.set_chkbx_int,
            R.id.set_chkbx_civ,
            R.id.set_chkbx_glide,
            R.id.set_chkbx_heli,
            R.id.set_chkbx_mil,
            R.id.set_chkbx_oth,
    };

    private final static int country_string_id[] = {
            R.string.country_all,
            R.string.country_at,
            R.string.country_cz,
            R.string.country_hu,
            R.string.country_pl,
            R.string.country_sk
    };

    private final static int aip_txt_id[] = {
            R.id.set_aip_text_at,
            R.id.set_aip_text_cz,
            R.id.set_aip_text_hu,
            R.id.set_aip_text_pl,
            R.id.set_aip_text_sk
    };

    private final static int aip_btn_id[] = {
            R.id.set_aip_refresh_at,
            R.id.set_aip_refresh_cz,
            R.id.set_aip_refresh_hu,
            R.id.set_aip_refresh_pl,
            R.id.set_aip_refresh_sk
    };

    private ArrayList<TextView> aip_txt;
    private TextView airspace_txt;
    private Spinner spnCountry;

    public MainActivity activity;
    public AIPManager aipManager;
    public MapOverlayManager mapOverlayManager;

    /**
     * Creates the view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch nightSwith = (Switch) view.findViewById(R.id.set_night_switch);
        nightSwith.setChecked(activity.nightMode);
        nightSwith.setOnCheckedChangeListener(this);

        Switch gpsViaBt = (Switch) view.findViewById(R.id.set_gps_mode_switch);
        gpsViaBt.setChecked(activity.gpsViaBt);
        gpsViaBt.setOnCheckedChangeListener(this);

        ImageButton airspaceBtn = (ImageButton) view.findViewById(R.id.set_refresh_airspace);
        airspaceBtn.setOnClickListener(this);
        airspace_txt = (TextView) view.findViewById(R.id.set_text_airspace);

        spnCountry = (Spinner) view.findViewById(R.id.set_spinner_country);
        ArrayList<String> countries = new ArrayList<>();
        for (int i: country_string_id){
            countries.add(getString(i));
        }
        spnCountry.setAdapter(new CountryAdapter(getContext(), countries));

        aip_txt = new ArrayList<>();

        for( int i : aip_txt_id)
            aip_txt.add((TextView) view.findViewById(i));

        for( int i : aip_btn_id){
            ImageButton btn= (ImageButton) view.findViewById(i);
            btn.setOnClickListener(this);
        }

        short i = 0;
        for (int id : apt_chk_box_id){
            CheckBox bx = (CheckBox) view.findViewById(id);
            bx.setOnCheckedChangeListener(this);
            bx.setChecked(mapOverlayManager.getAptTypeSelected(i));
            i++;
        }

        return view;
    }

    /**
     * Reload the AIP and map overlay status
     */
    @Override
    public void onResume(){
        for (int i = 0; i < aip_txt.size(); i++){
            aip_txt.get(i).setText(aipManager.getStatus(i));
        }
        airspace_txt.setText(mapOverlayManager.getStatus());

        spnCountry.setOnItemSelectedListener(null);
        spnCountry.setSelection(mapOverlayManager.country);
        spnCountry.setOnItemSelectedListener(this);
        super.onResume();
    }

    /**
     * On Switch chacked change
     * @param compoundButton
     * @param b
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        boolean end = true;
        switch (compoundButton.getId()){
            case R.id.set_night_switch:
                nightSwitchTrigeger(b);
                break;
            case R.id.set_gps_mode_switch:
                changeGpsSource(b);
                break;
            default:
                end = false;
        }
        if (end) return;

        short i = 0;
        for (int id : apt_chk_box_id){
            if (id == compoundButton.getId()){
                mapOverlayManager.setAirportType(i, b);
            }
            i++;
        }
    }

    /**
     * Switch between real GPS and GPS simulation via BT
     * @param b
     */
    private void changeGpsSource(boolean b){
        if (activity != null){
            activity.switchLocationService(b);
        }
    }

    /**
     * Change day/nigh mode colors
     * @param b
     */
    private void nightSwitchTrigeger(boolean b){
        if (activity != null){
            activity.prefs.edit().putBoolean(getString(R.string.pref_theme),b).apply();
            activity.changeToNight();

            final android.support.v4.app.FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.detach(this);
            ft.attach(this);
            ft.commit();
        }
    }

    /**
     * Button Click listener
     * @param view
     */
    @Override
    public void onClick(View view) {
        boolean found = false;
        int vId = view.getId();

        for (int i = 0; i < aip_btn_id.length; i++){
            if (aip_btn_id[i] == vId){
                aipManager.getUpdate(i);
                found = true;
            }
        }
        if (!found && vId == R.id.set_refresh_airspace){
            mapOverlayManager.getUpdate();
        }

    }

    /**
     * reloads map overlay data text
     */
    public void reloadMapOverlayData(){
        airspace_txt.setText(mapOverlayManager.getStatus());
    }

    /**
     * reloads AIP data text
     */
    public void reloadAipData(int c){
        aip_txt.get(c).setText(aipManager.getStatus(c));
    }

    /**
     * Spinner item click listener
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mapOverlayManager.changeCountry(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    // ---------------------------------------------------------------
    // List Adapters
    // ---------------------------------------------------------------

    /**
     * List afrapter for flight plan
     */

    private class CountryAdapter extends BaseAdapter {

        Context ctx;
        ArrayList<String> data;

        public CountryAdapter(Context context, ArrayList<String> d) {
            this.ctx = context;
            data = d;

        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View spinnerElement = inflater.inflate(R.layout.list_text_item, null);

            TextView airplane = (TextView) spinnerElement.findViewById(R.id.list_text);
            airplane.setText(data.get(position));

            return spinnerElement;
        }

    }


}