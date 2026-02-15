package com.meetup.server.review.presentation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.PlaceFixture;
import com.meetup.server.fixture.ReviewFixture;
import com.meetup.server.global.support.response.ResultType;
import com.meetup.server.review.application.ReviewService;
import com.meetup.server.review.dto.request.NonVisitedReviewRequest;
import com.meetup.server.review.dto.request.VisitedReviewRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends ControllerTestSupport {

    @MockitoBean
    private ReviewService reviewService;

    @DisplayName("확정 장소 방문 리뷰를 작성한다.")
    @Test
    void createVisitedReview() throws Exception {
        // given
        UUID placeId = PlaceFixture.PLACE_ID;
        UUID eventId = EventFixture.EVENT_ID;
        VisitedReviewRequest request = ReviewFixture.getVisitedReviewRequest();

        // when
        Mockito.doNothing().when(reviewService).createVisitedReview(any(UUID.class), any(UUID.class), any(Long.class), any(VisitedReviewRequest.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/places/{placeId}/reviews/visited", placeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .queryParam("eventId", eventId.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("review/create-visited",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Review API")
                                                .description("확정 장소 방문 리뷰를 작성한다.")
                                                .pathParameters(
                                                        parameterWithName("placeId").description("장소 ID (UUID)")
                                                )
                                                .queryParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)")
                                                )
                                                .requestFields(
                                                        fieldWithPath("visitedTime").type(JsonFieldType.STRING).description("방문 시간 (MORNING, LUNCH, NIGHT)"),
                                                        fieldWithPath("socket").type(JsonFieldType.NUMBER).description("콘센트 점수 (1~5)"),
                                                        fieldWithPath("seat").type(JsonFieldType.NUMBER).description("좌석 점수 (1~5)"),
                                                        fieldWithPath("quiet").type(JsonFieldType.NUMBER).description("한산함 점수 (1~5)"),
                                                        fieldWithPath("content").type(JsonFieldType.STRING).description("방문 후기").optional()
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),
                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("VisitedReviewRequest"))
                                                .build())
                        )
                );
    }

    @DisplayName("확정 장소 미방문 리뷰를 작성한다.")
    @Test
    void createNonVisitedReview() throws Exception {
        // given
        UUID placeId = PlaceFixture.PLACE_ID;
        UUID eventId = EventFixture.EVENT_ID;
        NonVisitedReviewRequest request = ReviewFixture.getNonVisitedReviewRequest();

        // when
        Mockito.doNothing().when(reviewService).createNonVisitedReview(any(UUID.class), any(UUID.class), any(Long.class), any(NonVisitedReviewRequest.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/places/{placeId}/reviews/non-visited", placeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .queryParam("eventId", eventId.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("review/create-non-visited",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Review API")
                                                .description("확정 장소 미방문 리뷰를 작성한다.")
                                                .pathParameters(
                                                        parameterWithName("placeId").description("장소 ID (UUID)")
                                                )
                                                .queryParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)")
                                                )
                                                .requestFields(
                                                        fieldWithPath("categories").type(JsonFieldType.ARRAY).description("방문하지 않은 이유 목록").optional(),
                                                        fieldWithPath("etcReason").type(JsonFieldType.STRING).description("기타 사유").optional(),
                                                        fieldWithPath("placeName").type(JsonFieldType.STRING).description("출발지명").optional(),
                                                        fieldWithPath("address").type(JsonFieldType.STRING).description("지번주소").optional(),
                                                        fieldWithPath("roadAddress").type(JsonFieldType.STRING).description("도로명주소").optional(),
                                                        fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도").optional(),
                                                        fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도").optional()
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),
                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("NonVisitedReviewRequest"))
                                                .build())
                        )
                );
    }
}
