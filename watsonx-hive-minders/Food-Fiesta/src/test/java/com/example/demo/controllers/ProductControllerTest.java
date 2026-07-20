package com.example.demo.controllers;

import com.example.demo.entities.Product;
import com.example.demo.services.ProductServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductServices productServices;

    // ── POST /addingProduct ──────────────────────────────────────────────

    @Test
    void addingProduct_savesProductAndRedirectsToAdminServices() throws Exception {
        doNothing().when(productServices).addProduct(any(Product.class));

        mockMvc.perform(post("/addingProduct")
                        .param("pname", "Veg Biryani")
                        .param("pprice", "220.0")
                        .param("pdescription", "Aromatic vegetarian biryani"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(productServices, times(1)).addProduct(any(Product.class));
    }

    @Test
    void addingProduct_withNoParams_stillSavesAndRedirects() throws Exception {
        doNothing().when(productServices).addProduct(any(Product.class));

        mockMvc.perform(post("/addingProduct"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(productServices, times(1)).addProduct(any(Product.class));
    }

    // ── GET /updatingProduct/{productId} ─────────────────────────────────

    @Test
    void updatingProduct_updatesProductAndRedirectsToAdminServices() throws Exception {
        doNothing().when(productServices).updateproduct(any(Product.class), eq(1));

        mockMvc.perform(get("/updatingProduct/1")
                        .param("pname", "Updated Biryani")
                        .param("pprice", "300.0")
                        .param("pdescription", "Updated description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(productServices, times(1)).updateproduct(any(Product.class), eq(1));
    }

    @Test
    void updatingProduct_differentId_updatesCorrectProduct() throws Exception {
        doNothing().when(productServices).updateproduct(any(Product.class), eq(5));

        mockMvc.perform(get("/updatingProduct/5")
                        .param("pname", "Paneer Tikka")
                        .param("pprice", "280.0")
                        .param("pdescription", "Grilled paneer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(productServices, times(1)).updateproduct(any(Product.class), eq(5));
    }

    // ── GET /deleteProduct/{productId} ───────────────────────────────────

    @Test
    void deleteProduct_deletesProductAndRedirectsToAdminServices() throws Exception {
        doNothing().when(productServices).deleteProduct(1);

        mockMvc.perform(get("/deleteProduct/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(productServices, times(1)).deleteProduct(1);
    }

    @Test
    void deleteProduct_differentId_deletesCorrectProduct() throws Exception {
        doNothing().when(productServices).deleteProduct(42);

        mockMvc.perform(get("/deleteProduct/42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(productServices, times(1)).deleteProduct(42);
    }
}
