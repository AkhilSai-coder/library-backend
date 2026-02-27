package com.griet.library.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;   // ✅ FIXED (instead of String userEmail)

    @ManyToOne
    private Book book;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}