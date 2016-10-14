package sk.lgstudio.easyflightbag.calculations;

import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2016-10-14.
 */

public class CalculatorTime extends Calculator implements RadioGroup.OnCheckedChangeListener, NumberPicker.OnValueChangeListener, View.OnClickListener {

    private final static int UNIT_COUNT = 4;
    private final static int HOUR = 3600;
    private final static int MINUTE = 60;

    private final static int[] SPEED_UNIT = {
            R.string.calc_unit_kmh,
            R.string.calc_unit_kn,
            R.string.calc_unit_mph,
            R.string.calc_unit_ftpmin
    };

    private final static int[] DISTANCE_UNIT = {
            R.string.calc_unit_km,
            R.string.calc_unit_ft,
            R.string.calc_unit_nmi,
            R.string.calc_unit_mi
    };

    private final static int[] RADIO = {
            R.id.calc_func_time_radio_speed,
            R.id.calc_func_time_radio_dst,
            R.id.calc_func_time_radio_time
    };

    private int selectedRadio = RADIO[0];

    private int selectedSpeed = 0;
    private int selectedDistance = 0;

    private RadioGroup radio;
    private NumberPicker pickTimeH;
    private NumberPicker pickTimeM;
    private NumberPicker pickTimeS;
    private EditText textSpeed;
    private EditText textDist;
    private Button btnSpeed;
    private Button btnDist;

    private Context context;

    private CalculatorTime self;

    public CalculatorTime(float[] r, float[] v, int[] l) {
        super(r, v, l);
        self = this;
    }

    @Override
    public void initView(View v) {

        context = v.getContext();

        radio = (RadioGroup) v.findViewById(R.id.calc_func_time_radio);
        radio.check(selectedRadio);
        radio.setOnCheckedChangeListener(this);

        pickTimeH = (NumberPicker) v.findViewById(R.id.calc_unit_time_pick_h);
        pickTimeM = (NumberPicker) v.findViewById(R.id.calc_unit_time_pick_min);
        pickTimeS = (NumberPicker) v.findViewById(R.id.calc_unit_time_pick_s);
        pickTimeH.setOnValueChangedListener(this);
        pickTimeM.setOnValueChangedListener(this);
        pickTimeS.setOnValueChangedListener(this);
        pickTimeH.setMinValue(0);
        pickTimeH.setMaxValue(99);
        pickTimeM.setMinValue(0);
        pickTimeM.setMaxValue(59);
        pickTimeS.setMinValue(0);
        pickTimeS.setMaxValue(59);

        textSpeed = (EditText) v.findViewById(R.id.calc_unit_text_time_s);
        textDist = (EditText) v.findViewById(R.id.calc_unit_text_time_d);
        textSpeed.setText(String.format("%.3f",values[0]));
        textDist.setText(String.format("%.3f",values[1]));
        textSpeed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    textSpeed.addTextChangedListener(self);
                }
                else{
                    textSpeed.removeTextChangedListener(self);
                    setSpeedText();
                }
            }
        });
        textDist.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    textDist.addTextChangedListener(self);
                }
                else{
                    textDist.removeTextChangedListener(self);
                    setDistanceText();
                }
            }
        });

        btnSpeed = (Button) v.findViewById(R.id.calc_func_time_s_btn);
        btnDist = (Button) v.findViewById(R.id.calc_func_time_d_btn);
        btnSpeed.setOnClickListener(this);
        btnDist.setOnClickListener(this);
        btnSpeed.setText(context.getString(SPEED_UNIT[selectedSpeed]));
        btnDist.setText(context.getString(DISTANCE_UNIT[selectedDistance]));

        onCheckedChanged(radio, selectedRadio);
    }

    private void updateData() {
        if (selectedRadio == RADIO[0]) { // calculte speed
            values[1] = calculateDistance();
            values[2] = calculateTime();
            values[0] = values[1] / (values[2]/HOUR);
            setSpeedText();
        }
        else if (selectedRadio == RADIO[1]) { // calculate distance
            values[0] = calculateSpeed();
            values[2] = calculateTime();
            values[1] = values[0] * (values[2]/HOUR);
            setDistanceText();
        }
        else if (selectedRadio == RADIO[2]) { // calculate time
            values[0] = calculateSpeed();
            values[1] = calculateDistance();
            values[2] = values[1] / values[0];
            setTimeNumbers();
        }

        Log.e("Speed", String.valueOf(values[0]));
        Log.e("Distance", String.valueOf(values[1]));
        Log.e("Time", String.valueOf(values[2]));
    }

    private float calculateTime(){
        return HOUR * pickTimeH.getValue() + MINUTE * pickTimeM.getValue() + pickTimeS.getValue();
    }

    private void setTimeNumbers(){
        pickTimeH.setValue((int) (values[2] / HOUR));
        pickTimeM.setValue((int) ((values[2] % HOUR) / MINUTE));
        pickTimeS.setValue((int) ((values[2] % HOUR) % MINUTE));
    }

    private float calculateSpeed() {
        if (textSpeed.getText().toString().length() > 0)
            return Float.valueOf(textSpeed.getText().toString()) / CalculatorData.speRatios[selectedSpeed];
        return 0f;
    }

    private void setSpeedText(){
        textSpeed.setText(String.format("%.3f",values[0] * CalculatorData.speRatios[selectedSpeed]));
    }

    private float calculateDistance() {
        if (textDist.getText().toString().length() > 0)
            return Float.valueOf(textDist.getText().toString()) / CalculatorData.speRatios[selectedDistance];
        return 0f;
    }

    private void setDistanceText(){
        textDist.setText(String.format("%.3f",values[0] * CalculatorData.dstRatiosToKm[selectedDistance]));
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        textSpeed.removeTextChangedListener(this);
        textDist.removeTextChangedListener(this);

        if (selectedRadio == RADIO[0])
            textSpeed.setEnabled(true);

        else if (selectedRadio == RADIO[1])
            textDist.setEnabled(true);

        else if (selectedRadio == RADIO[2]) {
            pickTimeH.setEnabled(true);
            pickTimeM.setEnabled(true);
            pickTimeS.setEnabled(true);
        }

        selectedRadio = i;

        if (selectedRadio == RADIO[0])
            textSpeed.setEnabled(false);

        else if (selectedRadio == RADIO[1])
            textDist.setEnabled(false);

        else if (selectedRadio == RADIO[2]) {
            pickTimeH.setEnabled(false);
            pickTimeM.setEnabled(false);
            pickTimeS.setEnabled(false);
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        Log.e("Change from " + String.valueOf(i),  "to " + String.valueOf(i1));
        updateData();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.calc_func_time_s_btn:
                selectedSpeed = (selectedSpeed + 1) % UNIT_COUNT;
                btnSpeed.setText(context.getString(SPEED_UNIT[selectedSpeed]));
                break;
            case R.id.calc_func_time_d_btn:
                selectedDistance = (selectedDistance + 1) % UNIT_COUNT;
                btnDist.setText(context.getString(DISTANCE_UNIT[selectedDistance]));
                break;
        }

        updateData();
    }
}
