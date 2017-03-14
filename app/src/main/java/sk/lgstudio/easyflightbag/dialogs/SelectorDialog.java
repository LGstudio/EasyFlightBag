package sk.lgstudio.easyflightbag.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;

/**
 * Selector Dialog - loads subfolder list .json file list in root folder
 *
 * Load content needs to be called after setContentView() before show()
 * 2 modes:
 *  - file (.json) selector : .loadContent(file, true)
 *  - folders selector : .loadContent(file, false)
 */

public class SelectorDialog extends Dialog implements View.OnClickListener, DialogInterface.OnCancelListener {

    private File root;
    private boolean files = false;
    public File selected = null; // the selected file/folder is returned in here
    public boolean edit = false; // if edit button was pressed set to true

    private EditText txtNew;
    private ListView list;
    private ImageButton btnAdd;
    private TextView title;

    private ArrayList<File> content;

    /**
     * Constructor
     */
    public SelectorDialog(Context context) {
        super(context);
    }

    /**
     * Loads and initializes content onto the view
     * @param f
     * @param areFiles
     */
    public void loadContent(File f, boolean areFiles, int titleResource, int hintResource){
        root = f;
        files = areFiles;

        title = (TextView) findViewById(R.id.selector_title);
        txtNew = (EditText) findViewById(R.id.selector_new_text);
        btnAdd = (ImageButton) findViewById(R.id.selector_new_add);
        list = (ListView) findViewById(R.id.selector_list);

        btnAdd.setOnClickListener(this);
        title.setText(titleResource);
        txtNew.setHint(hintResource);
        txtNew.clearFocus();

        fillList();

    }

    /**
     * Fills the list with the json/folder names from root
     */
    private void fillList(){

        content = new ArrayList<>();

        for (File inFile : root.listFiles()) {
            if (inFile.isDirectory() && ! files) {
                content.add(inFile);
            }
            else if (files){
                int p = inFile.getName().lastIndexOf(".");
                if (inFile.getName().substring(p).equals(".json"))
                    content.add(inFile);

            }
        }

        list.setAdapter(new ListAdapter(getContext(), R.layout.dialog_selector_row, content));
    }

    /**
     * Button onClick listener
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.selector_new_add:
                if (files)
                    addNewJsonFile();
                else
                    addNewFolder();
                break;
        }
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
            deleteFolder();
    }

    /**
     * Creates new json file int the folder
     */
    private void addNewJsonFile(){
        if (txtNew.getText() != null){
            File newFile = new File(root + "/" + txtNew.getText().toString() + ".json");
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
        selected.delete();
        selected = null;
        fillList();

    }

    /**
     * Creates new folder to root
     */
    private void addNewFolder() {

        if (txtNew.getText() != null){
            File newDir = new File(root + "/" + txtNew.getText());
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
    private void deleteFolder(){
        deleteRecursive(selected);
        selected = null;
        fillList();
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
    private class ListAdapter extends ArrayAdapter<File> {

        private ArrayList<File> data;
        private int layoutResourceId;

        public ListAdapter(Context context, int resource, ArrayList<File> objects) {
            super(context, resource, objects);
            data = objects;
            layoutResourceId = resource;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            final File f = data.get(position);
            String str = f.getName();

            TextView name = (TextView) row.findViewById(R.id.selector_item_name);
            ImageButton btnEdit = (ImageButton) row.findViewById(R.id.selector_edit);
            ImageButton btnDelete = (ImageButton) row.findViewById(R.id.selector_delete);

            if (files){
                int pos = str.lastIndexOf(".");
                name.setText(str.substring(0,pos));
            }
            else{
                name.setText(str);
            }

            if (files){
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selected = f;
                        edit = true;
                        cancel();
                    }
                });
            }
            else {
                btnEdit.setVisibility(View.GONE);
            }

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected = f;
                    askToDelete();
                }
            });

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected = f;
                    cancel();
                }
            });

            return row;
        }

    }
}
