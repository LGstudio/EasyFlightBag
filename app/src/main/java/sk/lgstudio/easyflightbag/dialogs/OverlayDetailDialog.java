package sk.lgstudio.easyflightbag.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;
import sk.lgstudio.easyflightbag.openAIP.Airport;
import sk.lgstudio.easyflightbag.openAIP.Airspace;

import static java.lang.Math.min;

/**
 * Created by LGstudio on 2017-02-28.
 */

public class OverlayDetailDialog extends Dialog implements AdapterView.OnItemClickListener, View.OnClickListener {

    private LinearLayout layoutLists;
    private ArrayList<Airport.Data> airports;
    protected LatLng location;
    protected String km;
    protected String d;

    public OverlayDetailDialog(Context context) {
        super(context);
        km = context.getString(R.string.calc_unit_km);
        d = context.getString(R.string.calc_unit_degree);
    }

    public void loadContent(LatLng myPosition, ArrayList<Airspace.Data> as, ArrayList<Airport.Data> ap){
        airports = ap;
        location = myPosition;

        layoutLists = (LinearLayout) findViewById(R.id.detail_lists);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        for (Airport.Data a: ap){
            View row = inflater.inflate(R.layout.detail_dialog_list_item, layoutLists, false);
            addAirportData(row, a);
            layoutLists.addView(row, layoutLists.getChildCount()-1);
        }

        for (Airspace.Data a: as){
            View row = inflater.inflate(R.layout.detail_dialog_list_item, layoutLists, false);
            addAirspaceData(row, a);
            layoutLists.addView(row);
        }

        ImageButton close = (ImageButton) findViewById(R.id.detail_close);
        close.setOnClickListener(this);
    }

    private void addAirportData(View v, Airport.Data dt){

        TextView ciao = (TextView) v.findViewById(R.id.detail_title);
        TextView type = (TextView) v.findViewById(R.id.detail_description);
        TextView dist = (TextView) v.findViewById(R.id.detail_end_top);
        TextView bear = (TextView) v.findViewById(R.id.detail_end_bottom);
        TextView icon = (TextView) v.findViewById(R.id.detail_icon);

        ciao.setText(dt.icao);
        type.setText(Airport.getAptType(dt.type) + " - " + dt.name);

        if (location != null) {
            float[] results = new float[3];
            Location.distanceBetween(location.latitude, location.longitude, dt.location.latitude, dt.location.longitude, results);

            String dstStr = new DecimalFormat("#.#").format(results[0]/1000) + km;
            String bearStr = new DecimalFormat("#.#").format(results[1]) + d;
            dist.setText(dstStr);
            bear.setText(bearStr);

        }
    }

    private void addAirspaceData(View v, Airspace.Data dt) {
        TextView name = (TextView) v.findViewById(R.id.detail_title);
        TextView detail = (TextView) v.findViewById(R.id.detail_description);
        TextView top = (TextView) v.findViewById(R.id.detail_end_top);
        TextView bottom = (TextView) v.findViewById(R.id.detail_end_bottom);
        TextView type = (TextView) v.findViewById(R.id.detail_icon);

        int cut = -1;
        int end = dt.name.indexOf("(");
        int comment = dt.name.indexOf("-");
        if (comment > -1 && end > -1) cut = min(end, comment);
        else if (end > -1) cut = end;
        else cut = comment;

        Log.e(dt.name, end + " / " + comment + " / " + cut);

        if (cut > -1){
            name.setText(dt.name.substring(0, cut));
            detail.setText(dt.name.substring(cut));
        }
        else {
            name.setText(dt.name);
        }

        top.setText(Airspace.getReference(dt.altlimit_top) + " - " + dt.altlimit_top_value + Airspace.getUnit(dt.altlimit_top_unit));
        bottom.setText(Airspace.getReference(dt.altlimit_bottom) + " - " + dt.altlimit_bottom_value + Airspace.getUnit(dt.altlimit_bottom_unit));

        type.setText(Airspace.getCategory(dt.category));

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(MapOverlayManager.airspaceFillColor(dt.category));
        gd.setStroke(4, MapOverlayManager.airspaceStrokeColor(dt.category));
        type.setBackground(gd);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public void onBackPressed(){
        dismiss();
    }
}
