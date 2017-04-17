package sk.lgstudio.easyflightbag.calculations;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.AirplaneEditorDialog;
import sk.lgstudio.easyflightbag.dialogs.SelectorDialog;
import sk.lgstudio.easyflightbag.dialogs.WbGraphDialog;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;

/**
 * Weight and Balance calculator screen and manager
 */

public class CalculatorWB extends Calculator implements View.OnClickListener, DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    private File parentFolder = null;
    private SelectorDialog dialogAirplane;
    private AirplaneManager airplaneManager;

    private TextView airplaneId = null;
    private ImageButton airplaneEditor = null;
    private ImageButton airplaneSelector = null;

    private ScrollView scrollView;
    private NumberPicker flightTimeH;
    private NumberPicker flightTimeM;
    private TableLayout tableFuel;
    private TableLayout tableWeights;

    private Context context;
    private LayoutInflater inflater;

    public CalculatorWB(Context c, LayoutInflater i, AirplaneManager ap, File f) {
        super(null, null, null);
        context = c;
        inflater = i;
        parentFolder = f;
        airplaneManager = ap;
    }

    @Override
    public void initView(View v) {

        airplaneEditor = null;
        airplaneId = null;

        airplaneSelector = (ImageButton) v.findViewById(R.id.wb_plane);
        airplaneEditor = (ImageButton) v.findViewById(R.id.wb_edit);
        airplaneId = (TextView) v.findViewById(R.id.wb_airplane_name);
        airplaneSelector.setOnClickListener(this);
        airplaneEditor.setOnClickListener(this);

        scrollView = (ScrollView) v.findViewById(R.id.ap_wb_scroll_layout);
        flightTimeH = (NumberPicker) v.findViewById(R.id.ac_wb_time_pick_h);
        flightTimeM = (NumberPicker) v.findViewById(R.id.ap_wb_time_pick_min);
        flightTimeH.setMinValue(0);
        flightTimeH.setMaxValue(99);
        flightTimeM.setMinValue(0);
        flightTimeM.setMaxValue(59);

        tableFuel = (TableLayout) v.findViewById(R.id.ap_wb_table_tanks);
        tableWeights = (TableLayout) v.findViewById(R.id.ap_wb_table_weights);
        Button btnCalc = (Button) v.findViewById(R.id.ap_wb_calc_btn);
        btnCalc.setOnClickListener(this);

        reloadContent();

    }

    @Override
    public void detachView() {// empty
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wb_plane:
                createAirplaneSelectorDialog();
                break;
            case R.id.wb_edit:
                createAirplaneEditorDialog();
                break;
            case R.id.ap_wb_calc_btn:
                createGraphDialog();
                break;
        }
    }


    /**
     * Creates W&B graph Dialog
     */
    private void createGraphDialog(){
        boolean isOk = true;

        for (AirplaneManager.Tanks t: airplaneManager.tanks){
            if (t.actual > t.max || t.actual < t.unus){
                isOk = false;
            }
        }
        for (AirplaneManager.Weights w: airplaneManager.additional_weight){
            if (w.max > 0 && w.actual > w.max){
                isOk = false;
            }
        }

        if (!isOk){
            Toast.makeText(context, context.getString(R.string.calc_warning_oflimit), Toast.LENGTH_SHORT).show();
            return;
        }

        if (flightTimeH.getValue() == 0 && flightTimeM.getValue() == 0){
            Toast.makeText(context, context.getString(R.string.calc_warning_set_time), Toast.LENGTH_SHORT).show();
            return;
        }
        airplaneManager.flightTimeH = flightTimeH.getValue();
        airplaneManager.flightTimeM = flightTimeM.getValue();

        WbGraphDialog dialog = new WbGraphDialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_wb_graph);
        dialog.loadContent(airplaneManager);
        dialog.show();


    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneSelectorDialog(){
        dialogAirplane = new SelectorDialog(context);
        dialogAirplane.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAirplane.setContentView(R.layout.dialog_selector);
        dialogAirplane.loadContent(parentFolder, airplaneManager.file ,true, SelectorDialog.TYPE_AIRPLANE);
        dialogAirplane.setOnCancelListener(this);
        dialogAirplane.setOnDismissListener(this);
        dialogAirplane.show();
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneEditorDialog(){
        AirplaneEditorDialog dialog = new AirplaneEditorDialog(context, R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_airplane_editor);
        dialog.loadContent(airplaneManager);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                reloadContent();
            }
        });
        dialog.show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialogAirplane != null){
            if (dialogAirplane.selected == null) {
                airplaneManager.clearFile();
                reloadContent();
            }
            dialogAirplane = null;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dialogAirplane != null) {
            if (dialogAirplane.selected != null){
                airplaneManager.loadFile(dialogAirplane.selected);
                if (dialogAirplane.edit || !airplaneManager.loaded) {
                    createAirplaneEditorDialog();
                    return;
                }
            }
            else {
                airplaneManager.clearFile();
            }
            dialogAirplane = null;
            reloadContent();
        }
    }


    /**
     * Relaod the view and the tables beased on the selected airplaneManager
     */
    private void reloadContent(){

        for (int i = tableFuel.getChildCount(); i > 2; i--){
            tableFuel.removeViewAt(i-1);
        }
        for (int i = tableWeights.getChildCount(); i > 2; i--){
            tableWeights.removeViewAt(i-1);
        }

        if (airplaneManager.file != null){
            airplaneEditor.setVisibility(View.VISIBLE);
            airplaneId.setText(airplaneManager.getName());

            if (airplaneManager.loaded){
                scrollView.setVisibility(View.VISIBLE);

                if (airplaneManager.flightTimeH != 0 && airplaneManager.flightTimeM != 0){
                    flightTimeH.setValue(airplaneManager.flightTimeH);
                    flightTimeM.setValue(airplaneManager.flightTimeM);
                    flightTimeM.setEnabled(false);
                    flightTimeH.setEnabled(false);
                    airplaneSelector.setEnabled(false);
                }
                else {
                    flightTimeH.setValue(0);
                    flightTimeM.setValue(0);
                    flightTimeM.setEnabled(true);
                    flightTimeH.setEnabled(true);
                    airplaneSelector.setEnabled(true);
                }

                for(final AirplaneManager.Tanks t: airplaneManager.tanks) {
                    TableRow row = (TableRow) inflater.inflate(R.layout.calc_wb_fuel_row, null);
                    TextView txtName = (TextView) row.findViewById(R.id.ap_wb_fuel_name);
                    TextView txtCap = (TextView) row.findViewById(R.id.ap_wb_fuel_capacity);
                    TextView txtUnus = (TextView) row.findViewById(R.id.ap_wb_fuel_unus);
                    EditText actual = (EditText) row.findViewById(R.id.ap_wb_fuel_content);
                    txtName.setText(t.name);
                    txtCap.setText(String.valueOf(t.max));
                    txtUnus.setText(String.valueOf(t.unus));
                    actual.setText(String.valueOf(t.actual));
                    actual.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (s.length() > 0)
                                t.actual = Double.parseDouble(s.toString());
                            else
                                t.actual = 0;
                        }

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });
                    tableFuel.addView(row);

                }

                for(final AirplaneManager.Weights w: airplaneManager.additional_weight) {
                    TableRow row = (TableRow) inflater.inflate(R.layout.calc_wb_weights_row, null);
                    TextView txtName = (TextView) row.findViewById(R.id.ap_wb_weight_name);
                    TextView txtMax = (TextView) row.findViewById(R.id.ap_wb_weight_max);
                    EditText actual = (EditText) row.findViewById(R.id.ap_wb_weight);
                    txtName.setText(w.name);
                    txtMax.setText(String.valueOf(w.max));
                    actual.setText(String.valueOf(w.actual));
                    actual.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (s.length() > 0)
                                w.actual = Double.parseDouble(s.toString());
                            else
                                w.actual = 0;
                        }

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });
                    tableWeights.addView(row);
                }
            }
            else {
                scrollView.setVisibility(View.GONE);
            }
        }
        else {
            airplaneId.setText("");
            airplaneEditor.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
            airplaneManager.clearFile();
        }
    }
}
