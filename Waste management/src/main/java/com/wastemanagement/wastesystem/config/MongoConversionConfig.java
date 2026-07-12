package com.wastemanagement.wastesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Registers custom Spring Data MongoDB converters for java.time.LocalTime.
 *
 * Spring Data MongoDB ships built-in JSR-310 support for LocalDate,
 * LocalDateTime, and Instant, but not LocalTime — a time-of-day with no
 * date component doesn't map onto BSON's Date type. CollectionSchedule's
 * startTime/endTime are LocalTime fields, and the Atlas seed data stores
 * them as plain time strings (e.g. "07:00:00"), so without this
 * converter, reading any schedule document throws
 * ConverterNotFoundException.
 */
@Configuration
public class MongoConversionConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new LocalTimeToStringConverter(),
                new StringToLocalTimeConverter()
        ));
    }

    static class LocalTimeToStringConverter implements Converter<LocalTime, String> {
        @Override
        public String convert(LocalTime source) {
            return source.format(DateTimeFormatter.ISO_LOCAL_TIME);
        }
    }

    static class StringToLocalTimeConverter implements Converter<String, LocalTime> {
        @Override
        public LocalTime convert(String source) {
            return LocalTime.parse(source);
        }
    }
}