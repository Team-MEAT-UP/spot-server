package com.meetup.server.auth.presentation;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.meetup.server.auth.application.AuthService;
import com.meetup.server.fixture.AuthFixture;
import com.meetup.server.fixture.UserFixture;
import com.meetup.server.global.support.response.ResultType;
import com.meetup.server.support.ControllerTest;
import com.meetup.server.support.ControllerTestSupport;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest(AuthController.class)
class AuthControllerTest extends ControllerTestSupport {

    @MockitoBean
    private AuthService authService;

    @DisplayName("Test를 위한 Access Token을 발급한다.")
    @Test
    void getAccessToken() throws Exception {
        // given
        Long userId = UserFixture.USER_ID;
        String accessToken = AuthFixture.getAccessToken();

        // when
        Mockito.when(authService.createAccessTokenForUser(anyLong()))
                .thenReturn(accessToken);

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/auth/test/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(
                        MockMvcRestDocumentationWrapper.document("auth/get-access-token",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Auth API")
                                                .description("Test를 위한 Access Token을 발급한다.")
                                                .pathParameters(
                                                        parameterWithName("userId").description("사용자 ID")
                                                )
                                                .build())
                        )
                );
    }

    @DisplayName("사용자가 로그아웃한다.")
    @Test
    void logout() throws Exception {
        // when
        Mockito.doNothing().when(authService).logout(any(HttpServletResponse.class));

        // then
        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(ResultType.SUCCESS.name()))
                .andDo(
                        MockMvcRestDocumentationWrapper.document("auth/logout",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Auth API")
                                                .description("사용자가 로그아웃한다.")
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
