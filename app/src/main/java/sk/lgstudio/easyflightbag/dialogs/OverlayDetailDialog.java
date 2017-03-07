package sk.lgstudio.easyflightbag.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
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
    private ArrayList<Airport.Data> airports;
    protected LatLng location;
    private String km;
    protected String d;
    private boolean canBack = false;
    ArrayList<View> views = null;

    /**
     * Constructor
     * @param context
     */
    public OverlayDetailDialog(Context context) {
        super(context);
        km = context.getString(R.string.calc_unit_km);
        d = context.getString(R.string.calc_unit_degree);
    }

    /**
     * Load content into an airport detail view
     * @param data
     */
    public void loadContent(Airport.Data data){
        layoutLists = (LinearLayout) findViewById(R.id.detail_lists);
        showAirportLayout(data);
    }

    /**
     * Initializes layout in airport detail mode
     * @param data
     */
    private void showAirportLayout(Airport.Data data){

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dialog_detail_airport, layoutLists, false);

        TextView icao = (TextView) view.findViewById(R.id.detail_ap_icao);
        TextView type = (TextView) view.findViewById(R.id.detail_ap_description);
        TextView loc = (TextView) view.findViewById(R.id.detail_ap_location);
        ImageView icon = (ImageView) view.findViewById(R.id.detail_ap_icon);

        icao.setText(data.icao+ " (" + data.country + ") - " + Airport.getAptType(data.type));
        type.setText(data.name);
        loc.setText("LAT: " + data.location.latitude + " / LON: " + data.location.longitude + " @ " + data.elevation + getContext().getString(R.string.calc_unit_m));
        icon.setImageBitmap(data.icon);
        icon.setScaleX(0.5f);
        icon.setScaleY(0.5f);

        LinearLayout list = (LinearLayout) view.findViewById(R.id.detail_ap_list);

        for (Airport.Radio r : data.radios){
            LinearLayout rV = (LinearLayout) inflater.inflate(R.layout.dialog_detail_airport_section_tiem, null);

            TextView rTitle = (TextView) rV.findViewById(R.id.detail_ap_section_title);
            rTitle.setText(getContext().getString(R.string.airport_radio)+ ": " + Airport.getRadioCategory(r.category));

            TextView rText = (TextView) rV.findViewById(R.id.detail_ap_section_text);
            String text = getContext().getString(R.string.airport_radio_fq)+ ": " + r.frequency + "\n";
            text += getContext().getString(R.string.airport_radio_type)+ ": " + r.type;
            if (r.specification.length() > 0) text += " (" + r.specification + ")\n";
            else text += "\n";
            if (r.description.length() > 0) text += getContext().getString(R.string.airport_radio_desc)+ ": " + r.description;
            rText.setText(text);

            list.addView(rV);
        }

        for (Airport.Runway r : data.runways){
            View rV = inflater.inflate(R.layout.dialog_detail_airport_section_tiem, list, false);

            TextView rTitle = (TextView) rV.findViewById(R.id.detail_ap_section_title);
            rTitle.setText(getContext().getString(R.string.airport_rwy) + ": " + r.name + " (" + Airport.getRwyOperations(r.operations) + ")");

            TextView rText = (TextView) rV.findViewById(R.id.detail_ap_section_text);
            String text = getContext().getString(R.string.airport_rwy_size) + ": " + r.width + getContext().getString(R.string.calc_unit_m) + " x " + r.length + getContext().getString(R.string.calc_unit_m) + "\n";
            text += getContext().getString(R.string.airport_rwy_sfc) + ": " + Airport.getRwySurface(r.sfc);
            if (r.strength_value.length() > 0) {
                text += " - " + r.strength_value;
                if (r.strength_unit == Airport.RWY_STRENGTH_MPW)
                    text += getContext().getString(R.string.calc_unit_t);
            }
            for (Airport.Direction d: r.directions){
                text += "\n" +getContext().getString(R.string.airport_rwy_tc) + ": " + d.tc;
                if (d.runs_tora > 0) text += " | " + getContext().getString(R.string.airport_rwy_tora)+ ": " + d.runs_tora;
                if (d.runs_lda > 0) text += " | " + getContext().getString(R.string.airport_rwy_lda) + ": " + d.runs_lda;
                if (d.land_ils.length() > 0) text += " | " + getContext().getString(R.string.airport_rwy_ils) + ": " + d.land_ils + " | " + getContext().getString(R.string.airport_rwy_papi) + d.land_papi;
            }

            rText.setText(text);

            list.addView(rV);
        }

        layoutLists.addView(view);
    }

    /**
     * Load content in airport and airspace list mode
     * @param myPosition
     * @param as
     * @param ap
     */
    public void loadContent(LatLng myPosition, ArrayList<Airspace.Data> as, ArrayList<Airport.Data> ap){
        airports = ap;
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

        ImageButton close = (ImageButton) findViewById(R.id.detail_close);
        close.setOnClickListener(this);
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

        icao.setText(dt.icao);
        type.setText(Airport.getAptType(dt.type) + " - " + dt.name);

        if (location != null) {
            float[] results = new float[3];
            Location.distanceBetween(location.latitude, location.longitude, dt.location.latitude, dt.location.longitude, results);

            String dstStr = new DecimalFormat("#.#").format(results[0]/1000) + km;
            String bearStr = new DecimalFormat("#.#").format(results[1]) + d;
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
        if (v.getId() == R.id.detail_close)
            dismiss();
        else {
            canBack = true;
            layoutLists.removeAllViews();
            showAirportLayout((Airport.Data) v.getTag());
        }
    }

    @Override
    public void onBackPressed(){
        if (!canBack)
            dismiss();
        else{
            canBack = false;
            layoutLists.removeAllViews();
            for (View v: views)
                layoutLists.addView(v);
        }
    }
}
