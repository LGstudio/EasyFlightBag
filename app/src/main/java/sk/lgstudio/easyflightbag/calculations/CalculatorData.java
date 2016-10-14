package sk.lgstudio.easyflightbag.calculations;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by L on 16/10/10.
 */

public final class CalculatorData {

    /**
     *  ======================================================================
     *      DISTANCE
     */

    public static final float[] dstRatios = {
            1f,                     // Meter to Meter
            3.2808410892388f,       // Meter to Feet
            0.000539957f,           // Meter to Nautical Mile
            0.000621371f,           // Meter to Mile
            1.0936136964129334892f, // Meter to yard
            39.370093070865607388f  // Meter to Inch
    };

    public static final float[] dstRatiosToKm = {
            1f,                     // KiloMeter to Meter
            3280.8410892388f,       // KiloMeter to Feet
            0.539957f,           // KiloMeter to Nautical Mile
            0.621371f           // KiloMeter to Mile
    };

    public static final int[] dstLayout = {
            R.id.calc_unit_text_m,
            R.id.calc_unit_text_ft,
            R.id.calc_unit_text_nm,
            R.id.calc_unit_text_mi,
            R.id.calc_unit_text_yd,
            R.id.calc_unit_text_in
    };

    public static final float[] dstValues = {0f, 0f, 0f, 0f, 0f, 0f};

    /**
     *  ======================================================================
     *      SPEED
     */

    public static final float[] speRatios = {
            1f,                     // Km/h to Km/h
            0.539957f,              // Km/h to Knot
            0.621371f,              // Km/h to Mph
            54.6807f                // Km/h to Ft/min
    };

    public static final int[] speLayout = {
            R.id.calc_unit_text_kmh,
            R.id.calc_unit_text_kn,
            R.id.calc_unit_text_mph,
            R.id.calc_unit_text_ftpmin
    };

    public static final float[] speValues = {0f, 0f, 0f, 0f};

    /**
     *  ======================================================================
     *      MASS
     */

    public static final float[] masRatios = {
            1f,                     // Kg to Kg
            0.001f,                 // Kg to Tonne
            2.20462f                // Kg to Pound
    };

    public static final int[] masLayout = {
            R.id.calc_unit_text_kg,
            R.id.calc_unit_text_t,
            R.id.calc_unit_text_lb
    };

    public static final float[] masValues = {0f, 0f, 0f };

    /**
     *  ======================================================================
     *      VOLUME
     */

    public static final float[] volRatios = {
            1f,                     // l to l
            0.001f,                 // l to cubic meter
            0.264172f,              // l to US gallon
            0.219969f,              // l to Imperial gallon
            33.814f                 // l to fl. ounce
    };

    public static final int[] volLayout = {
            R.id.calc_unit_text_l,
            R.id.calc_unit_text_m_cube,
            R.id.calc_unit_text_g_us,
            R.id.calc_unit_text_g_imp,
            R.id.calc_unit_text_oz
    };

    public static final float[] volValues = {0f, 0f, 0f, 0f, 0f };

    /**
     *  ======================================================================
     *      PRESSURE
     */

    public static final float[] preRatios = {
            1f,                     // hPa to hPa
            0.000986923f,           // hPa to atmosphere
            0.02953f                // hPa to inches of mercury
    };

    public static final int[] preLayout = {
            R.id.calc_unit_text_hpa_bma,
            R.id.calc_unit_text_atm,
            R.id.calc_unit_text_inhg,
    };

    public static final float[] preValues = {0f, 0f, 0f};

    /**
     *  ======================================================================
     *      TEMPARATURE
     */

    public static final int[] temLayout = {
            R.id.calc_unit_text_c,
            R.id.calc_unit_text_f,
            R.id.calc_unit_text_k,
    };

    public static final float[] temValues = {0f, 32f, 273.15f};

    /**
     *  ======================================================================
     *      FUEL / OIL
     */

    public static final int[] oilLayout = {
            R.id.calc_unit_text_fu_dens,
            R.id.calc_unit_text_fu_l,
            R.id.calc_unit_text_fu_kg,
            R.id.calc_unit_text_fu_usg,
            R.id.calc_unit_text_fu_impg,
            R.id.calc_unit_text_fu_lb
    };

    public static final float[] oilValues = {0f, 0f, 0f, 0f, 0f, 0f};

    /**
     *  ======================================================================
     *      TIME / SPEED / DISTANCE
     */

    public static final int[] timeLayout = {
            R.id.calc_unit_text_fu_dens,
            R.id.calc_unit_text_fu_l
    };

    public static final float[] timeValues = {0f, 0f, 0f};


}
