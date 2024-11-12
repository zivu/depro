package com.zi.vu.depro.model;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.zi.vu.depro.service.AudioService;
import com.zi.vu.depro.service.ChatGPTService;
import com.zi.vu.depro.service.GoogleTextToSpeechService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public class ResponseObserverFactory {

    public ResponseObserver<StreamingRecognizeResponse> create(ChatGPTService chatGPTService, GoogleTextToSpeechService textToSpeechService,
                                                               AudioService audioService) {
        return new ResponseObserver<>() {
            /**
             * This variable holds what was said by a user.
             */
            private final List<String> sentences = new ArrayList<>();

            public void onStart(StreamController controller) {
            }

            /**
             * Collects what user said into request list.
             * @param response from Google.
             */
            public void onResponse(StreamingRecognizeResponse response) {
                StreamingRecognitionResult result = response.getResultsList().getFirst();
                SpeechRecognitionAlternative alternative = result.getAlternativesList().getFirst();
                sentences.add(alternative.getTranscript());
            }

            /**
             * After recording is stopped, recorded user's sentences is sent to ChatGPT for translation
             * from technical language and to Google text-to-speech service to play it later.
             */
            public void onComplete() {
                String recordedSpeech = String.join(" ", sentences);
                String translation = chatGPTService.translate(recordedSpeech);
                ByteString speech = textToSpeechService.toSpeech(translation);
                audioService.playAudio(speech);
            }

            public void onError(Throwable t) {
                log.error(t.getMessage());
            }
        };
    }

}
