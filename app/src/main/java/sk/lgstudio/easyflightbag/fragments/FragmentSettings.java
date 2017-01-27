package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPDownloader;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPcz;

/**
 *
 */
public class FragmentSettings extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private final static int aip_txt_id[] = {
            R.id.set_aip_text_at,
            R.id.set_aip_text_cz,
            R.id.set_aip_text_hu,
            R.id.set_aip_text_sk
    };

    private final static int aip_btn_id[] = {
            R.id.set_aip_refresh_at,
            R.id.set_aip_refresh_cz,
            R.id.set_aip_refresh_hu,
            R.id.set_aip_refresh_sk
    };

    private ArrayList<TextView> aip_txt;
    private ArrayList<ImageButton> aip_btn;

    private Switch nightSwith;

    public MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        nightSwith = (Switch) view.findViewById(R.id.set_night_switch);
        nightSwith.setChecked(activity.nightMode);
        nightSwith.setOnCheckedChangeListener(this);

        aip_txt = new ArrayList<>();
        aip_btn = new ArrayList<>();

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
        int vId = view.getId();

        for (int i = 0; i < aip_btn_id.length; i++){
            if (aip_btn_id[i] == vId){
                activity.aipManager.getUpdate(i);
            }
        }
    }

    public void reloadAipData(int c){
        aip_txt.get(c).setText(activity.aipManager.getStatus(c));
    }



}