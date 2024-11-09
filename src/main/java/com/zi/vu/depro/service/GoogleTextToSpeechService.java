package com.zi.vu.depro.service;

// Imports the Google Cloud client library

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
 * Service that translates text into speech (ByteString) using Google API.
 */
@Service
public class GoogleTextToSpeechService {

    public static final String VOICE_NAME = "en-US-Studio-Q";

    /**
     * Translates text to speech.
     * @param inputString text to be transformed into speech.
     * @return audio in ByteString format.
     */
    @SneakyThrows
    public ByteString toSpeech(String inputString) {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(inputString)
                    .build();
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, buildVoiceConfig(), buildAudioConfig());
            return response.getAudioContent();
        }
    }

    private static AudioConfig buildAudioConfig() {
        return AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .build();
    }

    private static VoiceSelectionParams buildVoiceConfig() {
        return VoiceSelectionParams.newBuilder()
                .setLanguageCode(GoogleSpeechToText.US_LANGUAGE)
                .setSsmlGender(SsmlVoiceGender.MALE)
                .setName(VOICE_NAME)
                .build();
    }

}
