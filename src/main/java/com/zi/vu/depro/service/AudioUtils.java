package com.zi.vu.depro.service;

import com.google.protobuf.ByteString;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AudioUtils {

    public static byte[] convertToWav(ByteString audioData, int sampleRate, int channels) throws IOException {
        // Define the audio format
        AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(audioData.toByteArray());
        AudioInputStream audioInputStream = new AudioInputStream(byteInputStream, format, audioData.size());

        // Write the audio to a ByteArrayOutputStream in WAV format
        ByteArrayOutputStream wavOutputStream = new ByteArrayOutputStream();
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavOutputStream);

        return wavOutputStream.toByteArray();
    }

}
