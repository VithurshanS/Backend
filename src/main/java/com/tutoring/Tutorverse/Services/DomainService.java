package com.tutoring.Tutorverse.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tutoring.Tutorverse.Dto.DomainDto;
import com.tutoring.Tutorverse.Model.DomainEntity;
import com.tutoring.Tutorverse.Repository.DomainRepository;


@Service
public class DomainService {

    @Autowired
    private DomainRepository domainRepository;

    public void createDomain(DomainDto domainDto) {
       try{
           DomainEntity domainEntity = new DomainEntity();
           domainEntity.setName(domainDto.getName());
           domainRepository.save(domainEntity);
       } catch (Exception e) {
           e.printStackTrace();
       }
    }

    public void deleteDomain(Integer id) {
        try{
            domainRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
