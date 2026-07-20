package com.example.demo.services;

import com.example.demo.entities.Orders;
import com.example.demo.entities.User;
import com.example.demo.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServicesTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServices orderServices;

    private Orders order;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setU_id(1);
        user.setUname("Alice");
        user.setUemail("alice@example.com");
        user.setUpassword("secret");

        order = new Orders();
        order.setoId(1);
        order.setoName("Hyderabadi Chicken Biryani");
        order.setoPrice(350.0);
        order.setoQuantity(2);
        order.setTotalAmmout(700.0);
        order.setOrderDate(new Date());
        order.setUser(user);
    }

    // ── getOrders ───────────────────────────────────────────────────────

    @Test
    void getOrders_returnsAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Orders> result = orderServices.getOrders();

        assertEquals(1, result.size());
        assertEquals("Hyderabadi Chicken Biryani", result.get(0).getoName());
    }

    @Test
    void getOrders_emptyRepository_returnsEmpty() {
        when(orderRepository.findAll()).thenReturn(List.of());

        assertTrue(orderServices.getOrders().isEmpty());
    }

    // ── saveOrder ───────────────────────────────────────────────────────

    @Test
    void saveOrder_callsSave() {
        orderServices.saveOrder(order);

        verify(orderRepository, times(1)).save(order);
    }

    // ── updateOrder ─────────────────────────────────────────────────────

    @Test
    void updateOrder_setsIdAndSaves() {
        Orders updated = new Orders();
        updated.setoName("Updated Order");

        orderServices.updateOrder(5, updated);

        assertEquals(5, updated.getoId());
        verify(orderRepository, times(1)).save(updated);
    }

    // ── deleteOrder ─────────────────────────────────────────────────────

    @Test
    void deleteOrder_callsDeleteById() {
        orderServices.deleteOrder(1);

        verify(orderRepository, times(1)).deleteById(1);
    }

    // ── getOrdersForUser ────────────────────────────────────────────────

    @Test
    void getOrdersForUser_returnsUserOrders() {
        when(orderRepository.findOrdersByUser(user)).thenReturn(List.of(order));

        List<Orders> result = orderServices.getOrdersForUser(user);

        assertEquals(1, result.size());
        assertEquals(700.0, result.get(0).getTotalAmmout());
    }

    @Test
    void getOrdersForUser_noOrders_returnsEmpty() {
        when(orderRepository.findOrdersByUser(user)).thenReturn(List.of());

        assertTrue(orderServices.getOrdersForUser(user).isEmpty());
    }
}
