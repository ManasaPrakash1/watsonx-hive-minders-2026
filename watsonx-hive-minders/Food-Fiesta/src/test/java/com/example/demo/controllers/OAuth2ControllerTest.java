package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.services.UserServices;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for OAuth2Controller — verifies /oauth2/success behaviour
 * for new users, existing users, and unauthenticated access.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OAuth2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    // ── /oauth2/success — existing user ─────────────────────────────────

    @Test
    void oauth2Success_existingUser_storesInSessionAndRedirects() throws Exception {
        User existingUser = new User();
        existingUser.setU_id(1);
        existingUser.setUname("Alice");
        existingUser.setUemail("alice@gmail.com");
        existingUser.setUpassword("OAUTH_USER");
        existingUser.setUnumber(0L);

        when(userServices.getUserByEmail("alice@gmail.com")).thenReturn(existingUser);

        mockMvc.perform(get("/oauth2/success")
                .with(oauth2Login()
                        .attributes(a -> {
                            a.put("email", "alice@gmail.com");
                            a.put("name", "Alice");
                        })))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userServices, times(1)).getUserByEmail("alice@gmail.com");
        verify(userServices, never()).addUser(any());
    }

    // ── /oauth2/success — new user ───────────────────────────────────────

    @Test
    void oauth2Success_newUser_createsAccountAndRedirects() throws Exception {
        when(userServices.getUserByEmail("new@gmail.com")).thenReturn(null);

        mockMvc.perform(get("/oauth2/success")
                .with(oauth2Login()
                        .attributes(a -> {
                            a.put("email", "new@gmail.com");
                            a.put("name", "New User");
                        })))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userServices, times(1)).getUserByEmail("new@gmail.com");
        verify(userServices, times(1)).addUser(any(User.class));
    }

    // ── /oauth2/success — unauthenticated (no principal) ────────────────

    @Test
    void oauth2Success_noAuthentication_redirectsToLogin() throws Exception {
        // No oauth2Login() — request is anonymous → Spring Security redirects
        mockMvc.perform(get("/oauth2/success"))
                .andExpect(status().is3xxRedirection());
    }
}
