package com.meetup.server.startpoint.presentation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.StartPointFixture;
import com.meetup.server.global.clients.kakao.local.KakaoLocalResponse;
import com.meetup.server.global.support.response.ResultType;
import com.meetup.server.startpoint.application.StartPointService;
import com.meetup.server.startpoint.dto.request.StartPointRequest;
import com.meetup.server.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StartPointControllerTest extends ControllerTestSupport {

    @MockitoBean
    private StartPointService startPointService;

    @DisplayName("장소를 검색한다.")
    @Test
    void searchStartPoint() throws Exception {
        // given
        String textQuery = "선정릉역";
        KakaoLocalResponse response = new KakaoLocalResponse();

        // when
        Mockito.when(startPointService.searchStartPoint(anyString()))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/start-points/search")
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("textQuery", textQuery)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("start-point/search",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("StartPoint API")
                                                .description("장소를 검색한다.")
                                                .queryParameters(
                                                        parameterWithName("textQuery").description("검색할 장소명")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),

                                                        fieldWithPath("data.meta").type(JsonFieldType.OBJECT).description("카카오 검색 메타데이터").optional(),
                                                        fieldWithPath("data.documents").type(JsonFieldType.ARRAY).description("카카오 검색 결과 리스트").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .responseSchema(Schema.schema("KakaoLocalResponse"))
                                                .build())
                        )
                );
    }

    @DisplayName("출발지를 생성한다.")
    @Test
    void createStartPoint() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        UUID guestId = StartPointFixture.GUEST_ID;
        StartPointRequest request = StartPointFixture.getStartPointRequest();

        Event event = EventFixture.getEvent();
        com.meetup.server.startpoint.domain.StartPoint startPoint = StartPointFixture.getStartPoint(event, null);
        EventStartPointResponse response = EventStartPointResponse.of(event, startPoint);

        // when
        Mockito.when(startPointService.createStartPoint(any(UUID.class), nullable(Long.class), nullable(UUID.class), any(StartPointRequest.class)))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/events/{eventId}/start-points", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .queryParam("guestId", guestId.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.eventId").value(EventFixture.EVENT_ID.toString()))
                .andExpect(jsonPath("$.data.startPointId").value(StartPointFixture.START_POINT_ID.toString()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("start-point/create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("StartPoint API")
                                                .description("출발지를 생성한다. (회원 - JWT Token, 비회원 - guestId)")
                                                .pathParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)")
                                                )
                                                .queryParameters(
                                                        parameterWithName("guestId").description("비회원 ID (UUID)").optional()
                                                )
                                                .requestFields(
                                                        fieldWithPath("username").type(JsonFieldType.STRING).description("사용자명 (1~5자)"),
                                                        fieldWithPath("startPoint").type(JsonFieldType.STRING).description("출발지명"),
                                                        fieldWithPath("address").type(JsonFieldType.STRING).description("지번주소"),
                                                        fieldWithPath("roadAddress").type(JsonFieldType.STRING).description("도로명주소"),
                                                        fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도"),
                                                        fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도"),
                                                        fieldWithPath("isTransit").type(JsonFieldType.BOOLEAN).description("대중교통/자가용 선택 여부")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.eventId").type(JsonFieldType.STRING).description("이벤트 ID"),
                                                        fieldWithPath("data.startPointId").type(JsonFieldType.STRING).description("출발지 ID"),
                                                        fieldWithPath("data.guestId").type(JsonFieldType.STRING).description("비회원 ID").optional(),
                                                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("사용자명"),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("StartPointRequest"))
                                                .responseSchema(Schema.schema("EventStartPointResponse"))
                                                .build())
                        )
                );
    }

    @DisplayName("출발지를 수정한다.")
    @Test
    void updateStartPoint() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        UUID startPointId = StartPointFixture.START_POINT_ID;
        StartPointRequest request = StartPointFixture.getStartPointRequest();

        Event event = EventFixture.getEvent();
        com.meetup.server.startpoint.domain.StartPoint startPoint = StartPointFixture.getStartPoint(event, null);
        EventStartPointResponse response = EventStartPointResponse.of(event, startPoint);

        // when
        Mockito.when(startPointService.updateStartPoint(any(UUID.class), any(UUID.class), any(StartPointRequest.class)))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/events/{eventId}/start-points/{startPointId}", eventId, startPointId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.eventId").value(EventFixture.EVENT_ID.toString()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("start-point/update",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("StartPoint API")
                                                .description("출발지를 수정한다.")
                                                .pathParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)"),
                                                        parameterWithName("startPointId").description("출발지 ID (UUID)")
                                                )
                                                .requestFields(
                                                        fieldWithPath("username").type(JsonFieldType.STRING).description("사용자명 (1~5자)"),
                                                        fieldWithPath("startPoint").type(JsonFieldType.STRING).description("출발지명"),
                                                        fieldWithPath("address").type(JsonFieldType.STRING).description("지번주소"),
                                                        fieldWithPath("roadAddress").type(JsonFieldType.STRING).description("도로명주소"),
                                                        fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도"),
                                                        fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도"),
                                                        fieldWithPath("isTransit").type(JsonFieldType.BOOLEAN).description("대중교통/자가용 선택 여부")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.eventId").type(JsonFieldType.STRING).description("이벤트 ID"),
                                                        fieldWithPath("data.startPointId").type(JsonFieldType.STRING).description("출발지 ID"),
                                                        fieldWithPath("data.guestId").type(JsonFieldType.STRING).description("비회원 ID").optional(),
                                                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("사용자명"),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("StartPointRequest"))
                                                .responseSchema(Schema.schema("EventStartPointResponse"))
                                                .build())
                        )
                );
    }

    @DisplayName("출발지를 삭제한다.")
    @Test
    void deleteStartPoint() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        UUID startPointId = StartPointFixture.START_POINT_ID;

        // when
        Mockito.doNothing().when(startPointService).deleteStartPoint(any(UUID.class), any(UUID.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/events/{eventId}/start-points/{startPointId}", eventId, startPointId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("start-point/delete",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("StartPoint API")
                                                .description("출발지를 삭제한다.")
                                                .pathParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)"),
                                                        parameterWithName("startPointId").description("출발지 ID (UUID)")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .build())
                        )
                );
    }
}
