package com.griet.library.repository;

import com.griet.library.model.Role;
import com.griet.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository – added countByRole for dashboard stats.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCollegeId(String collegeId);

    boolean existsByCollegeId(String collegeId);

    long countByRole(Role role);
}
