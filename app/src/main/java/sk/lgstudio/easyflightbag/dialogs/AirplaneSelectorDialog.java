package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
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
 * Created by L on 16/10/05.
 */

public class AirplaneSelectorDialog extends Dialog implements View.OnClickListener {

    private File root;
    private boolean files = false;
    public File selected = null;

    private EditText newPlane;
    private Spinner planeSpinner;
    private ImageButton addPlane;
    private ImageButton deletePlane;
    private ImageButton done;

    private ArrayList<String> content;

    public AirplaneSelectorDialog(Context context) {
        super(context);
    }

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
                if (files)
                    deleteJsonFile();
                else
                    deletePlaneFolder();
                break;
            case R.id.chk_airplane_done:
                saveSelection();
                cancel();
                break;
        }
    }

    private void saveSelection(){
        if (content.size() > 0){
            String name =  root + "/" + content.get(planeSpinner.getSelectedItemPosition());
            if (files)
                name = name + ".json";
            selected = new File(name);
        }
        else selected = null;
    }

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

    private void deleteJsonFile(){
        if (content.size() > 0){
            File del = new File(root.getPath() + "/" + content.get(planeSpinner.getSelectedItemPosition()) + ".json");
            del.delete();
            fillSpinner();
        }
    }

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

    private void deletePlaneFolder(){

        if (content.size() > 0){
            File del = new File(root.getPath() + "/" + content.get(planeSpinner.getSelectedItemPosition()));
            deleteRecursive(del);
            fillSpinner();
        }

    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

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