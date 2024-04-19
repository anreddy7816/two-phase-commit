package com.avula.twopc.orderservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.avula.twopc.orderservice.dto.TransactionData;
import com.avula.twopc.orderservice.enums.OrderPreparationStatus;
import com.avula.twopc.orderservice.model.Order;
import com.avula.twopc.orderservice.repo.OrderRepository;

@RestController
public class OrderController {

	@Autowired
	private OrderRepository repository;

	@PostMapping("/prepare-order")
	public ResponseEntity<String> prepareOrder(@RequestBody TransactionData transactionData) {

		try {
			Order order = new Order();
			order.setItem(transactionData.getItem());
			order.setOrderNumber(transactionData.getOrderNumber());
			order.setPreparationStatus(OrderPreparationStatus.PREPARRING.name());
			

			repository.save(order);

			if (shouldFailedDuringPreparation()) {
				throw new RuntimeException("Prepare phase fiaed for order" + transactionData.getOrderNumber());
			}
			return ResponseEntity.ok("Order prepared Successfully");

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during order preparation");
		}
	}

	private boolean shouldFailedDuringPreparation() {
		return false;
	}

	@PostMapping("/commit-order")
	public ResponseEntity<String> commitOrder(@RequestBody TransactionData transactionData) {

		Order order = repository.findByItem(transactionData.getItem());

		if (order != null && order.getPreparationStatus().equalsIgnoreCase(OrderPreparationStatus.PREPARRING.name())) {
			order.setPreparationStatus(OrderPreparationStatus.COMMITTED.name());
			repository.save(order);

			return ResponseEntity.ok("Order committed successfully");
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Order cannot be committed");
	}

	@PostMapping("/rollback-order")
	public ResponseEntity<String> rollBackOrder(@RequestBody TransactionData transactionData) {

		Order order = repository.findByItem(transactionData.getItem());

		if (order != null) {
			order.setPreparationStatus(OrderPreparationStatus.ROLLBACK.name());
			repository.save(order);

			return ResponseEntity.ok("Order rollback successfully");
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Order cannot be committed");
	}
}
