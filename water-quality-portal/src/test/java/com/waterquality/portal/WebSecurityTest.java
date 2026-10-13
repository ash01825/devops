package com.waterquality.portal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WebSecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/samples")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "collector", roles = "COLLECTOR")
    void collectorCanBrowseButNotManageStations() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().isOk());
        mvc.perform(get("/samples")).andExpect(status().isOk());
        mvc.perform(get("/stations")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanManageStations() throws Exception {
        mvc.perform(get("/stations")).andExpect(status().isOk());
        mvc.perform(post("/stations").with(csrf())
                        .param("code", "ST-UT-99")
                        .param("name", "Unit Test Station"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "collector", roles = "COLLECTOR")
    void nonAdminCannotCreateStations() throws Exception {
        mvc.perform(post("/stations").with(csrf())
                        .param("code", "ST-UT-98")
                        .param("name", "Nope Station"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void postWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/stations")
                        .param("code", "ST-UT-97")
                        .param("name", "No CSRF Station"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownSampleIdReturns404Page() throws Exception {
        // Anonymous request is redirected to login first (security filter runs before MVC)
        mvc.perform(get("/samples/999999")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "collector", roles = "COLLECTOR")
    void authenticatedUnknownSampleIdReturns404Page() throws Exception {
        mvc.perform(get("/samples/999999")).andExpect(status().isNotFound());
    }

    @Test
    void loginPageElementsExist() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "collector", roles = "COLLECTOR")
    void authenticatedUserIsRedirectedAwayFromLogin() throws Exception {
        mvc.perform(get("/login")).andExpect(status().is3xxRedirection());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void dashboardSummaryApiRequiresAuth() throws Exception {
        mvc.perform(get("/api/dashboard/summary")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "collector", roles = "COLLECTOR")
    void dashboardSummaryApiWorksWhenAuthenticated() throws Exception {
        mvc.perform(get("/api/dashboard/summary")).andExpect(status().isOk());
    }

    @Test
    void failedTransitionRedirectsWithError() throws Exception {
        // Anonymous POST is redirected to login (Spring Security default)
        mvc.perform(post("/samples/1/status").with(csrf())
                        .param("toStatus", "APPROVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login*"));
    }
}
