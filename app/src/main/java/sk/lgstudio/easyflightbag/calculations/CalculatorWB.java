package sk.lgstudio.easyflightbag.calculations;

import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.AirplaneEditorDialog;
import sk.lgstudio.easyflightbag.dialogs.AirplaneSelectorDialog;
import sk.lgstudio.easyflightbag.fragments.FragmentCalc;

/**
 * Created by LGstudio on 2017-01-31.
 */

public class CalculatorWB extends Calculator implements View.OnClickListener, DialogInterface.OnCancelListener {

    private File selectedPlane = null;
    private AirplaneSelectorDialog dialogAirplane;
    private Airplane airplane;
    private boolean selectorDialogOpened = false;

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

        String folder = fragment.prefs.getString(fragment.getString(R.string.pref_wb_selected), null);
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
                createAirplaneEditorDialog();
                break;
        }
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneSelectorDialog(){
        selectorDialogOpened = true;
        dialogAirplane = new AirplaneSelectorDialog(fragment.getContext());
        dialogAirplane.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAirplane.setContentView(R.layout.dialog_airplane_selector);
        dialogAirplane.loadContent(fragment.folder, true);
        dialogAirplane.setOnCancelListener(this);
        dialogAirplane.show();
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneEditorDialog(){
        selectorDialogOpened = false;
        AirplaneEditorDialog dialog = new AirplaneEditorDialog(fragment.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_airplane_editor);
        dialog.loadContent(airplane, selectedPlane);
        dialog.setOnCancelListener(this);
        dialog.show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {

        if (selectorDialogOpened){
            selectedPlane = dialogAirplane.selected;
            if (selectedPlane != null){
                fragment.prefs.edit().putString(fragment.getString(R.string.pref_wb_selected), selectedPlane.getPath()).apply();
                airplaneEditor.setClickable(true);
            }
            else {
                fragment.prefs.edit().remove(fragment.getString(R.string.pref_wb_selected)).apply();
                airplaneEditor.setClickable(false);
            }
            reloadContent();
        }
        else{

        }
    }

    private void reloadContent(){
        if (selectedPlane != null){
            String name = selectedPlane.getName();
            int suffix = name.lastIndexOf('.');
            airplaneId.setText(name.substring(0, suffix));
            airplaneEditor.setVisibility(View.VISIBLE);
            airplane = loadAirplaneFromJson();

            if (airplane != null){
                // TODO: create new view matching airplane description
            }
            else {
                // TODO: clear/hide other views
            }
        }
        else {
            airplaneId.setText("");
            airplaneEditor.setVisibility(View.GONE);
            // TODO: clear/hide other views
        }
    }

    private Airplane loadAirplaneFromJson(){
        Airplane a = new Airplane();
        FileInputStream is = null;

        try {
            is = new FileInputStream(selectedPlane);
            String str = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while(line != null) str = str + line;

            JSONObject json = new JSONObject(str);

            a.unit_speed = json.getInt("unit_speed");
            a.unit_v_speed = json.getInt("unit_v_speed");
            a.unit_moment = json.getInt("unit_moment");
            a.unit_fuel = json.getInt("unit_fuel");
            a.cruise_sp = json.getInt("cruise_sp");
            a.climb_sp = json.getInt("climb_sp");
            a.descent_sp = json.getInt("descent_sp");
            a.climb_rate = json.getInt("climb_rate");
            a.desc_rate = json.getInt("desc_rate");
            a.fuel_density = json.getInt("fuel_density");
            a.fuel_flow = json.getInt("fuel_flow");
            a.max_takeoff = json.getInt("max_takeoff");
            a.max_landing = json.getInt("max_landing");
            a.empty_weight = json.getInt("empty_weight");
            a.empty_arm = json.getInt("empty_arm");

            a.additional_weight = new ArrayList<>();
            a.limits = new ArrayList<>();

            JSONArray jAW = json.getJSONArray("additional_weight");
            for (int i = 0; i < jAW.length(); i++){
                JSONArray j = jAW.getJSONArray(i);
                Weights w = new Weights();
                w.name = j.getString(0);
                w.arm = j.getInt(1);
                w.max_weight = j.getInt(2);
                if (j.length() > 3)
                    w.unus = j.getInt(4);
                a.additional_weight.add(w);
            }

            JSONArray jL = json.getJSONArray("limits");
            for (int i = 0; i < jL.length(); i++){
                JSONArray j = jAW.getJSONArray(i);
                Limits l = new Limits();
                l.arm = j.getInt(0);
                l.weight = j.getInt(1);
                a.limits.add(l);
            }

            return a;

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class Airplane{
        public int unit_speed = 0;
        public int unit_v_speed = 0;
        public int unit_moment = 0;
        public int unit_fuel = 0;
        public int cruise_sp = 0;
        public int climb_sp = 0;
        public int descent_sp = 0;
        public int climb_rate = 0;
        public int desc_rate = 0;
        public int fuel_density = 0;
        public int fuel_flow = 0;
        public int max_takeoff = 0;
        public int max_landing = 0;
        public int empty_weight = 0;
        public int empty_arm = 0;
        public ArrayList<Weights> additional_weight = null;
        public ArrayList<Limits> limits = null;
    }

    public static class Weights{
        public String name;
        public int arm = 0;
        public int max_weight = 0;
        public int unus = -1;
    }

    public static class Limits{
        public int arm = 0;
        public int weight = 0;
    }
}
