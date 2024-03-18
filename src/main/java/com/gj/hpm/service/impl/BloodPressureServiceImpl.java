package com.gj.hpm.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.request.CreateBloodPressureRequest;
import com.gj.hpm.dto.request.GetBloodPressureByTokenPagingRequest;
import com.gj.hpm.dto.request.GetBloodPressurePagingRequest;
import com.gj.hpm.dto.request.GetBloodPressureRequest;
import com.gj.hpm.dto.response.BaseDetailsResponse;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.dto.response.GetBloodPressureDetailPagingResponse;
import com.gj.hpm.dto.response.GetBloodPressurePagingResponse;
import com.gj.hpm.dto.response.GetBloodPressureResponse;
import com.gj.hpm.entity.BloodPressure;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StpBloodPressureRepository;
import com.gj.hpm.service.BloodPressureService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.MongoUtil;

@Service
public class BloodPressureServiceImpl implements BloodPressureService {
    @Autowired
    private StpBloodPressureRepository stpBloodPressureRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    @Override
    public BaseResponse createBloodPressure(String id, CreateBloodPressureRequest request) {
        BloodPressure valid = stpBloodPressureRepository.findBloodPressureCurrentTime(id).orElse(null);
        Duration duration = Duration.between(LocalDateTime.now(), LocalDateTime.now());
        if (valid != null)
            duration = Duration.between(valid.getCreateDate(), LocalDateTime.now());
        if (valid == null || duration.toHours() > 1 || duration.toDaysPart() > 0) {
            BloodPressure bloodPressure = new BloodPressure();
            bloodPressure.setSys(request.getSys());
            bloodPressure.setDia(request.getDia());
            bloodPressure.setPul(request.getPul());
            bloodPressure.setStatusFlag(StatusFlag.ACTIVE.code());
            bloodPressure.setCreateBy(User.builder().id(id).build());
            bloodPressure.setCreateDate(LocalDateTime.now());
            bloodPressure.setUpdateBy(User.builder().id(id).build());
            bloodPressure.setUpdateDate(LocalDateTime.now());
            stpBloodPressureRepository.save(bloodPressure);
            return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                    Collections.singletonList(new BaseDetailsResponse("Success ✅", "บันทึกข้อมูลความดันโลหิตสำเร็จ"))));
        }
        return new BaseResponse(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                ApiReturn.BAD_REQUEST.description(),
                Collections.singletonList(
                        new BaseDetailsResponse("Fail ❌", "ข้อมูลความดันโลหิตถูกบันทึกไปแล้ว ใน 1 ชั่วโมงนี้"))));
    }

    @Transactional
    @Override
    public GetBloodPressureResponse getBloodPressureById(GetBloodPressureRequest request) {
        // ? find user by id in database
        GetBloodPressureResponse resp = stpBloodPressureRepository
                .findByIdGetBloodPressureResp(request.getBloodPressureId()).orElse(null);

        // ? set status
        resp.setStatus(new BaseStatusResponse((resp != null) ? ApiReturn.SUCCESS.code() : ApiReturn.NOT_FOUND.code(),
                (resp != null) ? ApiReturn.SUCCESS.description() : ApiReturn.NOT_FOUND.description(),
                Collections
                        .singletonList(
                                (resp != null) ? new BaseDetailsResponse("Success ✅", "ดึงข้อมูลความดันโลหิตสำเร็จ")
                                        : new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลความดันโลหิต"))));

        return (resp != null) ? resp : new GetBloodPressureResponse();
    }

    @Override
    public GetBloodPressureResponse getBloodPressureByToken(String id, GetBloodPressureRequest request) {
        // ? find user by id in database
        GetBloodPressureResponse resp = stpBloodPressureRepository
                .findByTokenGetBloodPressureResp(id, request.getBloodPressureId()).orElse(null);

        // ? set status
        resp.setStatus(new BaseStatusResponse((resp != null) ? ApiReturn.SUCCESS.code() : ApiReturn.NOT_FOUND.code(),
                (resp != null) ? ApiReturn.SUCCESS.description() : ApiReturn.NOT_FOUND.description(),
                Collections
                        .singletonList(
                                (resp != null) ? new BaseDetailsResponse("Success ✅", "ดึงข้อมูลความดันโลหิตสำเร็จ")
                                        : new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลความดันโลหิต"))));

        return (resp != null) ? resp : new GetBloodPressureResponse();
    }

    @Transactional
    @Override
    public GetBloodPressurePagingResponse getBloodPressurePaging(GetBloodPressurePagingRequest request) {
        Page<GetBloodPressureDetailPagingResponse> bpPage = findByAggregation(request);

        GetBloodPressurePagingResponse response = new GetBloodPressurePagingResponse();
        response.setBps(bpPage.getContent());
        response.setTotalPages(bpPage.getTotalPages());
        response.setTotalItems(bpPage.getTotalElements());
        response.setStatus(new BaseStatusResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                Collections.singletonList(new BaseDetailsResponse("Success ✅", "ดึงข้อมูลความดันโลหิตสำเร็จ"))));

        return response;
    }

    private Page<GetBloodPressureDetailPagingResponse> findByAggregation(GetBloodPressurePagingRequest request) {

        Sort sort = MongoUtil.getSortFromRequest(request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                sort.isEmpty() ? Sort.by(Sort.Order.desc("createDate")) : sort);

        Criteria criteria = setCriteria(request);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(pageable.getSort()),
                Aggregation.skip((long) pageable.getOffset()),
                Aggregation.limit(pageable.getPageSize()));

        AggregationResults<GetBloodPressureDetailPagingResponse> aggregationResults = mongoTemplate.aggregate(
                aggregation, "bloodPressure", GetBloodPressureDetailPagingResponse.class);

        List<GetBloodPressureDetailPagingResponse> results = aggregationResults.getMappedResults();

        long total = mongoTemplate.count(new Query(criteria), BloodPressure.class, "bloodPressure");

        return new PageImpl<>(results, pageable, total);
    }

    private Criteria setCriteria(GetBloodPressurePagingRequest request) {
        Criteria criteria = new Criteria();

        addCriteriaIfNotEmpty(criteria, "sys", request.getSys());
        addCriteriaIfNotEmpty(criteria, "dia", request.getDia());
        addCriteriaIfNotEmpty(criteria, "pul", request.getPul());
        addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());

        if (StringUtils.isNotEmpty(request.getCreateBy())) {
            criteria.and("createBy.$id").is(new ObjectId(request.getCreateBy()));
        }

        return criteria;
    }

    private void addCriteriaIfNotEmpty(Criteria criteria, String field, String value) {
        if (StringUtils.isNotEmpty(value)) {
            criteria.and(field).regex(".*" + value + ".*");
        }
    }

    @Transactional
    @Override
    public GetBloodPressurePagingResponse getBloodPressurePagingByUserId(String id,
            GetBloodPressureByTokenPagingRequest request) {
        Page<GetBloodPressureDetailPagingResponse> bpPage = findByAggregationFromToken(id, request);

        GetBloodPressurePagingResponse response = new GetBloodPressurePagingResponse();
        response.setBps(bpPage.getContent());
        response.setTotalPages(bpPage.getTotalPages());
        response.setTotalItems(bpPage.getTotalElements());
        response.setStatus(new BaseStatusResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                Collections.singletonList(new BaseDetailsResponse("Success ✅", "ดึงข้อมูลความดันโลหิตสำเร็จ"))));

        return response;
    }

    private Page<GetBloodPressureDetailPagingResponse> findByAggregationFromToken(String id,
            GetBloodPressureByTokenPagingRequest request) {

        Sort sort = MongoUtil.getSortFromRequest(request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                sort.isEmpty() ? Sort.by(Sort.Order.desc("createDate")) : sort);

        Criteria criteria = setCriteriaFromToken(id, request);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(pageable.getSort()),
                Aggregation.skip((long) pageable.getOffset()),
                Aggregation.limit(pageable.getPageSize()));

        AggregationResults<GetBloodPressureDetailPagingResponse> aggregationResults = mongoTemplate.aggregate(
                aggregation, "bloodPressure", GetBloodPressureDetailPagingResponse.class);

        List<GetBloodPressureDetailPagingResponse> results = aggregationResults.getMappedResults();

        long total = mongoTemplate.count(new Query(criteria), BloodPressure.class, "bloodPressure");

        return new PageImpl<>(results, pageable, total);
    }

    private Criteria setCriteriaFromToken(String id, GetBloodPressureByTokenPagingRequest request) {
        Criteria criteria = new Criteria();

        criteria.and("createBy.$id").is(new ObjectId(id));

        addCriteriaIfNotEmpty(criteria, "sys", request.getSys());
        addCriteriaIfNotEmpty(criteria, "dia", request.getDia());
        addCriteriaIfNotEmpty(criteria, "pul", request.getPul());
        addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());

        return criteria;
    }

    @Override
    public BaseResponse updateBloodPressureById(BaseRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseResponse updateBloodPressureByToken(BaseRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseResponse deleteBloodPressureById(BaseRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseResponse deleteBloodPressureByToken(BaseRequest request) {
        // TODO Auto-generated method stub
        return null;
    }
}
