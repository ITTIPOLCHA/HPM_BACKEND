package com.gj.hpm.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gj.hpm.config.security.jwt.JwtUtils;
import com.gj.hpm.config.security.services.UserDetailsImpl;
import com.gj.hpm.dto.PageableDto;
import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.request.PasswordChangeRequest;
import com.gj.hpm.dto.request.PasswordForgotRequest;
import com.gj.hpm.dto.request.SignInRequest;
import com.gj.hpm.dto.request.SignUpRequest;
import com.gj.hpm.dto.request.UpdateUserByIdRequest;
import com.gj.hpm.dto.request.UpdateUserByTokenRequest;
import com.gj.hpm.dto.request.UpdateUserCheckStateRequest;
import com.gj.hpm.dto.response.BaseDetailsResponse;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.dto.response.GetUserDetailPagingResponse;
import com.gj.hpm.dto.response.GetUserListByLevelResponse;
import com.gj.hpm.dto.response.GetUserListByStatusFlagResponse;
import com.gj.hpm.dto.response.GetUserResponse;
import com.gj.hpm.dto.response.JwtResponse;
import com.gj.hpm.entity.ERole;
import com.gj.hpm.entity.Role;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmRoleRepository;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.repository.BloodPressureRecordRepository;
import com.gj.hpm.service.UserService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.Key;
import com.gj.hpm.util.Constant.Level;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.Constant.TypeSignIn;
import com.gj.hpm.util.Encryption;
import com.gj.hpm.util.LineUtil;
import com.gj.hpm.util.MongoUtil;
import com.gj.hpm.util.ResponseUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.extern.log4j.Log4j;

@Log4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

        @Value("${hpm.app.token.property}")
        private String token;

        @Value("${hpm.app.rich.menu}")
        private String richMenu;

        @Autowired
        private StmUserRepository stmUserRepository;

        @Autowired
        StmRoleRepository roleRepository;

        @Autowired
        private BloodPressureRecordRepository stpBloodPressureRepository;

        @Autowired
        MongoTemplate mongoTemplate;

        @Autowired
        PasswordEncoder encoder;

        @Autowired
        JwtUtils jwtUtils;

        @Autowired
        AuthenticationManager authenticationManager;

        @Override
        public JwtResponse signIn(SignInRequest request) {
                // Check Line Token
                if (StringUtils.isNotBlank(request.getLineToken())) {
                        JWTClaimsSet claimsSet = jwtUtils.decodeES256Jwt(request.getLineToken());
                        String lineId = claimsSet.getSubject();
                        User user = handleUserSignIn(request, lineId, claimsSet);
                        if (updateUserWithLineData(user, lineId, claimsSet)) {
                                stmUserRepository.save(user);
                        }
                }
                // Authentication
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // Create Jwt Token
                String jwt = jwtUtils.generateJwtToken(authentication);
                //
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());
                return new JwtResponse(jwt,
                                userDetails.getId(),
                                userDetails.getEmail(),
                                userDetails.getName(),
                                roles);
        }

        private User handleUserSignIn(SignInRequest request, String lineId, JWTClaimsSet claimsSet) {
                // Line Or Email
                if (TypeSignIn.line.toString().equals(request.getType())) {
                        User user = stmUserRepository.findByLineId(lineId)
                                        .orElseThrow();
                        request.setEmail(user.getEmail());
                        request.setPassword(Encryption.decodedData(user.getLineSubjectId()));
                        return user;
                } else {
                        return stmUserRepository.findByEmail(request.getEmail())
                                        .orElseThrow();
                }
        }

        private boolean updateUserWithLineData(User user, String lineId, JWTClaimsSet claimsSet) {
                boolean isUpdated = false;
                // Check update data from line.
                if (!StringUtils.equals(user.getLineId(), lineId)) {
                        user.setLineId(lineId);
                        isUpdated = true;
                }
                String lineName = claimsSet.getClaim(Key.name.toString()).toString();
                if (!StringUtils.equals(user.getLineName(), lineName)) {
                        user.setLineName(lineName);
                        isUpdated = true;
                }
                String pictureUrl = claimsSet.getClaim(Key.picture.toString()).toString();
                if (!StringUtils.equals(user.getPictureUrl(), pictureUrl)) {
                        user.setPictureUrl(pictureUrl);
                        isUpdated = true;
                }
                return isUpdated;
        }

        @Override
        public BaseResponse signUp(SignUpRequest request) {
                User user = new User();
                Set<Role> roles = new HashSet<>();
                Role role;
                String lineId = null;
                String name = null;
                String imageUrl = null;
                if (StringUtils.isNotBlank(request.getLineToken())) {
                        JWTClaimsSet claimsSet = jwtUtils.decodeES256Jwt(request.getLineToken());
                        lineId = claimsSet.getSubject();
                        name = claimsSet.getClaim(Key.name.toString()).toString();
                        imageUrl = claimsSet.getClaim(Key.picture.toString()).toString();
                }
                List<BaseDetailsResponse> details = validateSignUpRequest(request, lineId);
                if (!details.isEmpty())
                        return ResponseUtil.buildListBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), details);
                BeanUtils.copyProperties(request, user);
                if (StringUtils.isBlank(request.getLineToken())) {
                        role = roleRepository.findByName(ERole.ROLE_ADMIN);
                } else {
                        role = roleRepository.findByName(ERole.ROLE_USER);
                        user.setLineId(lineId);
                        user.setLineSubjectId(Encryption.encodedData(request.getPassword()));
                        user.setLineName(name);
                        user.setPictureUrl(imageUrl);
                        user.setStatusFlag(StatusFlag.INACTIVE.code());
                        if (!new LineUtil().changeRichmenu(user.getLineId(),
                                        richMenu, token))
                                return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(),
                                                "เกิดข้อผิดพลาด ❌",
                                                "เปลี่ยน Rich menu ไม่ได้.");
                        if (!new LineUtil().sentMessage(user.getLineId(),
                                        token, ("ระบบได้บันทึกข้อมูลของ " + request.getFirstName()
                                                        + " เรียบร้อยแล้ว✅ ท่านสามารถเลือกเมนู “ดูประวัติ” เพื่อดูประวัติการส่งผลวัดความดันโลหิต หรือ เลือกเมนู “ส่งผลวัด” เพื่อส่งผลวัดความดันโลหิตได้เลยครับ")))
                                return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(),
                                                "เกิดข้อผิดพลาด ❌",
                                                "ส่งข้อความไม่สำเร็จ.");
                }
                roles.add(role);
                user.setRoles(roles);
                user.setUsername(request.getEmail());
                user.setPassword(encoder.encode(request.getPassword()));
                stmUserRepository.save(user);
                user.setCreateBy(User.builder().id(user.getId()).build());
                user.setUpdateBy(User.builder().id(user.getId()).build());
                stmUserRepository.save(user);
                return ResponseUtil.buildBaseResponse(ApiReturn.SUCCESS.code(),
                                ApiReturn.SUCCESS.description(), "Success ✅",
                                "สมัครสมาชิกสำเร็จ");
        }

        private List<BaseDetailsResponse> validateSignUpRequest(SignUpRequest request, String lineId) {
                List<BaseDetailsResponse> details = new ArrayList<>();
                if (stmUserRepository.existsByEmail(request.getEmail()))
                        details.add(new BaseDetailsResponse("email", "อีเมลนี้ถูกใช้งานแล้ว"));
                if (stmUserRepository.existsByPhoneNumber(request.getPhoneNumber()))
                        details.add(new BaseDetailsResponse("phone", "เบอร์นี้ถูกใช้งานแล้ว"));
                if (stmUserRepository.existsByHospitalNumber(request.getHospitalNumber()))
                        details.add(new BaseDetailsResponse("hn", "หมายเลขผู้ป่วยนี้ถูกใช้งานแล้ว"));
                if (StringUtils.isNotBlank(lineId) && stmUserRepository.existsByLineId(lineId))
                        details.add(new BaseDetailsResponse("line", "Line นี้ถูกใช้งานแล้ว"));
                return details;
        }

        @Override
        @Transactional(readOnly = true)
        public GetUserResponse getUserById(GetUserByIdRequest request) {
                GetUserResponse response = stmUserRepository.findGetUserByIdRespByUser_id(request.getUserId())
                                .orElse(new GetUserResponse());
                if (response.getId() != null)
                        return response;
                response.setStatus(ResponseUtil.buildBaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(), "Not Found ❌", "ไม่พบข้อมูลผู้ใช้"));
                return response;
        }

        @Override
        @Transactional(readOnly = true)
        public GetUserResponse getUserByToken(String email) {
                GetUserResponse response = stmUserRepository.findGetUserByTokenRespByEmail(email)
                                .orElse(new GetUserResponse());
                if (response.getId() != null)
                        return response;
                response.setStatus(ResponseUtil.buildBaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(), "Not Found ❌", "ไม่พบข้อมูลผู้ใช้"));
                return response;
        }

        // ! ==============================
        @Override
        @Transactional(readOnly = true)
        public BaseResponse getUserPaging(GetUserPagingRequest request) {
                Page<GetUserDetailPagingResponse> userPage = findByAggregation(request);
                return new PageableDto<>(userPage);
        }

        private Page<GetUserDetailPagingResponse> findByAggregation(GetUserPagingRequest request) {
                Sort sort = MongoUtil.getSortFromRequest(request);
                Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                                sort.isEmpty() ? Sort.by(Sort.Order.asc("hospitalNumber")) : sort);
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
                if (StringUtils.isNotBlank(request.getStatusFlag())) {
                        criteria.andOperator(
                                        Criteria.where("statusFlag").ne(StatusFlag.DELETE.code()),
                                        Criteria.where("statusFlag").is(request.getStatusFlag().toUpperCase()));
                } else {
                        criteria.and("statusFlag").ne(StatusFlag.DELETE.code());
                }
                criteria.and("lineId").ne(null); // find only paint
                addCriteriaIfNotEmpty(criteria, "firstName", request.getFirstName());
                addCriteriaIfNotEmpty(criteria, "lastName", request.getLastName());
                addCriteriaIfNotEmpty(criteria, "email", request.getEmail());
                addCriteriaIfNotEmpty(criteria, "phoneNumber", request.getPhoneNumber());
                addCriteriaIfNotEmpty(criteria, "hospitalNumber", request.getHospitalNumber());
                addCriteriaIfNotEmpty(criteria, "level", request.getLevel());
                return criteria;
        }

        private void addCriteriaIfNotEmpty(Criteria criteria, String field, String value) {
                if (StringUtils.isNotEmpty(value))
                        criteria.and(field).regex(".*" + value + ".*");
        }
        // ! ==============================

        @Override
        @Transactional(readOnly = true)
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

        @Override
        public List<GetUserListByStatusFlagResponse> getUserListByStatusFlag() {
                TypedAggregation<User> aggregation = Aggregation.newAggregation(User.class,
                                Aggregation.match(Criteria.where("lineId").exists(true)),
                                Aggregation.match(Criteria.where("statusFlag").is(StatusFlag.INACTIVE.toString())),
                                Aggregation.sort(Sort.Direction.ASC, "hn"));

                return mongoTemplate.aggregate(aggregation, "user",
                                GetUserListByStatusFlagResponse.class).getMappedResults();
        }

        @Override
        public BaseResponse updateUserById(String id, UpdateUserByIdRequest request) {
                User user = stmUserRepository.findById(request.getUserId()).orElse(null);
                if (user != null) {
                        user.setFirstName(request.getFirstName());
                        user.setLastName(request.getLastName());
                        user.setEmail(request.getEmail());
                        user.setUsername(request.getEmail());
                        user.setPhoneNumber(request.getPhone());
                        user.setHospitalNumber(request.getHn());
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

        @Override
        public BaseResponse updateUserByToken(String id, UpdateUserByTokenRequest request) {
                User user = stmUserRepository.findById(id).orElse(null);
                if (user != null) {
                        user.setFirstName(request.getFirstName());
                        user.setLastName(request.getLastName());
                        user.setEmail(request.getEmail());
                        user.setUsername(request.getEmail());
                        user.setPhoneNumber(request.getPhone());
                        user.setHospitalNumber(request.getHn());
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

        @Override
        public BaseResponse updateUserCheckState(UpdateUserCheckStateRequest request) {
                User user = stmUserRepository.findById(request.getPatientId()).orElse(null);
                if (user != null) {
                        user.setVerified(request.isCheckStatus());
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

        @Override
        public BaseResponse setInactive() {
                Update update = new Update();
                update.set("statusFlag", StatusFlag.INACTIVE.code());
                update.set("level", Level.NORMAL);
                update.set("checkState", false);
                mongoTemplate.updateMulti(null, update, User.class, "user");
                return new BaseResponse(
                                new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                                ApiReturn.SUCCESS.description(),
                                                Collections.singletonList(
                                                                new BaseDetailsResponse("Success ✅",
                                                                                "เปลี่ยนสถานะเป็น Inactive สำเร็จ"))));
        }

        @Override
        public BaseResponse changePassword(String token, PasswordChangeRequest request) {

                User user = stmUserRepository.findById(jwtUtils.getIdFromHeader(token)).orElse(null);

                if (user != null) {
                        if (encoder.matches(request.getOldPassword(), user.getPassword())) {
                                user.setPassword(encoder.encode(request.getNewPassword()));
                                user.setUpdateDate(LocalDateTime.now());
                                stmUserRepository.save(user);
                                return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                                ApiReturn.SUCCESS.description(),
                                                Collections.singletonList(
                                                                new BaseDetailsResponse("Success ✅",
                                                                                "เปลี่ยนรหัสผ่านสำเร็จ"))));
                        } else {
                                return new BaseResponse(
                                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                                ApiReturn.BAD_REQUEST.description(),
                                                                Collections.singletonList(new BaseDetailsResponse(
                                                                                "Bad Request ❌",
                                                                                "รหัสผ่านไม่ตรงกับรหัสเก่า"))));
                        }
                } else {
                        return new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                        ApiReturn.BAD_REQUEST.description(),
                                                        Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                        "ไม่พบข้อมูลผู้ใช้"))));
                }
        }

        @Override
        public BaseResponse forgotPassword(PasswordForgotRequest request) {

                User user = stmUserRepository.findByEmailAndPhoneNumber(request.getEmail(), request.getPhone())
                                .orElse(null);

                if (user != null) {
                        user.setPassword(encoder.encode(request.getNewPassword()));
                        user.setUpdateDate(LocalDateTime.now());
                        stmUserRepository.save(user);
                        return new BaseResponse(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                        ApiReturn.SUCCESS.description(),
                                        Collections.singletonList(
                                                        new BaseDetailsResponse("Success ✅",
                                                                        "เปลี่ยนรหัสผ่านสำเร็จ"))));
                } else {
                        return new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                        ApiReturn.BAD_REQUEST.description(),
                                                        Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                        "ไม่พบข้อมูลผู้ใช้"))));
                }
        }

}
