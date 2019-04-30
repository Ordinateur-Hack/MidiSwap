package com.jimdo.dominicdj.midiswap.midimessage;

import android.support.annotation.Nullable;

import java.util.List;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.InvalidChannelNumberException;
import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.MidiChannel;

public class MidiController {

    private String name;
    private MidiMessage midiMessage;
    private List<MidiChannel> alternativeMidiChannels; // these MidiChannels are allowed and should include the standard MidiChannel

    public MidiController(String name, boolean isRecvController, List<MidiChannel> alternativeMidiChannels, MidiMessage midiMessage) throws InvalidChannelNumberException {
        this.name = name;
        this.midiMessage = midiMessage;

        if (alternativeMidiChannels != null) {
            this.alternativeMidiChannels = alternativeMidiChannels;
            // Loop through every alternative MidiChannel to make sure that all of them are either Recv-Channels
            // or Send-Channels. If this is not the case, .getByteRepresentation(...) will throw an error.
            for (MidiConstants.MidiChannel alternativeMidiChannel : alternativeMidiChannels) {
                alternativeMidiChannel.getByteRepresentation(isRecvController); // TODO: better approach, don't waste resources
            }
        }
    }

    public String getName() {
        return name;
    }

    public MidiMessage getMidiMessage() {
        return midiMessage;
    }

    @Nullable
    public List<MidiChannel> getAlternativeMidiChannels() {
        return alternativeMidiChannels;
    }

    /**
     * @return a readable representation of this MidiController used in the spinners
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MidiController) {
            MidiController m = (MidiController) obj;
            return m.getName().equals(name) && m.getMidiMessage().equals(midiMessage);
        }
        return false;
    }
}
