package com.example.demo.services;

import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
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
class UserServicesTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServices userServices;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setU_id(1);
        alice.setUname("Alice");
        alice.setUemail("alice@example.com");
        alice.setUpassword("secret");
        alice.setUnumber(123456789L);
    }

    // ── getAllUser ──────────────────────────────────────────────────────

    @Test
    void getAllUser_returnsListFromRepository() {
        when(userRepository.findAll()).thenReturn(List.of(alice));

        List<User> result = userServices.getAllUser();

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getUname());
    }

    @Test
    void getAllUser_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertTrue(userServices.getAllUser().isEmpty());
    }

    // ── getUser ─────────────────────────────────────────────────────────

    @Test
    void getUser_existingId_returnsUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(alice));

        User result = userServices.getUser(1);

        assertEquals("Alice", result.getUname());
    }

    @Test
    void getUser_missingId_throwsNoSuchElement() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userServices.getUser(99));
    }

    // ── getUserByEmail ──────────────────────────────────────────────────

    @Test
    void getUserByEmail_knownEmail_returnsUser() {
        when(userRepository.findUserByUemail("alice@example.com")).thenReturn(alice);

        User result = userServices.getUserByEmail("alice@example.com");

        assertNotNull(result);
        assertEquals("Alice", result.getUname());
    }

    @Test
    void getUserByEmail_unknownEmail_returnsNull() {
        when(userRepository.findUserByUemail("unknown@example.com")).thenReturn(null);

        assertNull(userServices.getUserByEmail("unknown@example.com"));
    }

    // ── addUser ─────────────────────────────────────────────────────────

    @Test
    void addUser_callsSaveOnRepository() {
        userServices.addUser(alice);

        verify(userRepository, times(1)).save(alice);
    }

    // ── updateUser ──────────────────────────────────────────────────────

    @Test
    void updateUser_setsIdAndSaves() {
        userServices.updateUser(alice, 42);

        assertEquals(42, alice.getU_id());
        verify(userRepository, times(1)).save(alice);
    }

    // ── deleteUser ──────────────────────────────────────────────────────

    @Test
    void deleteUser_callsDeleteById() {
        userServices.deleteUser(1);

        verify(userRepository, times(1)).deleteById(1);
    }

    // ── validateLoginCredentials ────────────────────────────────────────

    @Test
    void validateLoginCredentials_correctCredentials_returnsTrue() {
        when(userRepository.findAll()).thenReturn(List.of(alice));

        assertTrue(userServices.validateLoginCredentials("alice@example.com", "secret"));
    }

    @Test
    void validateLoginCredentials_wrongPassword_returnsFalse() {
        when(userRepository.findAll()).thenReturn(List.of(alice));

        assertFalse(userServices.validateLoginCredentials("alice@example.com", "wrong"));
    }

    @Test
    void validateLoginCredentials_wrongEmail_returnsFalse() {
        when(userRepository.findAll()).thenReturn(List.of(alice));

        assertFalse(userServices.validateLoginCredentials("other@example.com", "secret"));
    }

    @Test
    void validateLoginCredentials_emptyRepository_returnsFalse() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertFalse(userServices.validateLoginCredentials("alice@example.com", "secret"));
    }
}
