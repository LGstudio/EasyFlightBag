package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.DecimalFormat;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;
import sk.lgstudio.easyflightbag.managers.FlightPlanManager;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;

/**
 * Dialog To create a complete flight plan
 *      - Select plane
 *      - Fill flight info
 *      - Select Flight plan
 */
public class FlightPlanDialog extends Dialog implements View.OnClickListener, DialogInterface.OnCancelListener, DialogInterface.OnDismissListener, NumberPicker.OnValueChangeListener {

    public final static float KTS_TO_KMPH = 1.852f;

    private TextView txtAirplane;
    private TextView txtPlan;
    private TextView txtSum;
    private EditText edtCrSp;
    private TableLayout table;
    private NumberPicker depH;
    private NumberPicker depM;

    private File plansFolder;
    private File airplanesFolder;
    private AirplaneManager airplaneManager;
    private MapOverlayManager mapOverlayManager;
    private LatLng lastPosition;
    public FlightPlanManager flightPlanManager = null; // returned value
    private Bundle instance;

    private SelectorDialog dialog;
    private boolean isAirplane = false;
    private boolean notOK = false;
    private boolean noFuel = false;

    private String sumArr;
    private String sumDur;
    private String sumDist;
    private String sumEndur;
    private String sumReq;
    private String sumKm;
    private String sumL;
    private String f = "%02dh %02dmin";

    public FlightPlanDialog(Context context, int themeResId, AirplaneManager ap, MapOverlayManager map, LatLng pos, File plan, File airplane, Bundle bundle) {
        super(context, themeResId);

        airplanesFolder = airplane;
        plansFolder = plan;
        airplaneManager = ap;
        mapOverlayManager = map;
        lastPosition = pos;
        instance = bundle;

        sumArr = context.getString(R.string.plan_arrival_time);
        sumDur = context.getString(R.string.plan_duration_time);
        sumDist = context.getString(R.string.plan_duration_distance);
        sumKm = context.getString(R.string.calc_unit_km);
        sumEndur = context.getString(R.string.plan_fuel_endurance);
        sumL = context.getString(R.string.calc_unit_l);
        sumReq = context.getString(R.string.plan_fuel_required);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void show(){
        super.show();

        ImageButton btnBack = (ImageButton) findViewById(R.id.plan_bck);
        ImageButton btnSave = (ImageButton) findViewById(R.id.plan_save);
        Button btnAirplane = (Button) findViewById(R.id.plan_select_airplane);
        Button btnPlan = (Button) findViewById(R.id.plan_select_plan);

        txtAirplane = (TextView) findViewById(R.id.plan_select_airplane_text);
        txtPlan = (TextView) findViewById(R.id.plan_select_plan_text);
        txtSum = (TextView) findViewById(R.id.plan_sum);
        edtCrSp = (EditText) findViewById(R.id.plan_ap_cruise_sp);

        depH = (NumberPicker) findViewById(R.id.plan_departure_h);
        depM = (NumberPicker) findViewById(R.id.plan_departure_m);
        depH.setMaxValue(0);
        depH.setMaxValue(23);
        depH.setValue(12);
        depM.setMaxValue(0);
        depM.setMaxValue(59);

        table = (TableLayout) findViewById(R.id.plan_ap_data_table);

        loadAirplane();

        depH.setOnValueChangedListener(this);
        depM.setOnValueChangedListener(this);

        edtCrSp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    airplaneManager.cruise_sp = Float.valueOf(String.valueOf(s));
                else
                    airplaneManager.cruise_sp = 0f;
                validateData();

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnPlan.setOnClickListener(this);
        btnAirplane.setOnClickListener(this);
    }

    /**
     * Loads new airplane detials into the fields
     */
    private void loadAirplane(){

        while (table.getChildCount() > 2) {
            table.removeViewAt(2);
        }

        if (airplaneManager.loaded){
            txtAirplane.setText(airplaneManager.getName());
            edtCrSp.setEnabled(true);

            edtCrSp.setText(String.valueOf(airplaneManager.cruise_sp));

            for(final AirplaneManager.Tanks t: airplaneManager.tanks) {

                TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.calc_wb_fuel_row, null);
                TextView txtName = (TextView) row.findViewById(R.id.ap_wb_fuel_name);
                TextView txtCap = (TextView) row.findViewById(R.id.ap_wb_fuel_capacity);
                TextView txtUnus = (TextView) row.findViewById(R.id.ap_wb_fuel_unus);
                EditText actual = (EditText) row.findViewById(R.id.ap_wb_fuel_content);
                txtName.setText(t.name);
                txtCap.setText(String.valueOf(t.max));
                txtUnus.setText(String.valueOf(t.unus));
                actual.setText(String.valueOf(t.actual));
                actual.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0)
                            t.actual = Double.parseDouble(s.toString());
                        else
                            t.actual = 0;
                        validateData();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                table.addView(row);

            }

        }
        else {
            txtAirplane.setText(getContext().getString(R.string.plan_airplane_select_none));
            edtCrSp.setEnabled(false);
            edtCrSp.setText("");
        }
        validateData();
    }

    /**
     * Loads flight plan related data into fields
     */
    private void loadFlightPlan(){
        if (flightPlanManager == null){
            txtPlan.setText(getContext().getString(R.string.plan_select_none));
        }
        else{
            txtPlan.setText(flightPlanManager.getPlanName());
        }
        validateData();
    }

    /**
     * rewrites summary information
     */
    private void validateData(){
        notOK = false;
        if (airplaneManager.loaded && flightPlanManager != null){
            for(AirplaneManager.Tanks t: airplaneManager.tanks){
                if (t.actual > t.max || t.actual < t.unus)
                    notOK = true;
            }
            if (airplaneManager.cruise_sp == 0)
                notOK = true;
        }
        else notOK = true;

        if (notOK) {
            txtSum.setText(getContext().getString(R.string.plan_summary_empty));
        }
        else {
            double dist = flightPlanManager.getRoutLength();
            double sp = airplaneManager.cruise_sp * KTS_TO_KMPH;

            double trip = (dist/sp);

            int h = (int) trip;
            int m = (int) (trip*60)%60;

            airplaneManager.flightTimeH = h;
            airplaneManager.flightTimeM = m;

            flightPlanManager.data.arrivM = (m + flightPlanManager.data.depM) % 60;
            flightPlanManager.data.arrivH = (h + flightPlanManager.data.depH + ((m + flightPlanManager.data.depM) / 60)) % 24;


            float fuel = 0;
            for(AirplaneManager.Tanks t: airplaneManager.tanks){
                fuel += t.actual - t.unus;
            }

            double range = fuel/airplaneManager.fuel_flow;

            flightPlanManager.data.rangeH = (int) range;
            flightPlanManager.data.rangeM = (int) (range*60)%60;

            if(range < trip){
                noFuel = true;
                double req = trip * airplaneManager.fuel_flow;
                txtSum.setText(getContext().getString(R.string.plan_no_fuel) + "\n" + sumReq + ": " + new DecimalFormat("#.####").format(req) + sumL);
                airplaneManager.flightTimeH = 0;
                airplaneManager.flightTimeM = 0;
            }
            else {
                noFuel = false;
                String info = sumDist + ": " + new DecimalFormat("#.#").format(dist) + sumKm + "\n";
                info += sumDur + ": " + String.format(f, h, m) + "\n";
                info += sumArr + ": " + String.format(f, flightPlanManager.data.arrivH, flightPlanManager.data.arrivM) + "\n";
                info += sumEndur + ": " + String.format(f, flightPlanManager.data.rangeH, flightPlanManager.data.rangeM);
                txtSum.setText(info);
            }
        }
    }

    /**
     * Check if data are valid for flight then close the dialog
     */
    private void savePlan(){
        if (notOK)
            Toast.makeText(getContext(), getContext().getString(R.string.plan_summary_empty), Toast.LENGTH_SHORT).show();
        else if (noFuel)
            Toast.makeText(getContext(), getContext().getString(R.string.plan_no_fuel), Toast.LENGTH_SHORT).show();
        else
            dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.plan_bck:
                flightPlanManager = null;
                dismiss();
                break;
            case R.id.plan_save:
                savePlan();
                break;
            case R.id.plan_select_airplane:
                openAirplaneSelector();
                break;
            case R.id.plan_select_plan:
                openFlightPlanSelector();
                break;
        }
    }

    /**
     * Opens Airplane Selector Dialog
     */
    private void openAirplaneSelector(){
        isAirplane = true;
        dialog = new SelectorDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_selector);
        dialog.loadContent(airplanesFolder, airplaneManager.file ,true, SelectorDialog.TYPE_AIRPLANE);
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    /**
     * Opens Flight Plan Selector Dialog
     */
    private void openFlightPlanSelector(){
        isAirplane = false;
        dialog = new SelectorDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_selector);
        dialog.loadContent(plansFolder, null, true, SelectorDialog.TYPE_FLIGHTPLAN);
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);
        dialog.show();

    }

    @Override
    public void onCancel(DialogInterface d) {
        if (dialog == null) return;
        if (isAirplane){
            if (dialog.selected == null) {
                airplaneManager.clearFile();
                loadAirplane();
            }
        }
        else{
            if (dialog.selected == null) {
                flightPlanManager = null;
                loadFlightPlan();
            }
        }
        dialog = null;
    }

    @Override
    public void onDismiss(DialogInterface d) {
        if (dialog == null) return;
        if (isAirplane){
            if (dialog.selected != null){
                airplaneManager.loadFile(dialog.selected);
                if (dialog.edit || !airplaneManager.loaded) {
                    createAirplaneEditorDialog();
                    return;
                }
            }
            else {
                airplaneManager.clearFile();
            }
            loadAirplane();
        }
        else{
            if (dialog.selected != null){
                flightPlanManager = new FlightPlanManager(dialog.selected);
                if (dialog.edit || !flightPlanManager.loaded) {
                    openPlanEditor();
                }
            }
            else {
                flightPlanManager = null;
            }
            loadFlightPlan();
        }
        dialog = null;
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneEditorDialog(){
        AirplaneEditorDialog dialog = new AirplaneEditorDialog(getContext(), R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_airplane_editor);
        dialog.loadContent(airplaneManager);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loadAirplane();
            }
        });
        dialog.show();
    }

    /**
     * Opens flight plan editor Dialog
     */
    private void openPlanEditor(){
        FlighPlanEditorDialog dialog = new FlighPlanEditorDialog(getContext(), R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_plan_editor);
        dialog.loadContent(lastPosition, 30f, flightPlanManager, mapOverlayManager, instance);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {;
                loadFlightPlan();
            }
        });
        dialog.show();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        switch(picker.getId()) {
            case R.id.plan_departure_h:
                if (flightPlanManager != null) flightPlanManager.data.depH = newVal;
                break;
            case R.id.plan_departure_m:
                if (flightPlanManager != null) flightPlanManager.data.depM = newVal;
                if (newVal == 0 && oldVal == 59) {
                    if (flightPlanManager != null) flightPlanManager.data.depH += 1;
                    depH.setValue(depH.getValue()+1);
                }
                else if (newVal == 59 && oldVal == 0) {
                    if (flightPlanManager != null) flightPlanManager.data.depH -= 1;
                    depH.setValue(depH.getValue()-1);
                }
                break;
        }
        validateData();
    }

    @Override
    public void onBackPressed(){
        flightPlanManager = null;
        dismiss();
    }
}
