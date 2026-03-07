package com.griet.library.repository;

import com.griet.library.model.BookRequest;
import com.griet.library.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {

    // find requests by status
    List<BookRequest> findByStatus(RequestStatus status);

    // find requests by user's college ID
    List<BookRequest> findByUser_CollegeId(String collegeId);

}
