package com.jimdo.dominicdj.midiswap.midimessage;

import java.util.Arrays;
import java.util.List;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.*;

public class MidiChannelMessage extends MidiMessage {

    private MidiChannel standardMidiChannel;

    /**
     * @param midiChannelEvent
     * @param standardMidiChannel
     * @param firstDataByte
     * @param secondDataByte
     * @param isRecvChannel
     * @throws InvalidChannelNumberException
     */
    public MidiChannelMessage(MidiChannelEvent midiChannelEvent, MidiChannel standardMidiChannel,
                              String firstDataByte, String secondDataByte, boolean isRecvChannel) throws InvalidChannelNumberException {

        this.standardMidiChannel = standardMidiChannel;

        // Construct MidiChannelMessage by concatenating its parts.
        String midiChannelByteRepresentation = standardMidiChannel.getByteRepresentation(isRecvChannel);
        String midiChannelEventByteRepresentation = midiChannelEvent.getByteRepresentation();

        if (isRecvChannel) {
            // The Tyros attaches a swapped value-pair of MidiChannel and MidiChannelEvent to the beginning
            // of the messages it sends to a device, like our android phone
            // Example: if the message starts with "90...", the Tyros adds "09"
            // --> the whole message would then be "0990..."
            hexMessage = midiChannelByteRepresentation + midiChannelEventByteRepresentation;
        } else {
            // the Tyros3 needs this value at the beginning of each channel message to recognize sent data
            // TODO: "Learn if this is only true for channel messages or for every message. If true for every message,
            //  then we can put this code into the constructor of MidiMessage
            hexMessage = "1B";
        }

        hexMessage += midiChannelEventByteRepresentation + midiChannelByteRepresentation
                + firstDataByte + secondDataByte;
    }

    public MidiChannelMessage(MidiChannelEvent midiChannelEvent, MidiChannel standardMidiChannel,
                              ControlChange controlChange, boolean isRecvChannel) throws InvalidChannelNumberException {
        this(midiChannelEvent, standardMidiChannel, controlChange.getByteRepresentation(), "XX", isRecvChannel);
    }

    public MidiChannel getStandardMidiChannel() {
        return standardMidiChannel;
    }
}
