package com.gj.hpm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.gj.hpm.dto.response.GetBloodPressureCurrentResponse;
import com.gj.hpm.dto.response.GetBloodPressureResponse;
import com.gj.hpm.entity.BloodPressureRecord;

public interface BloodPressureRecordRepository extends MongoRepository<BloodPressureRecord, String> {

    @Query("{ '_id': ?0 }")
    Optional<GetBloodPressureResponse> findByIdGetBloodPressureResp(String id);

    Optional<GetBloodPressureResponse> findByIdAndCreateById(String id, String userId);

    @Query("{ '_id': ?0, 'patient._id': ?1}")
    Optional<BloodPressureRecord> findByIdAndPatient_Id(String id, String userId);

    @Query("{ 'patient._id': ?0 }")
    List<GetBloodPressureResponse> findByPatient_Id(String userId);

    boolean existsByCreateDateAfterAndCreateById(LocalDateTime createDate, String createById);

    boolean existsById(String id);

    boolean existsByIdAndCreateById(String id, String createById);

    @Aggregation(pipeline = { "{ $sort: { 'createDate': -1 } }", "{ $limit: 1 }" })
    Optional<GetBloodPressureCurrentResponse> findByCurrent();

    void deleteByPatient_Id(String patientId);
}
