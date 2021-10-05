package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserController userController;
    private UserRepository userRepository = mock(UserRepository.class);
    private CartRepository cartRepository = mock(CartRepository.class);
    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    @Before
    public void setUp(){
        userController = new UserController();
        // Another way to inject objects into private fields
        // We basically implemented a function that is available in Java
        //ReflectionTestUtils.setField(userController, "userRepository", userRepository);

        TestUtils.injectObjects(userController, "userRepository", userRepository);
        TestUtils.injectObjects(userController, "cartRepository", cartRepository);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", encoder);
    }

    // positive test case (sanity test case)
    @Test
    public void createUserHappyPath() throws Exception{
        // Since we will get back a hashed value, it's difficult to compare with an expected value
        // Stubbing is used in those cases
        when(encoder.encode("AaBbQq123.")).thenReturn("thisIsHashed.");
        CreateUserRequest r = new CreateUserRequest();
        r.setUsername("norma");
        r.setPassword("AaBbQq123.");
        r.setConfirmPassword("AaBbQq123.");

        final ResponseEntity<User> response = userController.createUser(r);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        // User is sent as the response body
        User user = response.getBody();
        assertNotNull(user);
        assertEquals(0, user.getId());
        assertEquals("norma", user.getUsername());
        assertEquals("thisIsHashed.", user.getPassword());
    }

    @Test
    public void verifyFindUserById(){
        User userExpected = new User();
        userExpected.setUsername("spooky");
        userExpected.setId(1L);
        userExpected.setPassword("CcDdEe123.");

        when(userRepository.findById(userExpected.getId())).thenReturn(Optional.of(userExpected));
        ResponseEntity<User> responseActual = userController.findById(userExpected.getId());
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        User userActual = responseActual.getBody();
        assertNotNull(userActual);
        assertEquals(userExpected, userActual);
        assertEquals("spooky", userActual.getUsername());
    }

    @Test
    public void verifyFindByUsername(){
        User userExpected = new User();
        userExpected.setUsername("daisy");

        when(userRepository.findByUsername(userExpected.getUsername())).thenReturn(userExpected);
        ResponseEntity<User> responseActual = userController.findByUserName(userExpected.getUsername());
        assertNotNull(responseActual);
        assertEquals(200, responseActual.getStatusCodeValue());

        User userActual = responseActual.getBody();
        assertNotNull(userActual);
        assertEquals(userExpected, userActual);
    }
}
