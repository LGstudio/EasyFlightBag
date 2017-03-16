package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;

/**
 * Airplane Editor - fullscreen dialog
 *
 * loadContent() must be called before show()
 */

public class AirplaneEditorDialog extends Dialog implements View.OnClickListener{

    private EditText edtCruiseSp;
    private EditText edtClimbSp;
    private EditText edtDescSp;
    private EditText edtClimbRt;
    private EditText edtDescRt;

    private EditText edtFuelDens;
    private EditText edtFuelFlow;

    private EditText edtMtow;
    private EditText edtMlw;

    private EditText edtEmptyW;
    private EditText edtEmptyA;

    private TableLayout tableTanks;
    private ArrayList<RowTank> tanks = new ArrayList<>();

    private TableLayout tableWeights;
    private ArrayList<RowWeight> weights = new ArrayList<>();

    private TableLayout tableLimits;
    private ArrayList<RowLimit> limits = new ArrayList<>();

    private AirplaneManager airplane;

    /**
     * Constructor
     * @param context
     * @param fullScreenDialog
     */
    public AirplaneEditorDialog(Context context, int fullScreenDialog) {
        super(context, fullScreenDialog);
    }

    /**
     * Loades the editor view based on the airplane manager data
     * @param ap
     */
    public void loadContent(AirplaneManager ap){
        airplane = ap;

        ImageButton btnBack = (ImageButton) findViewById(R.id.airplane_editor_back);
        ImageButton btnSave = (ImageButton) findViewById(R.id.airplane_editor_save);
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        TextView airplaneId = (TextView) findViewById(R.id.airplane_editor_name);
        airplaneId.setText(airplane.getName());

        edtCruiseSp =  (EditText) findViewById(R.id.ap_edit_perf_cr_sp);
        edtClimbSp =  (EditText) findViewById(R.id.ap_edit_perf_cl_sp);
        edtDescSp =  (EditText) findViewById(R.id.ap_edit_perf_de_sp);
        edtClimbRt =  (EditText) findViewById(R.id.ap_edit_perf_cl_rt);
        edtDescRt =  (EditText) findViewById(R.id.ap_edit_perf_de_rt);

        edtFuelDens =  (EditText) findViewById(R.id.ap_edit_perf_fuel_dens);
        edtFuelFlow =  (EditText) findViewById(R.id.ap_edit_perf_fuel_flow);

        edtMtow = (EditText) findViewById(R.id.ap_edit_perf_mtow);
        edtMlw = (EditText) findViewById(R.id.ap_edit_perf_mlw);

        edtEmptyW = (EditText) findViewById(R.id.ap_edit_perf_empty_w);
        edtEmptyA = (EditText) findViewById(R.id.ap_edit_perf_empty_a);

        tableTanks = (TableLayout) findViewById(R.id.ap_edit_pref_table_tanks);
        ImageButton tableTanksAdd = (ImageButton) findViewById(R.id.ap_edit_pref_table_tanks_add);
        tableTanksAdd.setOnClickListener(this);

        tableWeights = (TableLayout) findViewById(R.id.ap_edit_pref_table_weights);
        ImageButton tableWeightsAdd = (ImageButton) findViewById(R.id.ap_edit_pref_table_weights_add);
        tableWeightsAdd.setOnClickListener(this);

        tableLimits = (TableLayout) findViewById(R.id.ap_edit_pref_table_limits);
        ImageButton tableLimitsAdd = (ImageButton) findViewById(R.id.ap_edit_pref_table_limits_add);
        tableLimitsAdd.setOnClickListener(this);

        // load content from object
        if (airplane.loaded){

            edtCruiseSp.setText(String.valueOf(airplane.cruise_sp));
            edtClimbSp.setText(String.valueOf(airplane.climb_sp));
            edtDescSp.setText(String.valueOf(airplane.descent_sp));
            edtClimbRt.setText(String.valueOf(airplane.climb_rate));
            edtDescRt.setText(String.valueOf(airplane.desc_rate));

            edtFuelDens.setText(String.valueOf(airplane.fuel_density));
            edtFuelFlow.setText(String.valueOf(airplane.fuel_flow));

            edtMtow.setText(String.valueOf(airplane.max_takeoff));
            edtMlw.setText(String.valueOf(airplane.max_landing));

            edtEmptyW.setText(String.valueOf(airplane.empty_weight));
            edtEmptyA.setText(String.valueOf(airplane.empty_arm));

            for(AirplaneManager.Tanks w: airplane.tanks){
                addFuelTank(w.name, w.arm, w.max, w.unus);
            }

            for(AirplaneManager.Weights w: airplane.additional_weight){
                addWeights(w.name, w.arm, w.max);
            }

            for(AirplaneManager.Limits l: airplane.limits){
                addLimits(l.arm, l.weight);
            }
        }
    }

    /**
     * Handles button clicks
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.airplane_editor_back:
                cancel();
                break;
            case R.id.airplane_editor_save:
                saveAirplane();
                break;
            case R.id.ap_edit_pref_table_tanks_add:
                addFuelTank("", 0, 0 ,0);
                break;
            case R.id.ap_edit_pref_table_weights_add:
                addWeights("", 0, 0);
                break;
            case R.id.ap_edit_pref_table_limits_add:
                addLimits(0, 0);
                break;

        }
    }

    /**
     * Adds a Fuel Tank row into the table
     * @param n
     * @param a
     * @param c
     * @param u
     */
    private void addFuelTank(String n, double a, double c, double u){
        final RowTank row = new RowTank();
        row.row = (TableRow) getLayoutInflater().inflate(R.layout.dialog_airplane_editor_tank_row, null);
        row.name = (EditText) row.row.findViewById(R.id.ap_edit_fuel_row_title);
        row.arm = (EditText) row.row.findViewById(R.id.ap_edit_fuel_row_arm);
        row.cap = (EditText) row.row.findViewById(R.id.ap_edit_fuel_row_capacity);
        row.unu = (EditText) row.row.findViewById(R.id.ap_edit_fuel_row_unu);
        row.del = (ImageButton) row.row.findViewById(R.id.ap_edit_fuel_row_del);
        row.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableTanks.removeView(row.row);
                tanks.remove(row);
            }
        });

        row.name.setText(n);
        row.arm.setText(String.valueOf(a));
        row.cap.setText(String.valueOf(c));
        row.unu.setText(String.valueOf(u));

        tanks.add(row);
        tableTanks.addView(row.row);
    }

    /**
     * Adds a weights wor into the table
     * @param n
     * @param a
     * @param m
     */
    private void addWeights(String n, double a, double m) {
        final RowWeight row = new RowWeight();
        row.row = (TableRow) getLayoutInflater().inflate(R.layout.dialog_airplane_editor_weights_row, null);
        row.name = (EditText) row.row.findViewById(R.id.ap_edit_weight_row_title);
        row.arm = (EditText) row.row.findViewById(R.id.ap_edit_weight_row_arm);
        row.max = (EditText) row.row.findViewById(R.id.ap_edit_weight_row_max);
        row.del = (ImageButton) row.row.findViewById(R.id.ap_edit_weight_row_del);
        row.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableWeights.removeView(row.row);
                weights.remove(row);
            }
        });

        row.name.setText(n);
        row.arm.setText(String.valueOf(a));
        row.max.setText(String.valueOf(m));

        weights.add(row);
        tableWeights.addView(row.row);
    }

    /**
     * Adds a limit row int the table
     * @param a
     * @param w
     */
    private void addLimits(double a, double w) {
        final RowLimit row = new RowLimit();
        row.row = (TableRow) getLayoutInflater().inflate(R.layout.dialog_airplane_editor_limits_row, null);
        row.arm = (EditText) row.row.findViewById(R.id.ap_edit_limit_row_arm);
        row.weight = (EditText) row.row.findViewById(R.id.ap_edit_limit_row_weight);
        row.del = (ImageButton) row.row.findViewById(R.id.ap_edit_limit_row_del);
        row.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableLimits.removeView(row.row);
                limits.remove(row);
            }
        });

        row.arm.setText(String.valueOf(a));
        row.weight.setText(String.valueOf(w));

        limits.add(row);
        tableLimits.addView(row.row);


    }

    /**
     * Test and saves the filled editor
     */
    private void saveAirplane(){
        if (tanks.size() < 1){
            Toast.makeText(getContext(), getContext().getString(R.string.calc_warning_tank), Toast.LENGTH_SHORT).show();
            return;
        }
        if (weights.size() < 1){
            Toast.makeText(getContext(), getContext().getString(R.string.calc_warning_weight), Toast.LENGTH_SHORT).show();
            return;
        }
        if (limits.size() < 3){
            Toast.makeText(getContext(), getContext().getString(R.string.calc_warning_limit), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmpty()) {
            airplane.cruise_sp = Double.parseDouble(edtCruiseSp.getText().toString());
            airplane.climb_sp = Double.parseDouble(edtClimbSp.getText().toString());
            airplane.descent_sp = Double.parseDouble(edtDescSp.getText().toString());
            airplane.climb_rate = Double.parseDouble(edtClimbRt.getText().toString());
            airplane.desc_rate = Double.parseDouble(edtDescRt.getText().toString());
            airplane.fuel_density = Double.parseDouble(edtFuelDens.getText().toString());
            airplane.fuel_flow = Double.parseDouble(edtFuelFlow.getText().toString());
            airplane.max_takeoff = Double.parseDouble(edtMtow.getText().toString());
            airplane.max_landing = Double.parseDouble(edtMlw.getText().toString());
            airplane.empty_weight = Double.parseDouble(edtEmptyW.getText().toString());
            airplane.empty_arm = Double.parseDouble(edtEmptyA.getText().toString());

            airplane.tanks = new ArrayList<>();
            airplane.additional_weight = new ArrayList<>();
            airplane.limits = new ArrayList<>();

            for (RowTank t : tanks) {
                AirplaneManager.Tanks w = new AirplaneManager.Tanks();
                w.arm = Double.parseDouble(t.arm.getText().toString());
                w.max = Double.parseDouble(t.cap.getText().toString());
                w.unus = Double.parseDouble(t.unu.getText().toString());
                w.name = t.name.getText().toString();
                airplane.tanks.add(w);
            }
            for (RowWeight t : weights) {
                AirplaneManager.Weights w = new AirplaneManager.Weights();
                w.arm = Double.parseDouble(t.arm.getText().toString());
                w.max = Double.parseDouble(t.max.getText().toString());
                w.name = t.name.getText().toString();
                airplane.additional_weight.add(w);
            }
            for (RowLimit t : limits) {
                AirplaneManager.Limits w = new AirplaneManager.Limits();
                w.arm = Double.parseDouble(t.arm.getText().toString());
                w.weight = Double.parseDouble(t.weight.getText().toString());
                airplane.limits.add(w);
            }

            if (airplane.saveFile()){
                airplane.loaded = true;
                Toast.makeText(getContext(), getContext().getString(R.string.calc_warning_saved), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getContext(), getContext().getString(R.string.calc_warning_save_error), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), getContext().getString(R.string.calc_warning_notfilled), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tests if any field is left blank
     * @return
     */
    private boolean isEmpty(){
        boolean empty = (edtCruiseSp.getText().length() == 0)
                && (edtClimbSp.getText().length() == 0)
                && (edtDescSp.getText().length() == 0)
                && (edtClimbRt.getText().length() == 0)
                && (edtDescRt.getText().length() == 0)
                && (edtFuelDens.getText().length() == 0)
                && (edtFuelFlow.getText().length() == 0)
                && (edtMtow.getText().length() == 0)
                && (edtMlw.getText().length() == 0)
                && (edtEmptyW.getText().length() == 0)
                && (edtEmptyA.getText().length() == 0);

        for (RowTank t : tanks) {
            empty = empty
                    && (t.arm.getText().length() == 0)
                    && (t.cap.getText().length() == 0)
                    && (t.unu.getText().length() == 0)
                    && (t.name.getText().length() == 0);
        }
        for (RowWeight t : weights) {
            empty = empty
                    && (t.arm.getText().length() == 0)
                    && (t.max.getText().length() == 0)
                    && (t.name.getText().length() == 0);
        }
        for (RowLimit t : limits) {
            empty = empty
                    && (t.arm.getText().length() == 0)
                    && (t.weight.getText().length() == 0);
        }
        return empty;
    }

    /**
     * Holds the data of a fuel tank
     */
    private class RowTank{
        TableRow row;
        EditText name;
        EditText arm;
        EditText cap;
        EditText unu;
        ImageButton del;
    }

    /**
     * Holds the data of a weight
     */
    private class RowWeight{
        TableRow row;
        EditText name;
        EditText arm;
        EditText max;
        ImageButton del;
    }

    /**
     * Holds the data of a limit point
     */
    private class RowLimit{
        TableRow row;
        EditText arm;
        EditText weight;
        ImageButton del;
    }
}
