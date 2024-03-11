package com.gj.hpm.service.impl;

import java.util.ArrayList;
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
    public GetUserByIdResp getUserById(GetUserByIdRequest req) {
        // ? find user by id in database
        GetUserByIdResp resp = stmUserRepository.findGetUserByIdRespByUser_id(req.getUserId()).orElse(null);

        // ? set status
        String code = (resp != null) ? ApiReturn.SUCCESS.code() : ApiReturn.NOT_FOUND.code();
        String description = (resp != null) ? ApiReturn.SUCCESS.description() : ApiReturn.NOT_FOUND.description();
        List<BaseDetailsResp> details = new ArrayList<>();
        details.add(new BaseDetailsResp("User", (resp != null) ? "Successfully Get User Data" : "Not Found"));
        if (resp == null)
            resp = new GetUserByIdResp();
        resp.setStatus(new BaseStatusResp(code, description, details));

        return resp;
    }

    @Transactional
    @Override
    public GetUserByTokenResp getUserByToken(String email) {
        // ? find user by id in database
        GetUserByTokenResp resp = stmUserRepository.findGetUserByTokenRespByEmail(email).orElse(null);

        // ? set status
        String code = (resp != null) ? ApiReturn.SUCCESS.code() : ApiReturn.NOT_FOUND.code();
        String description = (resp != null) ? ApiReturn.SUCCESS.description() : ApiReturn.NOT_FOUND.description();
        List<BaseDetailsResp> details = new ArrayList<>();
        details.add(new BaseDetailsResp("User", (resp != null) ? "Successfully Get User Data" : "Not Found"));
        if (resp == null)
            resp = new GetUserByTokenResp();
        resp.setStatus(new BaseStatusResp(code, description, details));

        return resp;
    }

    // ! ==============================
    @Transactional
    @Override
    public GetUserPagingResponse getUserPaging(GetUserPagingRequest req) {
        Page<GetUserDetailPagingResponse> userPage = findByAggregation(req);

        GetUserPagingResponse response = new GetUserPagingResponse();
        response.setUsers(userPage.getContent());
        response.setTotalPages(userPage.getTotalPages());
        response.setTotalItems(userPage.getTotalElements());
        response.setStatus(new BaseStatusResp(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                null));

        return response;
    }

    private Page<GetUserDetailPagingResponse> findByAggregation(GetUserPagingRequest req) {

        Sort sort = MongoUtil.getSortFromRequest(req);

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(),
                sort.isEmpty() ? Sort.by(Sort.Order.asc("hn")) : sort);

        Criteria criteria = setCriteria(req);

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

    private Criteria setCriteria(GetUserPagingRequest req) {
        Criteria criteria = new Criteria();

        criteria.and("lineId").ne(null);

        addCriteriaIfNotEmpty(criteria, "firstName", req.getFirstName());
        addCriteriaIfNotEmpty(criteria, "lastName", req.getLastName());
        addCriteriaIfNotEmpty(criteria, "email", req.getEmail());
        addCriteriaIfNotEmpty(criteria, "phone", req.getPhone());
        addCriteriaIfNotEmpty(criteria, "hn", req.getHn());
        addCriteriaIfNotEmpty(criteria, "statusFlag", req.getStatusFlag());

        return criteria;
    }

    private void addCriteriaIfNotEmpty(Criteria criteria, String field, String value) {
        if (StringUtils.isNotEmpty(value)) {
            criteria.and(field).regex(".*" + value + ".*");
        }
    }
    // ! ==============================

    @Override
    public BaseResponse updateUserById(BaseRequest req) {
        // User user = stmUserRepository.findById(req()).orElse(null);
        return null;
    }

    @Override
    public GetUserByIdResp deleteUserById(BaseRequest req) {
        // TODO Auto-generated method stub
        return null;
    }
}
