package com.example.demo.repositories;

import com.example.demo.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String name, String email, String password) {
        User u = new User();
        u.setUname(name);
        u.setUemail(email);
        u.setUpassword(password);
        u.setUnumber(123456789L);
        return userRepository.save(u);
    }

    @Test
    void save_andFindById_works() {
        User saved = createUser("Alice", "alice@example.com", "secret");

        Optional<User> found = userRepository.findById(saved.getU_id());

        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getUname());
    }

    @Test
    void findUserByUemail_knownEmail_returnsUser() {
        createUser("Alice", "alice@example.com", "secret");

        User found = userRepository.findUserByUemail("alice@example.com");

        assertNotNull(found);
        assertEquals("Alice", found.getUname());
    }

    @Test
    void findUserByUemail_unknownEmail_returnsNull() {
        assertNull(userRepository.findUserByUemail("nobody@example.com"));
    }

    @Test
    void delete_removesUser() {
        User saved = createUser("Bob", "bob@example.com", "pass");
        int id = saved.getU_id();

        userRepository.deleteById(id);

        assertFalse(userRepository.findById(id).isPresent());
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        createUser("Alice", "alice@example.com", "s1");
        createUser("Bob",   "bob@example.com",   "s2");

        long count = ((java.util.List<User>) userRepository.findAll()).size();

        assertTrue(count >= 2);
    }
}
