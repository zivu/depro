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

@Slf4j
@UtilityClass
public class ResponseObserverFactory {

    public ResponseObserver<StreamingRecognizeResponse> create(ChatGPTService chatGPTService, GoogleTextToSpeechService textToSpeechService,
                                                               AudioService audioService) {
        return new ResponseObserver<>() {

            public void onStart(StreamController controller) {
            }

            /**
             * Collects what user said into request list.
             * @param response from Google.
             */
            public void onResponse(StreamingRecognizeResponse response) {
                String translation = chatGPTService.translate(getText(response));
                ByteString speech = textToSpeechService.toSpeech(translation);
                audioService.playAudio(speech);
            }

            private static String getText(StreamingRecognizeResponse response) {
                StreamingRecognitionResult result = response.getResultsList().getFirst();
                SpeechRecognitionAlternative alternative = result.getAlternativesList().getFirst();
                return alternative.getTranscript();
            }

            public void onComplete() {
            }

            public void onError(Throwable t) {
                log.error(t.getMessage());
            }
        };
    }

}
