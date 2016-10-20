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
    private ArrayList<Integer> docDepth = new ArrayList<>();
    private ArrayList<String> docValues = new ArrayList<>();

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

            //if (writeJsonFile())
            if (writeFile())
                sendReport(STATUS_FINISHED);
            else
                sendReport(STATUS_ERROR);

        } catch (IOException e) {
            sendReport(STATUS_ERROR);
            e.printStackTrace();
        }
    }

    private boolean writeFile() throws IOException {
        if (docDepth.size() != docValues.size()){
            return false;
        }

        File data = new File(aipFolder, "data.txt");
        FileWriter writer = new FileWriter(data);

        for (int i = 0; i < docDepth.size(); i++){
            if (docDepth.get(i) == -1) {
                writer.append(":");
                writer.append(docValues.get(i));
            }
            else{
                if (i > 0) writer.append("\n");

                writer.append(String.valueOf(docDepth.get(i)));
                writer.append(":\"");
                writer.append(docValues.get(i));
                writer.append("\"");
            }
        }

        writer.flush();
        writer.close();

        return true;
    }


    private void sendReport(int status){
        Context context = getApplicationContext();
        Intent intent = new Intent(context.getString(R.string.set_intent_aip_download));

        intent.putExtra(context.getString(R.string.set_intent_aip_status), status);
        intent.putExtra(context.getString(R.string.set_intent_aip_count), downloadedcount);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
