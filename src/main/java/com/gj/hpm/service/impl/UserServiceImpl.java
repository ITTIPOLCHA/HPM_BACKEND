package com.gj.hpm.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.request.UpdateUserByIdRequest;
import com.gj.hpm.dto.request.UpdateUserByTokenRequest;
import com.gj.hpm.dto.response.BaseDetailsResp;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResp;
import com.gj.hpm.dto.response.GetUserByIdResp;
import com.gj.hpm.dto.response.GetUserByTokenResp;
import com.gj.hpm.dto.response.GetUserDetailPagingResponse;
import com.gj.hpm.dto.response.GetUserPagingResponse;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.service.UserService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.MongoUtil;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private StmUserRepository stmUserRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Transactional
    @Override
    public GetUserByIdResp getUserById(GetUserByIdRequest request) {
        // ? find user by id in database
        GetUserByIdResp resp = stmUserRepository.findGetUserByIdRespByUser_id(request.getUserId()).orElse(null);

        // ? set status
        resp.setStatus(new BaseStatusResp((resp != null) ? ApiReturn.SUCCESS.code() : ApiReturn.NOT_FOUND.code(),
                (resp != null) ? ApiReturn.SUCCESS.description() : ApiReturn.NOT_FOUND.description(),
                Collections.singletonList((resp != null) ? new BaseDetailsResp("Success ✅", "ดึงข้อมูลผู้ใช้สำเร็จ")
                        : new BaseDetailsResp("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));

        return (resp != null) ? resp : new GetUserByIdResp();
    }

    @Transactional
    @Override
    public GetUserByTokenResp getUserByToken(String email) {
        // ? find user by id in database
        GetUserByTokenResp resp = stmUserRepository.findGetUserByTokenRespByEmail(email).orElse(null);

        // ? set status
        resp.setStatus(new BaseStatusResp((resp != null) ? ApiReturn.SUCCESS.code() : ApiReturn.NOT_FOUND.code(),
                (resp != null) ? ApiReturn.SUCCESS.description() : ApiReturn.NOT_FOUND.description(),
                Collections.singletonList((resp != null) ? new BaseDetailsResp("Success ✅", "ดึงข้อมูลผู้ใช้สำเร็จ")
                        : new BaseDetailsResp("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));

        return (resp != null) ? resp : new GetUserByTokenResp();
    }

    // ! ==============================
    @Transactional
    @Override
    public GetUserPagingResponse getUserPaging(GetUserPagingRequest request) {
        Page<GetUserDetailPagingResponse> userPage = findByAggregation(request);

        GetUserPagingResponse response = new GetUserPagingResponse();
        response.setUsers(userPage.getContent());
        response.setTotalPages(userPage.getTotalPages());
        response.setTotalItems(userPage.getTotalElements());
        response.setStatus(new BaseStatusResp(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                Collections.singletonList(new BaseDetailsResp("Success ✅", "ดึงข้อมูลผู้ใช้สำเร็จ"))));

        return response;
    }

    private Page<GetUserDetailPagingResponse> findByAggregation(GetUserPagingRequest request) {

        Sort sort = MongoUtil.getSortFromRequest(request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                sort.isEmpty() ? Sort.by(Sort.Order.asc("hn")) : sort);

        Criteria criteria = setCriteria(request);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(pageable.getSort()),
                Aggregation.skip((long) pageable.getOffset()),
                Aggregation.limit(pageable.getPageSize()));

        AggregationResults<GetUserDetailPagingResponse> aggregationResults = mongoTemplate.aggregate(
                aggregation, "user", GetUserDetailPagingResponse.class);

        List<GetUserDetailPagingResponse> results = aggregationResults.getMappedResults();

        long total = mongoTemplate.count(new Query(criteria), User.class, "user");

        return new PageImpl<>(results, pageable, total);
    }

    private Criteria setCriteria(GetUserPagingRequest request) {
        Criteria criteria = new Criteria();

        criteria.and("lineId").ne(null);

        addCriteriaIfNotEmpty(criteria, "firstName", request.getFirstName());
        addCriteriaIfNotEmpty(criteria, "lastName", request.getLastName());
        addCriteriaIfNotEmpty(criteria, "email", request.getEmail());
        addCriteriaIfNotEmpty(criteria, "phone", request.getPhone());
        addCriteriaIfNotEmpty(criteria, "hn", request.getHn());
        addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());

        return criteria;
    }

    private void addCriteriaIfNotEmpty(Criteria criteria, String field, String value) {
        if (StringUtils.isNotEmpty(value)) {
            criteria.and(field).regex(".*" + value + ".*");
        }
    }
    // ! ==============================

    @Transactional
    @Override
    public BaseResponse updateUserById(String id, UpdateUserByIdRequest request) {
        User user = stmUserRepository.findById(request.getUserId()).orElse(null);
        if (user != null) {
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setHn(request.getHn());
            user.setUpdateBy(id);
            user.setUpdateDate(LocalDateTime.now());
            stmUserRepository.save(user);
            return new BaseResponse(new BaseStatusResp(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                    Collections.singletonList(new BaseDetailsResp("Success ✅", "อัพเดทข้อมูลผู้ใช้สำเร็จ"))));
        }
        return new BaseResponse(new BaseStatusResp(ApiReturn.NOT_FOUND.code(), ApiReturn.NOT_FOUND.description(),
                Collections.singletonList(new BaseDetailsResp("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));
    }

    @Transactional
    @Override
    public BaseResponse updateUserByToken(String id, UpdateUserByTokenRequest request) {
        User user = stmUserRepository.findById(id).orElse(null);
        if (user != null) {
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setHn(request.getHn());
            user.setUpdateBy(id);
            user.setUpdateDate(LocalDateTime.now());
            stmUserRepository.save(user);
            return new BaseResponse(new BaseStatusResp(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                    Collections.singletonList(new BaseDetailsResp("Success ✅", "อัพเดทข้อมูลผู้ใช้สำเร็จ"))));
        }
        return new BaseResponse(new BaseStatusResp(ApiReturn.NOT_FOUND.code(), ApiReturn.NOT_FOUND.description(),
                Collections.singletonList(new BaseDetailsResp("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));
    }

    @Transactional
    @Override
    public BaseResponse deleteUserById(GetUserByIdRequest request) {
        boolean valid = stmUserRepository.existsById(request.getUserId());
        if (valid) {
            stmUserRepository.deleteById(request.getUserId());
            return new BaseResponse(new BaseStatusResp(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                    Collections.singletonList(new BaseDetailsResp("Success ✅", "ลบข้อมูลผู้ใช้สำเร็จ"))));
        }
        return new BaseResponse(new BaseStatusResp(ApiReturn.NOT_FOUND.code(), ApiReturn.NOT_FOUND.description(),
                Collections.singletonList(new BaseDetailsResp("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));
    }

    @Transactional
    @Override
    public BaseResponse deleteUserByToken(String id, BaseRequest request) {
        boolean valid = stmUserRepository.existsById(id);
        if (valid) {
            stmUserRepository.deleteById(id);
            return new BaseResponse(new BaseStatusResp(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                    Collections.singletonList(new BaseDetailsResp("Success ✅", "ลบข้อมูลผู้ใช้สำเร็จ"))));
        }
        return new BaseResponse(new BaseStatusResp(ApiReturn.NOT_FOUND.code(), ApiReturn.NOT_FOUND.description(),
                Collections.singletonList(new BaseDetailsResp("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));
    }

}
