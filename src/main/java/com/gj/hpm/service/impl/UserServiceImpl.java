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
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Switch.CaseOperator;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.request.UpdateUserByIdRequest;
import com.gj.hpm.dto.request.UpdateUserByTokenRequest;
import com.gj.hpm.dto.request.UpdateUserCheckStateRequest;
import com.gj.hpm.dto.response.BaseDetailsResponse;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.dto.response.GetUserDetailPagingResponse;
import com.gj.hpm.dto.response.GetUserListByLevelResponse;
import com.gj.hpm.dto.response.GetUserListByStatusFlagResponse;
import com.gj.hpm.dto.response.GetUserPagingResponse;
import com.gj.hpm.dto.response.GetUserResponse;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.repository.StpBloodPressureRepository;
import com.gj.hpm.service.UserService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.MongoUtil;

@Service
public class UserServiceImpl implements UserService {

        @Autowired
        private StmUserRepository stmUserRepository;

        @Autowired
        private StpBloodPressureRepository stpBloodPressureRepository;

        @Autowired
        MongoTemplate mongoTemplate;

        @Transactional
        @Override
        public GetUserResponse getUserById(GetUserByIdRequest request) {
                GetUserResponse response = stmUserRepository.findGetUserByIdRespByUser_id(request.getUserId())
                                .orElse(null);
                if (response != null)
                        return response;
                response = new GetUserResponse();
                response.setStatus(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));
                return response;
        }

        @Transactional
        @Override
        public GetUserResponse getUserByToken(String email) {
                GetUserResponse response = stmUserRepository.findGetUserByTokenRespByEmail(email).orElse(null);
                if (response != null)
                        return response;
                response = new GetUserResponse();
                response.setStatus(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลผู้ใช้"))));
                return response;
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
                if (StringUtils.isNotEmpty(request.getStatusFlag()))
                        criteria.and("statusFlag").is(request.getStatusFlag());
                addCriteriaIfNotEmpty(criteria, "level", request.getLevel());
                return criteria;
        }

        private void addCriteriaIfNotEmpty(Criteria criteria, String field, String value) {
                if (StringUtils.isNotEmpty(value))
                        criteria.and(field).regex(".*" + value + ".*");
        }
        // ! ==============================

        @Transactional
        @Override
        public List<GetUserListByLevelResponse> getUserListByLevel() {
                CaseOperator[] conditions = new CaseOperator[] {
                                CaseOperator.when(ComparisonOperators.valueOf("level").equalToValue("DANGER")).then(3),
                                CaseOperator.when(ComparisonOperators.valueOf("level").equalToValue("WARNING2"))
                                                .then(2),
                                CaseOperator.when(ComparisonOperators.valueOf("level").equalToValue("WARNING1")).then(1)
                };

                ConditionalOperators.Switch switchCases = ConditionalOperators.switchCases(conditions)
                                .defaultTo(0);

                TypedAggregation<User> aggregation = Aggregation.newAggregation(User.class,
                                Aggregation.match(Criteria.where("lineId").exists(true)),
                                Aggregation.match(Criteria.where("level").ne("NORMAL")),
                                Aggregation.addFields()
                                                .addFieldWithValue("newLevel", switchCases)
                                                .build(),
                                Aggregation.sort(Sort.Direction.DESC, "newLevel"));

                return mongoTemplate.aggregate(aggregation, "user",
                                GetUserListByLevelResponse.class).getMappedResults();
        }

        @Transactional
        @Override
        public List<GetUserListByStatusFlagResponse> getUserListByStatusFlag() {
                TypedAggregation<User> aggregation = Aggregation.newAggregation(User.class,
                                Aggregation.match(Criteria.where("lineId").exists(true)),
                                Aggregation.match(Criteria.where("statusFlag").is(StatusFlag.INACTIVE.toString())),
                                Aggregation.sort(Sort.Direction.ASC, "hn"));

                return mongoTemplate.aggregate(aggregation, "user",
                                GetUserListByStatusFlagResponse.class).getMappedResults();
        }

        @Transactional
        @Override
        public BaseResponse updateUserById(String id, UpdateUserByIdRequest request) {
                User user = stmUserRepository.findById(request.getUserId()).orElse(null);
                if (user != null) {
                        user.setFirstName(request.getFirstName());
                        user.setLastName(request.getLastName());
                        user.setEmail(request.getEmail());
                        user.setUsername(request.getEmail());
                        user.setPhone(request.getPhone());
                        user.setHn(request.getHn());
                        user.setUpdateBy(User.builder().id(id).build());
                        user.setUpdateDate(LocalDateTime.now());
                        stmUserRepository.save(user);
                        return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                        ApiReturn.SUCCESS.description(),
                                        Collections.singletonList(new BaseDetailsResponse("Success ✅",
                                                        "อัพเดทข้อมูลผู้ใช้สำเร็จ"))));
                }
                return new BaseResponse(
                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(),
                                                Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                "ไม่พบข้อมูลผู้ใช้"))));
        }

        @Transactional
        @Override
        public BaseResponse updateUserByToken(String id, UpdateUserByTokenRequest request) {
                User user = stmUserRepository.findById(id).orElse(null);
                if (user != null) {
                        user.setFirstName(request.getFirstName());
                        user.setLastName(request.getLastName());
                        user.setEmail(request.getEmail());
                        user.setUsername(request.getEmail());
                        user.setPhone(request.getPhone());
                        user.setHn(request.getHn());
                        user.setUpdateBy(User.builder().id(id).build());
                        user.setUpdateDate(LocalDateTime.now());
                        stmUserRepository.save(user);
                        return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                        ApiReturn.SUCCESS.description(),
                                        Collections.singletonList(new BaseDetailsResponse("Success ✅",
                                                        "อัพเดทข้อมูลผู้ใช้สำเร็จ"))));
                }
                return new BaseResponse(
                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(),
                                                Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                "ไม่พบข้อมูลผู้ใช้"))));
        }

        @Transactional
        @Override
        public BaseResponse updateUserCheckState(UpdateUserCheckStateRequest request) {
                User user = stmUserRepository.findById(request.getPatientId()).orElse(null);
                if (user != null) {
                        user.setCheckState(request.isCheckStatus());
                        stmUserRepository.save(user);
                        return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                        ApiReturn.SUCCESS.description(),
                                        Collections.singletonList(new BaseDetailsResponse("Success ✅",
                                                        "อัพเดทข้อมูลผู้ใช้สำเร็จ"))));
                }
                return new BaseResponse(
                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(),
                                                Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                "ไม่พบข้อมูลผู้ใช้"))));
        }

        @Transactional
        @Override
        public BaseResponse deleteUserById(GetUserByIdRequest request) {
                boolean verify = stmUserRepository.existsById(request.getUserId());
                if (verify) {
                        stmUserRepository.deleteById(request.getUserId());
                        stpBloodPressureRepository.deleteByCreateBy_Id(request.getUserId());
                        return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                        ApiReturn.SUCCESS.description(),
                                        Collections.singletonList(
                                                        new BaseDetailsResponse("Success ✅", "ลบข้อมูลผู้ใช้สำเร็จ"))));
                }
                return new BaseResponse(
                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(),
                                                Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                "ไม่พบข้อมูลผู้ใช้"))));
        }

        @Transactional
        @Override
        public BaseResponse deleteUserByToken(String id, BaseRequest request) {
                boolean verify = stmUserRepository.existsById(id);
                if (verify) {
                        stmUserRepository.deleteById(id);
                        return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                        ApiReturn.SUCCESS.description(),
                                        Collections.singletonList(
                                                        new BaseDetailsResponse("Success ✅", "ลบข้อมูลผู้ใช้สำเร็จ"))));
                }
                return new BaseResponse(
                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(),
                                                Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                "ไม่พบข้อมูลผู้ใช้"))));
        }

}
