package com.meetup.server.startpoint.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddressConverterTest{

    @Test
    void 출발지_주소를_변환한다() {
        // Given
        String test1 = "서울특별시 강남구 역삼동 123-45";
        String test2 = "경기도 성남시 분당구 분당동 678-90";
        String test3 = "서울특별시 강남구 123-45";
        String test4 = "경기 남양주시 별내면 청학리 산 103-3";
        String test5 = "서울특별시 중구 태평로1가";
        String test6 = "경기 가평군 가평읍 경반리 산 150";

        // When
        String result1 = AddressConverter.convertStartPointName(test1);
        String result2 = AddressConverter.convertStartPointName(test2);
        String result3 = AddressConverter.convertStartPointName(test3);
        String result4 = AddressConverter.convertStartPointName(test4);
        String result5 = AddressConverter.convertStartPointName(test5);
        String result6 = AddressConverter.convertStartPointName(test6);

        // Then
        assertEquals("강남구 역삼동", result1);
        assertEquals("분당구 분당동", result2);
        assertEquals("강남구", result3);
        assertEquals("남양주시 별내면", result4);
        assertEquals("중구 태평로1가", result5);
        assertEquals("가평군 가평읍", result6);
    }
}
