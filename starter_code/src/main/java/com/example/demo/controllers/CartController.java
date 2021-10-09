package com.example.demo.controllers;

import java.util.Optional;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	public static final Logger logger = LoggerFactory.getLogger(CartController.class);

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@PostMapping("/addToCart")
	public ResponseEntity<Cart> addTocart(@RequestBody ModifyCartRequest request) {
		logger.debug("User wants to add an item to their cart.");
		User user = userRepository.findByUsername(request.getUsername());
		if(user == null) {
			logger.info("Cannot add item to cart for unknown user {}.", request.getUsername());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		MDC.put("userName", request.getUsername());
		Optional<Item> item = itemRepository.findById(request.getItemId());
		if(!item.isPresent()) {
			logger.info("Requested item id {} is not available.", request.getItemId());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		Cart cart = user.getCart();
		logger.debug("Cart id {} is associated with user id {}.", cart.getId(), user.getId());
		logger.debug("Cart contains {} items. {} items to be added.", cart.getItems().size(), request.getQuantity());
		IntStream.range(0, request.getQuantity())
			.forEach(i -> cart.addItem(item.get()));
		logger.debug("Items added. Cart contains {} items.", cart.getItems().size());
		cartRepository.save(cart);
		logger.info("{} items saved to cart id {} for user id {}.", request.getQuantity(), cart.getId(), user.getId());
		return ResponseEntity.ok(cart);
	}
	
	@PostMapping("/removeFromCart")
	public ResponseEntity<Cart> removeFromcart(@RequestBody ModifyCartRequest request) {
		logger.debug("User wants to remove an item from their cart.");
		User user = userRepository.findByUsername(request.getUsername());
		if(user == null) {
			logger.info("Item not removed from cart. User {} could not be found.", request.getUsername());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		MDC.put("userName", user.getUsername());
		Optional<Item> item = itemRepository.findById(request.getItemId());
		if(!item.isPresent()) {
			logger.info("Item not removed from cart. Item id {} does not exist.", request.getItemId());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		Cart cart = user.getCart();
		logger.debug("Cart id {} is associated with user id {}.", cart.getId(), user.getId());
		logger.debug("{} items to be removed. {} items presently in cart.", request.getQuantity(), cart.getItems().size());
		// what happens if you try to remove too many items?
		IntStream.range(0, request.getQuantity())
			.forEach(i -> cart.removeItem(item.get()));
		logger.debug("Items removed. {} items presently in cart.", cart.getItems().size());
		cartRepository.save(cart);
		logger.info("Items removed and cart saved. Cart id {} contains {} items for user id {}.",
				cart.getId(), cart.getItems().size(), user.getId());
		return ResponseEntity.ok(cart);
	}
		
}
