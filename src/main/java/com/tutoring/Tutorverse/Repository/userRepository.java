package com.tutoring.Tutorverse.Repository;


import com.tutoring.Tutorverse.Model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface userRepository extends CrudRepository<User,UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderid(String providerid);
    boolean existsByEmail(String email);
    long count();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :role")
    long countByRoleName(String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.twoFactorEnabled = true")
    long countWithTwoFactor();

}