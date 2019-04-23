package com.jimdo.dominicdj.midiswap.USB;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.*;
import android.util.Log;
import android.widget.Toast;
import com.jimdo.dominicdj.midiswap.BroadcastReceiverWithFlag;

import java.util.ArrayList;
import java.util.Collection;

// very helpful: https://stackoverflow.com/questions/19736301/android-usb-host-read-from-device
public class UsbCommunicationManager {

    private Context context; // context passed from MainActivity

    private static final String ACTION_USB_PERMISSION = "com.jimdo.dominicdj.USB_PERMISSION";
    private boolean forceClaim = true;

    private UsbManager usbManager;

    private static boolean closedMyUsbDeviceConnection = false;
    private static MyUsbDeviceConnection myUsbDeviceConnection;

    private static final String TAG = UsbCommunicationManager.class.getSimpleName();

    private UsbConnectionListener usbConnectionListener;

    public interface UsbConnectionListener {
        void notifyAddItem(UsbDevice newDevice);

        void notifyRemoveItem(UsbDevice newDevice);

        void onConnectionSuccessful(UsbDevice usbDevice);
    }

    // initiate communication with USB-device
    // this includes to ask the user for permission
    private PendingIntent usbPermissionIntent;
    private final BroadcastReceiverWithFlag usbPermissionReceiver = new BroadcastReceiverWithFlag() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    // TODO: notify MainActivity that permission was granted
                    UsbDevice newUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (newUsbDevice != null) {
                        // Set up connection
                        // at the moment only for TYROS 3 !!!
                        // TODO: more general approach
                        UsbInterface intf = newUsbDevice.getInterface(0);
                        Log.d(TAG, "Set up connection with Interface: " + intf);
                        UsbEndpoint sendEndpoint = intf.getEndpoint(0);
                        UsbEndpoint receiveEndpoint = intf.getEndpoint(1);

                        UsbDeviceConnection connection = usbManager.openDevice(newUsbDevice);
                        connection.claimInterface(intf, forceClaim);

                        // Initialize an object to communicate with this specific device
                        myUsbDeviceConnection = new MyUsbDeviceConnection(connection, intf, sendEndpoint,
                                receiveEndpoint);
                        closedMyUsbDeviceConnection = false;

                        // TODO: check if connection is properly set up
                        /*usbDevice != null && intf != null && receiveEndpoint != null && sendEndpoint != null
                        && connection != null;*/

                        // now we can assume that everything worked correctly,
                        // so we can notify MainActivity to process further (redirect to Operations)
                        usbConnectionListener.onConnectionSuccessful(newUsbDevice);
                    }
                } else {
                    Log.d(TAG, "Permission denied for a USB-device);");
                }
            }
        }
    };
    private final BroadcastReceiverWithFlag usbReceiver = new BroadcastReceiverWithFlag() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) ||
                    action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice newDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                switch (action) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        Log.d(TAG, "USB device attached.");
                        Toast.makeText(context, "USB device attached",
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "New device to attach: " + newDevice);
                        if (newDevice != null) {
                            usbConnectionListener.notifyAddItem(newDevice);
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        Log.d(TAG, "USB device detached." + newDevice);
                        Toast.makeText(context, "USB device detached",
                                Toast.LENGTH_SHORT).show();
                        if (newDevice != null) {
                            usbConnectionListener.notifyRemoveItem(newDevice);
                        }
                        break;
                }
            }
        }
    };

    public UsbCommunicationManager(Context context, UsbConnectionListener usbConnectionListener) {
        this.context = context;
        this.usbConnectionListener = usbConnectionListener;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        // needed to later to ask permission from user to use the USB-device
        usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        registerReceivers();
    }

    public void registerReceivers() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbReceiver.register(context, usbFilter);
        usbPermissionReceiver.register(context, new IntentFilter(ACTION_USB_PERMISSION));
    }

    public void unregisterReceivers() {
        usbReceiver.unregister(context);
        usbPermissionReceiver.unregister(context);
    }

    public UsbDevice[] getAvailableDevices() {
        Collection<UsbDevice> values = usbManager.getDeviceList().values();
        return values.toArray(new UsbDevice[values.size()]);
    }

    /**
     * @param usbDevices list of plugged in USB-devices
     * @return list giving information about the specified USB-devices
     */
    public ArrayList<String> getDevicesInfo(UsbDevice[] usbDevices) {
        ArrayList<String> devicesInfo = new ArrayList<>();
        int i = 0;
        for (UsbDevice usbDevice : usbDevices) {
            // TODO: parse values, see http://www.linux-usb.org/usb.ids and https://stackoverflow
            StringBuilder buffer = new StringBuilder();
            buffer.append(usbDevice.getVendorId());
            buffer.append(" ");
            buffer.append(usbDevice.getProductId());

            devicesInfo.add(i++, buffer.toString());
        }

        return devicesInfo;
    }

    public String getDeviceInfo(UsbDevice usbDevice) {
        UsbDevice[] oneUsbDevice = new UsbDevice[1];
        oneUsbDevice[0] = usbDevice;
        ArrayList<String> oneUsbDeviceRepresentation = getDevicesInfo(oneUsbDevice);
        return oneUsbDeviceRepresentation.get(0);
    }

    public boolean connectToUsbDevice(UsbDevice usbDevice) {
        // this is the device the user selected (in the RecyclerView)
        usbManager.requestPermission(usbDevice, usbPermissionIntent); // user must approve of connection
        Log.d(TAG, "Has Permission? " + usbManager.hasPermission(usbDevice));
        return usbManager.hasPermission(usbDevice); // check if user got permission
    }

    public static MyUsbDeviceConnection getMyUsbDeviceConnection() {
        return !closedMyUsbDeviceConnection ? myUsbDeviceConnection : null;
    }

    public static void closeMyUsbDeviceConnection() {
        if (!closedMyUsbDeviceConnection && myUsbDeviceConnection != null /*just to be on the safe side*/) {
            myUsbDeviceConnection.releaseUsb();
            myUsbDeviceConnection = null;
            closedMyUsbDeviceConnection = true;
        }
    }

    public void onDestroy() {
        // We have to set these values to null, otherwise this could result in memory leaks,
        // because the activity is destroyed, but we still hold a reference to its context
        context = null; // we pass in the application context, however it is save to set the context to null as well
        usbConnectionListener = null;
    }

}