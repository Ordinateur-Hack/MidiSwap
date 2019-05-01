package com.jimdo.dominicdj.midiswap.midimessage;

import java.util.ArrayList;
import java.util.List;

import static com.jimdo.dominicdj.midiswap.midimessage.MidiConstants.*;

public class MidiControllerBuilder {

    public static List<MidiChannelController> getAvailableMidiControllers(boolean shouldBeRecvControllers) {
        List<MidiChannelController> midiChannelControllersList = new ArrayList<>();

        try {
            // Group often used alternative MidiChannels.
            // ...

            // ==========================================================================
            // Construct new MIDI-controllers.
            // ==========================================================================
            // Midi Channel Message Controllers
            midiChannelControllersList.add(
                    new MidiChannelController("Modulation", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.MODULATION, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Portamento Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.PORTAMENTO_TIME, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Volume", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.MAIN_VOLUME, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Panpot", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.PANPOT, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Harmonic Content", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.HARMONIC_CONTENT, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Release Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.RELEASE_TIME, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Attack Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.ATTACK_TIME, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Brightness", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.BRIGHTNESS, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Decay Time", shouldBeRecvControllers, MidiChannel.getVoiceMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.DECAY_TIME, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Reverb", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_REVERB, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Chorus", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_CHORUS, shouldBeRecvControllers)));

            midiChannelControllersList.add(
                    new MidiChannelController("Variation", shouldBeRecvControllers, MidiChannel.getVoiceAndStyleMidiChannels(),
                            new MidiChannelMessage(MidiChannelEvent.CONTROL_CHANGE,
                                    MidiChannel.RIGHT_1, ControlChange.EFFECT_DEPHT_VARIATION, shouldBeRecvControllers)));

            // Other controllers
            // TODO: create new MidiControllers to use

        } catch (InvalidChannelNumberException e) {
            e.printStackTrace();
        }
        return midiChannelControllersList;
    }

}
