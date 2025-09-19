package com.funbank.user.repository;

import com.funbank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByStatus(User.UserStatus status);

    List<User> findByRole(User.UserRole role);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt > :since")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:searchTerm% OR u.lastName LIKE %:searchTerm% OR u.username LIKE %:searchTerm%")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
}