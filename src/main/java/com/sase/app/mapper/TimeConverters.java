package com.sase.app.mapper;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * PostgreSQL {@code timestamp} (timezone yok) ile JPA/Hibernate uyumu; API DTO'larında UTC offset ile sunulur.
 */
@Component
public class TimeConverters {

    public OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }
}
