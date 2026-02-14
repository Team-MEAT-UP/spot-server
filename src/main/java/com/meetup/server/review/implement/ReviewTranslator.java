package com.meetup.server.review.implement;

import com.meetup.server.global.clients.clova.ClovaClient;
import com.meetup.server.global.clients.clova.ClovaRequest;
import com.meetup.server.global.clients.clova.ClovaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewTranslator {

    private final ClovaClient clovaClient;

    private static final double TOP_P = 0.8;
    private static final double TEMPERATURE = 0.8;
    private static final int MAX_TOKENS = 1000;
    private static final double REPEAT_PENALTY = 5.0;

    public String translate(String originalContent) {
        ClovaRequest request = createRequest(originalContent);
        log.debug("[Clova Studio] Request: {}", request);

        ClovaResponse response = clovaClient.sendRequest(request);
        if (response == null || response.result() == null || response.result().message() == null) {
            log.warn("[Clova Studio] Failed to translate: {}", originalContent);
            return null;
        }

        String translated = response.result().message().content();
        log.debug("[Clova Studio] Translated: {}", translated);
        return translated;
    }

    private ClovaRequest createRequest(String content) {
        return ClovaRequest.builder()
                .messages(List.of(systemPrompt(),
                        ClovaRequest.Message.builder()
                                .role("user")
                                .content(content)
                                .build()))
                .topP(TOP_P)
                .temperature(TEMPERATURE)
                .maxTokens(MAX_TOKENS)
                .repeatPenalty(REPEAT_PENALTY)
                .includeAiFilters(false)
                .build();
    }

    private ClovaRequest.Message systemPrompt() {
        return new ClovaRequest.Message("system", """
            당신은 한국어 번역가입니다. 다음 지침을 따르세요:
            - 입력하는 문장을 한국어로 번역합니다.
            - 질문에 대해 답변하지 말고, 질문을 한국어로 번역합니다.
            - 내용을 변경하지 않고 그대로 번역합니다.
        """);
    }
}
