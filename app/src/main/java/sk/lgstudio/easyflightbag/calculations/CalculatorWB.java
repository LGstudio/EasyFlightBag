package sk.lgstudio.easyflightbag.calculations;

import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.AirplaneDialog;
import sk.lgstudio.easyflightbag.fragments.FragmentCalc;

/**
 * Created by LGstudio on 2017-01-31.
 */

public class CalculatorWB extends Calculator implements View.OnClickListener, DialogInterface.OnCancelListener {

    private File selectedPlane = null;
    private AirplaneDialog dialogAirplane;

    private ImageButton airplaneSelect;
    private TextView airplaneId;
    private ImageButton airplaneEditor;

    protected FragmentCalc fragment;

    public CalculatorWB(FragmentCalc f, float[] r, float[] v, int[] l) {
        super(r, v, l);
        fragment = f;
    }

    @Override
    public void initView(View v) {

        String folder = fragment.activity.prefs.getString(fragment.getString(R.string.pref_wb_selected), null);
        if (folder != null)
            selectedPlane = new File (folder);

        airplaneSelect = (ImageButton) v.findViewById(R.id.wb_plane);
        airplaneId = (TextView) v.findViewById(R.id.wb_airplane_name);
        airplaneEditor = (ImageButton) v.findViewById(R.id.wb_edit);
        airplaneSelect.setOnClickListener(this);
        airplaneEditor.setOnClickListener(this);

        reloadContent();
    }

    @Override
    public void detachView() {// empty
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wb_plane:
                createAirplaneSelectorDialog();
                break;
            case R.id.wb_edit:
                break;
        }
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneSelectorDialog(){

        dialogAirplane = new AirplaneDialog(fragment.getContext());
        dialogAirplane.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAirplane.setContentView(R.layout.chk_airplane_dialog);
        dialogAirplane.loadContent(fragment.folder, true);
        dialogAirplane.setOnCancelListener(this);
        dialogAirplane.show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        selectedPlane = dialogAirplane.selected;
        if (selectedPlane != null){
            fragment.activity.prefs.edit().putString(fragment.getString(R.string.pref_wb_selected), selectedPlane.getPath()).apply();
        }
        else {
            fragment.activity.prefs.edit().remove(fragment.getString(R.string.pref_wb_selected)).apply();
        }
        reloadContent();
    }

    private void reloadContent(){
        if (selectedPlane != null){
            String name = selectedPlane.getName();
            int occurance = name.lastIndexOf('.');
            airplaneId.setText(name.substring(0, occurance));

            //TODO: create new view matching airplane description
        }
        else {
            airplaneId.setText("");
            // todo create
        }
    }

    private static class Airplane{
        int unit_speed;
        int unit_v_speed;
        int unit_moment;
        int unit_fuel;
        int cruise_sp;
        int climb_sp;
        int descent_sp;
        int climb_rate;
        int desc_rate;
        int fuel_denisty;
        int fuel_flow;
        int max_takeoff;
        int max_landing;
        int empty_weight;
        int empty_arm;
        ArrayList<Weights> additional_weight;
        ArrayList<Limits> limits;
    }

    private static class Weights{
        String name;
        int arm;
        int max_weight;
        int unus;
    }

    private static class Limits{
        int arm;
        int weight;
    }
}
