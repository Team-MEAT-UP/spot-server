package com.meetup.server.event.presentation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.meetup.server.event.application.EventService;
import com.meetup.server.event.domain.Event;
import com.meetup.server.event.domain.type.TrafficType;
import com.meetup.server.event.domain.value.MeetingPointRouteGroups;
import com.meetup.server.event.dto.request.EventRequest;
import com.meetup.server.event.dto.request.UpdateEventRequest;
import com.meetup.server.event.dto.request.UpdatePlaceRequest;
import com.meetup.server.event.dto.response.EventStartPointResponse;
import com.meetup.server.event.dto.response.route.MeetingPointRoutesResponse;
import com.meetup.server.fixture.EventFixture;
import com.meetup.server.fixture.StartPointFixture;
import com.meetup.server.global.support.response.ResultType;
import com.meetup.server.startpoint.domain.StartPoint;
import com.meetup.server.support.ControllerTest;
import com.meetup.server.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(EventController.class)
class EventControllerTest extends ControllerTestSupport {

    @MockitoBean
    private EventService eventService;

    @DisplayName("모임을 생성한다.")
    @Test
    void createEvent() throws Exception {
        // given
        EventRequest request = EventFixture.getEventRequest();
        UUID guestId = StartPointFixture.GUEST_ID;

        Event event = EventFixture.getEvent();
        StartPoint startPoint = StartPointFixture.getStartPoint(event, null);
        EventStartPointResponse response = EventStartPointResponse.of(event, startPoint);

        // when
        Mockito.when(eventService.createEvent(nullable(Long.class), nullable(UUID.class), any(EventRequest.class)))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .queryParam("guestId", guestId.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.eventId").value(EventFixture.EVENT_ID.toString()))
                .andExpect(jsonPath("$.data.startPointId").value(StartPointFixture.START_POINT_ID.toString()))
                .andExpect(jsonPath("$.data.guestId").value(guestId.toString()))
                .andExpect(jsonPath("$.data.username").value(request.username()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("event/create-event",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Event API")
                                                .description("모임을 생성한다. (회원 - JWT Token, 비회원 - guestId)")
                                                .requestHeaders(
                                                        headerWithName(HttpHeaders.AUTHORIZATION).description("JWT Token").optional()
                                                )
                                                .queryParameters(
                                                        parameterWithName("guestId").description("비회원 ID (UUID)").optional()
                                                )
                                                .requestFields(
                                                        fieldWithPath("eventName").type(JsonFieldType.STRING).description("모임 이름 (최소 1자, 최대 50자)"),
                                                        fieldWithPath("eventDate").type(JsonFieldType.STRING).description("모임 날짜 (YYYY-MM-DD)"),
                                                        fieldWithPath("eventTime").type(JsonFieldType.STRING).description("모임 시간 (HH:MM)"),
                                                        fieldWithPath("username").type(JsonFieldType.STRING).description("사용자 이름 (최소 1자, 최대 5자)"),
                                                        fieldWithPath("startPoint").type(JsonFieldType.STRING).description("출발지 이름"),
                                                        fieldWithPath("address").type(JsonFieldType.STRING).description("지번주소"),
                                                        fieldWithPath("roadAddress").type(JsonFieldType.STRING).description("도로명주소 (빈 문자열 허용)"),
                                                        fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도 (범위: -180.0 ~ 180.0)"),
                                                        fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도 (범위: -90.0 ~ 90.0)"),
                                                        fieldWithPath("isTransit").type(JsonFieldType.BOOLEAN).description("대중교통/자가용 선택 여부 (true/false)")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),

                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.eventId").type(JsonFieldType.STRING).description("이벤트 ID"),
                                                        fieldWithPath("data.startPointId").type(JsonFieldType.STRING).description("출발지 ID"),
                                                        fieldWithPath("data.guestId").type(JsonFieldType.STRING).description("비회원 ID"),
                                                        fieldWithPath("data.username").type(JsonFieldType.STRING).description("사용자 이름"),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("EventRequest"))
                                                .responseSchema(Schema.schema("EventStartPointResponse"))
                                                .build())
                        )
                );
    }

    @DisplayName("모임을 수정한다.")
    @Test
    void updateEvent() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        UpdateEventRequest request = EventFixture.getUpdateEventRequest();

        // when
        Mockito.doNothing().when(eventService).updateEvent(any(UUID.class), any(UpdateEventRequest.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/events/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("event/update-event",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Event API")
                                                .description("모임을 수정한다.")
                                                .pathParameters(
                                                        parameterWithName("eventId").description("수정할 이벤트 ID (UUID)")
                                                )
                                                .requestFields(
                                                        fieldWithPath("eventName").type(JsonFieldType.STRING).description("모임 이름 (최소 1자, 최대 50자)"),
                                                        fieldWithPath("eventDate").type(JsonFieldType.STRING).description("모임 날짜 (YYYY-MM-DD)"),
                                                        fieldWithPath("eventTime").type(JsonFieldType.STRING).description("모임 시간 (HH:MM)")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),

                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("UpdateEventRequest"))
                                                .build())
                        )
                );
    }

    @DisplayName("모임을 삭제한다.")
    @Test
    void deleteEvent() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;

        // when
        Mockito.doNothing().when(eventService).deleteEvent(any(UUID.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/events/{eventId}", eventId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("event/delete-event",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Event API")
                                                .description("모임을 삭제한다.")
                                                .pathParameters(
                                                        parameterWithName("eventId").description("삭제할 이벤트 ID (UUID)")
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

    @Test
    @DisplayName("중간 지점 경로를 조회한다.")
    void getMeetingPointRoutes() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        UUID guestId = StartPointFixture.GUEST_ID;

        Event event = EventFixture.getEventWithRoute();
        StartPoint startPoint = StartPointFixture.getStartPoint(event, null);
        MeetingPointRouteGroups meetingPointRouteGroups = event.getRoutes();
        MeetingPointRoutesResponse response = MeetingPointRoutesResponse.of(event, List.of(startPoint), meetingPointRouteGroups);

        // when
        Mockito.when(eventService.getMeetingPointRoutes(any(UUID.class), nullable(Long.class), nullable(UUID.class)))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/events/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("guestId", guestId.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.eventName").value("모임핑"))
                .andExpect(jsonPath("$.data.eventDate").value("2025-10-01"))
                .andExpect(jsonPath("$.data.eventTime").value("10:00"))
                .andExpect(jsonPath("$.data.eventMaker").value("땡수팟"))
                .andExpect(jsonPath("$.data.placeName").value("놀숲 건대점"))
                .andExpect(jsonPath("$.data.placeImage").value("https://example.com/place-image.jpg"))
                .andExpect(jsonPath("$.data.peopleCount").value(1))
                .andExpect(jsonPath("$.data.coordinate.subwayId").value(240))
                .andExpect(jsonPath("$.data.coordinate.averageTime").value(0))
                .andExpect(jsonPath("$.data.coordinate.meetingPoint.endStationName").value("논현"))
                .andExpect(jsonPath("$.data.coordinate.meetingPoint.endLongitude").value(127.021385))
                .andExpect(jsonPath("$.data.coordinate.meetingPoint.endLatitude").value(37.511108))
                .andExpect(jsonPath("$.data.coordinate.routeResponse").isArray())
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].isTransit").value(true))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].isMe").value(false))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].id").value("0198c64a-5a06-7095-a692-3c5e1a2c294f"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].nickname").value("김아무개"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].startName").value("강남구 삼성동"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].startLongitude").value(127.043999))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].startLatitude").value(37.510297))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].totalTime").value(10))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].trafficType").value(TrafficType.SUBWAY.name()))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].startExitNo").value("3"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].endExitNo").value("5"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].distance").value(3500.0))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].laneName").value("2"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].startBoardName").value("강남"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].endBoardName").value("교대"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].stationCount").value(2))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].sectionTime").value(10))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].passStopList.stations").isArray())
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].passStopList.stations[0].index").value(0))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].passStopList.stations[0].stationName").value("강남"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].passStopList.stations[0].x").value("127.027636"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[0].transitRoute[0].passStopList.stations[0].y").value("37.497985"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].isTransit").value(false))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingInfo.taxi").value(10000))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingInfo.toll").value(3500))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingInfo.duration").value(30))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingInfo.distance").value(15500))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingRoute[0].name").value("테헤란로/강남대로"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingRoute[0].coordinates").isArray())
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingRoute[0].coordinates[0].x").value("127.043999"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingRoute[0].coordinates[0].y").value(37.510297))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingRoute[0].coordinates[2].x").value("127.021385"))
                .andExpect(jsonPath("$.data.coordinate.routeResponse[1].drivingRoute[0].coordinates[2].y").value(37.511108))
                .andExpect(jsonPath("$.data.coordinate.parkingLot.name").value("강남대로150길(구)"))
                .andExpect(jsonPath("$.data.coordinate.parkingLot.longitude").value(127.0201132))
                .andExpect(jsonPath("$.data.coordinate.parkingLot.latitude").value(37.5156578))
                .andExpect(jsonPath("$.data.coordinate.parkingLot.distance").value(517.33672526))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("event/get-meeting-point-routes",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Event API")
                                                .description("중간 지점 경로를 조회한다. (회원 - JWT Token, 비회원 - guestId)")
                                                .requestHeaders(
                                                        headerWithName(HttpHeaders.AUTHORIZATION).description("JWT Token").optional()
                                                )
                                                .pathParameters(
                                                        parameterWithName("eventId").description("조회할 이벤트 ID (UUID)")
                                                )
                                                .queryParameters(
                                                        parameterWithName("guestId").description("비회원 ID (UUID)").optional()
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),

                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.eventName").type(JsonFieldType.STRING).description("모임 이름"),
                                                        fieldWithPath("data.eventDate").type(JsonFieldType.STRING).description("모임 날짜 (YYYY-MM-DD)"),
                                                        fieldWithPath("data.eventTime").type(JsonFieldType.STRING).description("모임 시간 (HH:MM)"),
                                                        fieldWithPath("data.eventMaker").type(JsonFieldType.STRING).description("모임 생성자"),
                                                        fieldWithPath("data.placeName").type(JsonFieldType.STRING).description("장소 이름").optional(),
                                                        fieldWithPath("data.placeImage").type(JsonFieldType.STRING).description("장소 이미지").optional(),
                                                        fieldWithPath("data.peopleCount").type(JsonFieldType.NUMBER).description("모임 참여자 수"),

                                                        fieldWithPath("data.coordinate").type(JsonFieldType.OBJECT).description("좌표 기반 중간 지점 경로 그룹"),
                                                        fieldWithPath("data.coordinate.subwayId").type(JsonFieldType.NUMBER).description("중간 지점의 지하철 ID"),
                                                        fieldWithPath("data.coordinate.averageTime").type(JsonFieldType.NUMBER).description("모든 참여자 출발지로부터의 평균 소요 시간 (분)"),
                                                        fieldWithPath("data.coordinate.meetingPoint").type(JsonFieldType.OBJECT).description("중간 지점 정보"),
                                                        fieldWithPath("data.coordinate.meetingPoint.endStationName").type(JsonFieldType.STRING).description("중간 지점 이름의 지하철역"),
                                                        fieldWithPath("data.coordinate.meetingPoint.endLongitude").type(JsonFieldType.NUMBER).description("중간 지점 경도"),
                                                        fieldWithPath("data.coordinate.meetingPoint.endLatitude").type(JsonFieldType.NUMBER).description("중간 지점 위도"),
                                                        fieldWithPath("data.coordinate.routeResponse").type(JsonFieldType.ARRAY).description("각 참여자의 출발지-중간 지점 경로 정보"),
                                                        fieldWithPath("data.coordinate.routeResponse[].isTransit").type(JsonFieldType.BOOLEAN).description("대중교통 경로 제공 여부 (false인 경우 자가용)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].isMe").type(JsonFieldType.BOOLEAN).description("경로 주인이 요청자 본인(guestId 또는 userId 일치)인지 여부"),
                                                        fieldWithPath("data.coordinate.routeResponse[].id").type(JsonFieldType.STRING).description("출발지 ID"),
                                                        fieldWithPath("data.coordinate.routeResponse[].userId").type(JsonFieldType.NUMBER).description("출발지 설정한 회원 ID (비회원인 경우 null)").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].guestId").type(JsonFieldType.STRING).description("출발지 설정한 비회원 ID (회원인 경우 null)").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                        fieldWithPath("data.coordinate.routeResponse[].profileImage").type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].startName").type(JsonFieldType.STRING).description("출발지 이름"),
                                                        fieldWithPath("data.coordinate.routeResponse[].startLongitude").type(JsonFieldType.NUMBER).description("출발지 경도"),
                                                        fieldWithPath("data.coordinate.routeResponse[].startLatitude").type(JsonFieldType.NUMBER).description("출발지 위도"),
                                                        fieldWithPath("data.coordinate.routeResponse[].totalTime").type(JsonFieldType.NUMBER).description("총 소요 시간 (분)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute").type(JsonFieldType.ARRAY).description("대중교통 경로 상세 정보 (isTransit=true일 때만 제공)").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].trafficType").type(JsonFieldType.STRING).description("교통 수단 ('SUBWAY', 'BUS', 'WALKING)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].startExitNo").type(JsonFieldType.STRING).description("출발 정거장의 출구 번호").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].endExitNo").type(JsonFieldType.STRING).description("도착 정거장의 출구 번호").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].distance").type(JsonFieldType.NUMBER).description("이동 거리 (m)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].laneName").type(JsonFieldType.STRING).description("노선 이름"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].startBoardName").type(JsonFieldType.STRING).description("승차 정거장 이름"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].endBoardName").type(JsonFieldType.STRING).description("하차 정거장 이름"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].stationCount").type(JsonFieldType.NUMBER).description("이동 정거장 수"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].passStopList").type(JsonFieldType.OBJECT).description("경유 정류장/역 목록").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].passStopList.stations").type(JsonFieldType.ARRAY).description("경유 정류장/역 상세 목록").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].passStopList.stations[].index").type(JsonFieldType.NUMBER).description("인덱스"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].passStopList.stations[].stationName").type(JsonFieldType.STRING).description("역/정류장 이름"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].passStopList.stations[].x").type(JsonFieldType.STRING).description("경도"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].passStopList.stations[].y").type(JsonFieldType.STRING).description("위도"),
                                                        fieldWithPath("data.coordinate.routeResponse[].transitRoute[].sectionTime").type(JsonFieldType.NUMBER).description("해당 구간 소요 시간 (분)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingInfo").type(JsonFieldType.OBJECT).description("자가용 경로 요약 정보 (isTransit=false일 때만 제공)").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingInfo.taxi").type(JsonFieldType.NUMBER).description("예상 택시 요금 (원)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingInfo.toll").type(JsonFieldType.NUMBER).description("예상 통행료 (원)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingInfo.duration").type(JsonFieldType.NUMBER).description("총 소요 시간 (초)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingInfo.distance").type(JsonFieldType.NUMBER).description("총 거리 (m)"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingRoute").type(JsonFieldType.ARRAY).description("자가용 경로 상세 정보 (isTransit=false일 때만 제공)").optional(),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingRoute[].name").type(JsonFieldType.STRING).description("도로명"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingRoute[].coordinates").type(JsonFieldType.ARRAY).description("경로 좌표 목록"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingRoute[].coordinates[].x").type(JsonFieldType.STRING).description("경도"),
                                                        fieldWithPath("data.coordinate.routeResponse[].drivingRoute[].coordinates[].y").type(JsonFieldType.STRING).description("위도"),
                                                        fieldWithPath("data.coordinate.parkingLot").type(JsonFieldType.OBJECT).description("중간 지점 근처 주차장 정보"),
                                                        fieldWithPath("data.coordinate.parkingLot.name").type(JsonFieldType.STRING).description("주차장 이름"),
                                                        fieldWithPath("data.coordinate.parkingLot.longitude").type(JsonFieldType.NUMBER).description("주차장 경도"),
                                                        fieldWithPath("data.coordinate.parkingLot.latitude").type(JsonFieldType.NUMBER).description("주차장 위도"),
                                                        fieldWithPath("data.coordinate.parkingLot.distance").type(JsonFieldType.NUMBER).description("중간 지점으로부터의 거리 (m)"),

                                                        fieldWithPath("data.popularity").type(JsonFieldType.OBJECT).description("인기 장소 기반 중간 지점 경로 그룹"),
                                                        fieldWithPath("data.popularity.subwayId").type(JsonFieldType.NUMBER).description("중간 지점의 지하철 ID"),
                                                        fieldWithPath("data.popularity.averageTime").type(JsonFieldType.NUMBER).description("모든 참여자 출발지로부터의 평균 소요 시간 (분)"),
                                                        fieldWithPath("data.popularity.meetingPoint").type(JsonFieldType.OBJECT).description("중간 지점 정보"),
                                                        fieldWithPath("data.popularity.meetingPoint.endStationName").type(JsonFieldType.STRING).description("중간 지점 이름의 지하철역"),
                                                        fieldWithPath("data.popularity.meetingPoint.endLongitude").type(JsonFieldType.NUMBER).description("중간 지점 경도"),
                                                        fieldWithPath("data.popularity.meetingPoint.endLatitude").type(JsonFieldType.NUMBER).description("중간 지점 위도"),
                                                        fieldWithPath("data.popularity.routeResponse").type(JsonFieldType.ARRAY).description("각 참여자의 출발지-중간 지점 경로 정보"),
                                                        fieldWithPath("data.popularity.routeResponse[].isTransit").type(JsonFieldType.BOOLEAN).description("대중교통 경로 제공 여부 (false인 경우 자가용)"),
                                                        fieldWithPath("data.popularity.routeResponse[].isMe").type(JsonFieldType.BOOLEAN).description("경로 주인이 요청자 본인(guestId 또는 userId 일치)인지 여부"),
                                                        fieldWithPath("data.popularity.routeResponse[].id").type(JsonFieldType.STRING).description("출발지 ID"),
                                                        fieldWithPath("data.popularity.routeResponse[].userId").type(JsonFieldType.NUMBER).description("출발지 설정한 회원 ID (비회원인 경우 null)").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].guestId").type(JsonFieldType.STRING).description("출발지 설정한 비회원 ID (회원인 경우 null)").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                        fieldWithPath("data.popularity.routeResponse[].profileImage").type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].startName").type(JsonFieldType.STRING).description("출발지 이름"),
                                                        fieldWithPath("data.popularity.routeResponse[].startLongitude").type(JsonFieldType.NUMBER).description("출발지 경도"),
                                                        fieldWithPath("data.popularity.routeResponse[].startLatitude").type(JsonFieldType.NUMBER).description("출발지 위도"),
                                                        fieldWithPath("data.popularity.routeResponse[].totalTime").type(JsonFieldType.NUMBER).description("총 소요 시간 (분)"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute").type(JsonFieldType.ARRAY).description("대중교통 경로 상세 정보 (isTransit=true일 때만 제공)").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].trafficType").type(JsonFieldType.STRING).description("교통 수단 ('SUBWAY', 'BUS', 'WALKING)"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].startExitNo").type(JsonFieldType.STRING).description("출발 정거장의 출구 번호").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].endExitNo").type(JsonFieldType.STRING).description("도착 정거장의 출구 번호").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].distance").type(JsonFieldType.NUMBER).description("이동 거리 (m)"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].laneName").type(JsonFieldType.STRING).description("노선 이름"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].startBoardName").type(JsonFieldType.STRING).description("승차 정거장 이름"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].endBoardName").type(JsonFieldType.STRING).description("하차 정거장 이름"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].stationCount").type(JsonFieldType.NUMBER).description("이동 정거장 수"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].passStopList").type(JsonFieldType.OBJECT).description("경유 정류장/역 목록").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].passStopList.stations").type(JsonFieldType.ARRAY).description("경유 정류장/역 상세 목록").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].passStopList.stations[].index").type(JsonFieldType.NUMBER).description("인덱스"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].passStopList.stations[].stationName").type(JsonFieldType.STRING).description("역/정류장 이름"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].passStopList.stations[].x").type(JsonFieldType.STRING).description("경도"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].passStopList.stations[].y").type(JsonFieldType.STRING).description("위도"),
                                                        fieldWithPath("data.popularity.routeResponse[].transitRoute[].sectionTime").type(JsonFieldType.NUMBER).description("해당 구간 소요 시간 (분)"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingInfo").type(JsonFieldType.OBJECT).description("자가용 경로 요약 정보 (isTransit=false일 때만 제공)").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingInfo.taxi").type(JsonFieldType.NUMBER).description("예상 택시 요금 (원)"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingInfo.toll").type(JsonFieldType.NUMBER).description("예상 통행료 (원)"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingInfo.duration").type(JsonFieldType.NUMBER).description("총 소요 시간 (초)"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingInfo.distance").type(JsonFieldType.NUMBER).description("총 거리 (m)"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingRoute").type(JsonFieldType.ARRAY).description("자가용 경로 상세 정보 (isTransit=false일 때만 제공)").optional(),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingRoute[].name").type(JsonFieldType.STRING).description("도로명"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingRoute[].coordinates").type(JsonFieldType.ARRAY).description("경로 좌표 목록"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingRoute[].coordinates[].x").type(JsonFieldType.STRING).description("경도"),
                                                        fieldWithPath("data.popularity.routeResponse[].drivingRoute[].coordinates[].y").type(JsonFieldType.STRING).description("위도"),
                                                        fieldWithPath("data.popularity.parkingLot").type(JsonFieldType.OBJECT).description("중간 지점 근처 주차장 정보"),
                                                        fieldWithPath("data.popularity.parkingLot.name").type(JsonFieldType.STRING).description("주차장 이름"),
                                                        fieldWithPath("data.popularity.parkingLot.longitude").type(JsonFieldType.NUMBER).description("주차장 경도"),
                                                        fieldWithPath("data.popularity.parkingLot.latitude").type(JsonFieldType.NUMBER).description("주차장 위도"),
                                                        fieldWithPath("data.popularity.parkingLot.distance").type(JsonFieldType.NUMBER).description("중간 지점으로부터의 거리 (m)"),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .responseSchema(Schema.schema("MeetingPointRoutesResponse"))
                                                .build())
                        )
                );
    }

    @DisplayName("모임 장소를 확정 또는 변경한다.")
    @Test
    void updatePlace() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;
        UpdatePlaceRequest request = EventFixture.getUpdatePlaceRequest();

        // when
        Mockito.doNothing().when(eventService).updatePlace(any(UUID.class), any(UpdatePlaceRequest.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/events/{eventId}/place", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("event/update-place",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Event API")
                                                .description("모임 장소를 확정 또는 변경한다.")
                                                .pathParameters(
                                                        parameterWithName("eventId").description("수정할 이벤트 ID (UUID)")
                                                )
                                                .requestFields(
                                                        fieldWithPath("placeId").type(JsonFieldType.STRING).description("장소 ID (UUID)"),
                                                        fieldWithPath("subwayId").type(JsonFieldType.NUMBER).description("지하철 ID")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),

                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("UpdatePlaceRequest"))
                                                .build())
                        )
                );
    }

    @DisplayName("확정된 모임 장소를 취소한다.")
    @Test
    void deletePlace() throws Exception {
        // given
        UUID eventId = EventFixture.EVENT_ID;

        // when
        Mockito.doNothing().when(eventService).deletePlace(any(UUID.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/events/{eventId}/place", eventId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("event/delete-place",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Event API")
                                                .description("확정된 모임 장소를 취소한다.")
                                                .pathParameters(
                                                        parameterWithName("eventId").description("삭제할 이벤트 ID (UUID)")
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
