package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {
    private OrderController orderController;
    private UserRepository userRepository = mock(UserRepository.class);
    private OrderRepository orderRepository = mock(OrderRepository.class);

    @Before
    public void setup(){
        orderController = new OrderController();
        ReflectionTestUtils.setField(orderController, "userRepository", userRepository);
        ReflectionTestUtils.setField(orderController, "orderRepository", orderRepository);

        // Create a user
        User user = new User();
        user.setUsername("normaworm");

        // Create items to add to the cart
        Item item = new Item();
        item.setId(1L);
        item.setName("A Widget");
        BigDecimal price = BigDecimal.valueOf(2.99);
        item.setPrice(price);
        item.setDescription("A widget description");
        Item item2 = new Item();
        item2.setName("Square Widget");
        item2.setPrice(BigDecimal.valueOf(1.99));
        item2.setDescription("A widget that is square");

        // Create a cart, add items to it, save to user
        Cart cart = new Cart();
        cart.addItem(item);
        cart.addItem(item);
        cart.addItem(item2);
        user.setCart(cart);

        // Define what mockito should do to look up the user just created by username
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);

        // Create an order based on the cart
        UserOrder order = UserOrder.createFromCart(cart);

        // Define what mockito should do to save the order
        when(orderRepository.save(order)).thenReturn(order);

        // Define what mockito should do when order repository gets orders for a user
        when(orderRepository.findByUser(user)).thenReturn(Arrays.asList(order));
    }

    @Test
    public void verifySubmitOrder(){
        ResponseEntity<UserOrder> responseActual = orderController.submit("normaworm");
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        UserOrder orderActual = responseActual.getBody();
        assertNotNull(orderActual);
        assertEquals(3, orderActual.getItems().size());
    }

    @Test
    public void verifyGetOrderHistory(){
        ResponseEntity<List<UserOrder>> responseActual = orderController.getOrdersForUser("normaworm");
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        List<UserOrder> ordersActual = responseActual.getBody();
        assertNotNull(ordersActual);
        assertEquals(1, ordersActual.size());
    }
}
