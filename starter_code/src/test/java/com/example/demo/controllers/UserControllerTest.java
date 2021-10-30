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

    // Negative test case (password doesn't fit criteria)
    @Test
    public void createUserSadPath() throws Exception{
        // Password fulfills regex, but too short
        CreateUserRequest r = new CreateUserRequest();
        r.setUsername("NormaShort");
        r.setPassword("aA1.");
        r.setConfirmPassword("aA1.");
        final ResponseEntity<User> response1 = userController.createUser(r);
        assertNotNull(response1);
        assertEquals(400, response1.getStatusCodeValue());

        // Password long enough but doesn't fulfill regex
        r.setPassword("AaBbC");
        r.setConfirmPassword("AaBbC");
        final ResponseEntity<User> response2 = userController.createUser(r);
        assertNotNull(response2);
        assertEquals(400, response2.getStatusCodeValue());

        // Password null
        r.setPassword(null);
        r.setConfirmPassword(null);
        final ResponseEntity<User> response3 = userController.createUser(r);
        assertNotNull(response3);
        assertEquals(400, response3.getStatusCodeValue());

        // Password doesn't match confirm password
        r.setPassword("AaBb1.");
        r.setConfirmPassword("AaBb2.");
        final ResponseEntity<User> response4 = userController.createUser(r);
        assertNotNull(response4);
        assertEquals(400, response4.getStatusCodeValue());
    }

    // Positive test case: user can be found by ID
    @Test
    public void verifyFindUserByIdPositive(){
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

    // Negative test case: user cannot be found by ID
    @Test
    public void verifyFindUserByIdNegative(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<User> responseActual = userController.findById(1L);
        assertNotNull(responseActual);
        assertEquals(404, responseActual.getStatusCodeValue());
    }

    // Positive test case: user can be found by username
    @Test
    public void verifyFindByUsernameHappy(){
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

    // Negative test case: user cannot be found by username
    @Test
    public void verifyFindByUsernameSad(){
        String username = "Cheese Ball";
        when(userRepository.findByUsername(username)).thenReturn(null);
        ResponseEntity<User> responseActual = userController.findByUserName(username);
        assertNotNull(responseActual);
        assertEquals(404, responseActual.getStatusCodeValue());
    }
}
