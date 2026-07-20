package com.example.demo.controllers;

import com.example.demo.entities.Product;
import com.example.demo.services.ProductServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductServices productServices;

    // ── /home ───────────────────────────────────────────────────────────

    @Test
    void home_returnsHomeView() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("Home"));
    }

    @Test
    void root_returnsHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("Home"));
    }

    // ── /products ───────────────────────────────────────────────────────

    @Test
    void products_returnsProductsViewWithModel() throws Exception {
        Product p = new Product();
        p.setPid(1);
        p.setPname("Biryani");
        p.setPprice(350.0);
        when(productServices.getAllProducts()).thenReturn(List.of(p));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("Products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("products", hasSize(1)));
    }

    @Test
    void products_emptyMenu_returnsEmptyList() throws Exception {
        when(productServices.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("Products"))
                .andExpect(model().attribute("products", hasSize(0)));
    }

    // ── /location ───────────────────────────────────────────────────────

    @Test
    void location_returnsLocateUsView() throws Exception {
        mockMvc.perform(get("/location"))
                .andExpect(status().isOk())
                .andExpect(view().name("Locate_us"));
    }

    // ── /about ──────────────────────────────────────────────────────────

    @Test
    void about_returnsAboutView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("About"));
    }

    // ── /login ──────────────────────────────────────────────────────────

    @Test
    void login_returnsLoginViewWithAdminLoginAttribute() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("Login"))
                .andExpect(model().attributeExists("adminLogin"));
    }

    // ── /userLogin ──────────────────────────────────────────────────────

    @Test
    void userLogin_returnsUserLoginView() throws Exception {
        mockMvc.perform(get("/userLogin"))
                .andExpect(status().isOk())
                .andExpect(view().name("UserLogin"));
    }

    // ── /register ───────────────────────────────────────────────────────

    @Test
    void register_returnsRegisterViewWithUserAttribute() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("userRegistration"));
    }

    // ── /product/search (GET redirect) ──────────────────────────────────

    @Test
    void productSearch_get_noSession_redirectsToUserLogin() throws Exception {
        mockMvc.perform(get("/product/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userLogin"));
    }
}
