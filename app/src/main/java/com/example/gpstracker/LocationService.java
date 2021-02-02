package com.example.gpstracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class LocationService extends Service {

    private static final String TAG = "MyLocationService";
    private static final float LOCATION_DISTANCE = 10f;

    private LocationManager mLocationManager = null;
    private Location liveLoc;
    private Location prevLoc;

    private boolean recordingLocation;

    private int recordFrequency;
    private double recordSpeed;
    private String savePath;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        LocationService getService() { return LocationService.this;}
    }

    private MutableLiveData<String> locationLiveData;

    public LiveData<String> getLocationLiveData() {
        if (locationLiveData == null) {
            locationLiveData = new MutableLiveData<>();
        }
        return locationLiveData;
    }

    public boolean getRecordingLocation(){
        return recordingLocation;
    }

    public void setSavePath(String savePath){
        this.savePath = savePath;
    }

    public void setRecordFrequency(int recordFrequency){
        this.recordFrequency = recordFrequency;
    }

    public void setRecordSpeed(double recordSpeed) {
        this.recordSpeed = recordSpeed;
    }

    LocationListener mLocationListener=new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            prevLoc.setLatitude(liveLoc.getLatitude());
            prevLoc.setLongitude(liveLoc.getLongitude());
            liveLoc.setLatitude(location.getLatitude());
            liveLoc.setLongitude(location.getLongitude());

            // obliczanie predkosci
            float elapsedTimeInSeconds = recordFrequency == 0 ? 1000 : recordFrequency; // sprawdzamy pozycje co sekunde
            float distanceInMeters = prevLoc.distanceTo(liveLoc); // dystans z poprzedniej pozycji do nowej
            liveLoc.setSpeed((distanceInMeters / elapsedTimeInSeconds) > 1000 ? 0 : (distanceInMeters / elapsedTimeInSeconds));


            Log.i(TAG, "location read");
            if (liveLoc.getSpeed() >= recordSpeed) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getDefault());
                String date = dateFormat.format(Calendar.getInstance().getTime());
                String msg = String.format("New Latitude: %.5f\nNew Longitude: %.5f\nMovement speed: %.5f\n%s",
                        liveLoc.getLatitude(), liveLoc.getLongitude(), liveLoc.getSpeed(), date);

                locationLiveData.postValue("Trwa nagrywanie, ostatni zapis z " + date);
                appendLog(msg);
                Log.i("Save", savePath == null ? getApplication().getApplicationInfo().dataDir : savePath);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    };

    private void appendLog(String text)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getDefault());
        String fileName = dateFormat.format(Calendar.getInstance().getTime()) + ".txt";
        File logFile = new File(savePath, fileName);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.append("--------------------");
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    recordFrequency,
                    LOCATION_DISTANCE,
                    mLocationListener
            );
            recordingLocation = true;
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mLocationManager.removeUpdates(mLocationListener);
                recordingLocation = false;
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listener, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager - LOCATION_INTERVAL: "+ recordFrequency + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            liveLoc = new Location("dummyprovider");
            prevLoc = new Location("dummyprovider");
        }
    }
}