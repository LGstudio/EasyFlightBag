package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;

/**
 * Created by LGstudio on 2017-02-09.
 */

public class WbGraphDialog extends Dialog {

    public WbGraphDialog(Context context) {
        super(context);
    }

    public void loadContent(AirplaneManager airplane){

        // getting graph colors
        Resources.Theme theme = getContext().getTheme();
        TypedValue textColor = new TypedValue();
        theme.resolveAttribute(R.attr.primaryTextColor, textColor, true);
        TypedValue limitsColor = new TypedValue();
        theme.resolveAttribute(R.attr.colorAccent, limitsColor, true);
        TypedValue mtowColor = new TypedValue();
        theme.resolveAttribute(R.attr.redColor, mtowColor, true);
        TypedValue mlwColor = new TypedValue();
        theme.resolveAttribute(R.attr.orangeColor, mlwColor, true);

        // setting text view
        TextView txtTime = (TextView) findViewById(R.id.wb_flight_time);
        txtTime.setText(getContext().getString(R.string.calc_fly_time) + ": " + String.valueOf(airplane.flightTimeH) + "h" + String.valueOf(airplane.flightTimeM));
        double consumption = airplane.fuel_flow * airplane.flightTimeH + airplane.fuel_flow * (airplane.flightTimeM/60);
        TextView fuelCons = (TextView) findViewById(R.id.wb_flight_fuel);
        fuelCons.setText(getContext().getString(R.string.calc_fuel_consumption)+ ": " + String.valueOf(consumption) + "l");

        TextView txtTakeoffWeight = (TextView) findViewById(R.id.wb_flight_takeoff_weight);
        TextView txtTakeoffArm = (TextView) findViewById(R.id.wb_flight_takeoff_arm);

        TextView txtLandingWeight = (TextView) findViewById(R.id.wb_flight_landing_weight);
        TextView txtLandingArm = (TextView) findViewById(R.id.wb_flight_landing_arm);

        TextView txtWarning = (TextView) findViewById(R.id.wb_flight_warning);
        String warning = "";

        // graph ====================================================================================
        LineChartView chart = (LineChartView) findViewById(R.id.wb_chart);

        List<Line> lines = new ArrayList<>();


        double min_arm = Double.MAX_VALUE;
        double max_arm = Double.MIN_VALUE;

        // --- Limits ---
        List<PointValue> pointsLimits = new ArrayList<>();
        for (int i = 0; i < airplane.limits.size(); i++){
            double arm = airplane.limits.get(i).arm;
            double weight = airplane.limits.get(i).weight;
            if (arm < min_arm) min_arm = arm;
            if (arm > max_arm) max_arm = arm;
            pointsLimits.add(new PointValue((float) arm, (float) weight));
        }
        pointsLimits.add(new PointValue((float) airplane.limits.get(0).arm, (float) airplane.limits.get(0).weight));
        Line lineLimits = new Line(pointsLimits);
        lineLimits.setColor(limitsColor.data);
        lineLimits.setCubic(false);
        lineLimits.setFilled(true);;
        lineLimits.setHasPoints(false);
        lines.add(lineLimits);

        // MTOW
        List<PointValue> pointsMtow = new ArrayList<>();
        pointsMtow.add(new PointValue((float) min_arm, (float) airplane.max_takeoff).setLabel(getContext().getString(R.string.calc_mtow)));
        pointsMtow.add(new PointValue((float) max_arm, (float) airplane.max_takeoff));
        Line lineMtow = new Line(pointsMtow);
        lineMtow.setColor(mtowColor.data);
        lineMtow.setCubic(false);
        lineMtow.setHasPoints(false);
        lines.add(lineMtow);

        // MTOW
        List<PointValue> pointsMlw = new ArrayList<>();
        pointsMlw.add(new PointValue((float) min_arm, (float) airplane.max_landing).setLabel(getContext().getString(R.string.calc_mlw)));
        pointsMlw.add(new PointValue((float) max_arm, (float) airplane.max_landing));
        Line lineMlw = new Line(pointsMlw);
        lineMlw.setColor(mlwColor.data);
        lineMlw.setCubic(false);
        lineMlw.setHasPoints(false);
        lines.add(lineMlw);

        LineChartData data = new LineChartData(lines);

        // AXIS
        Axis axisArm = new Axis();
        axisArm.setName(getContext().getString(R.string.calc_arm_mm));
        axisArm.setLineColor(textColor.data);
        axisArm.setTextColor(textColor.data);
        axisArm.setHasLines(true);
        axisArm.setHasSeparationLine(true);
        data.setAxisXBottom(axisArm);


        Axis axisWeight = new Axis();
        axisWeight.setName(getContext().getString(R.string.calc_weight_kg));
        axisWeight.setLineColor(textColor.data);
        axisWeight.setTextColor(textColor.data);
        axisWeight.setHasLines(true);
        axisWeight.setHasSeparationLine(true);
        data.setAxisYLeft(axisWeight);

        chart.setLineChartData(data);

        txtWarning.setText(warning);

    }
}
