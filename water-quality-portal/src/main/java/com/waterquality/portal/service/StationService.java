package com.waterquality.portal.service;

import com.waterquality.portal.domain.Station;
import com.waterquality.portal.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    public Station findById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + id));
    }

    public Station findByCode(String code) {
        return stationRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + code));
    }

    public Station save(Station station) {
        return stationRepository.save(station);
    }
}
