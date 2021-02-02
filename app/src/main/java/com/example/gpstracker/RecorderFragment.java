package com.example.gpstracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.codekidlabs.storagechooser.StorageChooser;
import com.example.gpstracker.databinding.RecorderFragmentBinding;

public class RecorderFragment extends Fragment {

    private RecorderFragmentBinding binding;
    private MyViewModel mViewModel;

    public static StorageChooser chooser;

    private LocationService locationService;

    boolean bounded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = RecorderFragmentBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);

        if(!bounded)
            doBindService();

        initChooser();

        return binding.getRoot();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationService = binder.getService();
        //    mBound = true;
            if(locationService != null){
                Log.i("service-bind", "Service is bonded successfully!");
                bounded = true;
                initService();
                init();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    void doBindService() {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        if (getActivity().bindService(new Intent(getActivity(), LocationService.class),
                connection, Context.BIND_AUTO_CREATE)) {
            //bounded = true;

        } else {
            Log.e("MY_APP_TAG", "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    void doUnbindService() {
        if (bounded) {
            // Release information about the service's state.
            Intent intent = new Intent(getActivity(), LocationService.class);
            getActivity().stopService(intent);
            getActivity().unbindService(connection);
            bounded = false;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupButtonsListeners(){
        binding.recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locationService.getRecordingLocation()){
                    stopRecording(binding.recordingButton);
                } else {
                    if(mViewModel.getSavePath() != null)
                        startRecording(binding.recordingButton);
                    else{
                        chooser.show();
                    }
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
        if(bounded) {
            initService();
            init();
            if (locationService.getRecordingLocation()) {
                binding.recordingButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.red_box, null));
                binding.recordingButton.setText(R.string.zatrzymaj);
            } else {
                binding.recordingButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.yellow_box, null));
                binding.recordingButton.setText(R.string.uruchom_w_tle);
            }
        }
    }

    private void init(){
        final Observer<String> locationObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                binding.recordingInfo.setText(newName);
            }
        };

        locationService.getLocationLiveData().observe(getViewLifecycleOwner(), locationObserver);
        setupButtonsListeners();
    }

    private void initService(){
        if(bounded) {
            locationService.setRecordSpeed(mViewModel.getRecodSpeed());
            locationService.setRecordFrequency(mViewModel.getRecodFrequency());
            locationService.setSavePath(mViewModel.getSavePath());
        }
    }

    private void startRecording(Button button){
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.red_box, null));
        button.setText(R.string.zatrzymaj);

        Intent intent = new Intent(getActivity(), LocationService.class);
        if(!bounded) {
            doBindService();
        }
        initService();
        getActivity().startService(intent);
    }

    private void stopRecording(Button button){
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.yellow_box, null));
        button.setText(R.string.uruchom_w_tle);

        doUnbindService();
    }

    private void initChooser(){
        chooser = new StorageChooser.Builder()
                // Specify context of the dialog
                .withActivity(getActivity())
                .withFragmentManager(((MainActivity)getActivity()).getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                // Define the mode as the FOLDER/DIRECTORY CHOOSER
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();

// 2. Handle what should happend when the user selects the directory !
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                // e.g /storage/emulated/0/Documents
                mViewModel.setSavePath(path);
                Log.i("Path: " , path);
            }
        });
    }

    public static void showChooser(){
        chooser.show();
    }
}
