package com.meetup.server.startpoint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record StartPointRequest(

        @NotBlank
        @Size(min = 1, max = 5)
        @Schema(description = "사용자명", example = "안연아바보")
        String username,

        @NotBlank(message = "출발지명은 필수 값입니다.")
        @Schema(description = "출발지명", example = "선정릉역 수인분당선")
        String startPoint,

        @NotBlank(message = "지번주소는 필수 값입니다.")
        @Schema(description = "지번주소", example = "서울특별시 강남구 삼성동 111-114")
        String address,

        @NotBlank(message = "도로명주소는 필수 값입니다.")
        @Schema(description = "도로명주소", example = "서울특별시 강남구 선릉로 지하580")
        String roadAddress,

        @DecimalMin(value = "-180.0", message = "경도는 -180.0보다 크거나 같아야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180.0보다 작거나 같아야 합니다.")
        @Schema(description = "경도", example = "127.043999")
        double longitude,

        @DecimalMin(value = "-90.0", message = "위도는 -90.0보다 크거나 같아야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90.0보다 작거나 같아야 합니다.")
        @Schema(description = "위도", example = "37.510297")
        double latitude
) {
}
