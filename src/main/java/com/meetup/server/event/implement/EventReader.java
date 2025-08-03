package com.meetup.server.event.implement;

import com.meetup.server.event.domain.Event;
import com.meetup.server.event.dto.response.MeetingPointRoutesResponse;
import com.meetup.server.event.exception.EventErrorType;
import com.meetup.server.event.exception.EventException;
import com.meetup.server.event.persistence.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventReader {

    private final EventRepository eventRepository;
    private final EventValidator eventValidator;
    private final CacheManager cacheManager;

    public Event read(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorType.EVENT_NOT_FOUND));
    }

    /**
     * #139 캐시 데이터 가져올 때 사용하는 메서드
     */
    public MeetingPointRoutesResponse readEventCache(UUID eventId) {
        Cache cache = cacheManager.getCache("routeDetails");
        Cache.ValueWrapper wrapper = cache.get(eventId);

        eventValidator.validateEventCacheExist(wrapper);
        return (MeetingPointRoutesResponse) wrapper.get();
    }
}
