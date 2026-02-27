package com.griet.library.repository;

import com.griet.library.model.BookRequest;
import com.griet.library.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {

    List<BookRequest> findByStatus(RequestStatus status);

    List<BookRequest> findByUser_Email(String email); // ✅ FIXED
}