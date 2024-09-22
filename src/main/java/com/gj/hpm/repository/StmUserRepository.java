package com.gj.hpm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.gj.hpm.dto.response.GetUserListResponse;
import com.gj.hpm.dto.response.GetUserResponse;
import com.gj.hpm.entity.User;

public interface StmUserRepository extends MongoRepository<User, String> {
        @Query("{'email': ?0}")
        Optional<User> findByEmail(String email);

        @Query("{'email': ?0, 'phoneNumber': ?1 }")
        Optional<User> findByEmailAndPhoneNumber(String email, String phone);

        @Query("{'_id': ?0}")
        Optional<GetUserResponse> findGetUserByIdRespByUser_id(String userId);

        @Query("{'email': ?0}")
        Optional<GetUserResponse> findGetUserByTokenRespByEmail(String email);

        Boolean existsByUsername(String username);

        Boolean existsByEmail(String email);

        Boolean existsByPhoneNumber(String phone);

        Boolean existsByHospitalNumber(String hn);

        Boolean existsByLineId(String lineId);

        // mismatch check in sign in
        @Query("{'lineId': ?0}")
        Optional<User> findByLineId(String lineId);

        @Aggregation(pipeline = {
                        "{ $match : { lineId : { $exists : true } } }"
        })
        List<GetUserListResponse> findAllUserWithLineId();

        @Aggregation(pipeline = {
                        "{ $match : { lineId : { $exists : true } } }"
        })
        List<User> findAllUserWithLine();

        @Aggregation(pipeline = {
                        "{ $match : { lineId : { $exists : true } } }"
        })
        List<User> findAllUserWithLineIdInState();

}
