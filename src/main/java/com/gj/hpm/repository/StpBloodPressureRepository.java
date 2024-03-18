package com.gj.hpm.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.gj.hpm.dto.response.GetBloodPressureResponse;
import com.gj.hpm.entity.BloodPressure;

public interface StpBloodPressureRepository extends MongoRepository<BloodPressure, String> {
    @Aggregation(pipeline = {
            "{ '$match': { 'createBy': ?0 } }",
            "{ '$sort': { 'createDate': -1 } }",
            "{ '$limit': 1 }"
    })
    Optional<BloodPressure> findBloodPressureCurrentTime(String userId);

    @Query("{ '_id': ?0 }")
    Optional<GetBloodPressureResponse> findByIdGetBloodPressureResp(String id);

    @Query("{ '_id': ?0,'createBy': ?1 }")
    Optional<GetBloodPressureResponse> findByTokenGetBloodPressureResp(String id, String userId);
}
