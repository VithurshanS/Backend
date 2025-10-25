package com.tutoring.Tutorverse.Repository;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;
import java.util.UUID;

@Repository
public class ModuleEmailRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<Object[]> getModuleEmails(UUID moduleId) {
        Query query = entityManager.createNativeQuery("SELECT * FROM get_module_emails(:moduleId)");
        query.setParameter("moduleId", moduleId);
        return query.getResultList();
    }
}