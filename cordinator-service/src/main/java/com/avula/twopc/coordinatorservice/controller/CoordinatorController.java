package com.avula.twopc.coordinatorservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.avula.twopc.coordinatorservice.dto.TransactionData;

@RestController
public class CoordinatorController {

	private final RestTemplate restTemplate = new RestTemplate();

	@PostMapping("/initiate-2pc")
	public String initiateTwoPhaseCommit(@RequestBody TransactionData trascationData) {

		if (callPreparePhase(trascationData)) {
			if (callCommitPhase(trascationData)) {
				return "Transacation committed successfully";
			}
			callRollbackPhase(trascationData);
			return "Transacation Rollback";
		}
		callRollbackPhase(trascationData);
		return "Transacation Rollback";
	}

	private boolean callPreparePhase(TransactionData transactionData) {

		boolean isOrderSuccess = callServices("http://localhost:8081/prepare-order", transactionData);
		boolean isPaymentSuccess = callServices("http://localhost:8082/prepare-payment", transactionData);
		return isOrderSuccess && isPaymentSuccess;
	}

	private boolean callCommitPhase(TransactionData transactionData) {

		boolean isOrderSuccess = callServices("http://localhost:8081/commit-order", transactionData);
		boolean isPaymentSuccess = callServices("http://localhost:8082/commit-payment", transactionData);
		return isOrderSuccess && isPaymentSuccess;
	}

	private boolean callServices(String url, TransactionData transactionData) {

		ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, transactionData, String.class);
		return postForEntity.getStatusCode().is2xxSuccessful();
	}

	private void callRollbackPhase(TransactionData transactionData) {

		callRollbackServices("http://localhost:8081/rollback-order", transactionData);
		callRollbackServices("http://localhost:8082/rollback-payment", transactionData);
	}

	private void callRollbackServices(String url, TransactionData transactionData) {
		restTemplate.postForEntity(url, transactionData, Void.class);
	}

}
