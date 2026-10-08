package com.waterquality.portal.controller;

import com.waterquality.portal.domain.Role;
import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.domain.SampleStatus;
import com.waterquality.portal.domain.Station;
import com.waterquality.portal.service.SampleService;
import com.waterquality.portal.service.StationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SampleViewController {

    private final SampleService sampleService;
    private final StationService stationService;

    public SampleViewController(SampleService sampleService, StationService stationService) {
        this.sampleService = sampleService;
        this.stationService = stationService;
    }

    @GetMapping("/samples")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) SampleStatus status,
                       @RequestParam(required = false) Long stationId,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model) {
        List<Sample> samples = sampleService.search(q, status, stationId, from, to);
        List<Station> stations = stationService.findAll();
        model.addAttribute("samples", samples);
        model.addAttribute("stations", stations);
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedStationId", stationId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("statuses", SampleStatus.values());
        return "samples/list";
    }

    @GetMapping("/samples/new")
    public String newForm(Model model) {
        model.addAttribute("sample", new Sample());
        model.addAttribute("stations", stationService.findAll());
        model.addAttribute("isEdit", false);
        return "samples/form";
    }

    @GetMapping("/samples/{id}")
    public String view(@PathVariable Long id, Model model, Authentication auth) {
        Sample sample = sampleService.findById(id);
        model.addAttribute("sample", sample);
        model.addAttribute("history", sampleService.historyFor(id));
        model.addAttribute("nextStatuses", SampleService.nextStatuses(sample.getStatus()));
        model.addAttribute("currentRole", currentRole(auth));
        return "samples/view";
    }

    private String currentRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return "";
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst().orElse("");
    }

    /** Exposed for templates: which next-status buttons the current role may use. */
    public static boolean canTransition(SampleStatus from, SampleStatus to, String roleName) {
        try {
            Role role = Role.valueOf(roleName);
            return SampleService.allowedRoles(from, to) != null
                    && SampleService.allowedRoles(from, to).contains(role);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
