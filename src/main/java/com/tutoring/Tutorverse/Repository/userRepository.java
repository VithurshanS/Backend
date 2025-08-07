package com.tutoring.Tutorverse.Repository;


import com.tutoring.Tutorverse.Model.userDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface userRepository extends CrudRepository<userDto,Long> {
    Optional<userDto> findByEmail(String email);

}