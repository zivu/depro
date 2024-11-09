package com.zi.vu.depro;

import com.zi.vu.depro.service.GoogleSpeechToText;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;


@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin
public class Controller {

    private final GoogleSpeechToText googleSpeechToText;

    @GetMapping("/ask")
    public void askChatGPT(@RequestParam String id) {
        CompletableFuture.runAsync(() -> googleSpeechToText.streamingMicRecognize(id));
    }

    @GetMapping("/stop")
    public void stop(@RequestParam String id) {
        googleSpeechToText.stop(id);
    }

}
