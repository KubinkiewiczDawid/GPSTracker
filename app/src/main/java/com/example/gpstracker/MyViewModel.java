package com.example.gpstracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class MyViewModel extends AndroidViewModel {

    private int recodFrequency = 1000;
    public void setRecodFrequency(int recodFrequency){
        this.recodFrequency = recodFrequency * 1000;
    }
    public int getRecodFrequency() {
        return recodFrequency;
    }

    private int recodSpeedInt = 5;
    public void setRecodSpeedInt(int recodSpeedInt){
        this.recodSpeedInt = recodSpeedInt;
    }
    public int getRecodSpeedInt() {
        return recodSpeedInt;
    }

    private String recodSpeedFraction = "0";
    public void setRecodSpeedFraction(String recodSpeedFraction){
        this.recodSpeedFraction = recodSpeedFraction;
    }
    public String getRecodSpeedFraction() {
        return recodSpeedFraction;
    }

    private double recodSpeed = 5;
    public void setRecodSpeed(double recodSpeed){
        this.recodSpeed = recodSpeed;
    }
    public double getRecodSpeed() {
        return recodSpeed;
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
    }
}
