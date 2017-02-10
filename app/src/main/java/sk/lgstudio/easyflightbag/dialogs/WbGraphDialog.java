package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
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

public class WbGraphDialog extends Dialog implements View.OnClickListener {

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
        TypedValue calcColor = new TypedValue();
        theme.resolveAttribute(R.attr.greenColor, calcColor, true);

        // setting text view
        TextView txtTime = (TextView) findViewById(R.id.wb_flight_time);
        txtTime.setText(getContext().getString(R.string.calc_fly_time) + ": " + String.valueOf(airplane.flightTimeH) + "h" + String.valueOf(airplane.flightTimeM));
        double consumptionLeft = airplane.fuel_flow * airplane.flightTimeH + airplane.fuel_flow * (airplane.flightTimeM/60);
        TextView fuelCons = (TextView) findViewById(R.id.wb_flight_fuel);
        fuelCons.setText(getContext().getString(R.string.calc_fuel_consumption)+ ": " + String.valueOf(consumptionLeft) + "l");
        ImageButton btn = (ImageButton) findViewById(R.id.wb_close);
        btn.setOnClickListener(this);

        TableLayout summary = (TableLayout) findViewById(R.id.wb_flight_summary);

        // error initialization
        TextView txtWarning = (TextView) findViewById(R.id.wb_flight_warning);
        boolean isWarning = false;
        String strWarning = "";

        // base calculation ---------------------------------------------------------------------
        double baseWeight = airplane.empty_weight;
        double baseMoment = airplane.empty_arm * airplane.empty_weight;
        for (AirplaneManager.Weights w : airplane.additional_weight){
            baseWeight += w.actual;
            baseMoment += w.arm * w.actual;
        }

        // Fuel & WB calculation ===================================================================
        List<PointValue> pointsCalculated = new ArrayList<>();
        ArrayList<PointValue> fuelPoints = new ArrayList<>();
        ArrayList<Integer> fuelSwitch = new ArrayList<>();

        double takeoffWeight = baseWeight;
        double takeoffMoment = baseMoment;
        double landingWeight = baseWeight;
        double landingMoment = baseMoment;

        boolean switched = false;
        for (int i = airplane.tanks.size(); i > 0; i--){
            if (consumptionLeft <= 0) break;

            if (switched){
                switched = false;
                fuelSwitch.add(i-1);
            }

            AirplaneManager.Tanks t = airplane.tanks.get(i-1);

            double fuelWeight = t.actual * airplane.fuel_density;
            takeoffWeight += fuelWeight;
            takeoffMoment += fuelWeight * t.arm;


            double consumedFuel = t.actual - consumptionLeft;
            if (t.unus > consumedFuel){ // there is still fuel needed
                consumedFuel = t.actual - t.unus;
                fuelWeight = t.unus * airplane.fuel_density;
                landingWeight += fuelWeight;
                landingMoment += fuelWeight * t.arm;

                if (consumedFuel > 0){
                    double emptyArm = landingMoment/landingWeight;
                    fuelPoints.add(new PointValue((float) emptyArm, (float) landingWeight));
                    switched = true;
                }
            }
            else { // tank contains enough fuel
                fuelWeight = consumedFuel * airplane.fuel_density;
                landingWeight += fuelWeight;
                landingMoment += fuelWeight * t.arm;
            }
            consumptionLeft -= consumedFuel;

        }

        // take off ----------------------------
        double takeoffArm = takeoffMoment/takeoffWeight;
        pointsCalculated.add(new PointValue((float) takeoffArm, (float) takeoffWeight));
        if (takeoffWeight > airplane.max_takeoff){
            isWarning = true;
            strWarning += getContext().getString(R.string.calc_warning) + " " + getContext().getString(R.string.calc_warning_takeoff_offlimit) + "\n";
        }
        summary.addView(createRow(getContext().getString(R.string.calc_takeoff), takeoffWeight, takeoffArm));

        // fuel --------------------------------
        for (int i = 0 ; i < fuelPoints.size(); i++){
            pointsCalculated.add(fuelPoints.get(i));
            String txtSwitch = getContext().getString(R.string.calc_switch) + " " + airplane.tanks.get(fuelSwitch.get(i)).name;
            summary.addView(createRow(txtSwitch, fuelPoints.get(i).getY(), fuelPoints.get(i).getX()));

        }

        // landing -----------------------------
        double landingArm = landingMoment / landingWeight;
        pointsCalculated.add(new PointValue((float) landingArm, (float) landingWeight));
        String landingStr = getContext().getString(R.string.calc_landing);
        if (consumptionLeft > 0){
            isWarning = true;
            strWarning += getContext().getString(R.string.calc_warning) + " " + getContext().getString(R.string.calc_warning_out_of_fuel) + "\n";
            landingStr = getContext().getString(R.string.calc_out_of_fuel);
        }
        else if (landingWeight > airplane.max_landing){
            isWarning = true;
            strWarning += getContext().getString(R.string.calc_warning) + " " + getContext().getString(R.string.calc_warning_landing_offlimit) + "\n";
        }
        summary.addView(createRow(landingStr, landingWeight, landingArm));

        // graph ===================================================================================
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

        // MLW
        List<PointValue> pointsMlw = new ArrayList<>();
        pointsMlw.add(new PointValue((float) min_arm, (float) airplane.max_landing).setLabel(getContext().getString(R.string.calc_mlw)));
        pointsMlw.add(new PointValue((float) max_arm, (float) airplane.max_landing));
        Line lineMlw = new Line(pointsMlw);
        lineMlw.setColor(mlwColor.data);
        lineMlw.setCubic(false);
        lineMlw.setHasPoints(false);
        lines.add(lineMlw);

        // calculated points
        Line wb = new Line (pointsCalculated);
        if (isWarning) wb.setColor(mtowColor.data);
        else wb.setColor(calcColor.data);
        wb.setCubic(false);
        wb.setHasPoints(true);
        lines.add(wb);

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

        if (isWarning)
            txtWarning.setText(strWarning);
        else
            txtWarning.setVisibility(View.GONE);

    }

    private TableRow createRow(String txt, double w, double a){
        TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.row_airplane_wb_summary, null);
        TextView txtName = (TextView) row.findViewById(R.id.wb_sum_title);
        txtName.setText(txt);
        TextView txtW = (TextView) row.findViewById(R.id.wb_sum_weight);
        txtW.setText(String.format("%.1f", w));
        TextView txtA = (TextView) row.findViewById(R.id.wb_sum_arm);
        txtA.setText(String.format("%.0f", a));

        return  row;
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
