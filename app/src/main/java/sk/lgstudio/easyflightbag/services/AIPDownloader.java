package sk.lgstudio.easyflightbag.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.URLUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2016-10-18.
 */

public class AIPDownloader extends IntentService {

    public final static int STATUS_STARTED = 0;
    public final static int STATUS_ERROR = 1;
    public final static int STATUS_FINISHED = 2;

    private File aipFolder;

    private ArrayList<Element> parents = new ArrayList<>();
    private HashMap<Integer,String> doc = new HashMap<>();

    private int downloadedcount = 0;

    public AIPDownloader() {
        super("AIP");
        aipFolder = new File(Environment.getExternalStorageDirectory() + "/EasyFlightBag/AIP/cz");
        if (!aipFolder.exists()){
            aipFolder.mkdir();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Document page = Jsoup.connect(getString(R.string.url_cz_ais)).get();

            Elements allDocs = page.getElementsByAttributeStarting("title");

            for (Element e: allDocs){
                if(e.parent().tagName().equals("span")){
                    if (e.className().equals("aip_minus")){
                        doc.put(parents.size(), e.className());
                        parents.add(e);
                    }
                    else {

                        

                        String fileUrl = getString(R.string.url_cz_ais_data) + e.attr("href").substring(5);
                        fileUrl = fileUrl.substring(0, fileUrl.lastIndexOf(".")) + ".pdf";
                        String fileName = URLUtil.guessFileName(fileUrl, null, null);

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
            }


            sendReport(STATUS_FINISHED);

        } catch (IOException e) {
            sendReport(STATUS_ERROR);
            e.printStackTrace();
            return;
        }

        try {
            writeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void writeFile() throws IOException {
        File data = new File(aipFolder, "data.txt");
        FileWriter writer = new FileWriter(data);

        writer.append("{\n");

        writer.flush();
        writer.close();

    }


    private void sendReport(int status){
        Context context = getApplicationContext();
        Intent intent = new Intent(context.getString(R.string.set_intent_aip_download));

        intent.putExtra(context.getString(R.string.set_intent_aip_status), status);
        intent.putExtra(context.getString(R.string.set_intent_aip_count), downloadedcount);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
