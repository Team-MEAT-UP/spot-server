package com.meetup.server.auth.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TossGenerateTokenResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("Toss invalid_grant 응답을 파싱한다.")
    @Test
    void parseInvalidGrantError() throws Exception {
        // given
        String response = """
                {
                  "error": "invalid_grant"
                }
                """;

        // when
        TossGenerateTokenResponse tokenResponse = objectMapper.readValue(response, TossGenerateTokenResponse.class);

        // then
        assertThat(tokenResponse.error().errorCode()).isEqualTo("invalid_grant");
        assertThat(tokenResponse.error().reason()).isNull();
    }

    @DisplayName("Toss 객체형 실패 응답을 파싱한다.")
    @Test
    void parseObjectError() throws Exception {
        // given
        String response = """
                {
                  "resultType": "FAIL",
                  "error": {
                    "errorCode": "INTERNAL_ERROR",
                    "reason": "요청을 처리하는 도중에 문제가 발생했습니다."
                  }
                }
                """;

        // when
        TossGenerateTokenResponse tokenResponse = objectMapper.readValue(response, TossGenerateTokenResponse.class);

        // then
        assertThat(tokenResponse.resultType()).isEqualTo("FAIL");
        assertThat(tokenResponse.error().errorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(tokenResponse.error().reason()).isEqualTo("요청을 처리하는 도중에 문제가 발생했습니다.");
    }
}
