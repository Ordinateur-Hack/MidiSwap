package com.jimdo.dominicdj.midiswap.USB;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.util.Log;
import com.jimdo.dominicdj.midiswap.Utils.Conversion;

import java.nio.ByteBuffer;

/**
 * Defines the connection to a specific USB-device and allows to communicate with it.
 */
public class MyUsbDeviceConnection {

    private UsbDeviceConnection connection;
    private UsbInterface intf; // only used to release resources
    private UsbEndpoint sendEndpoint; // from android to USB-device
    private UsbEndpoint receiveEndpoint; // from USB-device to android

    private static int TIMEOUT = 3000;

    private static final String TAG = MyUsbDeviceConnection.class.getSimpleName();


    public MyUsbDeviceConnection(UsbDeviceConnection connection, UsbInterface intf, UsbEndpoint sendEndpoint,
                                 UsbEndpoint receiveEndpoint) {
        this.connection = connection;
        this.intf = intf;
        this.sendEndpoint = sendEndpoint;
        this.receiveEndpoint = receiveEndpoint;
    }

    public boolean send(final byte[] bytes) {
        // maybe helpful:
        // https://stackoverflow.com/questions/42023388/no-response-from-android-bulktransfer-with-proper-endpoints
        // https://stackoverflow.com/questions/12345953/android-usb-host-asynchronous-interrupt-transfer

        String hexMessage = Conversion.toHexString(bytes);
        final int[] sentBytes = {-1};
        // TODO: do this on another thread
        sentBytes[0] = connection.bulkTransfer(sendEndpoint, bytes, bytes.length, TIMEOUT);
        /*new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();*/
        Log.d(TAG, "sentBytes: " + sentBytes[0]);
        if (sentBytes[0] < 0) { // failure
            Log.d(TAG, "Couldn't send message " + hexMessage);
            return false;
        } else {
            Log.d(TAG, "Successfully sent message " + hexMessage);
            return true;
        }
    }

    public ByteBuffer receive() {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        UsbRequest request = new UsbRequest();
        request.initialize(connection, receiveEndpoint);

        // queue an inbound request on the bulk transfer endpoint
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request.queue(buffer);
        } else {
            request.queue(buffer, 512);
        }
        // wait for event (confirmation that request was completed)
        if (connection.requestWait() == request) {
            Log.d(TAG, "USB request successfully completed.");
            return buffer;
        } else {
            Log.d(TAG, "USB request failed!");
            return null;
        }
    }

    public void releaseUsb() {
        if (connection != null) {
            if (intf != null) {
                connection.releaseInterface(intf);
            }
            connection.close();
        }
        Log.d(TAG, "MyUsbDeviceConnection successfully closed.");
    }

}
