package sk.lgstudio.easyflightbag.openAIP;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by LGstudio on 2017-02-23.
 */

public class Airport {

    public static final short APT_TYPE_CLOSED = 0;
    public static final short APT_TYPE_MIL = 1;
    public static final short APT_TYPE_CIVIL = 2;
    public static final short APT_TYPE_MIL_CIVIL = 3;
    public static final short APT_TYPE_WATER = 4;
    public static final short APT_TYPE_APT = 5;
    public static final short APT_TYPE_GLIDING = 6;
    public static final short APT_TYPE_HELI_CIVIL = 7;
    public static final short APT_TYPE_HELI_MIL = 8;
    public static final short APT_TYPE_INT = 9;
    public static final short APT_TYPE_LIGHT = 10;

    public static final short RADIO_CAT_COMM = 11;
    public static final short RADIO_CAT_INF = 12;
    public static final short RADIO_CAT_NAV = 13;
    public static final short RADIO_CAT_OTH = 14;

    public static final short RWY_CLOSED = 15;
    public static final short RWY_CLOSED_TEMP = 16;
    public static final short RWY_ACTIVE = 17;

    public static final short RWY_SFC_ASPH = 18;
    public static final short RWY_SFC_CONC = 19;
    public static final short RWY_SFC_GRAS = 20;
    public static final short RWY_SFC_GRVL = 21;
    public static final short RWY_SFC_ICE = 22;
    public static final short RWY_SFC_SAND = 23;
    public static final short RWY_SFC_SNOW = 24;
    public static final short RWY_SFC_SOIL = 25;
    public static final short RWY_SFC_WATE = 26;
    public static final short RWY_SFC_UNKN = 27;

    public static final short RWY_STRENGTH_PCN = 28;
    public static final short RWY_STRENGTH_MPW = 29;

    public static class Parser {

        public ArrayList<Airport.Data> parse(File f) {
            try {
                ArrayList<Airport.Data> data = new ArrayList<>();
                Document doc = Jsoup.parse(f, null);
                Elements airports = doc.select("AIRPORT");
                for(Element ap: airports){
                    Airport.Data d = new Airport.Data();

                    d.type = getAptType(ap.attr("TYPE"));
                    d.country = ap.getElementsByTag("COUNTRY").text();
                    d.name = ap.getElementsByTag("NAME").text();
                    d.icao = ap.getElementsByTag("ICAO").text();
                    Element geo = ap.getElementsByTag("GEOLOCATION").first();
                    d.location = new LatLng(Double.parseDouble(geo.getElementsByTag("LAT").text()), Double.parseDouble(geo.getElementsByTag("LON").text()));
                    d.elevation = Float.parseFloat(geo.getElementsByTag("ELEV").text());

                    Elements radios = ap.getElementsByTag("RADIO");
                    d.radios = new ArrayList<>();
                    for (Element r: radios){
                        Radio radio = new Radio();
                        radio.category = getRadioCategory(r.attr("CATEGORY"));
                        radio.description = r.getElementsByTag("DESCRIPTION").text();
                        radio.specification = r.getElementsByTag("TYPESPEC").text();

                        String freq = r.getElementsByTag("FREQUENCY").text();
                        if (freq.length() > 0) radio.frequency = Float.parseFloat(freq);
                        else radio.frequency = 0.0f;
                        radio.type = r.getElementsByTag("TYPE").text();
                        d.radios.add(radio);
                    }

                    Elements rwys = ap.getElementsByTag("RWY");
                    d.runways = new ArrayList<>();
                    for (Element r: rwys){
                        Runway rwy = new Runway();
                        rwy.operations = getRwyOperations(r.attr("OPERATIONS"));
                        rwy.name = r.getElementsByTag("NAME").text();
                        rwy.sfc = getRwySurface(r.attr("SFC"));
                        rwy.length = Float.parseFloat(r.getElementsByTag("LENGTH").text());
                        rwy.width = Float.parseFloat(r.getElementsByTag("WIDTH").text());
                        rwy.strength_value = r.getElementsByTag("STRENGTH").text();
                        rwy.strength_unit = getRwyStrength(r.getElementsByTag("STRENGTH").attr("UNIT"));

                        Elements directions = r.getElementsByTag("DIRECTIONS");
                        rwy.directions = new ArrayList<>();
                        for (Element direction: directions){
                            Direction dir = new Direction();
                            dir.tc = Short.parseShort(direction.attr("TC"));
                            dir.land_ils = Float.parseFloat(direction.getElementsByTag("LANDINGS").first().getElementsByTag("ILS").text());
                            dir.land_papi = getPapi(direction.getElementsByTag("LANDINGS").first().getElementsByTag("PAPI").text());
                            dir.runs_tora = Short.parseShort(direction.getElementsByTag("RUNS").first().getElementsByTag("TORA").text());
                            dir.runs_lda = Short.parseShort(direction.getElementsByTag("RUNS").first().getElementsByTag("LDA").text());
                            rwy.directions.add(dir);
                        }

                        d.runways.add(rwy);
                    }


                    data.add(d);
                }

                return data;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static short getAptType(String str){
        if (str.compareTo("AD_CLOSED") == 0) return APT_TYPE_CLOSED;
        if (str.compareTo("AD_MIL") == 0) return APT_TYPE_MIL;
        if (str.compareTo("AF_CIVIL") == 0) return APT_TYPE_CIVIL;
        if (str.compareTo("AF_MIL_CIVIL") == 0) return APT_TYPE_MIL_CIVIL;
        if (str.compareTo("AF_WATER") == 0) return APT_TYPE_WATER;
        if (str.compareTo("APT") == 0) return APT_TYPE_APT;
        if (str.compareTo("GLIDING") == 0) return APT_TYPE_GLIDING;
        if (str.compareTo("HELI_CIVIL") == 0) return APT_TYPE_HELI_CIVIL;
        if (str.compareTo("HELI_MIL") == 0) return APT_TYPE_HELI_MIL;
        if (str.compareTo("INTL_APT") == 0) return APT_TYPE_INT;
        if (str.compareTo("LIGHT_AIRCRAFT") == 0) return APT_TYPE_LIGHT;
        return -1;
    }

    public static String getAptType(short t){
        if (t == APT_TYPE_APT) return "Airfield";
        if (t == APT_TYPE_CIVIL) return "Civil";
        if (t == APT_TYPE_CLOSED) return "Closed";
        if (t == APT_TYPE_GLIDING) return "Glider Site";
        if (t == APT_TYPE_HELI_CIVIL) return "Heliport Civil";
        if (t == APT_TYPE_HELI_MIL) return "Heliport Military";
        if (t == APT_TYPE_INT) return "International";
        if (t == APT_TYPE_MIL) return "Military";
        if (t == APT_TYPE_MIL_CIVIL) return "Airfield (Military/Civil)";
        if (t == APT_TYPE_LIGHT) return "Ultra Light Flying Site";
        if (t == APT_TYPE_WATER) return "Water";
        return "";
    }

    private static short getRadioCategory(String str) {
        if (str.compareTo("COMMUNICATION") == 0) return RADIO_CAT_COMM;
        if (str.compareTo("INFORMATION") == 0) return RADIO_CAT_INF;
        if (str.compareTo("NAVIGATION") == 0) return RADIO_CAT_NAV;
        if (str.compareTo("OTHER") == 0) return RADIO_CAT_OTH;
        return -1;
    }

    public static String getRadioCategory(short r){
        if (r == RADIO_CAT_COMM) return "Communication";
        if (r == RADIO_CAT_INF) return "Information";
        if (r == RADIO_CAT_NAV) return "Navigation";
        if (r == RADIO_CAT_OTH) return "Other";
        return "";
    }

    private static short getRwyOperations(String str) {
        if (str.compareTo("ACTIVE") == 0) return RWY_ACTIVE;
        if (str.compareTo("TEMPORARILY_CLOSED") == 0) return RWY_CLOSED_TEMP;
        if (str.compareTo("CLOSED") == 0) return RWY_CLOSED;
        return -1;
    }

    public static String getRwyOperations(short r){
        if (r == RWY_ACTIVE) return "Active";
        if (r == RWY_CLOSED) return "Closed";
        if (r == RWY_CLOSED_TEMP) return "Temporarily Closed";
        return "";
    }

    private static short getRwySurface(String str) {
        if (str.compareTo("ASPH") == 0) return RWY_SFC_ASPH;
        if (str.compareTo("CONC") == 0) return RWY_SFC_CONC;
        if (str.compareTo("GRAS") == 0) return RWY_SFC_GRAS;
        if (str.compareTo("GRVL") == 0) return RWY_SFC_GRVL;
        if (str.compareTo("ICE") == 0) return RWY_SFC_ICE;
        if (str.compareTo("SAND") == 0) return RWY_SFC_SAND;
        if (str.compareTo("SNOW") == 0) return RWY_SFC_SNOW;
        if (str.compareTo("SOIL") == 0) return RWY_SFC_SOIL;
        if (str.compareTo("UNKN") == 0) return RWY_SFC_UNKN;
        if (str.compareTo("WATE") == 0) return RWY_SFC_WATE;
        return -1;
    }

    public String getRwySurface(short s){
        if (s == RWY_SFC_ASPH) return "Asphalt";
        if (s == RWY_SFC_CONC) return "Concrete";
        if (s == RWY_SFC_GRAS) return "Grass";
        if (s == RWY_SFC_GRVL) return "Gravel";
        if (s == RWY_SFC_ICE) return "Ice";
        if (s == RWY_SFC_SAND) return "Sand";
        if (s == RWY_SFC_SNOW) return "Snow";
        if (s == RWY_SFC_SOIL) return "Soil";
        if (s == RWY_SFC_UNKN) return "Unknown";
        if (s == RWY_SFC_WATE) return "Water";
        return "";
    }

    private static short getRwyStrength(String str) {
        if (str.compareTo("PCN") == 0) return RWY_STRENGTH_PCN;
        if (str.compareTo("MPW") == 0) return RWY_STRENGTH_MPW;
        return -1;
    }

    private static boolean getPapi(String str) {
        if (str.compareTo("TRUE") == 0) return true;
        return false;
    }

    public static class Data{
        public short type;
        public String name;
        public String country;
        public String icao;
        public LatLng location;
        public float elevation;
        public ArrayList<Radio> radios = null;
        public ArrayList<Runway> runways = null;

    }

    public static class Radio{
        public short category;
        public float frequency;
        public String type;
        public String specification;
        public String description;
    }

    public static class Runway{
        public short operations;
        public String name;
        public short sfc;
        public float length;
        public float width;
        public short strength_unit;
        public String strength_value;
        public ArrayList<Direction> directions;
    }

    public static class Direction{
        public short tc;
        public short runs_tora = 0;
        public short runs_lda = 0;
        public float land_ils = 0;
        public boolean land_papi = false;

    }

}
