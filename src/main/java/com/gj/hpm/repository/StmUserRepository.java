package com.gj.hpm.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.gj.hpm.entity.User;

public interface StmUserRepository extends MongoRepository<User, String> {
        @Query("{'email': ?0}")
        Optional<User> findByEmail(String email);

        @Query("{'username': ?0}")
        Optional<User> findByUsername(String username);

        Boolean existsByUsername(String username);

        Boolean existsByEmail(String email);

        // duplicate check in sign up
        @Query("{ $or: [ { 'email': ?0 }, { 'phone': ?1 }, {'hn':  ?2}, { $expr: { $eq: [ { $concat: ['$firstName', ' ', '$lastName'] }, ?3 ] } } ] }")
        Optional<User> findEmailOrPhoneOrHnOrFullName(String email, String phone, String hn, String fullName);

        // mismatch check in sign in
        @Query("{'lineId': ?0}")
        Optional<User> findByLineId(String lineId);

}
