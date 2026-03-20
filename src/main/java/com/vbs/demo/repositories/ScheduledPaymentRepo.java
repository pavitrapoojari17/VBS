package com.vbs.demo.repositories;

import com.vbs.demo.models.ScheduledPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduledPaymentRepo extends JpaRepository<ScheduledPayment, Integer> {
    List<ScheduledPayment> findAllByUserId(int userId);
    List<ScheduledPayment> findAllByActiveTrue();
}