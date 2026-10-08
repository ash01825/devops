package com.waterquality.portal.config;

import com.waterquality.portal.domain.Station;
import com.waterquality.portal.repository.StationRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts the station id posted by Thymeleaf forms into a Station entity.
 * Registered automatically with Spring's conversion service.
 */
@Component
public class StringToStationConverter implements Converter<String, Station> {

    private final StationRepository stations;

    public StringToStationConverter(StationRepository stations) {
        this.stations = stations;
    }

    @Override
    public Station convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return stations.findById(Long.valueOf(source.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown station: " + source));
    }
}
