package com.tutoring.Tutorverse.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tutoring.Tutorverse.Dto.DomainDto;
import com.tutoring.Tutorverse.Model.DomainEntity;
import com.tutoring.Tutorverse.Repository.DomainRepository;

import java.util.Collections;   


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


    public List<DomainDto> getAllDomains() {
        try {
            List<DomainEntity> domainEntities = domainRepository.findAll();
            return domainEntities.stream()
                    .map(domain -> new DomainDto(domain.getDomainId(), domain.getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void deleteDomain(Integer id) {
        try{
            domainRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
