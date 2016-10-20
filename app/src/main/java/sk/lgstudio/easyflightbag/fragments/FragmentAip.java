package sk.lgstudio.easyflightbag.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;

/**
 *
 */
public class FragmentAip extends Fragment {

    private HorizontalScrollView horizontalScrollView;
    private LinearLayout scrollLayout;

    public File folder;

    public ArrayList<FileStructure> files = new ArrayList<>();
    public boolean fillData = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aip, container, false);

        horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.aip_list_scroll);
        scrollLayout = (LinearLayout) view.findViewById(R.id.aip_list_layout);


        getData();
        fillView();


        return view;
    }


    /**
     * Loads data from data file to the files ArrayList
     */
    private void getData(){
        FileInputStream is;
        BufferedReader reader;
        final File data = new File(folder.getPath() + getString(R.string.file_aip_data));

        int lastLevel = 0;
        ArrayList<FileStructure> actual = files;

        if (data.exists()) {
            try {
                is = new FileInputStream(data);

                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                while(line != null){

                    FileStructure struct = new FileStructure();

                    int level = Integer.parseInt(line.substring(0, line.indexOf(':')));
                    struct.title = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
                    if (line.indexOf(':') == line.lastIndexOf(':')){
                        struct.files = new ArrayList<>();
                    }
                    else {
                        struct.url = line.substring(line.lastIndexOf(':')+1, line.length());
                    }

                    if (lastLevel > level){
                        actual = files;
                        for (int j = 0; j < level; j++){
                            actual = actual.get(actual.size()-1).files;
                        }
                        lastLevel = level;

                    }
                    else if (lastLevel < level){
                        actual = actual.get(actual.size()-1).files;
                        lastLevel = level;
                    }

                    actual.add(struct);

                    line = reader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillView(){

        Log.w("AIP", "Filling data here");

        fillData = false;
    }


    private static class FileStructure {
        public String title = null;
        public String url = null;
        public ArrayList<FileStructure> files = null;

    }
}