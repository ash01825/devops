package com.waterquality.portal.controller;

import com.waterquality.portal.service.SampleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final SampleService sampleService;

    public DashboardController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("countsByStatus", sampleService.countsByStatus());
        model.addAttribute("countsByStation", sampleService.countsByStation());
        model.addAttribute("totalCount", sampleService.totalCount());
        model.addAttribute("recent", sampleService.recent(8));
        return "dashboard";
    }
}
