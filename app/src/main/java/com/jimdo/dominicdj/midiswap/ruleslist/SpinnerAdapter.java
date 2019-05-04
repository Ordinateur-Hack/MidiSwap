package com.jimdo.dominicdj.midiswap.ruleslist;

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
import android.widget.Spinner;
import android.widget.TextView;
import com.jimdo.dominicdj.midiswap.R;
import com.jimdo.dominicdj.midiswap.midimessage.MidiChannelController;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<MidiChannelController> {

    private int resource;
    private int textViewResourceId;

    private int resourceDropdown;
    private int textViewDropdownResourceId;

    private View.OnClickListener settingsOnClickListener;


    public SpinnerAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId,
                          @LayoutRes int resourceDropdown, @IdRes int textViewDropdownResourceId,
                          @NonNull List<MidiChannelController> objects, View.OnClickListener settingsOnClickListener) {
        super(context, resource, textViewResourceId, objects);
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
        this.resourceDropdown = resourceDropdown;
        this.textViewDropdownResourceId = textViewDropdownResourceId;
        this.settingsOnClickListener = settingsOnClickListener;
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
        SpinnerViewHolder holder; // to reference the child views for later actions
        if (convertView == null) {
            // Inflate layout for a new view.
            if (isDropDownView) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(resourceDropdown, parent, false);
            } else {
                convertView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            }
            // Cache view fields into a new ViewHolder.
            holder = new SpinnerViewHolder(convertView, isDropDownView);
            // Associate the holder with the view for later lookup.
            convertView.setTag(holder);
        } else {
            // View already exists, so get the holder instance from the view.
            holder = (SpinnerViewHolder) convertView.getTag();
        }
        MidiChannelController midiChannelController = getItem(position);
        if (midiChannelController != null) {
            holder.bindData(parent, midiChannelController);
        }

        return convertView;
    }


    private class SpinnerViewHolder {

        private TextView nameTextView; // name of MidiChannelController
        private ImageButton settingsButton; // 'settings'-icon

        public SpinnerViewHolder(@NonNull View view, boolean isDropDownView) {
            if (isDropDownView) {
                nameTextView = view.findViewById(textViewDropdownResourceId);
            } else {
                nameTextView = view.findViewById(textViewResourceId);
                settingsButton = view.findViewById(R.id.image_button_item_settings);
            }
        }

        private void bindData(@NonNull ViewGroup parent, @NonNull MidiChannelController midiChannelController) {
            nameTextView.setText(midiChannelController.getName());
            if (settingsButton != null) {
                Spinner spinner = (Spinner) parent;
                settingsButton.setTag(R.id.TAG_SPINNER, spinner);
                settingsButton.setTag(R.id.TAG_MIDI_CHANNEL_CONTROLLER, midiChannelController);
                settingsButton.setOnClickListener(settingsOnClickListener);
            }
        }

    }

}
