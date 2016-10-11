package sk.lgstudio.easyflightbag.calculations;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by L on 16/10/06.
 */

public class Calculator implements View.OnFocusChangeListener, TextWatcher {

    protected final static int NONE = -1;

    protected float[] ratios;
    protected int[] layouts;
    protected float[] values;
    protected ArrayList<EditText> textViews = new ArrayList<>();
    protected EditText actualView;
    protected int actualFocus = NONE;


    public Calculator(float[] r, float[] v, int[] l){
        ratios = r;
        values = v;
        layouts = l;
    }

    public void initView(View v) {

        textViews.clear();

        for (int i = 0; i < layouts.length; i++){

            final EditText tv = (EditText) v.findViewById(layouts[i]);
            tv.setText(String.format("%.3f",values[i]));
            tv.setOnFocusChangeListener(this);
            textViews.add(tv);
        }
    }

    public void detachView() {
        if (actualView != null)
            actualView.removeTextChangedListener(this);
        actualFocus = NONE;
        actualView = null;
    }

    protected void updateData(float value){

        values[actualFocus] = value;
        values[0] = values[actualFocus]/ratios[actualFocus];

        for (int j = 0; j < textViews.size(); j++){
            if (j != actualFocus){
                values[j] = values[0]*ratios[j];
                textViews.get(j).setText(String.format("%.3f",values[j]));
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        EditText et = (EditText) v;
        int af = NONE;

        for (int i = 0; i < layouts.length; i++)
            if (et.getId() == layouts[i])
                af = i;

        if (hasFocus) {
            actualFocus = af;
            actualView = et;
            actualView.addTextChangedListener(this);
        }
        else {
            et.removeTextChangedListener(this);
            et.setText(String.format("%.3f",values[af]));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0)
            updateData(Float.valueOf(s.toString()));
        else
            updateData(0f);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}
