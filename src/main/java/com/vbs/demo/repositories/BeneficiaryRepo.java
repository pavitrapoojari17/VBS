package com.vbs.demo.repositories;

import com.vbs.demo.models.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface BeneficiaryRepo extends JpaRepository<Beneficiary, Integer> {

    List<Beneficiary> findAllByUserId(int userId);

    boolean existsByUserIdAndUsername(int userId, String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM Beneficiary b WHERE b.userId = :userId AND b.username = :username")
    void deleteByUserIdAndUsername(@Param("userId") int userId, @Param("username") String username);
}