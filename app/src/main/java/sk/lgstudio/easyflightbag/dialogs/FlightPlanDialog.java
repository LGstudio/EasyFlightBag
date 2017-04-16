package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AirplaneManager;
import sk.lgstudio.easyflightbag.managers.FlightPlanManager;
import sk.lgstudio.easyflightbag.managers.MapOverlayManager;

/**
 * Dialog To create a complete flight plan
 *      - Select plane
 *      - Fill flight info
 *      - Select Flight plan
 */
public class FlightPlanDialog extends Dialog implements View.OnClickListener, DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    private TextView txtAirplane;
    private TextView txtPlan;
    private TextView txtSum;
    private EditText edtCrSp;
    private TableLayout table;

    private float crSpeed = 0;

    private File plansFolder;
    private File airplanesFolder;
    private AirplaneManager airplaneManager;
    private MapOverlayManager mapOverlayManager;
    private LatLng lastPosition;
    public FlightPlanManager flightPlanManager = null; // returned value

    private SelectorDialog dialog;
    private boolean isAirplane = false;
    private boolean notOK = false;

    public FlightPlanDialog(Context context, int themeResId, AirplaneManager ap, MapOverlayManager map, LatLng pos, File plan, File airplane) {
        super(context, themeResId);

        airplanesFolder = airplane;
        plansFolder = plan;
        airplaneManager = ap;
        mapOverlayManager = map;
        lastPosition = pos;
    }

    @Override
    public void show(){
        super.show();

        ImageButton btnBack = (ImageButton) findViewById(R.id.plan_bck);
        ImageButton btnSave = (ImageButton) findViewById(R.id.plan_save);
        Button btnAirplane = (Button) findViewById(R.id.plan_select_airplane);
        Button btnPlan = (Button) findViewById(R.id.plan_select_plan);
        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnPlan.setOnClickListener(this);
        btnAirplane.setOnClickListener(this);

        txtAirplane = (TextView) findViewById(R.id.plan_select_airplane_text);
        txtPlan = (TextView) findViewById(R.id.plan_select_plan_text);
        txtSum = (TextView) findViewById(R.id.plan_sum);
        edtCrSp = (EditText) findViewById(R.id.plan_ap_cruise_sp);
        edtCrSp.setEnabled(false);
        edtCrSp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    crSpeed =  Float.valueOf(String.valueOf(s));
                else
                    crSpeed = 0f;
                writeSum();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        table = (TableLayout) findViewById(R.id.plan_ap_data_table);

        if (airplaneManager.loaded)
            loadAirplane();

    }

    /**
     * Loads new airplane detials into the fields
     */
    private void loadAirplane(){
        if (airplaneManager.loaded){
            txtAirplane.setText(airplaneManager.getName());
            edtCrSp.setEnabled(true);
            // TODO: LOAD fuel tanks
        }
        else {
            txtAirplane.setText(getContext().getString(R.string.plan_airplane_select_none));
            edtCrSp.setEnabled(false);
            edtCrSp.setText("");
            // TODO: Clear fuel tanks rows
        }
        writeSum();
    }

    /**
     * Loads flight plan related data into fields
     */
    private void loadFlightPlan(){
        if (flightPlanManager == null){
            txtPlan.setText(getContext().getString(R.string.plan_select_none));
        }
        else{
            txtPlan.setText(flightPlanManager.getPlanName());
        }
        writeSum();
    }

    /**
     * rewrites summary information
     */
    private void writeSum(){
        if (airplaneManager.loaded && flightPlanManager != null){

        }

        if (notOK) {
            txtSum.setText(getContext().getString(R.string.plan_summary_empty));
        }
    }

    /**
     * Check if data are valid for flight then close the dialog
     */
    private void savePlan(){
        // TODO: validate data, save fuel tanks ...
        dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.plan_bck:
                flightPlanManager = null;
                dismiss();
                break;
            case R.id.plan_save:
                savePlan();
                break;
            case R.id.plan_select_airplane:
                openAirplaneSelector();
                break;
            case R.id.plan_select_plan:
                openFlightPlanSelector();
                break;
        }
    }

    /**
     * Opens Airplane Selector Dialog
     */
    private void openAirplaneSelector(){
        isAirplane = true;
        dialog = new SelectorDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_selector);
        dialog.loadContent(airplanesFolder, airplaneManager.file ,true, SelectorDialog.TYPE_AIRPLANE);
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    /**
     * Opens Flight Plan Selector Dialog
     */
    private void openFlightPlanSelector(){
        isAirplane = false;
        dialog = new SelectorDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_selector);
        dialog.loadContent(plansFolder, null, true, SelectorDialog.TYPE_FLIGHTPLAN);
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);
        dialog.show();

    }

    @Override
    public void onCancel(DialogInterface d) {
        if (dialog == null) return;
        if (isAirplane){
            if (dialog.selected == null) {
                airplaneManager.clearFile();
                loadAirplane();
            }
        }
        else{
            if (dialog.selected == null) {
                flightPlanManager = null;
                loadFlightPlan();
            }
        }
        dialog = null;
    }

    @Override
    public void onDismiss(DialogInterface d) {
        if (dialog == null) return;
        if (isAirplane){
            if (dialog.selected != null){
                airplaneManager.loadFile(dialog.selected);
                if (dialog.edit) {
                    createAirplaneEditorDialog();
                    return;
                }
            }
            else {
                airplaneManager.clearFile();
            }
            loadAirplane();
        }
        else{
            if (dialog.selected != null){
                flightPlanManager = new FlightPlanManager(dialog.selected);
                if (dialog.edit) {
                    openPlanEditor();
                }
            }
            else {
                flightPlanManager = null;
            }
        }
        dialog = null;
    }

    /**
     * Creates Airplane Selector Dialog
     */
    private void createAirplaneEditorDialog(){
        AirplaneEditorDialog dialog = new AirplaneEditorDialog(getContext(), R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_airplane_editor);
        dialog.loadContent(airplaneManager);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loadAirplane();
            }
        });
        dialog.show();
    }

    /**
     * Opens flight plan editor Dialog
     */
    private void openPlanEditor(){
        PlanEditorDialog dialog = new PlanEditorDialog(getContext(), R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_plan_editor);
        dialog.loadContent(lastPosition, 30f, flightPlanManager, mapOverlayManager);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {;
                loadFlightPlan();
            }
        });
        dialog.show();
    }
}
