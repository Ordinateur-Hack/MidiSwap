package com.jimdo.dominicdj.midiswap.midimessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MidiConstants {

    public enum MidiChannel {

        RIGHT_1("0", "1"),
        RIGHT_2("1", "2"),
        RIGHT_3("2", "3"),
        LEFT("3", "4"),

        MULTI_PAD_1("4", null),
        MULTI_PAD_2("5", null),
        MULTI_PAD_3("6", null),
        MULTI_PAD_4("7", null),

        EXTRA_PART_1(null, "5"),
        EXTRA_PART_2(null, "6"),
        EXTRA_PART_3(null, "7"),

        STYLE_RHYTHM_1("8", "8"),
        STYLE_RHYTHM_2("9", "9"),
        STYLE_BASS("A", "A"),
        STYLE_CHORD_1("B", "B"),
        STYLE_CHORD_2("C", "C"),
        STYLE_PAD("D", "D"),
        STYLE_PHRASE_1("E", "E"),
        STYLE_PHRASE_2("F", "F");


        private String channelNumberRcv;
        private String channelNumberSend;

        MidiChannel(String channelNumberRcv, String channelNumberSend) {
            this.channelNumberRcv = channelNumberRcv;
            this.channelNumberSend = channelNumberSend;
        }

        public String getByteRepresentation(boolean isRecvChannel) throws InvalidChannelNumberException {
            if (isRecvChannel) {
                if (channelNumberRcv == null) {
                    throw new InvalidChannelNumberException("You cannot receive on the MidiChannel you selected.");
                }
                return channelNumberRcv;
            } else {
                if (channelNumberSend == null) {
                    throw new InvalidChannelNumberException("You cannot send on the MidiChannel you selected.");
                }
                return channelNumberSend;
            }
        }

        public static List<MidiChannel> getVoiceMidiChannels() {
            return Arrays.asList(
                    LEFT,
                    RIGHT_1,
                    RIGHT_2,
                    RIGHT_3
            );
        }

        public static List<MidiChannel> getMultiPadMidiChannels() {
            return Arrays.asList(
                    MULTI_PAD_1,
                    MULTI_PAD_2,
                    MULTI_PAD_3,
                    MULTI_PAD_4
            );
        }

        public static List<MidiChannel> getStyleMidiChannels() {
            return Arrays.asList(
                    STYLE_RHYTHM_1,
                    STYLE_RHYTHM_2,
                    STYLE_BASS,
                    STYLE_CHORD_1,
                    STYLE_CHORD_2,
                    STYLE_PAD,
                    STYLE_PHRASE_1,
                    STYLE_PHRASE_2
            );
        }

        public static List<MidiChannel> getVoiceAndStyleMidiChannels() {
            List<MidiChannel> voiceMidiChannels = getVoiceMidiChannels();
            List<MidiChannel> styleMidiChannels = getStyleMidiChannels();

            List<MidiChannel> voiceAndStyleMidiChannels =
                    new ArrayList<>(voiceMidiChannels.size() + styleMidiChannels.size());
            voiceAndStyleMidiChannels.addAll(voiceMidiChannels);
            voiceAndStyleMidiChannels.addAll(styleMidiChannels);

            return voiceAndStyleMidiChannels;
        }

    }

    public enum MidiChannelEvent {

        KEY_OFF("8"),
        KEY_ON("9"),
        CONTROL_CHANGE("B"),
        MODE_MESSAGE("B"),
        PROGRAM_CHANGE("C"),
        CHANNEL_AFTER_TOUCH("D"),
        POLYPHONIC_AFTER_TOUCH("A"),
        PITCH_BEND("E");

        private String byteRepresentation;

        MidiChannelEvent(String byteRepresentation) {
            this.byteRepresentation = byteRepresentation;
        }

        public String getByteRepresentation() {
            return byteRepresentation;
        }

    }

    public enum ControlChange {

        MODULATION("01"),
        PORTAMENTO_TIME("05"),
        DATA_ENTRY_MSB("06"),
        MAIN_VOLUME("07"),
        PANPOT("0A"),
        HARMONIC_CONTENT("47"),
        RELEASE_TIME("48"),
        ATTACK_TIME("49"),
        BRIGHTNESS("4A"),
        DECAY_TIME("4B"),
        EFFECT_DEPHT_REVERB("5B"),
        EFFECT_DEPHT_CHORUS("5D"),
        EFFECT_DEPHT_VARIATION("5E");
        // TODO: add more values to ControlChange

        private String byteRepresentation;

        ControlChange(String byteRepresentation) {
            this.byteRepresentation = byteRepresentation;
        }

        public String getByteRepresentation() {
            return byteRepresentation;
        }

    }

    public static class InvalidChannelNumberException extends Exception {

        public InvalidChannelNumberException(String message) {
            super(message);
        }

    }

}
