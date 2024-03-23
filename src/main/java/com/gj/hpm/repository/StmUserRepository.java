package com.gj.hpm.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.gj.hpm.dto.response.GetUserResponse;
import com.gj.hpm.entity.User;

public interface StmUserRepository extends MongoRepository<User, String> {
        @Query("{'email': ?0}")
        Optional<User> findByEmail(String email);

        @Query("{'_id': ?0}")
        Optional<GetUserResponse> findGetUserByIdRespByUser_id(String userId);

        @Query("{'email': ?0}")
        Optional<GetUserResponse> findGetUserByTokenRespByEmail(String email);

        Boolean existsByUsername(String username);

        Boolean existsByEmail(String email);

        Boolean existsByPhone(String phone);

        Boolean existsByHn(String hn);

        // mismatch check in sign in
        @Query("{'lineId': ?0}")
        Optional<User> findByLineId(String lineId);

}
