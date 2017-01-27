package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPDownloader;
import sk.lgstudio.easyflightbag.services.AIPDownloader.AIPcz;

/**
 *
 */
public class FragmentSettings extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {



    private Switch nightSwith;
    private TextView aipText;
    private ImageButton aipRefresh;

    public MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        nightSwith = (Switch) view.findViewById(R.id.set_night_switch);
        nightSwith.setChecked(activity.nightMode);
        nightSwith.setOnCheckedChangeListener(this);

        aipText = (TextView) view.findViewById(R.id.set_aip_refresh_text);
        aipRefresh = (ImageButton) view.findViewById(R.id.set_aip_refresh);
        aipRefresh.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context activity){
        super.onAttach(activity);

        // TODO update AIP status

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
        switch (view.getId()){
            case R.id.set_aip_refresh:

                break;
        }
    }



}