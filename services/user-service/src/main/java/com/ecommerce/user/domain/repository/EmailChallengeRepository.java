package com.ecommerce.user.domain.repository;

import com.ecommerce.user.domain.model.aggregate.EmailChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EmailChallengeRepository extends JpaRepository<EmailChallenge, UUID> {
    Optional<EmailChallenge> findByEmailAndPurpose(String email, EmailChallenge.Purpose purpose);
}
