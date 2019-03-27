package com.jimdo.dominicdj.midityros;

import Usb.UsbCommunicationManager;
import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements UsbDevicesAdapter.UsbDeviceOnClickHandler,
        UsbCommunicationManager.UsbDevicesListener {

    private static UsbCommunicationManager usbCommunicationManager;
    private RecyclerView usbDeviceRecyclerView;
    private static UsbDevicesAdapter usbDevicesAdapter;

    private UsbDevice[] availableDevices;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbDeviceRecyclerView = findViewById(R.id.recyclerview_usb_device_list);

        usbCommunicationManager = new UsbCommunicationManager(this, this);
        initRecyclerView();
    }

    /**
     * Set up the RecyclerView and fill with elements extracted from {@link UsbCommunicationManager}
     */
    private void initRecyclerView() {
        availableDevices = usbCommunicationManager.getAvailableDevices();
        usbDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // set adapter

        if (availableDevices.length == 0) {
            Log.d(TAG, "No midi device attached");
            Toast.makeText(this, "No MIDI device attached", Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<String> devicesInfo = usbCommunicationManager.getDevicesInfo(availableDevices);
            usbDevicesAdapter = new UsbDevicesAdapter(devicesInfo, this);
            usbDeviceRecyclerView.setAdapter(usbDevicesAdapter);
        }
    }

    @Override
    public void onClickDevice(int adapterPosition) {
        availableDevices = usbCommunicationManager.getAvailableDevices(); // to prevent index out of bound exception
        UsbDevice usbDevice = availableDevices[adapterPosition]; // TODO: ensure that order is maintained!!!
        if (usbCommunicationManager.connectToUsbDevice(usbDevice)) {
            Log.d(TAG, "Connected to usbDevice: " + usbDevice);
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Operations.class));
        } else {
            Log.d(TAG, "Connection to usbDevice failed: " + usbDevice);
        }
    }

    public static UsbCommunicationManager getUsbCommunicationManager() {
        return usbCommunicationManager;
    }

    public static UsbDevicesAdapter getUsbDevicesAdapter() {
        return usbDevicesAdapter;
    }

    @Override
    public void appendItem(UsbDevice newDevice) {
        if (usbDevicesAdapter == null) {
            usbDevicesAdapter = new UsbDevicesAdapter(new ArrayList<String>(), this); // empty arraylist?
            usbDeviceRecyclerView.setAdapter(usbDevicesAdapter);
        }
        // update adapter
        usbDevicesAdapter.appendItem(usbCommunicationManager.getDeviceInfo(newDevice));
    }

    @Override
    public void deleteItem(UsbDevice newDevice) {
        // if usbDevicesAdapter is null, no item can be deleted (there hasn't been an item before)
        if (usbDevicesAdapter == null) {
            return;
        }
        // find out the index of newDevice in adapter
        ArrayList<String> devicesInfoList = usbDevicesAdapter.getDeviceInfoList(); // TODO: memory leak possible?
        String newDeviceRepresentation = usbCommunicationManager.getDeviceInfo(newDevice); // TODO: make a contract
        int index = -1;
        for (int i = 0; i < devicesInfoList.size(); i++) {
            if (devicesInfoList.get(i).equals(newDeviceRepresentation)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // update adapter
            usbDevicesAdapter.deleteItem(index);
        }
    }
}
