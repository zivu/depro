package com.zi.vu.depro.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is speech-to-text Google service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSpeechToText {

    public static final int SAMPLE_RATE = 16000;
    public static final int SAMPLE_SIZE_IN_BITS = 16;
    public static final int CHANNELS = 1;
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = false;
    public static final String US_LANGUAGE = "en-US";
    private final RecognitionConfig recognitionConfig = buildRecordingConfig();
    private final ChatGPTService chatGPTService;
    private final GoogleTextToSpeechService textToSpeechService;
    private final AudioService audioService;
    private final Map<String, TargetDataLine> sessions = new ConcurrentHashMap<>();
    private static SpeechClient googleSpeechAPI = null;

    static {
        try {
            googleSpeechAPI = SpeechClient.create();
        } catch (IOException e) {
            log.error("Speech client init failed");
        }
    }

    /**
     * Stops recording microphone.
     *
     * @param sessionId user's unique id used to associate with microphone.
     */
    public void stop(String sessionId) {
        TargetDataLine targetDataLine = sessions.remove(sessionId);
        if (null != targetDataLine) {
            targetDataLine.stop();
            targetDataLine.close();
        }
    }

    public static void saveToWav(byte[] audioBytes, File outputFile, AudioFormat format) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
             AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize())) {

            // Write the AudioInputStream to a .wav file
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
            System.out.println("Audio saved to " + outputFile.getAbsolutePath());
        }
    }

    /**
     * This method opens microphone and records what user says.
     * After stop() method is called, it sends request to Google speech-to-text server.
     *
     * @param id unique session id of a user, used to connect it with microphone.
     */
    @SneakyThrows
    public ByteString streamingMicRecognize(String id, byte[] audioBytes) {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        File outputFile = new File("output.wav");
        saveToWav(audioBytes, outputFile, format);
        String translation = "";
        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("en-US")
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioBytes))
                    .build();
            RecognizeRequest request = RecognizeRequest.newBuilder()
                    .setConfig(config)
                    .setAudio(audio)
                    .build();
            RecognizeResponse response = speechClient.recognize(request);
            String text = response.getResultsList().getFirst().getAlternativesList().getFirst().getTranscript();
            translation = chatGPTService.translate(text);
        } catch (Exception e) {
            log.error("ex: {}", e.getMessage());
        }
        return textToSpeechService.toSpeech(translation);
    }

    private ClientStream<StreamingRecognizeRequest> prepareGoogleSTTService(ResponseObserver<StreamingRecognizeResponse> responseObserver) {
        ClientStream<StreamingRecognizeRequest> configuredGoogleAPI =
                googleSpeechAPI.streamingRecognizeCallable().splitCall(responseObserver);
        StreamingRecognitionConfig streamingRecognitionConfig =
                StreamingRecognitionConfig.newBuilder()
                        .setConfig(recognitionConfig)
                        .build();
        StreamingRecognizeRequest configRequest =
                StreamingRecognizeRequest.newBuilder()
                        .setStreamingConfig(streamingRecognitionConfig)
                        .build();
        configuredGoogleAPI.send(configRequest);
        return configuredGoogleAPI;
    }

    private static void record(TargetDataLine recorder, AudioInputStream audio, ClientStream<StreamingRecognizeRequest> configuredGoogleAPI) throws IOException {
        while (recorder.isOpen()) {
            byte[] data = new byte[6400];
            audio.read(data);
            StreamingRecognizeRequest speechToTextRequest =
                    StreamingRecognizeRequest.newBuilder()
                            .setAudioContent(ByteString.copyFrom(data))
                            .build();
            configuredGoogleAPI.send(speechToTextRequest);
        }
    }

    private void associateSessionIdWithMicrophone(String id, TargetDataLine recorder) {
        sessions.put(id, recorder);
    }

    private static RecognitionConfig buildRecordingConfig() {
        return RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setLanguageCode(US_LANGUAGE)
                .setSampleRateHertz(16000)
                .build();
    }

}
