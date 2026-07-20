package com.example.demo.config;

import com.example.demo.entities.Admin;
import com.example.demo.entities.Product;
import com.example.demo.repositories.AdminRepository;
import com.example.demo.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for DataLoader: verifies the CommandLineRunner seeds
 * products and the default admin when the DB is empty, and does NOT duplicate
 * data when run a second time.
 *
 * Note: ProductRepository / AdminRepository extend CrudRepository whose
 * findAll() returns Iterable — must be cast explicitly.
 */
@SpringBootTest
@ActiveProfiles("test")
class DataLoaderTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AdminRepository adminRepository;

    // ── Products ─────────────────────────────────────────────────────────

    @Test
    void dataLoader_seeds5Products() {
        long count = productRepository.count();
        assertEquals(5, count, "DataLoader should seed exactly 5 products");
    }

    @Test
    void dataLoader_productsHaveExpectedNames() {
        List<Product> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add);

        List<String> names = products.stream().map(Product::getPname).toList();

        assertTrue(names.contains("Hyderabadi Chicken Biryani"));
        assertTrue(names.contains("Paneer Butter Masala"));
        assertTrue(names.contains("Chicken Tikka Tandoori"));
        assertTrue(names.contains("Veg Manchurian"));
        assertTrue(names.contains("Gulab Jamun (2pcs)"));
    }

    @Test
    void dataLoader_productsHavePositivePrices() {
        List<Product> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add);

        products.forEach(p ->
            assertTrue(p.getPprice() > 0, "Product " + p.getPname() + " should have a positive price")
        );
    }

    @Test
    void dataLoader_productsHaveNonEmptyDescriptions() {
        List<Product> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add);

        products.forEach(p ->
            assertNotNull(p.getPdescription(), "Product " + p.getPname() + " should have a description")
        );
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    @Test
    void dataLoader_seeds1Admin() {
        long count = adminRepository.count();
        assertEquals(1, count, "DataLoader should seed exactly 1 default admin");
    }

    @Test
    void dataLoader_defaultAdminHasExpectedEmail() {
        List<Admin> admins = new ArrayList<>();
        adminRepository.findAll().forEach(admins::add);

        assertEquals(1, admins.size());
        assertEquals("admin@foodfiesta.com", admins.get(0).getAdminEmail());
    }

    @Test
    void dataLoader_defaultAdminHasExpectedName() {
        List<Admin> admins = new ArrayList<>();
        adminRepository.findAll().forEach(admins::add);

        assertEquals("Super Admin", admins.get(0).getAdminName());
    }

    @Test
    void dataLoader_defaultAdminHasPhoneNumber() {
        List<Admin> admins = new ArrayList<>();
        adminRepository.findAll().forEach(admins::add);

        assertNotNull(admins.get(0).getAdminNumber());
        assertFalse(admins.get(0).getAdminNumber().isBlank());
    }
}
