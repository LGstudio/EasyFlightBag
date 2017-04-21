package sk.lgstudio.easyflightbag.calculations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by LGstudio on 2017-03-06.
 */

public class CalculatorWind extends Calculator implements SeekBar.OnSeekBarChangeListener {

    private TextView image;
    private TextView txtHeadWind;
    private TextView txtTailWind;
    private TextView txtLeftWind;
    private TextView txtRightWind;
    private TextView txtMetar;

    private int runwayNum = 1;
    private int windDirection = 90;
    private int windSpeed = 0;

    private VectorDrawable vectorRunway;
    private VectorDrawable vectorCompass;
    private VectorDrawable vectorWind;
    private int imgH;
    private int imgW;

    Context context;

    public CalculatorWind(Context ctx) {
        super(null, null, null);
        context = ctx;
        vectorRunway = (VectorDrawable) ctx.getDrawable(R.drawable.compass_runway);
        vectorCompass = (VectorDrawable) ctx.getDrawable(R.drawable.compass);
        vectorWind = (VectorDrawable) ctx.getDrawable(R.drawable.compass_wind);
        imgH = vectorCompass.getIntrinsicHeight();
        imgW = vectorCompass.getIntrinsicWidth();
        vectorRunway.setBounds(0,0,imgW,imgH);
        vectorCompass.setBounds(0,0,imgW,imgH);
        vectorWind.setBounds(0,0,imgW,imgH);
    }
    @Override
    public void initView(View v) {

        image = (TextView) v.findViewById(R.id.wind_image);
        txtHeadWind = (TextView) v.findViewById(R.id.wind_head_wind);
        txtTailWind = (TextView) v.findViewById(R.id.wind_tail_wind);
        txtLeftWind = (TextView) v.findViewById(R.id.wind_left_wind);
        txtRightWind = (TextView) v.findViewById(R.id.wind_right_wind);
        txtMetar = (TextView) v.findViewById(R.id.wind_metar);

        SeekBar sRunway = (SeekBar) v.findViewById(R.id.wind_seek_runway);
        SeekBar sWindDir = (SeekBar) v.findViewById(R.id.wind_seek_dir);
        SeekBar sWindSp = (SeekBar) v.findViewById(R.id.wind_seek_speed);

        sRunway.setMax(35);
        sWindDir.setMax(35);
        sWindSp.setMax(50);

        sWindDir.setProgress(windDirection/10);

        sRunway.setOnSeekBarChangeListener(this);
        sWindDir.setOnSeekBarChangeListener(this);
        sWindSp.setOnSeekBarChangeListener(this);

        (new RedrawImage()).execute();

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.wind_seek_runway:
                runwayNum = (progress+1);
                break;
            case R.id.wind_seek_dir:
                windDirection = (progress+1)*10;
                break;
            case R.id.wind_seek_speed:
                windSpeed = progress;
                break;
        }

        (new RedrawImage()).execute();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {    }

    private class RedrawImage extends AsyncTask<Void, Void, Void>{

        private Bitmap bmp;
        private String head = "0";
        private String tail = "0";
        private String left = "0";
        private String right = "0";

        @Override
        protected Void doInBackground(Void... params) {
            bmp = Bitmap.createBitmap(imgW, imgH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            vectorRunway.draw(canvas);
            canvas.rotate((float) -runwayNum*10, imgW/2, imgH/2);
            vectorCompass.draw(canvas);
            canvas.rotate((float) windDirection, imgW/2, imgH/2);
            vectorWind.draw(canvas);
            canvas.rotate((float) -windDirection, imgW/2, imgH/2);


            int relativeDirection = (windDirection-(runwayNum*10));

            if (relativeDirection < 0)
                relativeDirection = 360 + relativeDirection;

            double cos = Math.cos(Math.toRadians(relativeDirection)); // head/tail
            double sin = Math.sin(Math.toRadians(relativeDirection)); // left/right

            if (cos > 0)
                head = new DecimalFormat("#.#").format(cos*windSpeed);
            else
                tail = new DecimalFormat("#.#").format(Math.abs(cos*windSpeed));

            if (sin > 0)
                right = new DecimalFormat("#.#").format(sin*windSpeed);
            else
                left = new DecimalFormat("#.#").format(Math.abs(sin*windSpeed));

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            String rwy = "";
            if (runwayNum < 10) rwy = "0" + runwayNum;
            else rwy = String.valueOf(runwayNum);
            image.setText(rwy);
            image.setBackground(new BitmapDrawable(context.getResources(), bmp));

            txtHeadWind.setText(head);
            txtTailWind.setText(tail);
            txtLeftWind.setText(left);
            txtRightWind.setText(right);

            txtMetar.setText("RWY" + rwy + " " + windDirection + "° " + windSpeed + "kt" );
        }
    }
}
