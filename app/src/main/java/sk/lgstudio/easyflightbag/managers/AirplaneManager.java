package sk.lgstudio.easyflightbag.managers;

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

    private File file;

    public boolean loaded = false;

    public int flightTimeH = 0;
    public int flightTimeM = 0;

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
    public ArrayList<Weights> additional_weight;
    public ArrayList<Tanks> tanks;
    public ArrayList<Limits> limits;

    /**
     * Constructor - reads the .json file
     * @param f
     */
    public AirplaneManager(File f){
        file = f;
        FileInputStream is = null;

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

            additional_weight = new ArrayList<>();
            tanks = new ArrayList<>();
            limits = new ArrayList<>();

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
     * Saves the modified data int a .json file
     * @return
     */
    public boolean saveFile(){
        JSONObject json = new JSONObject();
        try {
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
