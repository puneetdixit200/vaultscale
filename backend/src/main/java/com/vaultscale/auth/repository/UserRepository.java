package com.vaultscale.auth.repository;

import com.vaultscale.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// This interface is used to work with the users table in the database.
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Finds a user using their email.
    // It may return a User, or it may return nothing.
    Optional<User> findByEmail(String email);

    // Checks whether a user with this email already exists.
    // Returns true if found, otherwise false.
    boolean existsByEmail(String email);
}
