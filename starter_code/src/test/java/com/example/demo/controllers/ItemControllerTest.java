package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Needed help for creating list of items
// https://knowledge.udacity.com/questions/467893

public class ItemControllerTest {

    private ItemController itemController;
    private ItemRepository itemRepository = mock(ItemRepository.class);

    @Before
    public void setUp(){
        itemController = new ItemController();
        TestUtils.injectObjects(itemController,"itemRepository",itemRepository);
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

        when(itemRepository.findAll()).thenReturn(Arrays.asList(item, item2));
        when(itemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(itemRepository.findByName("Square Widget")).thenReturn(Arrays.asList(item2));
    }
    @Test
    public void verifyGetAllItems() {
        ResponseEntity<List<Item>> responseActual = itemController.getItems();
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        List<Item> items = responseActual.getBody();
        assertNotNull(items);
        assertEquals(2, items.size());

    }

    @Test
    public void verifyGetItemById(){
        ResponseEntity<Item> responseActual = itemController.getItemById(1L);
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        Item itemActual = responseActual.getBody();
        assertNotNull(itemActual);
        assertEquals("A Widget", itemActual.getName());
    }

    @Test
    public void verifyGetItemByName(){
        ResponseEntity<List<Item>> responseActual = itemController.getItemsByName("Square Widget");
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        Item itemActual = responseActual.getBody().get(0);
        assertNotNull(itemActual);
        assertEquals("Square Widget", itemActual.getName());
        assertEquals("A widget that is square", itemActual.getDescription());
    }
}
