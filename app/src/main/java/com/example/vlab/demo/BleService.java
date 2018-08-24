package com.example.vlab.demo;

import android.app.Service;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.bluetooth.*;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class BleService extends Service {

    static final int MSG_START_SCAN_DEVICE = 1;
    static final int MSG_END_SCAN_DEVICE = 2;
    static final int MSG_FOUND_DEVICE = 3;

//    // Messenger Type
//    final Messenger mMessanger = new Messenger(new IncomingHandler());

    private BluetoothDevice mCurrentDevice = null;

    private boolean debug = true;
    static private BluetoothAdapter mBTAdapter;
    BluetoothLeScanner mLEscanner;
    private final IBinder mBinder = new ServiceBinder();
    private ICallback mCallback;
    private FCallback mFragmentCallback;
    private BluetoothGatt mGatt;

    private Set deviceSet = new HashSet();

    class BTInfo {
        private String MAC;
        private String name;
        private BluetoothDevice device;
        BTInfo(String MAC, String name, BluetoothDevice device) {
            this.MAC = MAC;
            this.name = name;
            this.device = device;
        }

        String getMAC() {return MAC;}
        String getName() {return name;}

        @Override
        public boolean equals(Object op_) {
            BTInfo op = (BTInfo)op_;
            if(this.MAC == op.MAC)
                return true;
            else
                return false;
        }

        @Override
        public int hashCode() {
            return this.MAC.hashCode();
        }

        @Override
        public String toString() {
            return MAC+"\n"+name;
        }
    }

    class ServiceBinder extends Binder {
        BleService getService() {
            return BleService.this;
        }
    }

    public interface ICallback { public void remoteCall(Set<BTInfo> deviceSet); }
    public interface FCallback { public void remoteCall(Data data); }

    public void registerCallback(ICallback cb){ mCallback = cb; }
    public void registerCallback(FCallback cb) {mFragmentCallback = cb;}

//    // Messenger Type
//    class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_START_SCAN_DEVICE:
//                    Toast.makeText(getApplicationContext(), "Start scanning", Toast.LENGTH_SHORT).show();
//                    scanDevice();
//                    break;
//                case MSG_END_SCAN_DEVICE:
//                    Toast.makeText(getApplicationContext(), "End scanning", Toast.LENGTH_SHORT).show(); break;
//                default:
//                    super.handleMessage(msg); break;
//            }
//        }
//    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (debug) {
            android.util.Log.i("서비스 테스트", "onCreate()");
        }

    }

   @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(debug) {
            android.util.Log.i("서비스 테스트", "onStartCommand()");
            Toast.makeText(getApplicationContext(), "service onStartCommand", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if(debug) {
            android.util.Log.i("서비스 테스트", "onDestory()");
            Toast.makeText(getApplicationContext(), "service destory", Toast.LENGTH_SHORT).show();
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /*
        액티비티에서 bindservice를 호출하면 실행됨.
        리턴한 Ibinder 객체는 서비스와 클라이언트사이의 인터페이스 정의임.
         */
        if (debug) {
            android.util.Log.i("서비스 테스트", "onBind()");
        }

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null) {
            // device  not support Bluetooth!!

        } else {
            mLEscanner = mBTAdapter.getBluetoothLeScanner();
            if (!mBTAdapter.isEnabled()) { //사용자에게 블루투스 활성화 query
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);
            }
        }

        return mBinder;
    }


    /**
     * Blooth
     */
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            //When discovery finds a device
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                android.util.Log.i("서비스 테스트", "Device founded");
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                BTInfo info = new BTInfo(device.getAddress(), device.getName());
//                deviceSet.add(info);
//                mCallback.remoteCall(deviceSet);
//                Toast.makeText(getApplicationContext(), device.getName(), Toast.LENGTH_SHORT).show();
//            }
//
//
//        }
//    };

    private ScanCallback mLEScanCallback;

    public void startScan() {
        Toast.makeText(getApplicationContext(), "Start scanning", Toast.LENGTH_SHORT).show();
        deviceSet.clear();
        mLEscanner = mBTAdapter.getBluetoothLeScanner();
        mLEScanCallback = new ScanCallback() {

            public void onScanResult(int callbackType, ScanResult result) {
                //Log.i("callbackType", String.valueOf(callbackType));
                //Log.i("result", result.toString());

                // LE장치가 2개 이상일때는 ???
                BluetoothDevice device = result.getDevice();

                BTInfo info = new BTInfo(
                        device.getAddress(),
                        device.getName(),
                        device

                );

                boolean flag = true;
                for (Object itr : deviceSet) {
                    if (((BTInfo)itr).hashCode() == info.hashCode()) {
                        flag = false;
                    }
                }

                if (flag == true) {
                    if (info.name == null) {
                        info.name = "No named";
                    }
                    deviceSet.add(info);
                    mCallback.remoteCall(deviceSet);
                }
                //connectToDevice(btDevice);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult sr : results) {
                    Log.i("ScanResult - Results", sr.toString());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e("Scan Failed", "Error Code: " + errorCode);
            }
        };
        mLEscanner.startScan(mLEScanCallback);

//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver,filter);
    }

    public void stopScan() {
        android.util.Log.i("서비스 테스트", "End Scanning");
        mLEscanner.stopScan(mLEScanCallback);

        mLEScanCallback = null;
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);
            switch(newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("BLE testing", "Service connect");
                    gatt.discoverServices();

                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("BLE testing", "Service disconnected");
                    break;
                    default:break;
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("BLE testing", services.toString());

            BluetoothGattService service = gatt.getService(UUID.fromString("00001523-1212-efde-1523-785feabcd123"));

            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("00001524-1212-efde-1523-785feabcd123"));
            mGatt.setCharacteristicNotification(characteristic, true);

            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.writeDescriptor(descriptor);
            }
            updateValue(characteristic );


        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("BLE testing", "changedCharacteristic");
            updateValue(characteristic);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i("BLE testing", "readCharacteristic");
            updateValue(characteristic);
        }
    };

    public void connect(String mac) {

        BluetoothDevice device = null;
        for (Object itr : this.deviceSet) {

            if (((BTInfo)itr).getMAC() == mac) {
                device = ((BTInfo) itr).device;
            }
        }

        Log.i("BLE testing", "Connection required");
        mGatt = device.connectGatt(this, false, gattCallback);
        mCurrentDevice = device;
    }

    public void disconnect(String mac) {

        BluetoothDevice device = null;
        for (Object itr : this.deviceSet) {

            if (((BTInfo)itr).getMAC() == mac) {
                device = ((BTInfo) itr).device;
            }
        }

//        mGatt = device.connectGatt(this, false, gattCallback);
        mGatt.disconnect();
        mCurrentDevice = null;
    }



    class Data {
        public String type;
        public int data1, data2, data3;
        Data(String type, int data1 ,int  data2, int data3) {
            this.type = type;
            this.data1 = data1;
            this.data2 = data2;
            this.data3 = data3;
        }
    }


    public void updateValue(BluetoothGattCharacteristic characteristic) {
        Log.i("BLE testing", "Data update");
        byte[] data = characteristic.getValue();
        if (data != null){
            if (data[0] == 0x10) {

                int input1 = (data[1] << 24) + ((data[2] & 0xFF) << 16) + ((data[3] & 0xFF) << 8) + (data[4] & 0xFF);
                int input2 = (data[5] << 24) + ((data[6] & 0xFF) << 16) + ((data[7] & 0xFF) << 8) + (data[8] & 0xFF);
                int input3 = -1;

                mFragmentCallback.remoteCall(new Data("TYPE1", input1, input2, input3));
//            intent.putExtra(EXTRA_DATA1, String.valueOf(input1));
//            intent.putExtra(EXTRA_DATA2, String.valueOf(input2));
//            intent.putExtra(EXTRA_DATA3, String.valueOf(input3));

            } else if (data[0] == 0x11) {

                int input1 = (data[1] << 24) + ((data[2] & 0xFF) << 16) + ((data[3] & 0xFF) << 8) + (data[4] & 0xFF);
                int input2 = (data[5] << 24) + ((data[6] & 0xFF) << 16) + ((data[7] & 0xFF) << 8) + (data[8] & 0xFF);
                int input3 = (data[9] << 24) + ((data[10] & 0xFF) << 16) + ((data[11] & 0xFF) << 8) + (data[12] & 0xFF);

                mFragmentCallback.remoteCall(new Data("TYPE2", input1, input2, input3));
//            intent.putExtra(EXTRA_DATA1, String.valueOf(input1));
//            intent.putExtra(EXTRA_DATA2, String.valueOf(input2));
//            intent.putExtra(EXTRA_DATA3, String.valueOf(input3));

            }
        }


    }



}
