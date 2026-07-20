package com.example.demo.services;

import com.example.demo.entities.Product;
import com.example.demo.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServicesTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServices productServices;

    private Product biryani;

    @BeforeEach
    void setUp() {
        biryani = new Product();
        biryani.setPid(1);
        biryani.setPname("Hyderabadi Chicken Biryani");
        biryani.setPprice(350.0);
        biryani.setPdescription("Authentic biryani");
    }

    // ── addProduct ──────────────────────────────────────────────────────

    @Test
    void addProduct_callsSave() {
        productServices.addProduct(biryani);

        verify(productRepository, times(1)).save(biryani);
    }

    // ── getAllProducts ──────────────────────────────────────────────────

    @Test
    void getAllProducts_returnsAllFromRepository() {
        when(productRepository.findAll()).thenReturn(List.of(biryani));

        List<Product> result = productServices.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Hyderabadi Chicken Biryani", result.get(0).getPname());
    }

    @Test
    void getAllProducts_emptyRepository_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        assertTrue(productServices.getAllProducts().isEmpty());
    }

    // ── getProduct ──────────────────────────────────────────────────────

    @Test
    void getProduct_existingId_returnsProduct() {
        when(productRepository.findById(1)).thenReturn(Optional.of(biryani));

        Product result = productServices.getProduct(1);

        assertEquals("Hyderabadi Chicken Biryani", result.getPname());
        assertEquals(350.0, result.getPprice());
    }

    @Test
    void getProduct_missingId_throwsException() {
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> productServices.getProduct(99));
    }

    // ── deleteProduct ───────────────────────────────────────────────────

    @Test
    void deleteProduct_callsDeleteById() {
        productServices.deleteProduct(1);

        verify(productRepository, times(1)).deleteById(1);
    }

    // ── getProductByName ────────────────────────────────────────────────

    @Test
    void getProductByName_matchFound_returnsFirstMatch() {
        when(productRepository.findByPnameContainingIgnoreCase("biryani"))
                .thenReturn(List.of(biryani));

        Product result = productServices.getProductByName("biryani");

        assertNotNull(result);
        assertEquals("Hyderabadi Chicken Biryani", result.getPname());
    }

    @Test
    void getProductByName_noMatch_returnsNull() {
        when(productRepository.findByPnameContainingIgnoreCase("pizza"))
                .thenReturn(List.of());

        assertNull(productServices.getProductByName("pizza"));
    }

    @Test
    void getProductByName_caseInsensitive_returnsMatch() {
        when(productRepository.findByPnameContainingIgnoreCase("BIRYANI"))
                .thenReturn(List.of(biryani));

        assertNotNull(productServices.getProductByName("BIRYANI"));
    }

    // ── updateproduct ───────────────────────────────────────────────────

    @Test
    void updateproduct_existingId_setsIdAndSaves() {
        Product updated = new Product();
        updated.setPname("Updated Biryani");
        updated.setPprice(400.0);

        when(productRepository.findById(1)).thenReturn(Optional.of(biryani));

        productServices.updateproduct(updated, 1);

        assertEquals(1, updated.getPid());
        verify(productRepository, times(1)).save(updated);
    }
}
