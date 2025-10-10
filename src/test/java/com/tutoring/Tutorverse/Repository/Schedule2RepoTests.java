package com.tutoring.Tutorverse.Repository;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.transaction.Transactional;




@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class Schedule2RepoTests extends BaseRepositoryTest {
    
    
}
