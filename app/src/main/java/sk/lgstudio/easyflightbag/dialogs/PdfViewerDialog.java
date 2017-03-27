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
 * Dialog with Simple PDF viewer
 *
 * loadContent must be called before show()
 */

public class PdfViewerDialog extends Dialog implements View.OnClickListener {

    private ImageButton btnBack;
    private ImageButton btnOrientation;
    private TextView titleText;
    private PDFView pdfView;
    private SharedPreferences prefs;

    private boolean rotation = false;

    private String file;

    /**
     * Constructor calling super
     * @param context
     * @param themeStyle
     */
    public PdfViewerDialog(Context context, int themeStyle) {
        super(context, themeStyle);
    }

    /**
     * Loads the content of the view
     * @param title - title to be displayed
     * @param location - file location folder
     * @param filename - filename with extenseion
     */
    public void loadContent(String title, String location, String filename){

        btnBack = (ImageButton) findViewById(R.id.pdf_back);
        btnOrientation = (ImageButton) findViewById(R.id.pdf_orientation);
        titleText = (TextView) findViewById(R.id.pdf_title);
        pdfView = (PDFView) findViewById(R.id.pdf_view);
        btnBack.setOnClickListener(this);
        btnOrientation.setOnClickListener(this);

        file = filename;
        int page = getPage();

        titleText.setText(title);
        pdfView.fromFile(new File(location + "/" + file))
                .defaultPage(page)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .load();
    }

    /**
     * Reads the page that was last opened for this document
     * @return
     */
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
            case R.id.pdf_orientation:
                rotatePdf();
                break;
        }
    }

    /**
     * Rotates the pdf
     */
    private void rotatePdf(){
        rotation  = !rotation;

        if (rotation){

        }
        else {

        }
    }

    @Override
    public void dismiss() {
        prefs.edit().putInt(getContext().getString(R.string.pref_document_page) + file, pdfView.getCurrentPage()).apply();
        super.dismiss();
    }
}
