package sk.lgstudio.easyflightbag.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.File;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2017-01-25.
 */

public class PdfViewer extends Dialog implements View.OnClickListener, OnPageChangeListener {

    private ImageButton btnBack;
    private TextView titleText;
    private PDFView pdfView;
    private SharedPreferences prefs;

    private String file;

    public PdfViewer(Context context, int themeStyle) {
        super(context, themeStyle);
    }

    public void loadContent(String title, String location, String filename){

        btnBack = (ImageButton) findViewById(R.id.pdf_back);
        titleText = (TextView) findViewById(R.id.pdf_title);
        pdfView = (PDFView) findViewById(R.id.pdf_view);

        file = filename;
        int page = getPage();

        btnBack.setOnClickListener(this);
        titleText.setText(title);
        pdfView.fromFile(new File(location + "/" + file))
                .defaultPage(page)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onPageChange(this)
                .load();
    }

    private int getPage(){
        prefs = getContext().getSharedPreferences(getContext().getString(R.string.app_prefs), Context.MODE_PRIVATE);
        return prefs.getInt(getContext().getString(R.string.pref_document_page) + file, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pdf_back:
                dismiss();
                break;
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        prefs.edit().putInt(getContext().getString(R.string.pref_document_page) + file, page).apply();
    }
}
