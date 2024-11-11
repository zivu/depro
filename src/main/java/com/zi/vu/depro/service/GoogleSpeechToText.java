package com.zi.vu.depro.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.zi.vu.depro.model.ResponseObserverFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
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

    /**
     * This method opens microphone and records what user says.
     * After stop() method is called, it sends request to Google speech-to-text server.
     *
     * @param id unique session id of a user, used to connect it with microphone.
     */
    @SneakyThrows
    public void streamingMicRecognize(String id) {
        ResponseObserver<StreamingRecognizeResponse> responseObserver = ResponseObserverFactory.create(chatGPTService, textToSpeechService, audioService);
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
        AudioFormat withAudioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        DataLine.Info microphone = new DataLine.Info(TargetDataLine.class, withAudioFormat);
        TargetDataLine recorder = (TargetDataLine) AudioSystem.getLine(microphone);
        associateSessionIdWithMicrophone(id, recorder);
        recorder.open(withAudioFormat);
        recorder.start();
        log.info("Start speaking");
        AudioInputStream audio = new AudioInputStream(recorder);
        while (recorder.isOpen()) {
            byte[] data = new byte[6400];
            audio.read(data);
            StreamingRecognizeRequest speechToTextRequest =
                    StreamingRecognizeRequest.newBuilder()
                            .setAudioContent(ByteString.copyFrom(data))
                            .build();
            configuredGoogleAPI.send(speechToTextRequest);
        }
        responseObserver.onComplete();
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
