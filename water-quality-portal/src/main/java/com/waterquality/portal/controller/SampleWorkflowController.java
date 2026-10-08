package com.waterquality.portal.controller;

import com.waterquality.portal.domain.Role;
import com.waterquality.portal.domain.SampleStatus;
import com.waterquality.portal.repository.UserRepository;
import com.waterquality.portal.service.SampleService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SampleWorkflowController {

    private final SampleService sampleService;
    private final UserRepository userRepository;

    public SampleWorkflowController(SampleService sampleService, UserRepository userRepository) {
        this.sampleService = sampleService;
        this.userRepository = userRepository;
    }

    @PostMapping("/samples/{id}/status")
    public String transition(@PathVariable Long id,
                             @RequestParam SampleStatus toStatus,
                             @RequestParam(required = false) String comment,
                             Authentication auth,
                             RedirectAttributes redirect) {
        String username = auth.getName();
        Role role = userRepository.findByUsername(username)
                .map(u -> u.getRole())
                .orElse(Role.COLLECTOR);
        try {
            sampleService.transition(id, toStatus, comment, username, role);
            redirect.addFlashAttribute("success",
                    "Status changed to " + toStatus + ".");
        } catch (SecurityException | IllegalStateException | IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/samples/" + id;
    }
}
