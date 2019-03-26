package com.jimdo.dominicdj.midityros;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class UsbDevicesAdapter extends RecyclerView.Adapter<UsbDevicesAdapter.UsbDevicesViewHolder> {

    private String[] deviceInfoList;
    private final UsbDeviceOnClickHandler usbDeviceOnClickHandler;

    interface UsbDeviceOnClickHandler {
        void onClickDevice(int adapterPosition);
    }


    public UsbDevicesAdapter(String[] deviceInfoList, UsbDeviceOnClickHandler usbDeviceOnClickHandler) {
        this.deviceInfoList = deviceInfoList;
        this.usbDeviceOnClickHandler = usbDeviceOnClickHandler;
    }

    public class UsbDevicesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView usbDeviceInfo;

        public UsbDevicesViewHolder(View view) {
            super(view);
            usbDeviceInfo = view.findViewById(R.id.tv_usb_device_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            usbDeviceOnClickHandler.onClickDevice(getAdapterPosition());
        }
    }

    @Override
    public UsbDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.usb_device_list_item,
                viewGroup, false);
        return new UsbDevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsbDevicesViewHolder usbDevicesViewHolder, int position) {
        usbDevicesViewHolder.usbDeviceInfo.setText(deviceInfoList[position]);
    }

    @Override
    public int getItemCount() {
        return deviceInfoList.length;
    }
}
