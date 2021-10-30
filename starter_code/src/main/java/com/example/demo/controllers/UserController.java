package com.example.demo.controllers;

import java.util.Optional;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;

// checking for uppercase, lowercase, number and special character from:
// https://www.geeksforgeeks.org/check-if-a-string-contains-uppercase-lowercase-special-characters-and-numeric-values/

@RestController
@RequestMapping("/api/user")
public class UserController {
	public static final Logger logger = LoggerFactory.getLogger(UserController.class);


	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	private String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-+_!@#$%^&*., ?]).+$";

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		logger.info("User entered endpoint /api/user/id/{}", id);
		ResponseEntity<User> response = ResponseEntity.of(userRepository.findById(id));

		if (response.getStatusCodeValue() == 200){
			logger.info("findById {}: Success", id);
		}else{
			logger.info("findById {}: Failure", id);
		}

		return response;
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<User> findByUserName(@PathVariable String username) {
		logger.info("User entered endpoint /api/user/{}", username);
		User user = userRepository.findByUsername(username);

		if (user == null){
			logger.info("findByUserName {}: Success", username);
		}else{
			logger.info("findByUserName {}: Failure", username);
		}

		return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<User> createUser(@RequestBody CreateUserRequest createUserRequest) {
		logger.debug("User entered endpoint /api/user/create");
		User user = new User();
		user.setUsername(createUserRequest.getUsername());
		logger.debug("Username set to: {}.", createUserRequest.getUsername());
		String password = createUserRequest.getPassword();
		String passwordConfirm = createUserRequest.getConfirmPassword();
		if(password == null || !password.matches(regex) || password.length() < 5 || !password.equals(passwordConfirm)){
			logger.info("createUser failure: User passwords did not match or meet password criteria.");
			return ResponseEntity.badRequest().build();
		}
		MDC.put("userName", (user.getUsername()));
		user.setPassword(bCryptPasswordEncoder.encode(password));
		logger.debug("Password successfully set.");
		Cart cart = new Cart();
		logger.debug("Cart created.");
		cartRepository.save(cart);
		logger.debug("Cart saved to cart repository.");
		user.setCart(cart);
		logger.debug("Cart saved to user.");
		userRepository.save(user);
		logger.info("createUser success: User {} created with userId {}.", user.getUsername(), user.getId());
		return ResponseEntity.ok(user);
	}
	
}
