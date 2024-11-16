package com.zi.vu.depro.service;

import com.zi.vu.depro.model.ChatResponse;
import com.zi.vu.depro.model.Message;
import com.zi.vu.depro.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatGPTService {

    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String BEARER = "Bearer ";
    public static final String CHAT_FUNCTIONALITY_DESCRIPTION = """
            You are an assistant that translates technical IT language into simple, everyday human language. For example,\s
            if you receive 'Our PM said that we have a bug,' you should translate it to 'One of our managers said we have a bad situation.'\s
            Use plain language that avoids technical terms. Don't answer any questions, but translate them instead.""";
    public static final String MODEL = "model";
    public static final String GPT_4_TURBO = "gpt-4-turbo";
    public static final String MESSAGES = "messages";

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${apenai.api.url}")
    private String apiURL;

    public String translate(String recordedSpeech) {
        Message userRequest = new Message(Role.user, recordedSpeech);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(createRequestBody(userRequest), createHeaders());
        ChatResponse response = restTemplate.exchange(apiURL, HttpMethod.POST, httpEntity, ChatResponse.class).getBody();
        if (!hasResponseMessage(response)) {
            throw new NoSuchElementException("No response returned from Chat Completions API");
        }
        return response.getChoices().getFirst().getMessage().getContent();
    }

    private static boolean hasResponseMessage(ChatResponse response) {
        return null != response && null != response.getChoices() && null != response.getChoices().getFirst()
                && null != response.getChoices().getFirst().getMessage();
    }

    private static Map<String, Object> createRequestBody(Message userRequest) {
        Message chatConfig = new Message(Role.system, CHAT_FUNCTIONALITY_DESCRIPTION);
        return Map.of(MODEL, GPT_4_TURBO,
                MESSAGES, List.of(chatConfig, userRequest));
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, BEARER + apiKey);
        headers.set(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }

}
