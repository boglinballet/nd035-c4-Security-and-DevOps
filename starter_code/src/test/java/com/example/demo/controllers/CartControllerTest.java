package com.example.demo.controllers;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CartControllerTest {
    private CartController cartController;
    private UserRepository userRepository = mock(UserRepository.class);
    private CartRepository cartRepository = mock(CartRepository.class);
    private ItemRepository itemRepository = mock(ItemRepository.class);

    @Before
    public void setUp(){
        cartController = new CartController();

        // Try out using the included function in Mockito instead of our TestUtils class
        ReflectionTestUtils.setField(cartController, "userRepository", userRepository);
        ReflectionTestUtils.setField(cartController, "cartRepository", cartRepository);
        ReflectionTestUtils.setField(cartController, "itemRepository", itemRepository);

        Item item = new Item();
        item.setId(1L);
        item.setName("A Widget");
        BigDecimal price = BigDecimal.valueOf(2.99);
        item.setPrice(price);
        item.setDescription("A widget description");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        User user = new User();
        user.setUsername("normaworm");
        Cart cart = new Cart();
        cart.addItem(item);
        user.setCart(cart);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        when(cartRepository.save(cart)).thenReturn(cart);
    }

    @Test
    public void verifyAddToCart(){
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("normaworm");
        request.setItemId(1L);
        request.setQuantity(2);

        ResponseEntity<Cart> responseActual = cartController.addTocart(request);
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        Cart cartActual = responseActual.getBody();
        assertNotNull(cartActual);
        assertEquals(3, cartActual.getItems().size());
    }

    @Test
    public void verifyRemoveFromCart(){
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("normaworm");
        request.setItemId(1L);
        request.setQuantity(1);

        ResponseEntity<Cart> responseActual = cartController.removeFromcart(request);
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        Cart cartActual = responseActual.getBody();
        assertNotNull(cartActual);
        assertEquals(0, cartActual.getItems().size());
    }
}
