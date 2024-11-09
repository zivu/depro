package com.zi.vu.depro.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class wrapper of chatGPT response.
 * We collect needed fields and ignore the rest.
 * Typical json response body looks the following:
 * {
 *   "id": "chatcmpl-123",
 *   "object": "chat.completion",
 *   "created": 1234567890,
 *   "choices": [
 *     {
 *       "index": 0,
 *       "message": {
 *         "role": "assistant",
 *         "content": "Hello, how can I help you today?"
 *       },
 *       "finish_reason": "stop"
 *     }
 *   ],
 *   "usage": {
 *     "prompt_tokens": 9,
 *     "completion_tokens": 12,
 *     "total_tokens": 21
 *   }
 * }
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {

    /**
     * List of chatGPT responses.
     */
    List<Choice> choices;

    /**
     * Wrapper class for chatGPT responses.
     */
    @Getter
    @Setter
    public static class Choice {
        private Message message;
    }

    /**
     * Will contain exact response from chatGPT.
     */
    @Getter
    @Setter
    public static class Message {
        String content;
    }

}
