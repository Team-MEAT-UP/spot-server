package com.meetup.server.log.presentation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.meetup.server.fixture.LogEventInflowFixture;
import com.meetup.server.global.support.response.ResultType;
import com.meetup.server.log.application.LogService;
import com.meetup.server.log.dto.request.LogEventInflowRequest;
import com.meetup.server.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogControllerTest extends ControllerTestSupport {

    @MockitoBean
    private LogService logService;

    @DisplayName("이벤트 유입 경로를 기록한다.")
    @Test
    void logEventInflow() throws Exception {
        // given
        LogEventInflowRequest request = LogEventInflowFixture.getLogEventInflowRequest();
        String ipAddress = "127.0.0.1";
        String userAgent = "Mozilla/5.0";

        // when
        Mockito.doNothing().when(logService).logEventInflow(any(LogEventInflowRequest.class), nullable(Long.class), nullable(String.class), nullable(String.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/logs/inflow")
                                .header("X-Real-IP", ipAddress)
                                .header(HttpHeaders.USER_AGENT, userAgent)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("log/log-event-inflow",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Log API")
                                                .description("이벤트 유입 경로를 기록한다.")
                                                .requestHeaders(
                                                        headerWithName(HttpHeaders.AUTHORIZATION).description("JWT Token").optional(),
                                                        headerWithName("X-Real-IP").description("Client IP").optional(),
                                                        headerWithName(HttpHeaders.USER_AGENT).description("User Agent").optional()
                                                )
                                                .requestFields(
                                                        fieldWithPath("inflowType").type(JsonFieldType.STRING).description("유입 경로"),
                                                        fieldWithPath("eventId").type(JsonFieldType.STRING).description("조회할 이벤트 ID (UUID)")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),

                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("LogEventInflowRequest"))
                                                .build())
                        )
                );
    }
}
