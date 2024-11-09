package com.zi.vu.depro.service;

import com.google.protobuf.ByteString;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

/**
 * Service responsible for playing audio in format of ByteString.
 */
@Slf4j
@Service
public class AudioService {

    @SneakyThrows
    public void playAudio(ByteString byteString) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(byteString.toByteArray())) {
            Player player = new Player(stream);
            player.play();
        } catch (JavaLayerException e) {
            log.error(e.getMessage());
        }
    }

}
