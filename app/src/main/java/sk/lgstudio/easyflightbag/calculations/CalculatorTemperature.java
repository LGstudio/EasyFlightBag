package sk.lgstudio.easyflightbag.calculations;

/**
 * Created by L on 16/10/10.
 */

public class CalculatorTemperature extends Calculator {

    private final static float KELVIN = 273.15f;
    private final static float F_MULT = 1.8f;
    private final static int F_ADD = 32;

    public CalculatorTemperature(float[] r, float[] v, int[] l) {
        super(r, v, l);
    }


    private void updateTemperatures(float temp){

        values[actualFocus] = temp;

        switch (actualFocus){
            case 0:
                values[1] = ((temp*F_MULT) + F_ADD);
                values[2] = (temp + KELVIN);
                textViews.get(1).setText(String.format("%.3f",values[1]));
                textViews.get(2).setText(String.format("%.3f",values[2]));
                break;
            case 1:
                values[0] = ((temp-F_ADD)/F_MULT);
                values[2] = (values[0] + KELVIN);
                textViews.get(0).setText(String.format("%.3f",values[0]));
                textViews.get(2).setText(String.format("%.3f",values[2]));
                break;
            case 2:
                values[0] = (temp-KELVIN);
                values[1] = ((values[0]*F_MULT) + F_ADD);
                textViews.get(1).setText(String.format("%.3f",values[1]));
                textViews.get(0).setText(String.format("%.3f",values[0]));
                break;
        }
    }


    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0)
            updateTemperatures(Float.valueOf(s.toString()));
        else
            updateTemperatures(0f);
    }

}
