package sk.lgstudio.easyflightbag.calculations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by L on 16/10/11.
 */

public class CalculatorFuelOil extends Calculator implements AdapterView.OnItemSelectedListener {

    private final static float KG_TO_LB = 2.20462f;
    private final static float L_TO_G_US = 0.264172f;
    private final static float L_TO_G_IMP = 0.219969f;

    private Spinner fuelType;
    private int selectedFuel = 0;

    private final float[] density = {0f, 721f, 2f}; // kg/m^3
    private final String[] fuel = {"Custom", "Avgas", "B"};

    public CalculatorFuelOil(float[] r, float[] v, int[] l) {
        super(r, v, l);
    }

    @Override
    public void initView(View v) {
        super.initView(v);

        fuelType = (Spinner) v.findViewById(R.id.calc_unit_fueltype);
        fuelType.setAdapter(new FuelAdapter(v.getContext()));
        fuelType.setOnItemSelectedListener(this);
        fuelType.setSelection(selectedFuel);

    }

    @Override
    protected void updateData(float value){
        values[actualFocus] = value;
        switch (actualFocus){
            case 0:
            case 1:
                values[2] = values[0] / 1000 * values[1];
                values[3] = values[1] * L_TO_G_US;
                values[4] = values[1] * L_TO_G_IMP;
                values[5] = values[2] * KG_TO_LB;
                break;
            case 2:
                values[1] = (values[2] * 1000) / values[0];
                values[3] = values[1] * L_TO_G_US;
                values[4] = values[1] * L_TO_G_IMP;
                values[5] = values[2] * KG_TO_LB;
                break;
            case 3:
                values[1] = values[3] / L_TO_G_US;
                values[2] = values[0] / 1000 * values[1];
                values[4] = values[1] * L_TO_G_IMP;
                values[5] = values[2] * KG_TO_LB;
                break;
            case 4:
                values[1] = values[3] / L_TO_G_IMP;
                values[2] = values[0] / 1000 * values[1];
                values[4] = values[1] * L_TO_G_US;
                values[5] = values[2] * KG_TO_LB;
                break;
            case 5:
                values[2] = values[5] / KG_TO_LB;
                values[1] = (values[2] * 1000) / values[0];
                values[3] = values[1] * L_TO_G_US;
                values[4] = values[1] * L_TO_G_IMP;
                break;
        }

        for (int i = 0; i < textViews.size(); i++){
            if (i != actualFocus)
                textViews.get(i).setText(String.format("%.3f",values[i]));
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedFuel = position;
        textViews.get(0).setEnabled(selectedFuel == 0);
        if (selectedFuel > 0 && textViews.get(0).requestFocus()){
            actualView.setText(String.valueOf(density[selectedFuel]));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class FuelAdapter extends BaseAdapter {

        Context ctx;

        public FuelAdapter(Context context) {
            this.ctx = context;
        }

        @Override
        public int getCount() {
            return fuel.length;
        }

        @Override
        public Object getItem(int position) {
            return fuel[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View spinnerElement = inflater.inflate(R.layout.list_text_item, null);

            TextView airplane = (TextView) spinnerElement.findViewById(R.id.list_text);
            airplane.setText(fuel[position]);

            return spinnerElement;
        }

    }
}
