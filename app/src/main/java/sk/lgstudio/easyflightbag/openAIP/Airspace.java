package sk.lgstudio.easyflightbag.openAIP;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Parser for Airspace .aip file v1.1
 *
 * usage: new Airspace.Parser.parse(file)
 * returns ArrayList<Airspace.Data>
 */

public class Airspace {

    public static final short CATEGORY_A = 0;
    public static final short CATEGORY_B = 1;
    public static final short CATEGORY_C = 2;
    public static final short CATEGORY_CTR = 3;
    public static final short CATEGORY_D = 4;
    public static final short CATEGORY_DANGER = 5;
    public static final short CATEGORY_E = 6;
    public static final short CATEGORY_F = 7;
    public static final short CATEGORY_G = 8;
    public static final short CATEGORY_GLIDING = 9;
    public static final short CATEGORY_OTH = 10;
    public static final short CATEGORY_RESTRICTED = 11;
    public static final short CATEGORY_TMA = 12;
    public static final short CATEGORY_TMZ = 13;
    public static final short CATEGORY_WAVE = 14;
    public static final short CATEGORY_PROHIBITED = 15;
    public static final short CATEGORY_FIR = 16;
    public static final short CATEGORY_UIR = 17;
    public static final short CATEGORY_RMZ = 18;

    public static final short ALT_REF_GND = 19;
    public static final short ALT_REF_MSL = 20;
    public static final short ALT_REF_STD = 21;

    public static final short ALT_UNIT_F = 22;
    public static final short ALT_UNIT_FL = 23;


    public static class Parser {

        public ArrayList<Data> parse(File f) {
            try {
                ArrayList<Data> data = new ArrayList<>();
                Document doc = Jsoup.parse(f, null);
                Elements airspaces = doc.select("ASP");
                for(Element asp: airspaces){
                    Data d = new Data();

                    d.category = getCategory(asp.attr("CATEGORY"));
                    d.id = Integer.parseInt(asp.getElementsByTag("id").text());
                    d.name = asp.getElementsByTag("NAME").text();
                    d.country = asp.getElementsByTag("COUNTRY").text();

                    Element top = asp.getElementsByTag("ALTLIMIT_TOP").first();
                    d.altlimit_top = getReference(top.attr("REFERENCE"));
                    d.altlimit_top_unit = getUnit(top.getElementsByTag("ALT").attr("UNIT"));
                    d.altlimit_top_value = Integer.parseInt(top.getElementsByTag("ALT").text());

                    Element bottom = asp.getElementsByTag("ALTLIMIT_BOTTOM").first();
                    d.altlimit_bottom = getReference(bottom.attr("REFERENCE"));
                    d.altlimit_bottom_unit = getUnit(bottom.getElementsByTag("ALT").attr("UNIT"));
                    d.altlimit_bottom_value = Integer.parseInt(bottom.getElementsByTag("ALT").text());

                    d.polygon = getPolygon(asp.getElementsByTag("GEOMETRY").get(0).getElementsByTag("POLYGON").text());

                    data.add(d);
                }

                return data;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static ArrayList<LatLng> getPolygon(String str){
        ArrayList<LatLng> list = new ArrayList<>();

        String coord[] = str.split(",");
        for (int i = 0; i < coord.length; i++){
            int index = coord[i].lastIndexOf(" ");
            double lon = Double.valueOf(coord[i].substring(0,index));
            double lat = Double.valueOf(coord[i].substring(index+1));
            list.add(new LatLng(lat, lon));
        }

        return list;
    }

    private static short getCategory(String str){
        if (str.compareTo("A") == 0) return CATEGORY_A;
        if (str.compareTo("B") == 0) return CATEGORY_B;
        if (str.compareTo("C") == 0) return CATEGORY_C;
        if (str.compareTo("CTR") == 0) return CATEGORY_CTR;
        if (str.compareTo("D") == 0) return CATEGORY_D;
        if (str.compareTo("DANGER") == 0) return CATEGORY_DANGER;
        if (str.compareTo("E") == 0) return CATEGORY_E;
        if (str.compareTo("F") == 0) return CATEGORY_F;
        if (str.compareTo("G") == 0) return CATEGORY_G;
        if (str.compareTo("GLIDING") == 0) return CATEGORY_GLIDING;
        if (str.compareTo("RESTRICTED") == 0) return CATEGORY_RESTRICTED;
        if (str.compareTo("TMA") == 0) return CATEGORY_TMA;
        if (str.compareTo("TMZ") == 0) return CATEGORY_TMZ;
        if (str.compareTo("WAVE") == 0) return CATEGORY_WAVE;
        if (str.compareTo("PROHIBITED") == 0) return CATEGORY_PROHIBITED;
        if (str.compareTo("FIR") == 0) return CATEGORY_FIR;
        if (str.compareTo("UIR") == 0) return CATEGORY_UIR;
        if (str.compareTo("RMZ") == 0) return CATEGORY_RMZ;
        if (str.compareTo("OTH") == 0) return CATEGORY_OTH;
        return -1;
    }

    public static String getCategory(short c){
        if (c == CATEGORY_A) return "A";
        if (c == CATEGORY_B) return "B";
        if (c == CATEGORY_C) return "C";
        if (c == CATEGORY_CTR) return "CTR";
        if (c == CATEGORY_D) return "D";
        if (c == CATEGORY_DANGER) return "Danger";
        if (c == CATEGORY_E) return "E";
        if (c == CATEGORY_F) return "F";
        if (c == CATEGORY_FIR) return "FIR";
        if (c == CATEGORY_G) return "G";
        if (c == CATEGORY_GLIDING) return "Gliding";
        if (c == CATEGORY_RESTRICTED) return "Restricted";
        if (c == CATEGORY_RMZ) return "RMZ";
        if (c == CATEGORY_TMA) return "TMA";
        if (c == CATEGORY_TMZ) return "TMZ";
        if (c == CATEGORY_WAVE) return "Wave";
        if (c == CATEGORY_PROHIBITED) return "Prohibited";
        if (c == CATEGORY_UIR) return "UIR";
        if (c == CATEGORY_OTH) return "Other";
        return "";
    }

    private static short getReference(String str){
        if (str.compareTo("GND") == 0) return ALT_REF_GND;
        if (str.compareTo("STD") == 0) return ALT_REF_STD;
        if (str.compareTo("MSL") == 0) return ALT_REF_MSL;
        return -1;
    }

    public static String getReference(short r){
        if (r == ALT_REF_GND) return "GND";
        if (r == ALT_REF_STD) return "STD";
        if (r == ALT_REF_MSL) return "MSL";
        return "";
    }

    private static short getUnit(String str){
        if (str.compareTo("F") == 0) return ALT_UNIT_F;
        if (str.compareTo("FL") == 0) return ALT_UNIT_FL;
        return -1;
    }

    public static String getUnit(short u){
        if (u == ALT_UNIT_F) return "f";
        if (u == ALT_UNIT_FL) return "fl";
        return "";
    }

    public static class Data{
        public short category;
        public int id;
        public String country;
        public String name;
        public short altlimit_top;
        public short altlimit_top_unit;
        public int altlimit_top_value;
        public short altlimit_bottom;
        public short altlimit_bottom_unit;
        public int altlimit_bottom_value;
        public ArrayList<LatLng> polygon = null;
    }

}
