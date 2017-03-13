package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;

/**
 * Airplane Selector Dialog
 *
 * Load content needs to be called after setContentView() before show()
 * 2 modes:
 *  - file (.json) selector : .loadContent(file, true)
 *  - folders selector : .loadContent(file, false)
 */

public class AirplaneSelectorDialog extends Dialog implements View.OnClickListener, DialogInterface.OnCancelListener {

    private File root;
    private boolean files = false;
    public File selected = null;

    private EditText newPlane;
    private Spinner planeSpinner;
    private ImageButton addPlane;
    private ImageButton deletePlane;
    private ImageButton done;

    private ArrayList<String> content;

    /**
     * Constructor
     */
    public AirplaneSelectorDialog(Context context) {
        super(context);
    }

    /**
     * Loads and initializes content onto the view
     * @param f
     * @param areFiles
     */
    public void loadContent(File f, boolean areFiles){
        root = f;
        files = areFiles;

        newPlane = (EditText) findViewById(R.id.chk_airplane_new);
        addPlane = (ImageButton) findViewById(R.id.chk_airplane_add);
        deletePlane = (ImageButton) findViewById(R.id.chk_airplane_delete);
        done = (ImageButton)  findViewById(R.id.chk_airplane_done);
        planeSpinner = (Spinner) findViewById(R.id.chk_airplane_spinner);

        done.setOnClickListener(this);
        addPlane.setOnClickListener(this);
        deletePlane.setOnClickListener(this);

        fillSpinner();
    }

    /**
     * Fills the spinner with the json/folder names from root
     */
    private void fillSpinner(){

        content = new ArrayList<>();

        for (File inFile : root.listFiles()) {
            if (inFile.isDirectory() && ! files) {
                content.add(inFile.getName());
            }
            else if (files){
                String str = inFile.getName();
                int extPos = str.lastIndexOf(".");
                content.add(str.substring(0, extPos));
            }
        }

        planeSpinner.setAdapter(new PlaneAdapter(getContext()));
    }

    /**
     * Button onClick listener
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chk_airplane_add:
                if (files)
                    addNewJsonFile();
                else
                    addNewPlaneFolder();
                break;
            case R.id.chk_airplane_delete:
                askToDelete();
                break;
            case R.id.chk_airplane_done:
                saveSelection();
                cancel();
                break;
        }
    }

    /**
     * Saves the selected file into the selected variable
     */
    private void saveSelection(){
        if (content.size() > 0){
            String name =  root + "/" + content.get(planeSpinner.getSelectedItemPosition());
            if (files)
                name = name + ".json";
            selected = new File(name);
        }
        else selected = null;
    }

    /**
     * Dialog to ask if sure to delete file/folder
     */
    private void askToDelete(){
        DeleteDialog dialog = new DeleteDialog(getContext());
        dialog.setContentView(R.layout.dialog_delete);
        dialog.loadContent();
        dialog.setOnCancelListener(this);
        dialog.show();
    }

    /**
     * Question dialog Yes answer given
     * @param dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        if (files)
            deleteJsonFile();
        else
            deletePlaneFolder();
    }

    /**
     * Creates new json file int the folder
     */
    private void addNewJsonFile(){
        if (newPlane.getText() != null){
            File newFile = new File(root + "/" + newPlane.getText().toString() + ".json");
            try {
                newFile.createNewFile();
                selected = newFile;
                cancel();
            } catch (IOException e) {
                Toast.makeText(getContext(), getContext().getString(R.string.chk_plane_exists_toast), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Deletes json file from folder
     */
    private void deleteJsonFile(){
        if (content.size() > 0){
            File del = new File(root.getPath() + "/" + content.get(planeSpinner.getSelectedItemPosition()) + ".json");
            del.delete();
            fillSpinner();
        }
    }

    /**
     * Creates new folder to root
     */
    private void addNewPlaneFolder() {

        if (newPlane.getText() != null){
            File newDir = new File(root + "/" + newPlane.getText());
            if(!newDir.exists()) {
                newDir.mkdir();
                selected = newDir;
                cancel();
            }
            else{
                Toast.makeText(getContext(), getContext().getString(R.string.chk_plane_exists_toast), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Deletes the folder from the root
     */
    private void deletePlaneFolder(){

        if (content.size() > 0){
            File del = new File(root.getPath() + "/" + content.get(planeSpinner.getSelectedItemPosition()));
            deleteRecursive(del);
            fillSpinner();
        }

    }

    /**
     * Recursive delete of the content of the folder
     * @param fileOrDirectory
     */
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     * Adapter to load file/folder names into the spinner
     */
    private class PlaneAdapter extends BaseAdapter {

        Context ctx;

        public PlaneAdapter(Context context) {
            this.ctx = context;
        }

        @Override
        public int getCount() {
            return content.size();
        }

        @Override
        public Object getItem(int position) {
            return content.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            View spinnerElement = inflater.inflate(R.layout.spinner_text_item, null);

            TextView airplane = (TextView) spinnerElement.findViewById(R.id.spinner_text_item);
            airplane.setText(content.get(position));

            return spinnerElement;
        }

    }
}
