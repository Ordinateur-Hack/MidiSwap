package com.jimdo.dominicdj.midiswap;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelController;

import java.util.List;

public class CustomMidiControllerAdapter extends ArrayAdapter<MidiChannelController> {

    private int resource;
    private int textViewResourceId;
    private int resourceDropdown;
    private int textViewDropdownResourceId;

    public CustomMidiControllerAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId,
                                       @LayoutRes int resourceDropdown, @IdRes int textViewDropdownResourceId,
                                       @NonNull List<MidiChannelController> objects) {
        super(context, resource, textViewResourceId, objects);
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.resourceDropdown = resourceDropdown;
        this.textViewDropdownResourceId = textViewDropdownResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return instantiateCustomView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return instantiateCustomView(position, convertView, parent, true);
    }

    private View instantiateCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent,
                                       boolean isDropDownView) {
        MyViewHolder holder; // to reference the child views for later actions
        if (convertView == null) {
            // Cache view fields into the holder.
            holder = new MyViewHolder();
            if (isDropDownView) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(resourceDropdown, parent, false);
                holder.nameTextView = convertView.findViewById(textViewDropdownResourceId);
            } else {
                convertView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                holder.nameTextView = convertView.findViewById(textViewResourceId);
                holder.spinnerItemSettingsImageButton = convertView.findViewById(R.id.image_button_spinner_item_settings);
            }
            // Associate the holder with the view for later lookup.
            convertView.setTag(holder);
        } else {
            // View already exists, so get the holder instance from the view.
            holder = (MyViewHolder) convertView.getTag();
        }

        // no findViewById here because we used the view holder pattern
        // instead we can use holder.nameTextView
        MidiChannelController midiChannelController = getItem(position);
        holder.nameTextView.setText(midiChannelController.getName());
        if (holder.spinnerItemSettingsImageButton != null) {
            holder.spinnerItemSettingsImageButton.setTag(R.id.TAG_MIDI_CONTROLLER, midiChannelController);
        }

        return convertView;
    }

    private static class MyViewHolder {
        private TextView nameTextView;
        private ImageButton spinnerItemSettingsImageButton;
    }

}
