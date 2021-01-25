package com.example.gpstracker;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.gpstracker.databinding.RecorderFragmentBinding;

public class RecorderFragment extends Fragment {

    private RecorderFragmentBinding binding;
    private MyViewModel mViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = RecorderFragmentBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(MyViewModel.class);

        final Observer<String> sensorsObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                //binding.recordingInfo.setText(newName);
            }
        };

        final Observer<String> locationObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                binding.recordingInfo.setText(newName);
            }
        };

        mViewModel.getCurrentSensorData().observe(getViewLifecycleOwner(), sensorsObserver);
        mViewModel.getLocationLiveData().observe(getViewLifecycleOwner(), locationObserver);

        setupButtonsListeners();
        return binding.getRoot();
    }

    private void setupButtonsListeners(){
        binding.recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewModel.getRecordingLocation()){
                    stopRecording(binding.recordingButton);
                } else {
                    startRecording(binding.recordingButton);
                }
            }
        });
        binding.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_recorderFragment_to_settingsFragment);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mViewModel.getRecordingLocation()){
            binding.recordingButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.red_box, null));
            binding.recordingButton.setText(R.string.zatrzymaj);
        }else{
            binding.recordingButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.yellow_box, null));
            binding.recordingButton.setText(R.string.uruchom_w_tle);
        }
    }

    private void startRecording(Button button){
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.red_box, null));
        button.setText(R.string.zatrzymaj);
        mViewModel.registerSensors();
        mViewModel.registerLocationService();
    }

    private void stopRecording(Button button){
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.yellow_box, null));
        button.setText(R.string.uruchom_w_tle);
        mViewModel.unregisterSensors();
        mViewModel.unregisterLocationService();
    }


}
