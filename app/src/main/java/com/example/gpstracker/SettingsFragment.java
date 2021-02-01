package com.example.gpstracker;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codekidlabs.storagechooser.StorageChooser;
import com.example.gpstracker.databinding.FragmentSettingsBinding;
import com.example.gpstracker.databinding.RecorderFragmentBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    public StorageChooser chooser;

    public  static final int PERMISSIONS_MULTIPLE_REQUEST_CODE = 123;

    private MyViewModel mViewModel;

    private int startRecordSpeedInt;
    private int startRecordSpeedFraction;

    private String dataPath;
    private int recodFrequency;
    private int startRecodSpeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        setupTextData();
        setupListeners();
        return binding.getRoot();
    }

    private void setupTextData() {
        binding.currentSavePath.setText(mViewModel.getSavePath());
        binding.recordSpeedStartDigit.setText(Integer.toString(mViewModel.getRecodSpeedInt()));
        binding.recordSpeedStartFraction.setText(mViewModel.getRecodSpeedFraction());
        binding.recordFrequency.setText(Integer.toString(mViewModel.getRecodeFrequency() / 1000));
    }

    private void setupListeners(){
        binding.recordPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecorderFragment.showChooser();
            }
        });

        binding.recordSpeedStartDigit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    mViewModel.setRecodSpeedInt(Integer.parseInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.recordSpeedStartFraction.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    mViewModel.setRecodSpeedFraction(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.recordFrequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    mViewModel.setRecodeFrequency(Integer.parseInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.reinitLocationListener();
                Navigation.findNavController(v).popBackStack();
            }
        });
    }
}
