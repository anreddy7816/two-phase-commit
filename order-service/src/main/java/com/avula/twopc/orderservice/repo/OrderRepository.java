package com.avula.twopc.orderservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.avula.twopc.orderservice.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{

	Order findByItem(String item);

}
