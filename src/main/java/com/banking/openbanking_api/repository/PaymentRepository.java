package com.banking.openbanking_api.repository;

import com.banking.openbanking_api.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}