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
 * Created by LGstudio on 2017-02-07.
 */

public class AirplaneManager {

    private File file;

    public boolean loaded = false;

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

            cruise_sp = json.getDouble("cruise_sp");
            climb_sp = json.getDouble("climb_sp");
            descent_sp = json.getDouble("descent_sp");
            climb_rate = json.getDouble("climb_rate");
            desc_rate = json.getDouble("desc_rate");
            fuel_density = json.getDouble("fuel_density");
            fuel_flow = json.getDouble("fuel_flow");
            max_takeoff = json.getDouble("max_takeoff");
            max_landing = json.getDouble("max_landing");
            empty_weight = json.getDouble("empty_weight");
            empty_arm = json.getDouble("empty_arm");

            additional_weight = new ArrayList<>();
            tanks = new ArrayList<>();
            limits = new ArrayList<>();

            JSONArray jAW = json.getJSONArray("additional_weight");
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

            JSONArray jL = json.getJSONArray("limits");
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

    public boolean saveFile(){
        JSONObject json = new JSONObject();
        try {
            json.put("cruise_sp", cruise_sp);
            json.put("climb_sp", climb_sp);
            json.put("descent_sp", descent_sp);
            json.put("climb_rate", climb_rate);
            json.put("desc_rate", desc_rate);
            json.put("fuel_density", fuel_density);
            json.put("fuel_flow", fuel_flow);
            json.put("max_takeoff", max_takeoff);
            json.put("max_landing", max_landing);
            json.put("empty_weight", empty_weight);
            json.put("empty_arm", empty_arm);

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
            json.put("additional_weight", jAW);

            JSONArray jL = new JSONArray();
            for (Limits l: limits){
                JSONArray j = new JSONArray();
                j.put(l.arm);
                j.put(l.weight);
                jL.put(j);
            }
            json.put("limits", jL);

            FileWriter writer = new FileWriter(file);
            writer.append(json.toString());
            writer.flush();
            writer.close();

        } catch (JSONException | IOException e1) {
            return false;
        }

        return true;
    }

    public String getName(){
        String name = file.getName();
        int suffix = name.lastIndexOf('.');
        return (name.substring(0, suffix));
    }

    public static class Weights{
        public String name;
        public double arm = 0;
        public double max = 0;
        public double actual = 0;
    }

    public static class Tanks{
        public String name;
        public double arm = 0;
        public double max = 0;
        public double unus = 0;
        public double actual = 0;
    }

    public static class Limits{
        public double arm = 0;
        public double weight = 0;
    }

}
