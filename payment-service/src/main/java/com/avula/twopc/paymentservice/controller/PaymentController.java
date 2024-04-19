package com.avula.twopc.paymentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.avula.twopc.paymentservice.dto.TransactionData;
import com.avula.twopc.paymentservice.enums.PaymentStatus;
import com.avula.twopc.paymentservice.model.Payment;
import com.avula.twopc.paymentservice.repo.PaymentRepository;

@RestController
public class PaymentController {

	@Autowired
	private PaymentRepository paymentRepo;

	@PostMapping("/prepare-payment")
	public ResponseEntity<String> preparePayment(@RequestBody TransactionData transactionData) {

		try {
			Payment payment = new Payment();
			payment.setItem(transactionData.getItem());
			payment.setOrderNumber(transactionData.getOrderNumber());
			payment.setPreparationStatus(PaymentStatus.PENDING.name());
			payment.setPrice(transactionData.getPrice());
			payment.setPaymentMode(transactionData.getPaymentMode());

			paymentRepo.save(payment);

			if (shouldFailedDuringPreparation()) {
				throw new RuntimeException("Prepare phase fiaed for payment" + transactionData.getOrderNumber());
			}
			return ResponseEntity.ok("Payment prepared Successfully");

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during payment preparation");
		}
	}

	private boolean shouldFailedDuringPreparation() {
		return false;
	}

	@PostMapping("/commit-payment")
	public ResponseEntity<String> commitPayment(@RequestBody TransactionData transactionData) {

		Payment payment = paymentRepo.findByItem(transactionData.getItem());

		if (payment != null && payment.getPreparationStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
			payment.setPreparationStatus(PaymentStatus.APPROVED.name());
			paymentRepo.save(payment);

			return ResponseEntity.ok("Payment approved successfully");
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment cannot be approved");
	}

	@PostMapping("/rollback-payment")
	public ResponseEntity<String> rollBackOrder(@RequestBody TransactionData transactionData) {

		Payment payment = paymentRepo.findByItem(transactionData.getItem());

		if (payment != null) {
			payment.setPreparationStatus(PaymentStatus.ROLLBACK.name());
			paymentRepo.save(payment);

			return ResponseEntity.ok("Payment rollback successfully");
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during payment rollback");
	}
}
