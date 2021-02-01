package com.example.gpstracker;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class MyViewModel extends AndroidViewModel {

    private LocationManager lm;
    private Location liveLoc;
    private Location prevLoc;

    private boolean recordingLocation;
    public boolean getRecordingLocation(){
        return recordingLocation;
    }
    public void setRecordingLocation(boolean recordingLocation){
        this.recordingLocation = recordingLocation;
    }

    private MutableLiveData<String> locationLiveData;

    public LiveData<String> getLocationLiveData() {
        if (locationLiveData == null) {
            locationLiveData = new MutableLiveData<>();
        }
        return locationLiveData;
    }

    private int recodeFrequency = 1000;
    public void setRecodeFrequency(int recodeFrequency){
        this.recodeFrequency = recodeFrequency * 1000;
    }
    public int getRecodeFrequency(){
        return recodeFrequency;
    }

    private int recodSpeedInt = 5;
    public void setRecodSpeedInt(int recodSpeedInt){
        this.recodSpeedInt = recodSpeedInt;
    }
    public int getRecodSpeedInt(){
        return recodSpeedInt;
    }

    private String recodSpeedFraction = "0";
    public void setRecodSpeedFraction(String recodSpeedFraction){
        this.recodSpeedFraction = recodSpeedFraction;
    }
    public String getRecodSpeedFraction(){
        return recodSpeedFraction;
    }

    private double recodSpeed = 5;
    public void setRecodSpeed(double recodSpeed){
        this.recodSpeed = recodSpeed;
    }

    private String savePath;
    public void setSavePath(String savePath){
        this.savePath = savePath;
    }
    public String getSavePath(){
        return savePath;
    }

    public MyViewModel(@NonNull Application application) {
        super(application);
    }

    public void reinitLocationListener(){
        String decimalText = "0." + recodSpeedFraction;
        setRecodSpeed(recodSpeedInt + (Double.parseDouble(decimalText)));
        double x = recodSpeed;
        if(recordingLocation)
            registerLocationService();
    }

    @SuppressLint("MissingPermission")
    public void registerLocationService() {
        if(recordingLocation)
            unregisterLocationService();
        lm = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        liveLoc = new Location("dummyprovider");
        prevLoc = new Location("dummyprovider");
        int recodeFrequencyFinal = recodeFrequency == 0 ? 1000 : recodeFrequency;
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, recodeFrequencyFinal, 10, locationListener);
        setRecordingLocation(true);
    }

    public void unregisterLocationService(){
        lm.removeUpdates(locationListener);
        setRecordingLocation(false);
    }

    LocationListener locationListener=new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            prevLoc.setLatitude(liveLoc.getLatitude());
            prevLoc.setLongitude(liveLoc.getLongitude());
            liveLoc.setLatitude(location.getLatitude());
            liveLoc.setLongitude(location.getLongitude());

            // obliczanie predkosci
            float elapsedTimeInSeconds = recodeFrequency == 0 ? 1000 : recodeFrequency; // sprawdzamy pozycje co sekunde
            float distanceInMeters = prevLoc.distanceTo(liveLoc); // dystans z poprzedniej pozycji do nowej
            liveLoc.setSpeed((distanceInMeters / elapsedTimeInSeconds) > 1000 ? 0 : (distanceInMeters / elapsedTimeInSeconds));

            Log.i("Location", "location read");

            if (liveLoc.getSpeed() >= recodSpeed) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
        public void onStatusChanged(String provider, int status, Bundle extras) {
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
}
