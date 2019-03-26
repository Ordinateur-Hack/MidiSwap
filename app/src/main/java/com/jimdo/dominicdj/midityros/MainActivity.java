package com.jimdo.dominicdj.midityros;

import Usb.UsbCommunicationManager;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements UsbDevicesAdapter.UsbDeviceOnClickHandler {

    private static UsbCommunicationManager usbCommunicationManager;
    private RecyclerView usbDeviceRecyclerView;

    private UsbDevice[] availableDevices;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbDeviceRecyclerView = findViewById(R.id.recyclerview_usb_device_list);

        usbCommunicationManager = new UsbCommunicationManager(this);
        initRecyclerView();
    }

    /**
     * Set up the RecyclerView and fill with elements extracted from {@link UsbCommunicationManager}
     */
    private void initRecyclerView() {
        availableDevices = usbCommunicationManager.getAvailableDevices(); // do this again if device attached/detached

        if (availableDevices.length == 0) {
            Toast.makeText(this, "No MIDI device attached!", Toast.LENGTH_SHORT).show();
        } else { // TODO: can we do this? no setting recyclerView, is it empty or exception?
            String[] devicesInfo = usbCommunicationManager.getDevicesInfo(availableDevices);
            usbDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            UsbDevicesAdapter adapter = new UsbDevicesAdapter(devicesInfo, this);
            usbDeviceRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onClickDevice(int adapterPosition) {
        UsbDevice usbDevice = availableDevices[adapterPosition]; // ensure that order is maintained!!!
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
}
