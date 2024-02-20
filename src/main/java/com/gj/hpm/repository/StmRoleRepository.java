package com.gj.hpm.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.gj.hpm.entity.ERole;
import com.gj.hpm.entity.Role;

public interface StmRoleRepository extends MongoRepository<Role, String> {

    @Query("{'name': ?0}")
    Role findByName(ERole name);

}
