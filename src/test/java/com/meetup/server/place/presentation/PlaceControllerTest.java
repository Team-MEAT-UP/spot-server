package com.meetup.server.place.presentation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.PlaceFixture;
import com.meetup.server.fixture.SubwayFixture;
import com.meetup.server.global.support.response.ResultType;
import com.meetup.server.place.application.PlaceService;
import com.meetup.server.place.dto.response.PlaceDetailResponse;
import com.meetup.server.place.dto.response.PlaceResponseList;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaceControllerTest extends ControllerTestSupport {

    @MockitoBean
    private PlaceService placeService;

    @DisplayName("장소 추천 리스트를 조회한다.")
    @Test
    void getAllPlaces() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        int subwayId = SubwayFixture.SUBWAY_ID;
        PlaceResponseList response = PlaceFixture.getPlaceResponseList();

        // when
        Mockito.when(placeService.getAllPlaces(any(UUID.class), anyInt()))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/places")
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("eventId", eventId.toString())
                                .queryParam("subwayId", String.valueOf(subwayId))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("place/get-all",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Place API")
                                                .description("장소 추천 리스트를 조회한다.")
                                                .queryParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)"),
                                                        parameterWithName("subwayId").description("지하철 ID")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.eventName").type(JsonFieldType.STRING).description("모임명"),
                                                        fieldWithPath("data.middlePointName").type(JsonFieldType.STRING).description("중간지점명"),

                                                        fieldWithPath("data.confirmedPlaceResponse").type(JsonFieldType.OBJECT).description("확정 장소").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.id").type(JsonFieldType.STRING).description("장소 ID").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.category").type(JsonFieldType.STRING).description("카테고리").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.name").type(JsonFieldType.STRING).description("장소명").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.image").type(JsonFieldType.STRING).description("이미지 URL").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.openTime").type(JsonFieldType.STRING).description("영업 시작 시간").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.closeTime").type(JsonFieldType.STRING).description("영업 종료 시간").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.distance").type(JsonFieldType.NUMBER).description("거리").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.averageRating").type(JsonFieldType.NUMBER).description("평균 평점").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.googleRating").type(JsonFieldType.NUMBER).description("구글 평점").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore").type(JsonFieldType.OBJECT).description("장소 점수").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore.socket").type(JsonFieldType.NUMBER).description("콘센트 점수").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore.seat").type(JsonFieldType.NUMBER).description("좌석 점수").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore.quiet").type(JsonFieldType.NUMBER).description("한산함 점수").optional(),

                                                        fieldWithPath("data.placeResponses[]").type(JsonFieldType.ARRAY).description("장소 추천 리스트").optional(),
                                                        fieldWithPath("data.placeResponses[].id").type(JsonFieldType.STRING).description("장소 ID").optional(),
                                                        fieldWithPath("data.placeResponses[].category").type(JsonFieldType.STRING).description("카테고리").optional(),
                                                        fieldWithPath("data.placeResponses[].name").type(JsonFieldType.STRING).description("장소명").optional(),
                                                        fieldWithPath("data.placeResponses[].image").type(JsonFieldType.STRING).description("이미지 URL").optional(),
                                                        fieldWithPath("data.placeResponses[].openTime").type(JsonFieldType.STRING).description("영업 시작 시간").optional(),
                                                        fieldWithPath("data.placeResponses[].closeTime").type(JsonFieldType.STRING).description("영업 종료 시간").optional(),
                                                        fieldWithPath("data.placeResponses[].distance").type(JsonFieldType.NUMBER).description("거리").optional(),
                                                        fieldWithPath("data.placeResponses[].averageRating").type(JsonFieldType.NUMBER).description("평균 평점").optional(),
                                                        fieldWithPath("data.placeResponses[].googleRating").type(JsonFieldType.NUMBER).description("구글 평점").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore").type(JsonFieldType.OBJECT).description("장소 점수").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore.socket").type(JsonFieldType.NUMBER).description("콘센트 점수").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore.seat").type(JsonFieldType.NUMBER).description("좌석 점수").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore.quiet").type(JsonFieldType.NUMBER).description("한산함 점수").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .responseSchema(Schema.schema("PlaceResponseList"))
                                                .build())
                        )
                );
    }

    @DisplayName("장소 상세 정보를 조회한다.")
    @Test
    void getPlaceDetails() throws Exception {
        // given
        UUID placeId = PlaceFixture.PLACE_ID;
        UUID eventId = EventFixture.EVENT_ID;
        int subwayId = SubwayFixture.SUBWAY_ID;
        PlaceDetailResponse response = PlaceFixture.getPlaceDetailResponse();

        // when
        Mockito.when(placeService.getPlace(any(UUID.class), any(UUID.class), anyInt()))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/places/{placeId}", placeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("eventId", eventId.toString())
                                .queryParam("subwayId", String.valueOf(subwayId))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("place/get-details",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Place API")
                                                .description("장소 상세 정보를 조회한다.")
                                                .pathParameters(
                                                        parameterWithName("placeId").description("장소 ID (UUID)")
                                                )
                                                .queryParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)"),
                                                        parameterWithName("subwayId").description("지하철 ID")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.id").type(JsonFieldType.STRING).description("장소 ID"),
                                                        fieldWithPath("data.kakaoPlaceId").type(JsonFieldType.STRING).description("카카오 플레이스 ID"),
                                                        fieldWithPath("data.category").type(JsonFieldType.STRING).description("장소 카테고리"),
                                                        fieldWithPath("data.name").type(JsonFieldType.STRING).description("장소 이름"),
                                                        fieldWithPath("data.images").type(JsonFieldType.ARRAY).description("장소 이미지 목록").optional(),
                                                        fieldWithPath("data.openTime").type(JsonFieldType.STRING).description("영업 시작 시간").optional(),
                                                        fieldWithPath("data.closeTime").type(JsonFieldType.STRING).description("영업 종료 시간").optional(),
                                                        fieldWithPath("data.distance").type(JsonFieldType.NUMBER).description("거리"),
                                                        fieldWithPath("data.averageRating").type(JsonFieldType.NUMBER).description("평균 평점").optional(),
                                                        fieldWithPath("data.placeQuietnessResponse").type(JsonFieldType.OBJECT).description("장소 혼잡도 정보").optional(),
                                                        fieldWithPath("data.reviews").type(JsonFieldType.ARRAY).description("사용자 리뷰 목록").optional(),
                                                        fieldWithPath("data.googleReviews").type(JsonFieldType.ARRAY).description("구글 리뷰 목록").optional(),
                                                        fieldWithPath("data.placeScore").type(JsonFieldType.OBJECT).description("장소 점수").optional(),
                                                        fieldWithPath("data.placeScore.socket").type(JsonFieldType.NUMBER).description("콘센트 점수").optional(),
                                                        fieldWithPath("data.placeScore.seat").type(JsonFieldType.NUMBER).description("좌석 점수").optional(),
                                                        fieldWithPath("data.placeScore.quiet").type(JsonFieldType.NUMBER).description("한산함 점수").optional(),
                                                        fieldWithPath("data.isConfirmed").type(JsonFieldType.BOOLEAN).description("확정 여부"),
                                                        fieldWithPath("data.isChanged").type(JsonFieldType.BOOLEAN).description("변경 여부"),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .responseSchema(Schema.schema("PlaceDetailResponse"))
                                                .build())
                        )
                );
    }

    @DisplayName("리뷰용 장소를 조회한다.")
    @Test
    void getPlaceForReview() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        PlaceResponseList response = PlaceFixture.getPlaceResponseListForReview();

        // when
        Mockito.when(placeService.getPlaceForReview(any(UUID.class)))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/places/review")
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("eventId", eventId.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("place/get-for-review",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Place API")
                                                .description("리뷰용 장소를 조회한다.")
                                                .queryParameters(
                                                        parameterWithName("eventId").description("이벤트 ID (UUID)")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.eventName").type(JsonFieldType.STRING).description("모임명"),
                                                        fieldWithPath("data.middlePointName").type(JsonFieldType.STRING).description("중간지점명"),

                                                        fieldWithPath("data.confirmedPlaceResponse").type(JsonFieldType.OBJECT).description("확정 장소"),
                                                        fieldWithPath("data.confirmedPlaceResponse.id").type(JsonFieldType.STRING).description("장소 ID"),
                                                        fieldWithPath("data.confirmedPlaceResponse.category").type(JsonFieldType.STRING).description("카테고리"),
                                                        fieldWithPath("data.confirmedPlaceResponse.name").type(JsonFieldType.STRING).description("장소명"),
                                                        fieldWithPath("data.confirmedPlaceResponse.image").type(JsonFieldType.STRING).description("이미지 URL").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.openTime").type(JsonFieldType.STRING).description("영업 시작 시간").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.closeTime").type(JsonFieldType.STRING).description("영업 종료 시간").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.distance").type(JsonFieldType.NUMBER).description("거리").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.averageRating").type(JsonFieldType.NUMBER).description("평균 평점").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.googleRating").type(JsonFieldType.NUMBER).description("구글 평점").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore").type(JsonFieldType.OBJECT).description("장소 점수").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore.socket").type(JsonFieldType.NUMBER).description("콘센트 점수").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore.seat").type(JsonFieldType.NUMBER).description("좌석 점수").optional(),
                                                        fieldWithPath("data.confirmedPlaceResponse.placeScore.quiet").type(JsonFieldType.NUMBER).description("한산함 점수").optional(),

                                                        fieldWithPath("data.placeResponses").type(JsonFieldType.ARRAY).description("추천 장소 리스트").optional(),
                                                        fieldWithPath("data.placeResponses[].id").type(JsonFieldType.STRING).description("장소 ID").optional(),
                                                        fieldWithPath("data.placeResponses[].category").type(JsonFieldType.STRING).description("카테고리").optional(),
                                                        fieldWithPath("data.placeResponses[].name").type(JsonFieldType.STRING).description("장소명").optional(),
                                                        fieldWithPath("data.placeResponses[].image").type(JsonFieldType.STRING).description("이미지 URL").optional(),
                                                        fieldWithPath("data.placeResponses[].openTime").type(JsonFieldType.STRING).description("영업 시작 시간").optional(),
                                                        fieldWithPath("data.placeResponses[].closeTime").type(JsonFieldType.STRING).description("영업 종료 시간").optional(),
                                                        fieldWithPath("data.placeResponses[].distance").type(JsonFieldType.NUMBER).description("거리").optional(),
                                                        fieldWithPath("data.placeResponses[].averageRating").type(JsonFieldType.NUMBER).description("평균 평점").optional(),
                                                        fieldWithPath("data.placeResponses[].googleRating").type(JsonFieldType.NUMBER).description("구글 평점").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore").type(JsonFieldType.OBJECT).description("장소 점수").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore.socket").type(JsonFieldType.NUMBER).description("콘센트 점수").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore.seat").type(JsonFieldType.NUMBER).description("좌석 점수").optional(),
                                                        fieldWithPath("data.placeResponses[].placeScore.quiet").type(JsonFieldType.NUMBER).description("한산함 점수").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .responseSchema(Schema.schema("PlaceResponseList"))
                                                .build())
                        )
                );
    }
}
