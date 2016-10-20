package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private ArrayList<FileAdapter> adapters = new ArrayList<>();
    private int openedLevel = 1;
    public boolean fillData = true;

    LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater i, ViewGroup container, Bundle savedInstanceState) {
        inflater = i;
        View view = inflater.inflate(R.layout.fragment_aip, container, false);

        horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.aip_list_scroll);
        scrollLayout = (LinearLayout) view.findViewById(R.id.aip_list_layout);

        if (fillData) {
            files.clear();
            getData();
        }
        fillView(view);



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

    private void fillView(View v){

        scrollLayout.removeAllViewsInLayout();
        adapters.clear();

        for (int i = 0; i < openedLevel; i++){
            final int l = i;
            adapters.add(new FileAdapter(getContext(), R.layout.aip_list_item, getLevel(i+1), i+1));
            ListView list = (ListView) inflater.inflate(R.layout.aip_list, scrollLayout, false);

            list.setAdapter(adapters.get(i));
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l1) {
                    handleOnItemClick(l, position);
                }
            });

            //ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int) (scrollLayout.getWidth()*0.6), ViewGroup.LayoutParams.MATCH_PARENT);
            //list.setLayoutParams(params);

            scrollLayout.addView(list);
        }


        fillData = false;
    }

    private void handleOnItemClick(int lvl, int pos){
        if (getLevel(lvl+1).get(pos).files == null){
            // TODO: Open file here
        }
        else {
            while (selectedItems.size() > lvl)
                selectedItems.remove(selectedItems.size()-1);

            selectedItems.add(pos);
            openedLevel = selectedItems.size() + 1;

            fillView(getView());
        }

    }

    private ArrayList<FileStructure> getLevel(int l){
        ArrayList<FileStructure> level = files;

        for(int i = 1; i < l; i++){
            level = level.get(selectedItems.get(i-1)).files;
        }

        return level;
    }


    private static class FileStructure {
        public String title = null;
        public String url = null;
        public ArrayList<FileStructure> files = null;

    }

    private class FileAdapter extends ArrayAdapter<FileStructure> {

        private Context context;
        private int layoutResourceId;
        private ArrayList<FileStructure> data = null;
        private int level;

        public FileAdapter(Context c, int layoutR, ArrayList<FileStructure> d, int l){
            super(c, layoutR, d);
            context = c;
            layoutResourceId = layoutR;
            data = d;
            level = l;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            TextView title = (TextView) row.findViewById(R.id.aip_row_title);
            TextView description = (TextView) row.findViewById(R.id.aip_row_description);

            int pos = data.get(position).title.indexOf('-');
            int len = data.get(position).title.length();

            //Log.e("title", data.get(position).title.substring(0,pos));
            //Log.w("dsc", data.get(position).title.substring(pos+1,len));

            title.setText(data.get(position).title.substring(0,pos));
            description.setText(data.get(position).title.substring(pos+1,len));

            ImageView icon;
            if (data.get(position).files == null)
                icon = (ImageView) row.findViewById(R.id.aip_row_image_list);
            else
                icon = (ImageView) row.findViewById(R.id.aip_row_image_doc);
            icon.setVisibility(View.GONE);

            if (selectedItems.size() >= level)
                if (position == selectedItems.get(level-1))
                    row.setBackgroundResource(R.drawable.bck_item_selected);
                else
                    row.setBackgroundResource(R.drawable.bck_transparent);

            return row;
        }

    }
}