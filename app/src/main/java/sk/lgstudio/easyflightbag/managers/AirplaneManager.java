package sk.lgstudio.easyflightbag.managers;

import android.app.Activity;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;


/**
 * Manages a given airplane. Opens and reads the .json description and allows data manipulation
 */

public class AirplaneManager {

    private static final String KEY_CR_SP = "cruise_sp";
    private static final String KEY_CL_SP = "climb_sp";
    private static final String KEY_DS_SP = "descent_sp";
    private static final String KEY_CL_RT = "climb_rate";
    private static final String KEY_DS_RT = "desc_rate";
    private static final String KEY_FUEL_DENS = "fuel_density";
    private static final String KEY_FUEL_FLOW = "fuel_flow";
    private static final String KEY_MTW = "max_takeoff";
    private static final String KEY_MLW = "max_landing";
    private static final String KEY_EMPTY_W = "empty_weight";
    private static final String KEY_EMPTY_A = "empty_arm";
    private static final String KEY_ADD_W = "additional_weight";
    private static final String KEY_LIMITS = "limits";
    private static final String KEY_EQ_COM_NAV = "eq_com_nav";
    private static final String KEY_EQ_SSR = "eq_ssr";
    private static final String KEY_TYPE = "eq_type";
    private static final String KEY_COLOR = "eq_color";
    private static final String KEY_ARM_IS_SAT = "arm_sat";

    public File file = null;

    public boolean loaded = false;
    public boolean isRoute = false;

    public int flightTimeH = 0;
    public int flightTimeM = 0;

    public String type = "";
    public String color = "";
    public String eq_nav = "";
    public String eq_ssr = "";
    public boolean eq_nav_bool[] = {false, false, false, false, false, false, false, false, false, false};
    public boolean eq_ssr_bool[] = {false, false, false, false, false, false, false, false, false, false};

    public double cruise_sp = 0;
    public double climb_sp = 0;
    public double descent_sp = 0;
    public double climb_rate = 0;
    public double desc_rate = 0;
    public double fuel_density = 0;
    public double fuel_flow = 0;
    public double max_takeoff = 0;
    public double max_landing = 0;
    public double empty_weight = 0;
    public double empty_arm = 0;

    public boolean is_arm_sat = false;

    public ArrayList<Weights> additional_weight = new ArrayList<>();
    public ArrayList<Tanks> tanks= new ArrayList<>();
    public ArrayList<Limits> limits= new ArrayList<>();

    private SharedPreferences prefs;
    private String prefKey;

    /**
     * Constructor
     */
    public AirplaneManager(Activity a, SharedPreferences p){
        prefs = p;
        prefKey = a.getString(R.string.pref_airplane_selected);
        String filename = prefs.getString(prefKey, "");
        if (filename.length() > 0){
            file = new File(filename);
            if (file.exists()) loadFile(file);
            else file = null;
        }
    }

    /**
     *  reads the .json file
     */
    public void loadFile(File f){
        file = f;
        FileInputStream is = null;

        prefs.edit().putString(prefKey, file.getPath()).apply();

        limits.clear();
        additional_weight.clear();
        tanks.clear();

        try {
            is = new FileInputStream(file);
            String str = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while(line != null) {
                str = str + line;
                line = reader.readLine();
            }

            JSONObject json = new JSONObject(str);

            type = json.getString(KEY_TYPE);
            color = json.getString(KEY_COLOR);
            eq_nav = json.getString(KEY_EQ_COM_NAV);
            eq_ssr = json.getString(KEY_EQ_SSR);

            eq_nav_bool[0] = (eq_nav.contains("D"));
            eq_nav_bool[1] = (eq_nav.contains("F"));
            eq_nav_bool[2] = (eq_nav.contains("G"));
            eq_nav_bool[3] = (eq_nav.contains("H"));
            eq_nav_bool[4] = (eq_nav.contains("K"));
            eq_nav_bool[5] = (eq_nav.contains("L"));
            eq_nav_bool[6] = (eq_nav.contains("O"));
            eq_nav_bool[7] = (eq_nav.contains("U"));
            eq_nav_bool[8] = (eq_nav.contains("V"));
            eq_nav_bool[9] = (eq_nav.contains("Z"));

            eq_ssr_bool[0] = (eq_ssr.contains("N"));
            eq_ssr_bool[1] = (eq_ssr.contains("A"));
            eq_ssr_bool[2] = (eq_ssr.contains("C"));
            eq_ssr_bool[3] = (eq_ssr.contains("E"));
            eq_ssr_bool[4] = (eq_ssr.contains("H"));
            eq_ssr_bool[5] = (eq_ssr.contains("I"));
            eq_ssr_bool[6] = (eq_ssr.contains("L"));
            eq_ssr_bool[7] = (eq_ssr.contains("X"));
            eq_ssr_bool[8] = (eq_ssr.contains("P"));
            eq_ssr_bool[9] = (eq_ssr.contains("S"));

            cruise_sp = json.getDouble(KEY_CR_SP);
            climb_sp = json.getDouble(KEY_CL_SP);
            descent_sp = json.getDouble(KEY_DS_SP);
            climb_rate = json.getDouble(KEY_CL_RT);
            desc_rate = json.getDouble(KEY_DS_RT);
            fuel_density = json.getDouble(KEY_FUEL_DENS);
            fuel_flow = json.getDouble(KEY_FUEL_FLOW);
            max_takeoff = json.getDouble(KEY_MTW);
            max_landing = json.getDouble(KEY_MLW);
            empty_weight = json.getDouble(KEY_EMPTY_W);
            empty_arm = json.getDouble(KEY_EMPTY_A);

            is_arm_sat = json.getBoolean(KEY_ARM_IS_SAT);

            JSONArray jAW = json.getJSONArray(KEY_ADD_W);
            for (int i = 0; i < jAW.length(); i++){
                JSONArray j = jAW.getJSONArray(i);
                if (j.length() > 3){
                    Tanks w = new Tanks();
                    w.name = j.getString(0);
                    w.arm = j.getDouble(1);
                    w.max = j.getDouble(2);
                    w.unus = j.getDouble(3);
                    tanks.add(w);
                }
                else{
                    Weights w = new Weights();
                    w.name = j.getString(0);
                    w.arm = j.getDouble(1);
                    w.max = j.getDouble(2);
                    additional_weight.add(w);
                }

            }

            JSONArray jL = json.getJSONArray(KEY_LIMITS);
            for (int i = 0; i < jL.length(); i++){
                JSONArray j = jL.getJSONArray(i);
                Limits l = new Limits();
                l.arm = j.getDouble(0);
                l.weight = j.getDouble(1);
                limits.add(l);
            }

            loaded = true;
        } catch (JSONException | IOException e) {
            loaded = false;
        }
    }

    /**
     * Clears airplane data
     */
    public void clearFile(){
        file = null;
        limits.clear();
        additional_weight.clear();
        tanks.clear();
        loaded = false;
    }

    /**
     * Saves the modified data int a .json file
     * @return
     */
    public boolean saveFile(){
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_TYPE, type);
            json.put(KEY_COLOR, color);

            eq_nav = "";
            if (eq_nav_bool[0]) eq_nav += "D";
            if (eq_nav_bool[1]) eq_nav += "F";
            if (eq_nav_bool[2]) eq_nav += "G";
            if (eq_nav_bool[3]) eq_nav += "H";
            if (eq_nav_bool[4]) eq_nav += "K";
            if (eq_nav_bool[5]) eq_nav += "L";
            if (eq_nav_bool[6]) eq_nav += "O";
            if (eq_nav_bool[7]) eq_nav += "U";
            if (eq_nav_bool[8]) eq_nav += "V";
            if (eq_nav_bool[9]) eq_nav += "Z";
            json.put(KEY_EQ_COM_NAV, eq_nav);

            eq_ssr = "";
            if (eq_ssr_bool[0]) eq_ssr += "N";
            if (eq_ssr_bool[1]) eq_ssr += "A";
            if (eq_ssr_bool[2]) eq_ssr += "C";
            if (eq_ssr_bool[3]) eq_ssr += "E";
            if (eq_ssr_bool[4]) eq_ssr += "H";
            if (eq_ssr_bool[5]) eq_ssr += "I";
            if (eq_ssr_bool[6]) eq_ssr += "L";
            if (eq_ssr_bool[7]) eq_ssr += "X";
            if (eq_ssr_bool[8]) eq_ssr += "P";
            if (eq_ssr_bool[9]) eq_ssr += "S";
            json.put(KEY_EQ_SSR, eq_ssr);

            json.put(KEY_CR_SP, cruise_sp);
            json.put(KEY_CL_SP, climb_sp);
            json.put(KEY_DS_SP, descent_sp);
            json.put(KEY_CL_RT, climb_rate);
            json.put(KEY_DS_RT, desc_rate);
            json.put(KEY_FUEL_DENS, fuel_density);
            json.put(KEY_FUEL_FLOW, fuel_flow);
            json.put(KEY_MTW, max_takeoff);
            json.put(KEY_MLW, max_landing);
            json.put(KEY_EMPTY_W, empty_weight);
            json.put(KEY_EMPTY_A, empty_arm);

            json.put(KEY_ARM_IS_SAT, is_arm_sat);

            JSONArray jAW = new JSONArray();
            for (Tanks t: tanks){
                JSONArray j = new JSONArray();
                j.put(t.name);
                j.put(t.arm);
                j.put(t.max);
                j.put(t.unus);
                jAW.put(j);
            }
            for (Weights w: additional_weight){
                JSONArray j = new JSONArray();
                j.put(w.name);
                j.put(w.arm);
                j.put(w.max);
                jAW.put(j);
            }
            json.put(KEY_ADD_W, jAW);

            JSONArray jL = new JSONArray();
            for (Limits l: limits){
                JSONArray j = new JSONArray();
                j.put(l.arm);
                j.put(l.weight);
                jL.put(j);
            }
            json.put(KEY_LIMITS, jL);

            FileWriter writer = new FileWriter(file);
            writer.append(json.toString());
            writer.flush();
            writer.close();

        } catch (JSONException | IOException e1) {
            return false;
        }

        return true;
    }

    /**
     * Gives the filename without the .json extension
     * @return
     */
    public String getName(){
        String name = file.getName();
        int suffix = name.lastIndexOf('.');
        return (name.substring(0, suffix));
    }

    /**
     * Holds airplane Weight Data
     */
    public static class Weights{
        public String name;
        public double arm = 0;
        public double max = 0;
        public double actual = 0;
    }

    /**
     * Holds airplane fuel tank data
     */
    public static class Tanks{
        public String name;
        public double arm = 0;
        public double max = 0;
        public double unus = 0;
        public double actual = 0;
    }

    /**
     * Holds Airplane limit data point
     */
    public static class Limits{
        public double arm = 0;
        public double weight = 0;
    }

}
