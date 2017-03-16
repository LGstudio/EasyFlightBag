package sk.lgstudio.easyflightbag.services.AIPDownloader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sk.lgstudio.easyflightbag.MainActivity;
import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2016-10-18.
 */

public abstract class AIPDownloader extends IntentService {

    public final static int STATUS_STARTED = 0;
    public final static int STATUS_ERROR = 1;
    public final static int STATUS_FINISHED = 2;

    protected File aipFolder;

    protected int country;

    private NotificationCompat.Builder builder = null;
    private NotificationManager notificationManager;

    protected ArrayList<Element> parents = new ArrayList<>();
    protected ArrayList<Integer> docDepth = new ArrayList<>();
    protected ArrayList<String> docValues = new ArrayList<>();

    protected int downloadedcount = 0;

    public AIPDownloader() {
        super("AIP");
    }

    protected boolean writeFile() throws IOException {
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


    protected void sendReport(int status){
        switch (status){
            case STATUS_STARTED:
                if (builder != null){
                    builder.setContentText(String.valueOf(downloadedcount));
                    notificationManager.notify(MainActivity.NOTIFICATION_AIP, builder.build());
                }
                else {
                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    builder = new NotificationCompat.Builder(getApplicationContext())
                            .setContentTitle(getString(R.string.aip_progress_download))
                            .setContentText(String.valueOf(downloadedcount))
                            .setSmallIcon(R.drawable.ic_download)
                            .setAutoCancel(false)
                            .setOngoing(true)
                            .setOnlyAlertOnce(true);

                    notificationManager.notify(MainActivity.NOTIFICATION_AIP ,builder.build());
                }
                break;
            case STATUS_FINISHED:
                notificationManager.cancel(MainActivity.NOTIFICATION_AIP);
            case STATUS_ERROR:
                Context context = getApplicationContext();
                Intent intent = new Intent(context.getString(R.string.service_aip_download));

                intent.putExtra(context.getString(R.string.intent_aip_status), status);
                intent.putExtra(context.getString(R.string.intent_aip_country), country);

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
