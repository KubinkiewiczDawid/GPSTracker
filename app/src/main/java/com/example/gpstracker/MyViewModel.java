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

public class MyViewModel extends AndroidViewModel implements SensorEventListener {

    private LocationManager lm;
    private Location liveLoc;
    private Location prevLoc;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private MutableLiveData<String> sensorDataLiveData;

    private boolean recordingLocation;
    public boolean getRecordingLocation(){
        return recordingLocation;
    }
    public void setRecordingLocation(boolean recordingLocation){
        this.recordingLocation = recordingLocation;
    }


    public LiveData<String> getCurrentSensorData() {
        if (sensorDataLiveData == null) {
            sensorDataLiveData = new MutableLiveData<>();
        }
        return sensorDataLiveData;
    }

    private MutableLiveData<String> locationLiveData;

    public LiveData<String> getLocationLiveData() {
        if (locationLiveData == null) {
            locationLiveData = new MutableLiveData<>();
        }
        return locationLiveData;
    }

    private int recodeFrequency;
    public void setRecodeFrequency(int recodeFrequency){
        this.recodeFrequency = recodeFrequency * 1000;
        if(recordingLocation)
            registerLocationService();
    }

    private int recodeSpeed = 5;
    public void setRecodeSpeed(int recodeSpeed){
        this.recodeSpeed = recodeSpeed;
    }

    private String savePath;
    public void setSavePath(String savePath){
        this.savePath = savePath;
    }

    public MyViewModel(@NonNull Application application) {
        super(application);
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
            float elapsedTimeInSeconds = 1; // sprawdzamy pozycje co sekunde
            float distanceInMeters = prevLoc.distanceTo(liveLoc); // dystans z poprzedniej pozycji do nowej
            liveLoc.setSpeed((distanceInMeters / elapsedTimeInSeconds) > 350 ? 0 : (distanceInMeters / elapsedTimeInSeconds));


            String msg = String.format("New Latitude: %.5f\nNew Longitude: %.5f\nMovement speed: %.5f",
                    liveLoc.getLatitude(), liveLoc.getLongitude(), liveLoc.getSpeed());

            Log.i("Location myViewModel", "location read");

            locationLiveData.postValue(msg);
            if (liveLoc.getSpeed() >= recodeSpeed) {
                //tutaj zapis do pliku savePath
                Log.i("Save path myViewModel", savePath == null ? getApplication().getApplicationInfo().dataDir : savePath);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    public void registerSensors(){
        sensorManager = (SensorManager) getApplication().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensors(){
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            sensorDataLiveData.postValue(event.values[0] + " " + event.values[1] + " " + event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
