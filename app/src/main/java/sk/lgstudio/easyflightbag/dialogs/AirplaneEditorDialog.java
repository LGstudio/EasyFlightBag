package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.calculations.CalculatorWB;

/**
 * Created by LGstudio on 2017-02-01.
 */

public class AirplaneEditorDialog extends Dialog implements View.OnClickListener{

    private ImageButton btnBack;
    private ImageButton btnSave;
    private TextView airplaneId;

    private Button btnUnitSpeed;
    private Button btnUnitVSpeed;
    private Button btnUnitMoment;
    private Button btnUnitFuel;
    private boolean isDefaultSepeed = true; //kn
    private boolean isDefaultVSepeed = true; // ft/min
    private boolean isDefaultMoment = true; // kg x m
    private boolean isDefaultFuel = true; // liter

    private EditText edtCruiseSp;
    private EditText edtClimbSp;
    private EditText edtDescSp;
    private EditText edtClimbRt;
    private EditText edtDescRt;
    private TextView txtCruiseSp;
    private TextView txtClimbSp;
    private TextView txtDescSp;
    private TextView txtClimbRt;
    private TextView txtDescRt;

    private EditText edtFuelDens;
    private EditText edtFuelFlow;
    private TextView txtFuelDens;
    private TextView txtFuelFlow;

    private File file;
    public CalculatorWB.Airplane airplane;

    public AirplaneEditorDialog(Context context) {
        super(context);
    }

    public void loadContent(CalculatorWB.Airplane ap, File f){
        file = f;
        airplane = ap;

        btnBack = (ImageButton) findViewById(R.id.airplane_editor_back);
        btnSave = (ImageButton) findViewById(R.id.airplane_editor_save);
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        airplaneId = (TextView) findViewById(R.id.airplane_editor_name);
        String name = file.getName();
        int suffix = name.lastIndexOf('.');
        airplaneId.setText(name.substring(0, suffix));

        btnUnitSpeed = (Button) findViewById(R.id.ap_edit_unit_speed);
        btnUnitSpeed.setOnClickListener(this);
        btnUnitVSpeed = (Button) findViewById(R.id.ap_edit_unit_v_speed);
        btnUnitVSpeed.setOnClickListener(this);
        btnUnitMoment = (Button) findViewById(R.id.ap_edit_unit_moment);
        btnUnitMoment.setOnClickListener(this);
        btnUnitFuel = (Button) findViewById(R.id.ap_edit_unit_fuel);
        btnUnitFuel.setOnClickListener(this);

        edtCruiseSp =  (EditText) findViewById(R.id.ap_edit_perf_cr_sp);
        edtClimbSp =  (EditText) findViewById(R.id.ap_edit_perf_cl_sp);
        edtDescSp =  (EditText) findViewById(R.id.ap_edit_perf_de_sp);
        edtClimbRt =  (EditText) findViewById(R.id.ap_edit_perf_cl_rt);
        edtDescRt =  (EditText) findViewById(R.id.ap_edit_perf_de_rt);
        txtCruiseSp =  (TextView) findViewById(R.id.ap_edit_perf_cr_sp_txt);
        txtClimbSp =  (TextView) findViewById(R.id.ap_edit_perf_cl_sp_txt);
        txtDescSp =  (TextView) findViewById(R.id.ap_edit_perf_de_sp_txt);
        txtClimbRt =  (TextView) findViewById(R.id.ap_edit_perf_cl_rt_txt);
        txtDescRt =  (TextView) findViewById(R.id.ap_edit_perf_de_rt_txt);

        edtFuelDens =  (EditText) findViewById(R.id.ap_edit_perf_fuel_dens);
        edtFuelFlow =  (EditText) findViewById(R.id.ap_edit_perf_fuel_flow);
        txtFuelDens =  (TextView) findViewById(R.id.ap_edit_perf_fuel_dens_txt);
        txtFuelFlow =  (TextView) findViewById(R.id.ap_edit_perf_fuel_flow_txt);

        // load content from object
        if (airplane != null){
            if (airplane.unit_speed == 1){
                isDefaultSepeed = false;
                reloadSpeedViews();
            }
            if (airplane.unit_v_speed == 1){
                isDefaultVSepeed = false;
                reloadVSpeedViews();
            }
            if (airplane.unit_moment == 1){
                isDefaultMoment = false;
                reloadMomentViews();
            }
            if (airplane.unit_fuel == 1){
                isDefaultFuel = false;
                reloadFuelViews();
            }

            edtCruiseSp.setText(String.valueOf(airplane.cruise_sp));
            edtClimbSp.setText(String.valueOf(airplane.climb_sp));
            edtDescSp.setText(String.valueOf(airplane.descent_sp));
            edtClimbRt.setText(String.valueOf(airplane.climb_rate));
            edtDescRt.setText(String.valueOf(airplane.desc_rate));

            edtFuelDens.setText(String.valueOf(airplane.fuel_density));
            edtFuelFlow.setText(String.valueOf(airplane.fuel_flow));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.airplane_editor_back:
                cancel();
                break;
            case R.id.airplane_editor_save:
                saveAirplane();
                break;
            case R.id.ap_edit_unit_speed:
                isDefaultSepeed = !isDefaultSepeed;
                reloadSpeedViews();
                break;
            case R.id.ap_edit_unit_v_speed:
                isDefaultVSepeed = !isDefaultVSepeed;
                reloadVSpeedViews();
                break;
            case R.id.ap_edit_unit_moment:
                isDefaultMoment = !isDefaultMoment;
                reloadMomentViews();
                break;
            case R.id.ap_edit_unit_fuel:
                isDefaultFuel = !isDefaultFuel;
                reloadFuelViews();
                break;

        }
    }

    private void reloadSpeedViews(){
        int id = R.string.calc_unit_kmh;
        if (isDefaultSepeed){
            id = R.string.calc_unit_kn;
        }
        btnUnitSpeed.setText(id);
        txtClimbSp.setText(id);
        txtCruiseSp.setText(id);
        txtDescSp.setText(id);
    }

    private void reloadVSpeedViews(){
        int id = R.string.calc_unit_mps;
        if (isDefaultVSepeed){
            id = R.string.calc_unit_ftpmin;
        }
        btnUnitVSpeed.setText(id);
        txtClimbRt.setText(id);
        txtDescRt.setText(id);
    }

    private void reloadMomentViews(){
        int id = R.string.calc_unit_lbxin;
        int idW = R.string.calc_unit_lb;
        int idD = R.string.calc_unit_in;
        if (isDefaultMoment){
            id = R.string.calc_unit_kgxm;
            idW = R.string.calc_unit_kg;
            idD = R.string.calc_unit_mm;
        }
        btnUnitMoment.setText(id);

    }

    private void reloadFuelViews(){
        int id = R.string.calc_unit_g_us;
        int id2 = R.string.calc_unit_gph;
        if (isDefaultFuel){
            id = R.string.calc_unit_l;
            id2 = R.string.calc_unit_lph;
        }
        btnUnitFuel.setText(id);

        txtFuelFlow.setText(id2);


    }

    private void saveAirplane(){
        //TODO: save to file & Airplane object & make Toast
    }
}
