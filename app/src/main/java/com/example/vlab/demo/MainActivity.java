package com.example.vlab.demo;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import java.util.HashSet;
import java.util.Set;

import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Messenger mServiceMessenger = null;

    private BleService.ServiceBinder mServiceBinder = null;
    private BleService mService;
    private boolean mBound = false;
    private boolean debug = true;
    private Button btnStartScan, btnEndScan;
    private ProgressBar pgbScanning;
    private BleService.BTInfo selectBTInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Main Activity");

        btnStartScan = (Button) findViewById(R.id.btn_start_scan);
        btnEndScan = (Button) findViewById(R.id.btn_end_scan);
        pgbScanning = (ProgressBar) findViewById(R.id.progressBar);

        btnStartScan.setOnClickListener(buttonOnClickListener);
        btnEndScan.setOnClickListener(buttonOnClickListener);

//        btnStartScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(mService!=null) {
//                    LinearLayout scView = (LinearLayout)findViewById(R.id.scrollLinear);
//                    scView.removeAllViews();
//                    mService.startScan();
//                    pgbScanning.setVisibility(pgbScanning.VISIBLE);
//                }
////                // Messenger Type
////                if(mBound) {
////                    Message msg = Message.obtain(null, BleService.MSG_START_SCAN_DEVICE, 0,0);
////                    try  {
////
////                        mServiceMessenger.send(msg);
////                        //pgbScanning.setVisibility(pgbScanning.VISIBLE);
////                    } catch (RemoteException e) {
////                        e.printStackTrace();
////                    }
////                    //Toast.makeText(getApplicationContext(), "push Button", Toast.LENGTH_SHORT).show();
////                }
//            }
//        });
//
//        btnEndScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mService.stopScan();
//                pgbScanning.setVisibility(pgbScanning.INVISIBLE);
//                Toast.makeText(getApplicationContext(), "End scanning", Toast.LENGTH_SHORT).show();
////                // Messnger Type
////                if(mBound) {
////                    Message msg = Message.obtain(null, BleService.MSG_END_SCAN_DEVICE, 0,0);
////                    try  {
////                        mServiceMessenger.send(msg);
////                        pgbScanning.setVisibility(pgbScanning.INVISIBLE);
////                    } catch (RemoteException e) {
////                        e.printStackTrace();
////                    }
////                    //Toast.makeText(getApplicationContext(), "push Button", Toast.LENGTH_SHORT).show();
////                }
//            }
//        });

    }

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {

                case R.id.btn_end_scan:
                    mService.stopScan();
                    pgbScanning.setVisibility(pgbScanning.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "End scanning", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_start_scan:
                    if(mService!=null) {
                        LinearLayout scView = (LinearLayout)findViewById(R.id.scrollLinear);
                        scView.removeAllViews();
                        mService.startScan();
                        pgbScanning.setVisibility(pgbScanning.VISIBLE);
                    }
                    break;

            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this,BleService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mConnection);
            mBound = true;
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceBinder = (BleService.ServiceBinder) iBinder;
            mService = mServiceBinder.getService();
            mService.registerCallback(mCallback);

//            // Messenger Type
//            mServiceMessenger = new Messenger(iBinder);
//            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBinder = null;
            mService = null;

//            // Messenger Type
//            mServiceMessenger = null;
//            mBound = false;
        }
    };


    private BleService.ICallback mCallback = new BleService.ICallback() {
        @Override
        public void remoteCall(Set<BleService.BTInfo> device) {
            Toast.makeText(getApplicationContext(), "Found device count : " + device.size(), Toast.LENGTH_SHORT).show();
            LinearLayout scView = (LinearLayout)findViewById(R.id.scrollLinear);
            scView.removeAllViews();
            for (BleService.BTInfo itr : device) {
//                if (true) {
                Log.i("BLE DEVICE LIST",itr.toString());
                    TextView txt = new TextView(getApplicationContext());
                    txt.setText(itr.toString()+" (Hash : "  + itr.hashCode() + ")");
                    if (itr.getName().compareTo("Fleixble Oximetery") == 0) {
                        selectBTInfo = itr;
                    }
                    txt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            createFragment();
                            // TODO : Fragment & Transaction (https://medium.com/bynder-tech/how-to-use-material-transitions-in-fragment-transactions-5a62b9d0b26b)
                            //Toast.makeText(getApplicationContext(), "TODO : Fragment", Toast.LENGTH_SHORT).show();
                        }
                    });
                    scView.addView(txt);


            }
        }
    };

    private void createFragment() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        Fragment mainFragment = new MainFragment();
        Bundle bundle = new Bundle(2);
        bundle.putBinder("serviceBinder", mServiceBinder);
        bundle.putString("deviceMAC", selectBTInfo.getMAC());
        mainFragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.mainLayout, mainFragment);
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

//    class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case BleService.MSG_FOUND_DEVICE:
//                    Toast.makeText(getApplicationContext(), "Device found", Toast.LENGTH_SHORT).show();
//
//                    break;
//                default:
//                    super.handleMessage(msg); break;
//            }
//        }
//    }
}
