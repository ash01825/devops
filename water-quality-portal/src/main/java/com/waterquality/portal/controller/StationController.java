package com.waterquality.portal.controller;

import com.waterquality.portal.domain.Station;
import com.waterquality.portal.service.StationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("stations", stationService.findAll());
        return "stations/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("station", new Station());
        return "stations/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("station") Station station,
                         BindingResult bindingResult,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "stations/form";
        }
        try {
            Station saved = stationService.save(station);
            redirect.addFlashAttribute("success", "Station " + saved.getCode() + " created.");
            return "redirect:/stations";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Could not create station: " + e.getMessage());
            return "redirect:/stations/new";
        }
    }
}
