package com.example.demo.repositories;

import com.example.demo.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void seed() {
        Product p1 = new Product();
        p1.setPname("Hyderabadi Chicken Biryani");
        p1.setPprice(350.0);
        p1.setPdescription("Authentic biryani");

        Product p2 = new Product();
        p2.setPname("Paneer Butter Masala");
        p2.setPprice(280.0);
        p2.setPdescription("Rich paneer curry");

        productRepository.save(p1);
        productRepository.save(p2);
    }

    @Test
    void findByPnameContainingIgnoreCase_exactMatch_returnsProduct() {
        List<Product> results = productRepository.findByPnameContainingIgnoreCase("Biryani");

        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getPname().toLowerCase().contains("biryani"));
    }

    @Test
    void findByPnameContainingIgnoreCase_lowerCase_returnsProduct() {
        List<Product> results = productRepository.findByPnameContainingIgnoreCase("biryani");

        assertFalse(results.isEmpty());
    }

    @Test
    void findByPnameContainingIgnoreCase_upperCase_returnsProduct() {
        List<Product> results = productRepository.findByPnameContainingIgnoreCase("PANEER");

        assertFalse(results.isEmpty());
    }

    @Test
    void findByPnameContainingIgnoreCase_noMatch_returnsEmpty() {
        List<Product> results = productRepository.findByPnameContainingIgnoreCase("Pizza");

        assertTrue(results.isEmpty());
    }

    @Test
    void findByPnameContainingIgnoreCase_partialMatch_returnsResults() {
        List<Product> results = productRepository.findByPnameContainingIgnoreCase("butter");

        assertFalse(results.isEmpty());
    }

    @Test
    void save_andFindById_works() {
        Product p = new Product();
        p.setPname("Veg Manchurian");
        p.setPprice(220.0);
        p.setPdescription("Spicy veg balls");
        Product saved = productRepository.save(p);

        Optional<Product> found = productRepository.findById(saved.getPid());

        assertTrue(found.isPresent());
        assertEquals("Veg Manchurian", found.get().getPname());
    }

    @Test
    void delete_removesProduct() {
        Product p = new Product();
        p.setPname("Temp Product");
        p.setPprice(100.0);
        Product saved = productRepository.save(p);
        int id = saved.getPid();

        productRepository.deleteById(id);

        assertFalse(productRepository.findById(id).isPresent());
    }
}
