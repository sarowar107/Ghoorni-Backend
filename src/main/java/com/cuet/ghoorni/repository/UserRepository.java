package com.cuet.ghoorni.repository;

import com.cuet.ghoorni.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> { // Changed to String
    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(String userId); // New method for login

    // Methods for notification targeting
    List<User> findByBatch(String batch);

    List<User> findByDeptName(String deptName);

    List<User> findByDeptNameAndBatch(String deptName, String batch);
}