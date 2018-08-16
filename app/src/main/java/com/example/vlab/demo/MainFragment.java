package com.example.vlab.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;

public class MainFragment extends Fragment {


    private BleService.ServiceBinder mServiceBinder;
    private BleService mService;
    private String macAddress;

    private BleService.FCallback mCallback = new BleService.FCallback() {
        @Override
        public void remoteCall(BleService.Data data) {
            TextView heartRate = getView().findViewById(R.id.heartRate);
            TextView oxygenRate = getView().findViewById(R.id.heartRate);
            if (data.type == "TYPE1") {
                heartRate.setText("T1 : " + data.data1 + " " + data.data2 + " " + data.data3);
            } else if (data.type == "TYPE2") {
                oxygenRate.setText("T2 : " + data.data1 + " " + data.data2 + " " + data.data3);
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mServiceBinder = (BleService.ServiceBinder)getArguments().getBinder("serviceBinder");
        macAddress = getArguments().getString("deviceMAC");
        return inflater.inflate(R.layout.fragment_main, container, false);
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mService = mServiceBinder.getService();
        mService.connect(macAddress);
        mService.registerCallback(mCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
