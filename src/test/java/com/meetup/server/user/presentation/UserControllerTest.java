package com.meetup.server.user.presentation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.meetup.server.fixture.UserFixture;
import com.meetup.server.global.support.response.ResultType;
import com.meetup.server.support.ControllerTestSupport;
import com.meetup.server.user.application.UserService;
import com.meetup.server.user.dto.request.UserAgreementRequest;
import com.meetup.server.user.dto.response.UserEventHistoryResponseList;
import com.meetup.server.user.dto.response.UserProfileInfoResponse;
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

class UserControllerTest extends ControllerTestSupport {

    @MockitoBean
    private UserService userService;

    @DisplayName("사용자 프로필 정보를 조회한다.")
    @Test
    void getUserProfileInfo() throws Exception {
        // given
        UserProfileInfoResponse response = UserFixture.getUserProfileInfoResponse();

        // when
        Mockito.when(userService.getUserProfileInfo(nullable(Long.class)))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/users")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.nickname").value(UserFixture.TEST_NICKNAME))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("user/get-profile",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User API")
                                                .description("사용자 프로필 정보를 조회한다.")
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                                        fieldWithPath("data.userId").type(JsonFieldType.NUMBER).description("사용자 ID"),
                                                        fieldWithPath("data.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                        fieldWithPath("data.profileImageUrl").type(JsonFieldType.STRING).description("프로필 이미지 URL").optional(),
                                                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                        fieldWithPath("data.personalInfoAgreement").type(JsonFieldType.BOOLEAN).description("개인정보 동의 여부"),
                                                        fieldWithPath("data.marketingAgreement").type(JsonFieldType.BOOLEAN).description("마케팅 동의 여부"),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .responseSchema(Schema.schema("UserProfileInfoResponse"))
                                                .build())
                        )
                );
    }

    @DisplayName("사용자 약관 동의를 저장한다.")
    @Test
    void saveUserAgreement() throws Exception {
        // given
        UserAgreementRequest request = UserFixture.getUserAgreementRequest();

        // when
        Mockito.doNothing().when(userService).saveUserAgreement(nullable(Long.class), any(Boolean.class), any(Boolean.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/users/agreement")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("user/save-agreement",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User API")
                                                .description("사용자 약관 동의를 저장한다.")
                                                .requestFields(
                                                        fieldWithPath("isPersonalInfoAgreement").type(JsonFieldType.BOOLEAN).description("개인정보 동의 여부"),
                                                        fieldWithPath("isMarketingAgreement").type(JsonFieldType.BOOLEAN).description("마케팅 동의 여부")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .requestSchema(Schema.schema("UserAgreementRequest"))
                                                .build())
                        )
                );
    }

    @DisplayName("모임 히스토리 리스트를 조회한다.")
    @Test
    void getUserEventHistory() throws Exception {
        // given
        int size = 10;
        UUID lastViewedEventId = UUID.randomUUID();
        UserEventHistoryResponseList response = UserFixture.getUserEventHistoryResponseList();

        // when
        Mockito.when(userService.getUserEventHistory(nullable(Long.class), nullable(UUID.class), anyInt()))
                .thenReturn(response);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/users/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("size", String.valueOf(size))
                                .queryParam("lastViewedEventId", lastViewedEventId.toString())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.userEventHistoryResponses").isArray())
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("user/get-event-history",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User API")
                                                .description("모임 히스토리 리스트를 조회한다.")
                                                .queryParameters(
                                                        parameterWithName("lastViewedEventId").description("마지막으로 조회한 이벤트 ID").optional(),
                                                        parameterWithName("size").description("조회할 크기")
                                                )
                                                .responseFields(
                                                        fieldWithPath("result").type(JsonFieldType.STRING).description("API 호출 결과"),
                                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터").optional(),

                                                        fieldWithPath("data.userEventHistoryResponses").type(JsonFieldType.ARRAY).description("이벤트 목록"),
                                                        fieldWithPath("data.userEventHistoryResponses[].eventId").type(JsonFieldType.STRING).description("이벤트 ID").optional(),
                                                        fieldWithPath("data.userEventHistoryResponses[].eventName").type(JsonFieldType.STRING).description("이벤트 이름").optional(),
                                                        fieldWithPath("data.userEventHistoryResponses[].eventDate").type(JsonFieldType.STRING).description("이벤트 날짜").optional(),
                                                        fieldWithPath("data.userEventHistoryResponses[].eventTime").type(JsonFieldType.STRING).description("이벤트 시간").optional(),
                                                        fieldWithPath("data.userEventHistoryResponses[].placeName").type(JsonFieldType.STRING).description("장소 이름").optional(),
                                                        fieldWithPath("data.userEventHistoryResponses[].subwayName").type(JsonFieldType.STRING).description("지하철 역 이름").optional(),

                                                        fieldWithPath("data.hasNextPage").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부"),
                                                        fieldWithPath("data.lastEventId").type(JsonFieldType.STRING).description("마지막 조회 이벤트 ID").optional(),

                                                        fieldWithPath("error").type(JsonFieldType.OBJECT).description("API 호출 에러").optional(),
                                                        fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드").optional(),
                                                        fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지").optional()
                                                )
                                                .responseSchema(Schema.schema("UserEventHistoryResponseList"))
                                                .build())
                        )
                );
    }

    @DisplayName("사용자 닉네임을 수정한다.")
    @Test
    void updateNickname() throws Exception {
        // given
        String nickname = UserFixture.NEW_NICKNAME;

        // when
        Mockito.doNothing().when(userService).updateNickname(nullable(Long.class), anyString());

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("nickname", nickname)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("user/update-nickname",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User API")
                                                .description("사용자 닉네임을 수정한다.")
                                                .queryParameters(
                                                        parameterWithName("nickname").description("새 닉네임 (1~5자)")
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

    @DisplayName("사용자를 탈퇴한다.")
    @Test
    void withdrawUser() throws Exception {
        // when
        Mockito.doNothing().when(userService).withdraw(nullable(Long.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/users")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("user/withdraw",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User API")
                                                .description("사용자를 탈퇴한다.")
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
