package sk.lgstudio.easyflightbag.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.dialogs.PdfViewerDialog;

/**
 *
 */
public class FragmentDocs extends Fragment implements AdapterView.OnItemClickListener {

    public File folder;
    private ListView list;
    private ArrayList<File> files;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_docs, container, false);

        list = (ListView) view.findViewById(R.id.docs_list);

        files = new ArrayList<>();
        for (File f: folder.listFiles()){
            if (!f.isDirectory()){
                String str = f.getName();
                int extPos = str.lastIndexOf(".");
                String ext = str.substring(extPos+1);
                if (ext.equalsIgnoreCase("pdf")){
                    files.add(f);
                }
            }
        }

        FileAdapter adapter = new FileAdapter(getContext(), R.layout.item_file_structure, files);

        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File f = files.get(position);

        PdfViewerDialog viewer = new PdfViewerDialog(getContext(), R.style.FullScreenDialog);
        viewer.setContentView(R.layout.dialog_pdf_viewer);
        viewer.loadContent(f.getName(), folder.getPath(), f.getName());
        viewer.show();

    }

    private class FileAdapter extends ArrayAdapter<File> {

        private Context context;
        private int layoutResourceId;
        private ArrayList<File> data = null;

        public FileAdapter(Context c, int layoutR, ArrayList<File> d){
            super(c, layoutR, d);
            context = c;
            layoutResourceId = layoutR;
            data = d;
        }

        @Override
        public View getView(int position, View row, ViewGroup parent) {

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            TextView dsc = (TextView) row.findViewById(R.id.aip_row_description);
            dsc.setVisibility(View.GONE);

            TextView title = (TextView) row.findViewById(R.id.aip_row_title);
            String name = data.get(position).getName();
            title.setText(name.substring(0,name.lastIndexOf(".")));

            ImageView icon = (ImageView) row.findViewById(R.id.aip_row_icon);
            icon.setImageResource(R.drawable.ic_docs_inv);

            return row;
        }

    }

}