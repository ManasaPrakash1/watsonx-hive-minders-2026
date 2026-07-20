package com.example.demo.services;

import com.example.demo.entities.Admin;
import com.example.demo.repositories.AdminRepository;
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
class AdminServicesTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminServices adminServices;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setAdminId(1);
        admin.setAdminName("Super Admin");
        admin.setAdminEmail("admin@foodfiesta.com");
        admin.setAdminPassword("admin123");
        admin.setAdminNumber("9876543210");
    }

    // ── getAll ──────────────────────────────────────────────────────────

    @Test
    void getAll_returnsListFromRepository() {
        when(adminRepository.findAll()).thenReturn(List.of(admin));

        List<Admin> result = adminServices.getAll();

        assertEquals(1, result.size());
        assertEquals("Super Admin", result.get(0).getAdminName());
    }

    // ── getAdmin ────────────────────────────────────────────────────────

    @Test
    void getAdmin_existingId_returnsAdmin() {
        when(adminRepository.findById(1)).thenReturn(Optional.of(admin));

        Admin result = adminServices.getAdmin(1);

        assertEquals("Super Admin", result.getAdminName());
    }

    @Test
    void getAdmin_missingId_throwsException() {
        when(adminRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> adminServices.getAdmin(99));
    }

    // ── addAdmin ────────────────────────────────────────────────────────

    @Test
    void addAdmin_callsSave() {
        adminServices.addAdmin(admin);

        verify(adminRepository, times(1)).save(admin);
    }

    // ── delete ──────────────────────────────────────────────────────────

    @Test
    void delete_callsDeleteById() {
        adminServices.delete(1);

        verify(adminRepository, times(1)).deleteById(1);
    }

    // ── validateAdminCredentials ────────────────────────────────────────

    @Test
    void validateAdminCredentials_correctCredentials_returnsTrue() {
        when(adminRepository.findByAdminEmail("admin@foodfiesta.com")).thenReturn(admin);

        assertTrue(adminServices.validateAdminCredentials("admin@foodfiesta.com", "admin123"));
    }

    @Test
    void validateAdminCredentials_wrongPassword_returnsFalse() {
        when(adminRepository.findByAdminEmail("admin@foodfiesta.com")).thenReturn(admin);

        assertFalse(adminServices.validateAdminCredentials("admin@foodfiesta.com", "badpass"));
    }

    @Test
    void validateAdminCredentials_unknownEmail_returnsFalse() {
        when(adminRepository.findByAdminEmail("unknown@foodfiesta.com")).thenReturn(null);

        assertFalse(adminServices.validateAdminCredentials("unknown@foodfiesta.com", "admin123"));
    }

    // ── update ──────────────────────────────────────────────────────────

    @Test
    void update_matchingId_callsSave() {
        Admin updatedAdmin = new Admin();
        updatedAdmin.setAdminName("Updated Admin");
        when(adminRepository.findAll()).thenReturn(List.of(admin));

        adminServices.update(updatedAdmin, 1);

        verify(adminRepository, times(1)).save(updatedAdmin);
    }

    @Test
    void update_nonMatchingId_doesNotCallSave() {
        Admin updatedAdmin = new Admin();
        when(adminRepository.findAll()).thenReturn(List.of(admin));

        adminServices.update(updatedAdmin, 99);

        verify(adminRepository, never()).save(any());
    }
}
