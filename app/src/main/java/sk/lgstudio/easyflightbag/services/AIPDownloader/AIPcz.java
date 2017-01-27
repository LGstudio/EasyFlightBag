package sk.lgstudio.easyflightbag.services.AIPDownloader;

import android.content.Intent;
import android.os.Environment;
import android.webkit.URLUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sk.lgstudio.easyflightbag.R;
import sk.lgstudio.easyflightbag.managers.AIPManager;

/**
 * Created by LGstudio on 2017-01-25.
 */

public class AIPcz extends AIPDownloader {
    @Override
    protected void onHandleIntent(Intent intent) {

        country = AIPManager.AIP_CZ;

        aipFolder = new File(Environment.getExternalStorageDirectory() + getString(R.string.folder_root) + getString(R.string.folder_aip) + getString(R.string.folder_cz));
        if (!aipFolder.exists()){
            aipFolder.mkdir();
        }

        try {
            // download html file and get all elements with title
            Document page = Jsoup.connect(getString(R.string.url_cz_ais)).get();
            Elements allDocs = page.body().getElementsByAttributeStarting("title");

            for (Element e: allDocs){

                int pCount = 0;
                // check parents / groups
                for (Element p: e.parents()){
                    if (p.hasAttr("title")){
                        pCount++;
                    }
                }
                while (parents.size() != pCount)
                    parents.remove(parents.size()-1);

                // if the element can be expanded
                if (e.className().equals("aip_minus")){
                    // put the element into the hash map
                    docDepth.add(parents.size());
                    docValues.add(e.attr("title"));
                    parents.add(e);
                }
                // element with document url
                else {

                    docDepth.add(parents.size());
                    docValues.add(e.attr("title"));

                    // url from the actial element
                    Element a = e.getElementsByTag("a").first();

                    // get url and filename
                    String fileUrl = getString(R.string.url_cz_ais_data) + a.attr("href").substring(5);
                    fileUrl = fileUrl.substring(0, fileUrl.lastIndexOf(".")) + ".pdf";
                    String fileName = URLUtil.guessFileName(fileUrl, null, null);

                    // mark url with hash -1
                    docDepth.add(-1);
                    docValues.add(fileName);

                    // connection to the url of the file
                    URL url = new URL(fileUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    File file = new File(aipFolder, fileName);

                    //this will be used to write the downloaded data into the file we created
                    FileOutputStream fileOutput = new FileOutputStream(file);

                    //this will be used in reading the data from the internet
                    InputStream inputStream = urlConnection.getInputStream();

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    //now, read through the input buffer and write the contents to the file
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);
                    }
                    //close the output stream when done
                    fileOutput.close();
                    urlConnection.disconnect();

                    downloadedcount++;
                }
                sendReport(STATUS_STARTED);
            }

            if (writeFile())
                sendReport(STATUS_FINISHED);
            else
                sendReport(STATUS_ERROR);

        } catch (IOException e) {
            sendReport(STATUS_ERROR);
            e.printStackTrace();
        }
    }
}
