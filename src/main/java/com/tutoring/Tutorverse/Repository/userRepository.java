package com.tutoring.Tutorverse.Repository;


import com.tutoring.Tutorverse.Model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface userRepository extends CrudRepository<User,UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderid(String providerid);
    boolean existsByEmail(String email);
    Optional<User> findById(UUID id);

}