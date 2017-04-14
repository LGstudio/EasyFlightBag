package sk.lgstudio.easyflightbag.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
 * Dialog for Airport and Airspace details
 */
public class OverlayDetailDialog extends Dialog implements View.OnClickListener {

    private LinearLayout layoutLists;
    protected LatLng location;
    private String km;
    private String d;
    private String m;
    private boolean canBack = false;
    private ArrayList<View> views = null;
    private TextView title;

    /**
     * Constructor
     * @param context
     */
    public OverlayDetailDialog(Context context) {
        super(context);
        km = context.getString(R.string.calc_unit_km);
        d = context.getString(R.string.calc_unit_degree);
        m = context.getString(R.string.calc_unit_m);
    }

    /**
     * Load content into an airport detail view
     * @param data
     */
    public void loadContent(Airport.Data data){
        layoutLists = (LinearLayout) findViewById(R.id.detail_lists);
        showAirportLayout(data);

        loadButtons();
        title = (TextView) findViewById(R.id.detail_position_text);
        title.setVisibility(View.GONE);
    }

    /**
     * Load content in airport and airspace list mode
     * @param myPosition
     * @param as
     * @param ap
     */
    public void loadContent(LatLng myPosition, LatLng clicked, ArrayList<Airspace.Data> as, ArrayList<Airport.Data> ap){
        location = myPosition;

        views = new ArrayList<>();
        layoutLists = (LinearLayout) findViewById(R.id.detail_lists);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        for (Airport.Data a: ap){
            View row = inflater.inflate(R.layout.dialog_detail_list_item, layoutLists, false);
            addAirportData(row, a);
            row.setOnClickListener(this);
            row.setTag(a);
            layoutLists.addView(row);
            views.add(row);
        }

        for (Airspace.Data a: as){
            View row = inflater.inflate(R.layout.dialog_detail_list_item, layoutLists, false);
            addAirspaceData(row, a);
            layoutLists.addView(row);
            views.add(row);
        }

        loadButtons();

        float[] results = new float[3];
        Location.distanceBetween(location.latitude, location.longitude, clicked.latitude, clicked.longitude, results);

        double b = results[1];
        if (b < 0) b = 360 + b;
        String dstStr = new DecimalFormat("#.#").format(results[0]/1000) + km;
        String bearStr = new DecimalFormat("#.#").format(b) + d;
        String lat = new DecimalFormat("#.#####").format(clicked.latitude);
        String lon = new DecimalFormat("#.#####").format(clicked.longitude);

        title = (TextView) findViewById(R.id.detail_position_text);
        title.setText("LAT:" + lat + "/LON:" + lon + " - " + dstStr + " / " + bearStr );
    }

    private void loadButtons(){
        ImageButton close = (ImageButton) findViewById(R.id.detail_close);
        close.setOnClickListener(this);
    }

    /**
     * Inflates airport detail mode
     * @param data
     */
    private void showAirportLayout(Airport.Data data){

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dialog_detail_list_item, null);

        TextView icao = (TextView) view.findViewById(R.id.detail_title);
        TextView type = (TextView) view.findViewById(R.id.detail_description);
        TextView loc = (TextView) view.findViewById(R.id.detail_bottom_center);
        TextView icon = (TextView) view.findViewById(R.id.detail_icon);
        TextView top = (TextView) view.findViewById(R.id.detail_end_top);
        TextView bottom = (TextView) view.findViewById(R.id.detail_end_bottom);
        top.setVisibility(View.GONE);
        bottom.setVisibility(View.GONE);

        icao.setText(data.icao+ " (" + data.country + ") - " + Airport.getAptType(data.type));
        type.setText(data.name);
        loc.setText("LAT: " + data.location.latitude + " / LON: " + data.location.longitude + " @ " + data.elevation + getContext().getString(R.string.calc_unit_m));
        icon.setBackground(new BitmapDrawable(getContext().getResources(), data.icon));
        icon.setScaleX(0.5f);
        icon.setScaleY(0.5f);

        layoutLists.addView(view);

        for (Airport.Radio r : data.radios){
            View rV = inflater.inflate(R.layout.dialog_detail_airport_section_tiem, null);

            TextView rTitle = (TextView) rV.findViewById(R.id.detail_ap_section_title);
            String text = getContext().getString(R.string.airport_radio)+ ": " + Airport.getRadioCategory(r.category) + " - " + r.frequency + " - " + r.type;
            if (r.specification.length() > 0) text += " (" + r.specification + ")";
            rTitle.setText(text);

            TextView rText = (TextView) rV.findViewById(R.id.detail_ap_section_text);
            if (r.description.length() > 0) rText.setText(getContext().getString(R.string.airport_radio_desc)+ ": " + r.description);
            else rText.setVisibility(View.GONE);

            layoutLists.addView(rV);
        }

        for (Airport.Runway r : data.runways){
            View rV = inflater.inflate(R.layout.dialog_detail_airport_section_tiem, null);

            TextView rTitle = (TextView) rV.findViewById(R.id.detail_ap_section_title);
            rTitle.setText(getContext().getString(R.string.airport_rwy) + ": " + r.name + " (" + Airport.getRwyOperations(r.operations) + ")");

            TextView rText = (TextView) rV.findViewById(R.id.detail_ap_section_text);
            String text = Airport.getRwySurface(r.sfc);
            if (r.strength_value.length() > 0) {

                if (r.strength_unit == Airport.RWY_STRENGTH_MPW)
                    text += "(MPW:" + r.strength_value + getContext().getString(R.string.calc_unit_t) + ")";
                else
                    text += "(" + r.strength_value + ")";

            }
            String len = new DecimalFormat("#").format(r.length);
            String wid = new DecimalFormat("#").format(r.width);
            text += ": " + len + " X " + wid + m;

            for (Airport.Direction d: r.directions){
                text += "\n" +getContext().getString(R.string.airport_rwy_tc) + ": " + d.tc;
                if (d.runs_tora > 0) text += " - " + getContext().getString(R.string.airport_rwy_tora)+ ": " + d.runs_tora + getContext().getString(R.string.calc_unit_m);
                if (d.runs_lda > 0) text += " - " + getContext().getString(R.string.airport_rwy_lda) + ": " + d.runs_lda + getContext().getString(R.string.calc_unit_m);
                if (d.land_ils.length() > 0) text += " - " + getContext().getString(R.string.airport_rwy_ils) + ": " + d.land_ils + " | " + getContext().getString(R.string.airport_rwy_papi) + d.land_papi;
            }

            rText.setText(text);

            layoutLists.addView(rV);
        }
    }


    /**
     * Inflates airport cards
     * @param v
     * @param dt
     */
    private void addAirportData(View v, Airport.Data dt){

        TextView icao = (TextView) v.findViewById(R.id.detail_title);
        TextView type = (TextView) v.findViewById(R.id.detail_description);
        TextView dist = (TextView) v.findViewById(R.id.detail_end_top);
        TextView bear = (TextView) v.findViewById(R.id.detail_end_bottom);
        TextView icon = (TextView) v.findViewById(R.id.detail_icon);
        TextView bottom =  (TextView) v.findViewById(R.id.detail_bottom_center);
        bottom.setVisibility(View.GONE);

        icao.setText(dt.icao);
        type.setText(Airport.getAptType(dt.type) + " - " + dt.name);

        if (location != null) {
            float[] results = new float[3];
            Location.distanceBetween(location.latitude, location.longitude, dt.location.latitude, dt.location.longitude, results);

            double b = results[1];
            if (b < 0) b = 360 + b;
            String dstStr = new DecimalFormat("#.#").format(results[0]/1000) + km;
            String bearStr = new DecimalFormat("#.#").format(b) + d;
            dist.setText(dstStr);
            bear.setText(bearStr);

            icon.setBackground(new BitmapDrawable(getContext().getResources(), dt.icon));
            icon.setScaleX(0.5f);
            icon.setScaleY(0.5f);
        }
    }

    /**
     * Inflates airspace cards
     * @param v
     * @param dt
     */
    private void addAirspaceData(View v, Airspace.Data dt) {
        TextView name = (TextView) v.findViewById(R.id.detail_title);
        TextView detail = (TextView) v.findViewById(R.id.detail_description);
        TextView top = (TextView) v.findViewById(R.id.detail_end_top);
        TextView bottom = (TextView) v.findViewById(R.id.detail_end_bottom);
        TextView type = (TextView) v.findViewById(R.id.detail_icon);
        TextView gone =  (TextView) v.findViewById(R.id.detail_bottom_center);
        gone.setVisibility(View.GONE);

        int cut = -1;
        int end = dt.name.indexOf("(");
        int comment = dt.name.indexOf("-");
        if (comment > -1 && end > -1) cut = min(end, comment);
        else if (end > -1) cut = end;
        else cut = comment;

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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.detail_close:
                dismiss();
                break;
            default:
                canBack = true;
                layoutLists.removeAllViews();
                title.setVisibility(View.GONE);
                showAirportLayout((Airport.Data) v.getTag());

        }
    }

    @Override
    public void onBackPressed(){
        if (!canBack)
            dismiss();
        else{
            canBack = false;
            title.setVisibility(View.VISIBLE);
            layoutLists.removeAllViews();
            for (View v: views)
                layoutLists.addView(v);
        }
    }
}
