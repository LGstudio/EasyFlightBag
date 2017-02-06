package sk.lgstudio.easyflightbag.calculations;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.AirplaneEditorDialog;
import sk.lgstudio.easyflightbag.dialogs.AirplaneSelectorDialog;

/**
 * Created by LGstudio on 2017-01-31.
 */

public class CalculatorWB extends Calculator implements View.OnClickListener, DialogInterface.OnCancelListener {

    private File selectedPlane = null;
    private File parentFolder = null;
    private AirplaneSelectorDialog dialogAirplane;
    private Airplane airplane;
    private boolean selectorDialogOpened = false;

    private ImageButton airplaneSelector = null;
    private TextView airplaneId = null;
    private ImageButton airplaneEditor = null;

    private SharedPreferences prefs;
    private Context context;

    public CalculatorWB(SharedPreferences p, Context c, File f, float[] r, float[] v, int[] l) {
        super(r, v, l);
        prefs = p;
        context = c;
        parentFolder = f;
    }

    @Override
    public void initView(View v) {

        String folder = prefs.getString(context.getString(R.string.pref_wb_selected), null);
        if (folder != null)
            selectedPlane = new File (folder);
        else

        airplaneEditor = null;
        airplaneSelector = null;
        airplaneId = null;

        airplaneSelector = (ImageButton) v.findViewById(R.id.wb_plane);
        airplaneEditor = (ImageButton) v.findViewById(R.id.wb_edit);
        airplaneId = (TextView) v.findViewById(R.id.wb_airplane_name);
        airplaneSelector.setOnClickListener(this);
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
        dialogAirplane = new AirplaneSelectorDialog(context);
        dialogAirplane.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAirplane.setContentView(R.layout.dialog_airplane_selector);
        dialogAirplane.loadContent(parentFolder, true);
        dialogAirplane.selected = selectedPlane;
        dialogAirplane.setOnCancelListener(this);
        dialogAirplane.show();
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneEditorDialog(){
        selectorDialogOpened = false;
        AirplaneEditorDialog dialog = new AirplaneEditorDialog(context);
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
                prefs.edit().putString(context.getString(R.string.pref_wb_selected), selectedPlane.getPath()).apply();
                airplaneEditor.setClickable(true);
            }
            else {
                prefs.edit().remove(context.getString(R.string.pref_wb_selected)).apply();
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
                //Log.w("Airplane", "created");
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
            while(line != null) {
                str = str + line;
                Log.w("json String", line);
                line = reader.readLine();
            }


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
                    w.unus = j.getInt(3);
                a.additional_weight.add(w);
            }

            JSONArray jL = json.getJSONArray("limits");
            for (int i = 0; i < jL.length(); i++){
                JSONArray j = jL.getJSONArray(i);
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

        @Override
        public String toString(){
            String str = "";

            str = str + unit_v_speed + ", " + unit_v_speed + ", " + unit_moment + ", " + unit_fuel + " \n";
            str = str + cruise_sp + ", " + climb_sp + ", " + descent_sp + ", " + climb_rate +  ", " + desc_rate + " \n";
            str = str + fuel_density + ", " + fuel_flow + ", " + max_takeoff + ", " + max_landing + " \n";
            str = str + empty_weight + ", " + empty_arm + " \n";
            for (Weights w : additional_weight){
                str = str + w.name + ", " + w.arm + ", " + w.max_weight + ", " + w.unus + " \n";
            }
            for (Limits l : limits){
                str = str + l.arm + ", " + l.weight + " \n";
            }

            return str;
        }
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
