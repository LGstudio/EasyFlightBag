package sk.lgstudio.easyflightbag.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentSettings extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

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

    public MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Switch nightSwith = (Switch) view.findViewById(R.id.set_night_switch);
        nightSwith.setChecked(activity.nightMode);
        nightSwith.setOnCheckedChangeListener(this);

        ImageButton airspaceBtn = (ImageButton) view.findViewById(R.id.set_refresh_airspace);
        airspaceBtn.setOnClickListener(this);
        airspace_txt = (TextView) view.findViewById(R.id.set_text_airspace);

        aip_txt = new ArrayList<>();
        ArrayList<ImageButton> aip_btn = new ArrayList<>();

        for( int i : aip_txt_id)
            aip_txt.add((TextView) view.findViewById(i));

        for( int i : aip_btn_id){
            ImageButton btn= (ImageButton) view.findViewById(i);
            btn.setOnClickListener(this);
            aip_btn.add(btn);
        }

        return view;
    }

    @Override
    public void onResume(){
        for (int i = 0; i < aip_txt.size(); i++){
            aip_txt.get(i).setText(activity.aipManager.getStatus(i));
        }
        airspace_txt.setText(activity.airspaceManager.getStatus());
        super.onResume();
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (activity != null){
            activity.prefs.edit().putBoolean(getString(R.string.pref_theme),b).apply();
            activity.changeToNight();

            final android.support.v4.app.FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.detach(this);
            ft.attach(this);
            ft.commit();
        }
    }

    @Override
    public void onClick(View view) {
        boolean found = false;
        int vId = view.getId();

        for (int i = 0; i < aip_btn_id.length; i++){
            if (aip_btn_id[i] == vId){
                activity.aipManager.getUpdate(i);
                found = true;
            }
        }
        if (!found && vId == R.id.set_refresh_airspace){
            activity.airspaceManager.getUpdate();
        }

    }

    public void reloadAirspaceData(){
        airspace_txt.setText(activity.airspaceManager.getStatus());
    }

    public void reloadAipData(int c){
        aip_txt.get(c).setText(activity.aipManager.getStatus(c));
    }



}