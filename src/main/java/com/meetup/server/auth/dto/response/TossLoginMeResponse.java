package com.meetup.server.auth.dto.response;

import java.util.List;

public record TossLoginMeResponse(
        String resultType,
        TossLoginMeSuccess success,
        TossErrorResponse error
) {
    public record TossLoginMeSuccess(
            Long userKey,
            String scope,
            List<String> agreedTerms,
            String name,
            String phone,
            String birthday,
            String ci,
            String di,
            String gender,
            String nationality,
            String email
    ) {
    }
}
