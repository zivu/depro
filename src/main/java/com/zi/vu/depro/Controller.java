package com.zi.vu.depro;

import com.google.protobuf.ByteString;
import com.zi.vu.depro.service.AudioUtils;
import com.zi.vu.depro.service.GoogleSpeechToText;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin
public class Controller {

    private final GoogleSpeechToText googleSpeechToText;

    @PostMapping("/ask")
    public ResponseEntity<byte[]> handleAudioUpload(@RequestParam String id, @RequestParam("file") MultipartFile file) throws IOException {
        byte[] audioBytes = file.getBytes();
        ByteString processedAudio = googleSpeechToText.streamingMicRecognize(id, audioBytes);
        byte[] wavAudio = AudioUtils.convertToWav(processedAudio, 16000, 1);

        // Set headers to indicate binary audio content
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "audio/wav");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"output.wav\"");
        return new ResponseEntity<>(wavAudio, headers, HttpStatus.OK);
    }

    @GetMapping("/stop")
    public void stop(@RequestParam String id) {
        googleSpeechToText.stop(id);
    }

}
