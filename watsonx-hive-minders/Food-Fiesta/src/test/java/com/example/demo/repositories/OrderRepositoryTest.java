package com.example.demo.repositories;

import com.example.demo.entities.Orders;
import com.example.demo.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setUname("Alice");
        alice.setUemail("alice@example.com");
        alice.setUpassword("secret");
        alice.setUnumber(111111111L);
        alice = userRepository.save(alice);

        bob = new User();
        bob.setUname("Bob");
        bob.setUemail("bob@example.com");
        bob.setUpassword("pass");
        bob.setUnumber(222222222L);
        bob = userRepository.save(bob);
    }

    private Orders createOrder(String name, double price, int qty, User user) {
        Orders o = new Orders();
        o.setoName(name);
        o.setoPrice(price);
        o.setoQuantity(qty);
        o.setTotalAmmout(price * qty);
        o.setOrderDate(new Date());
        o.setUser(user);
        return orderRepository.save(o);
    }

    // ── findOrdersByUser ─────────────────────────────────────────────────

    @Test
    void findOrdersByUser_returnsOnlyUserOrders() {
        createOrder("Biryani", 350.0, 2, alice);
        createOrder("Paneer", 280.0, 1, alice);
        createOrder("Tikka",  320.0, 1, bob);

        List<Orders> aliceOrders = orderRepository.findOrdersByUser(alice);

        assertEquals(2, aliceOrders.size());
        assertTrue(aliceOrders.stream().allMatch(o -> o.getUser().getU_id() == alice.getU_id()));
    }

    @Test
    void findOrdersByUser_userWithNoOrders_returnsEmpty() {
        List<Orders> result = orderRepository.findOrdersByUser(bob);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_andFindById_persists() {
        Orders saved = createOrder("Gulab Jamun", 120.0, 3, alice);

        assertTrue(orderRepository.findById(saved.getoId()).isPresent());
        assertEquals("Gulab Jamun", orderRepository.findById(saved.getoId()).get().getoName());
    }

    @Test
    void deleteById_removesOrder() {
        Orders saved = createOrder("Manchurian", 220.0, 2, alice);
        int id = saved.getoId();

        orderRepository.deleteById(id);

        assertFalse(orderRepository.findById(id).isPresent());
    }
}
