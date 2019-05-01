package com.jimdo.dominicdj.midiswap.midimessage;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.InvalidChannelNumberException;
import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.MidiChannel;

public class MidiChannelController {

    private String name;
    private MidiChannelMessage standardMidiMessage;
    private List<MidiChannel> selectedMidiChannels;
    private List<MidiChannel> allAvailableMidiChannels;
    boolean isRecvController;

    public MidiChannelController(String name, boolean isRecvController, @NonNull List<MidiChannel> allAvailableMidiChannels,
                                 @NonNull MidiChannelMessage standardMidiMessage) throws InvalidChannelNumberException {
        this.name = name;
        this.standardMidiMessage = standardMidiMessage;
        this.isRecvController = isRecvController;
        // on initialization this MidiChannelController should have the MidiChannel of the only MidiMessage selected
        List<MidiChannel> selectedMidiChannels = new ArrayList<>(1);
        selectedMidiChannels.add(standardMidiMessage.getMidiChannel());
        this.selectedMidiChannels = selectedMidiChannels;

        this.allAvailableMidiChannels = allAvailableMidiChannels;
        // Loop through every alternative MidiChannel to make sure that all of them are either Recv-Channels
        // or Send-Channels. If this is not the case, .getByteRepresentation(...) will throw an error.
        for (MidiChannel availableMidiChannel : allAvailableMidiChannels) {
            availableMidiChannel.getByteRepresentation(isRecvController); // TODO: better approach, don't waste resources here
        }

    }

    public String getName() {
        return name;
    }

    @NonNull
    public MidiChannelMessage getStandardMidiMessage() {
        return standardMidiMessage;
    }

    @NonNull
    public List<MidiChannel> getAllAvailableMidiChannels() {
        return allAvailableMidiChannels;
    }

    @NonNull
    public List<MidiChannel> getSelectedMidiChannels() {
        return selectedMidiChannels;
    }

    public void selectMidiChannel(MidiChannel midiChannel) {
        selectedMidiChannels.add(midiChannel);
    }

    /**
     * Removes the given MidiChannel from the selected MidiChannels list.
     *
     * @param midiChannel the MidiChannel to remove
     * @return if the selected MidiChannels list contained the MidiChannels that should be removed
     */
    public boolean unselectMidiChannel(MidiChannel midiChannel) {
        return selectedMidiChannels.remove(midiChannel);
    }

    public boolean isRecvController() {
        return isRecvController;
    }


    /**
     * @return a readable representation of this MidiChannelController used in the spinners
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MidiChannelController) {
            MidiChannelController m = (MidiChannelController) obj;
            return m.getName().equals(name) && m.getStandardMidiMessage().equals(standardMidiMessage)
                    && m.getAllAvailableMidiChannels().equals(allAvailableMidiChannels);
        }
        return false;
    }
}
