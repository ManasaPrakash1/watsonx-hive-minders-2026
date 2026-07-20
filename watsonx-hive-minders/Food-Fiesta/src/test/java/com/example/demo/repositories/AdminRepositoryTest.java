package com.example.demo.repositories;

import com.example.demo.entities.Admin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    private Admin createAdmin(String name, String email, String password) {
        Admin a = new Admin();
        a.setAdminName(name);
        a.setAdminEmail(email);
        a.setAdminPassword(password);
        a.setAdminNumber("0600000000");
        return adminRepository.save(a);
    }

    @Test
    void save_andFindById_works() {
        Admin saved = createAdmin("Super Admin", "admin@foodfiesta.com", "admin123");

        Optional<Admin> found = adminRepository.findById(saved.getAdminId());

        assertTrue(found.isPresent());
        assertEquals("Super Admin", found.get().getAdminName());
    }

    @Test
    void findByAdminEmail_knownEmail_returnsAdmin() {
        createAdmin("Super Admin", "admin@foodfiesta.com", "admin123");

        Admin found = adminRepository.findByAdminEmail("admin@foodfiesta.com");

        assertNotNull(found);
        assertEquals("admin123", found.getAdminPassword());
    }

    @Test
    void findByAdminEmail_unknownEmail_returnsNull() {
        assertNull(adminRepository.findByAdminEmail("nobody@foodfiesta.com"));
    }

    @Test
    void delete_removesAdmin() {
        Admin saved = createAdmin("ToDelete", "delete@foodfiesta.com", "pass");
        int id = saved.getAdminId();

        adminRepository.deleteById(id);

        assertFalse(adminRepository.findById(id).isPresent());
    }

    @Test
    void update_existingAdmin_persistsChanges() {
        Admin saved = createAdmin("Old Name", "update@foodfiesta.com", "pass");
        saved.setAdminName("New Name");
        adminRepository.save(saved);

        Admin found = adminRepository.findById(saved.getAdminId()).orElseThrow();
        assertEquals("New Name", found.getAdminName());
    }
}
