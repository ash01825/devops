package com.waterquality.portal.controller;

import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.service.SampleService;
import com.waterquality.portal.service.StationService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SampleFormController {

    private final SampleService sampleService;
    private final StationService stationService;

    public SampleFormController(SampleService sampleService, StationService stationService) {
        this.sampleService = sampleService;
        this.stationService = stationService;
    }

    @PostMapping("/samples")
    public String create(@Valid @ModelAttribute("sample") Sample sample,
                         BindingResult bindingResult, Model model,
                         Authentication auth, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stations", stationService.findAll());
            model.addAttribute("isEdit", false);
            return "samples/form";
        }
        try {
            String username = (auth == null) ? "system" : auth.getName();
            Sample saved = sampleService.create(sample, username);
            redirect.addFlashAttribute("success", "Sample " + saved.getSampleId() + " created.");
            return "redirect:/samples/" + saved.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("stations", stationService.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("error", e.getMessage());
            return "samples/form";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("stations", stationService.findAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("error", "Sample ID already exists. Choose a unique ID.");
            return "samples/form";
        }
    }

    @GetMapping("/samples/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("sample", sampleService.findById(id));
        model.addAttribute("stations", stationService.findAll());
        model.addAttribute("isEdit", true);
        return "samples/form";
    }

    @PostMapping("/samples/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("sample") Sample form,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("stations", stationService.findAll());
            model.addAttribute("isEdit", true);
            return "samples/form";
        }
        try {
            Sample saved = sampleService.update(id, form);
            redirect.addFlashAttribute("success", "Sample " + saved.getSampleId() + " updated.");
            return "redirect:/samples/" + saved.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("stations", stationService.findAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("error", e.getMessage());
            return "samples/form";
        }
    }
}
