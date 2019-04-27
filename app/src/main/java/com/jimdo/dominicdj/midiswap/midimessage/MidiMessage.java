package com.jimdo.dominicdj.midiswap.midimessage;

public class MidiMessage {

    protected String hexMessage;

    public MidiMessage() {
    }

    public MidiMessage(String hexMessage) {
        this.hexMessage = hexMessage;
    }

    public String getHexMessage() {
        return hexMessage;
    }
}
