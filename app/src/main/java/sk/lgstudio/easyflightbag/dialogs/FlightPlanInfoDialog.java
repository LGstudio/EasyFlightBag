package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;
import sk.lgstudio.easyflightbag.managers.FlightPlanManager;

/**
 * Created by LGstudio on 2017-04-18.
 */

public class FlightPlanInfoDialog extends Dialog {

    private AirplaneManager airplaneManager;
    private FlightPlanManager flightPlanManager;
    private Context c;

    private String f = "%02d:%02d";
    private String n = "\n";
    private String d = ": ";
    private String q = "?";

    public FlightPlanInfoDialog(Context context, AirplaneManager ap, FlightPlanManager fp) {
        super(context);
        flightPlanManager = fp;
        airplaneManager = ap;
        c = context;
    }

    @Override
    public void show(){
        if (!airplaneManager.loaded || flightPlanManager == null) return;

        super.show();

        ImageButton bck = (ImageButton) findViewById(R.id.plan_info_close);
        bck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TextView tv = (TextView) findViewById(R.id.plan_info_text);
        String text = "";

        text += c.getString(R.string.plan_aircraft_id) + d + airplaneManager.getName() + n;
        text += c.getString(R.string.plan_aircraft_type) + d + airplaneManager.type + n;
        text += c.getString(R.string.plan_plane_equip) + d + airplaneManager.eq_nav + "/" + airplaneManager.eq_ssr + n;

        String depAp = flightPlanManager.plan.get(0).name;
        if (depAp.length() != 4) depAp = q;
        text += c.getString(R.string.plan_dep_airp) + d + depAp + n;

        text += c.getString(R.string.plan_departure_time) + d + String.format(f, flightPlanManager.data.depH, flightPlanManager.data.depM) + n;
        text += c.getString(R.string.calc_cruise_sp) + d + airplaneManager.cruise_sp + c.getString(R.string.calc_unit_kn) + n;

        String arrAp = flightPlanManager.plan.get(flightPlanManager.plan.size()-1).name;
        if (arrAp.length() != 4) arrAp = q;
        text += c.getString(R.string.plan_arr_airp) + d + arrAp + n;

        text += c.getString(R.string.plan_arrival_time) + d + String.format(f, flightPlanManager.data.arrivH, flightPlanManager.data.arrivM) + n;
        text += c.getString(R.string.plan_fuel_endurance) + d + String.format(f, flightPlanManager.data.rangeH, flightPlanManager.data.rangeM) + n;
        text += c.getString(R.string.calc_airplane_color) + d + airplaneManager.color + n;

        tv.setText(text);
    }
}
