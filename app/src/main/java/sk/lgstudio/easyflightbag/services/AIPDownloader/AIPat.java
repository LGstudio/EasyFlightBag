package sk.lgstudio.easyflightbag.services.AIPDownloader;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AIPManager;

/**
 * Created by LGstudio on 2017-01-27.
 */

public class AIPat extends AIPDownloader {
    @Override
    protected void onHandleIntent(Intent intent) {

        country = AIPManager.AIP_AT;

        aipFolder = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_root) + getString(R.string.folder_aip) + getString(R.string.folder_at));
        if (!aipFolder.exists()){
            aipFolder.mkdir();
        }

        try{
            Document page = Jsoup.connect(getString(R.string.url_at_aip)).get();
            Elements current = page.body().getElementsContainingText("current version");
            if (current.size() == 1){
                String url = current.get(0).attributes().get("href");
                Log.e("AT", url);
            }


            sendReport(STATUS_FINISHED);

        } catch (IOException e) {
            sendReport(STATUS_ERROR);
            e.printStackTrace();
        }

    }
}
