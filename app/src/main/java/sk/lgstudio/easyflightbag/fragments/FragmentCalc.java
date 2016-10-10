package sk.lgstudio.easyflightbag.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.calculations.CalculatorData;
import sk.lgstudio.easyflightbag.calculations.CalculatorView;

/**
 *
 */
public class FragmentCalc extends Fragment implements View.OnClickListener {

    private final int[] idLayout = {
            R.layout.calc_distance,
            R.layout.calc_speed,
            R.layout.calc_mass,
            R.layout.calc_volume,
            R.layout.calc_pressure,
            R.layout.calc_temperature,
            R.layout.calc_fuel,
            R.layout.calc_coord,
            R.layout.calc_time,
            R.layout.calc_tas,
            R.layout.calc_f_fuel,
            R.layout.calc_wind};

    private final int[] idMenu = {
            R.id.calc_conv_dist,
            R.id.calc_conv_speed,
            R.id.calc_conv_mass,
            R.id.calc_conv_vol,
            R.id.calc_conv_press,
            R.id.calc_conv_temp,
            R.id.calc_conv_fuel,
            R.id.calc_func_coord,
            R.id.calc_func_time,
            R.id.calc_func_tas,
            R.id.calc_func_fuel,
            R.id.calc_func_wind
    };

    private final CalculatorView[] calcFunctions = {
            new CalculatorView(CalculatorData.dstRatios, CalculatorData.dstValues, CalculatorData.dstLayout),
            new CalculatorView(CalculatorData.speRatios, CalculatorData.speValues, CalculatorData.speLayout)
    };

    private ArrayList<TextView> menu = new ArrayList<>();
    private ArrayList<View> calcView =  new ArrayList<>();

    private int openedViewId = idMenu[0];
    private int openedView = 0;
    private ScrollView frame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calc, container, false);

        menu.clear();
        calcView.clear();

        for (int id: idMenu){
            TextView tv = (TextView) view.findViewById(id);
            tv.setOnClickListener(this);
            menu.add(tv);
        }

        frame = (ScrollView) view.findViewById(R.id.calc_frame);

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

    private void addCalcView(int id){
        if (calcFunctions.length > openedView){
            calcFunctions[openedView].detachView();
        }
        frame.removeAllViewsInLayout();

        for (int i = 0; i < idMenu.length; i++) {
            if (idMenu[i] == id) {
                openedView = i;
                View v = calcView.get(i);
                if (calcFunctions.length > i){// TODO: remove condition when all convertors done
                    calcFunctions[i].initView(v);
                }
                frame.addView(v);
                menu.get(i).setBackgroundResource(R.drawable.bck_item_selected);
            }
            else if (idMenu[i] == openedViewId){
                menu.get(i).setBackgroundResource(R.drawable.bck_transparent);
            }
        }
    }
}