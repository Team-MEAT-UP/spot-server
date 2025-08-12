package com.meetup.server.startpoint.domain.type;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Address {

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "road_address", nullable = true)
    private String roadAddress;

    public static Address of(String address, String roadAddress) {
        return new Address(
                address,
                (roadAddress == null || roadAddress.isBlank()) ? null : roadAddress
        );
    }
}
