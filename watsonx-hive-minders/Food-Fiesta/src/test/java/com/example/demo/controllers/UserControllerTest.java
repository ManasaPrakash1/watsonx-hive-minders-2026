package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.services.UserServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    // ── POST /addingUser ─────────────────────────────────────────────────

    @Test
    void addingUser_savesUserAndRedirectsToAdminServices() throws Exception {
        doNothing().when(userServices).addUser(any(User.class));

        mockMvc.perform(post("/addingUser")
                        .param("uname", "Alice")
                        .param("uemail", "alice@example.com")
                        .param("upassword", "secret")
                        .param("unumber", "0600000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(userServices, times(1)).addUser(any(User.class));
    }

    // ── POST /register ───────────────────────────────────────────────────

    @Test
    void register_savesUserAndRedirectsToLogin() throws Exception {
        doNothing().when(userServices).addUser(any(User.class));

        mockMvc.perform(post("/register")
                        .param("uname", "Bob")
                        .param("uemail", "bob@example.com")
                        .param("upassword", "pass123")
                        .param("unumber", "0700000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userServices, times(1)).addUser(any(User.class));
    }

    // ── GET /updatingUser/{id} ───────────────────────────────────────────

    @Test
    void updatingUser_updatesUserAndRedirectsToAdminServices() throws Exception {
        doNothing().when(userServices).updateUser(any(User.class), eq(1));

        mockMvc.perform(get("/updatingUser/1")
                        .param("uname", "Alice Updated")
                        .param("uemail", "alice@example.com")
                        .param("upassword", "newpass")
                        .param("unumber", "0600000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(userServices, times(1)).updateUser(any(User.class), eq(1));
    }

    // ── GET /deleteUser/{id} ─────────────────────────────────────────────

    @Test
    void deleteUser_deletesUserAndRedirectsToAdminServices() throws Exception {
        doNothing().when(userServices).deleteUser(1);

        mockMvc.perform(get("/deleteUser/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(userServices, times(1)).deleteUser(1);
    }
}
