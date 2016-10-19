package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.services.AIPDownloader;
import sk.lgstudio.easyflightbag.services.GPSTrackerService;

/**
 *
 */
public class FragmentSettings extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private Switch nightSwith;
    private TextView aipText;
    private ImageButton aipRefresh;

    private boolean aipDownloadReady = true;

    public MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        nightSwith = (Switch) view.findViewById(R.id.set_night_switch);
        nightSwith.setChecked(activity.nightMode);
        nightSwith.setOnCheckedChangeListener(this);

        aipText = (TextView) view.findViewById(R.id.set_aip_refresh_text);
        if (aipDownloadReady) reloadAipText();
        aipRefresh = (ImageButton) view.findViewById(R.id.set_aip_refresh);
        aipRefresh.setOnClickListener(this);

        return view;
    }

    private void reloadAipText(){
        String aipUpdated = getString(R.string.set_aip) + activity.aipLastUpdate;
        aipText.setText(aipUpdated);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (activity != null){
            activity.prefs.edit().putBoolean(getString(R.string.pref_theme),b).apply();
            activity.changeToNight();
            //activity.menu.initView(activity); <- This does not work

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
                if (aipDownloadReady){
                    activity.startService(new Intent(activity, AIPDownloader.class));
                    LocalBroadcastManager.getInstance(activity)
                            .registerReceiver(activity.aipDownloadedReceiver,
                                    new IntentFilter(this.getString(R.string.set_intent_aip_download)));
                    aipDownloadReady = false;
                }
                break;
        }
    }

    private void finishAIPDownloader(){
        reloadAipText();
        aipDownloadReady = true;
        activity.stopService(new Intent(activity, AIPDownloader.class));
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(activity.aipDownloadedReceiver);
    }

    public void aipUpdate(int code, int files) {
        switch (code) {
            case AIPDownloader.STATUS_STARTED:
                String aipUpdated = getString(R.string.set_aip) + " - " + String.valueOf(files);
                aipText.setText(aipUpdated);
                break;
            case AIPDownloader.STATUS_ERROR:
            case AIPDownloader.STATUS_FINISHED:
                Toast.makeText(getContext(), getString(R.string.set_aip_downloader_finished), Toast.LENGTH_SHORT).show();
                finishAIPDownloader();
                break;
        }
    }

}