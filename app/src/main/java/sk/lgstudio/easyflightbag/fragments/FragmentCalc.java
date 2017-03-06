package sk.lgstudio.easyflightbag.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.Inflater;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.calculations.CalculatorData;
import sk.lgstudio.easyflightbag.calculations.CalculatorFuelOil;
import sk.lgstudio.easyflightbag.calculations.CalculatorTemperature;
import sk.lgstudio.easyflightbag.calculations.Calculator;
import sk.lgstudio.easyflightbag.calculations.CalculatorTime;
import sk.lgstudio.easyflightbag.calculations.CalculatorWB;
import sk.lgstudio.easyflightbag.calculations.CalculatorWind;

/**
 *
 */
public class FragmentCalc extends Fragment implements View.OnClickListener {

    private final int[] idLayout = {
            R.layout.calc_wb,
            R.layout.calc_time,
            R.layout.calc_wind,
            R.layout.calc_fuel,
            R.layout.calc_distance,
            R.layout.calc_speed,
            R.layout.calc_mass,
            R.layout.calc_volume,
            R.layout.calc_pressure,
            R.layout.calc_temperature
    };

    private final int[] idMenu = {
            R.id.calc_WB,
            R.id.calc_func_time,
            R.id.calc_func_wind,
            R.id.calc_conv_fuel,
            R.id.calc_conv_dist,
            R.id.calc_conv_speed,
            R.id.calc_conv_mass,
            R.id.calc_conv_vol,
            R.id.calc_conv_press,
            R.id.calc_conv_temp
    };

    private ArrayList<Calculator> calcFunctions = null;
    private ArrayList<TextView> menu = new ArrayList<>();
    private ArrayList<View> calcView =  new ArrayList<>();

    private int openedViewId = idMenu[0];
    private int openedView = 0;
    private FrameLayout frame;

    public File folder;
    public SharedPreferences prefs;
    private LayoutInflater layoutInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_calc, container, false);

        if (calcFunctions == null)
            initCalculators();

        menu.clear();
        calcView.clear();

        for (int id: idMenu){
            TextView tv = (TextView) view.findViewById(id);
            tv.setOnClickListener(this);
            menu.add(tv);
        }

        frame = (FrameLayout) view.findViewById(R.id.calc_frame);

        for (int id: idLayout){
            View v = inflater.inflate(id, frame, false);
            calcView.add(v);
        }

        addCalcView(openedViewId);
        return view;
    }


    @Override
    public void onDestroyView(){
        frame.removeAllViewsInLayout();
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {

        int newlyOpened = v.getId();
        addCalcView(newlyOpened);
        openedViewId = newlyOpened;

    }

    private void initCalculators(){
        calcFunctions = new ArrayList<>();

        calcFunctions.add(new CalculatorWB(prefs, getActivity(), layoutInflater, folder));
        calcFunctions.add(new CalculatorTime(null, CalculatorData.timeValues, CalculatorData.timeLayout));
        calcFunctions.add(new CalculatorWind(getContext()));
        calcFunctions.add(new CalculatorFuelOil(null, CalculatorData.oilValues, CalculatorData.oilLayout));
        calcFunctions.add(new Calculator(CalculatorData.dstRatios, CalculatorData.dstValues, CalculatorData.dstLayout));
        calcFunctions.add(new Calculator(CalculatorData.speRatios, CalculatorData.speValues, CalculatorData.speLayout));
        calcFunctions.add(new Calculator(CalculatorData.masRatios, CalculatorData.masValues, CalculatorData.masLayout));
        calcFunctions.add(new Calculator(CalculatorData.volRatios, CalculatorData.volValues, CalculatorData.volLayout));
        calcFunctions.add(new Calculator(CalculatorData.preRatios, CalculatorData.preValues, CalculatorData.preLayout));
        calcFunctions.add(new CalculatorTemperature(null, CalculatorData.temValues, CalculatorData.temLayout));
    }

    private void addCalcView(int id){
        if (calcFunctions.size() > openedView){
            calcFunctions.get(openedView).detachView();
        }
        frame.removeAllViewsInLayout();

        for (int i = 0; i < idMenu.length; i++) {
            if (idMenu[i] == id) {
                openedView = i;
                View v = calcView.get(i);
                frame.addView(v);
                calcFunctions.get(i).initView(v);
                menu.get(i).setBackgroundResource(R.drawable.bck_item_selected);
            }
            else if (idMenu[i] == openedViewId){
                menu.get(i).setBackgroundResource(R.drawable.bck_transparent);
            }
        }
    }
}