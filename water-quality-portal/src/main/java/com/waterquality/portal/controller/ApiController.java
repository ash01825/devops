package com.waterquality.portal.controller;

import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.domain.SampleStatus;
import com.waterquality.portal.service.SampleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * JSON API used by the dashboard and external integrations.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final SampleService sampleService;

    public ApiController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/samples")
    public List<Sample> samples(@RequestParam(required = false) String search,
                                @RequestParam(required = false) SampleStatus status,
                                @RequestParam(required = false) Long station) {
        return sampleService.search(search, status, station, null, null);
    }

    @GetMapping("/samples/{id}")
    public Sample sample(@PathVariable Long id) {
        return sampleService.findById(id);
    }

    @GetMapping("/dashboard/summary")
    public Map<String, Object> summary() {
        return Map.of(
                "total", sampleService.totalCount(),
                "byStatus", sampleService.countsByStatus().entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                e -> e.getKey().name(), Map.Entry::getValue)),
                "byStation", sampleService.countsByStation());
    }

    @GetMapping("/auth/login")
    public Map<String, String> loginInfo() {
        return Map.of("login", "/login", "note", "Use form login at /login");
    }
}
