package com.tutoring.Tutorverse.Repository;


import com.tutoring.Tutorverse.Model.userDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface userRepository extends CrudRepository<userDto, UUID> {
    Optional<userDto> findByEmail(String email);
}