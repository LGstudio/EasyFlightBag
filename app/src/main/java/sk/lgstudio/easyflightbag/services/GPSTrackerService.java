package sk.lgstudio.easyflightbag.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import sk.lgstudio.easyflightbag.R;

/**
 * Created by L on 16/09/26.
 */

public class GPSTrackerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final long PERIOD = 500;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationAvailability mLocationAvailability;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(PERIOD)
                .setFastestInterval(PERIOD);
    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mLastLocation = null;
            sendLocation();
        }
    }

    public boolean isPeriodicallyUpdated() {
        return mRequestingLocationUpdates;
    }

    public void setPeriodicLocationUpdates(boolean set) {
        mRequestingLocationUpdates = set;

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        } else {
            stopLocationUpdates();
        }
    }

    public int onStartCommand(Intent intent, int flags, int id) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_NOT_STICKY;
        }
        mLocationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestingLocationUpdates = true;
        createLocationRequest();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onConnected(Bundle status) {
        sendLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        sendLocation();
    }

    public void onConnectionFailed(ConnectionResult result) {
        if (mRequestingLocationUpdates)
            stopLocationUpdates();
    }

    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    public void sendLocation() {

        Context context = getApplicationContext();
        Intent intent = new Intent(context.getString(R.string.gps_intent_filter));
        if (mLastLocation != null) {

            intent.putExtra(context.getString(R.string.gps_latitude), mLastLocation.getLatitude());
            intent.putExtra(context.getString(R.string.gps_longitude), mLastLocation.getLongitude());
            intent.putExtra(context.getString(R.string.gps_accuracy), mLastLocation.getAccuracy());
            intent.putExtra(context.getString(R.string.gps_bearing), mLastLocation.getBearing());
            intent.putExtra(context.getString(R.string.gps_speed), mLastLocation.getSpeed());
            intent.putExtra(context.getString(R.string.gps_time), mLastLocation.getTime());
            intent.putExtra(context.getString(R.string.gps_altitude), mLastLocation.getAltitude());
            intent.putExtra(getString(R.string.gps_enabled), true);
        }
        else {
            intent.putExtra(getString(R.string.gps_enabled), false);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}