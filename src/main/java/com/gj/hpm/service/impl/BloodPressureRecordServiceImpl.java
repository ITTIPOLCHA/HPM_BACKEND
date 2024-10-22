package com.gj.hpm.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gj.hpm.dto.request.CreateBloodPressureRequest;
import com.gj.hpm.dto.request.DeleteBloodPressureByIdRequest;
import com.gj.hpm.dto.request.DeleteBloodPressureByTokenRequest;
import com.gj.hpm.dto.request.GetBloodPressureByTokenPagingRequest;
import com.gj.hpm.dto.request.GetBloodPressureCreateByRequest;
import com.gj.hpm.dto.request.GetBloodPressurePagingRequest;
import com.gj.hpm.dto.request.GetBloodPressureRequest;
import com.gj.hpm.dto.request.UpdateBloodPressureByIdRequest;
import com.gj.hpm.dto.request.UpdateBloodPressureByTokenRequest;
import com.gj.hpm.dto.response.BaseDetailsResponse;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.dto.response.GetBloodPressureDetailPagingResponse;
import com.gj.hpm.dto.response.GetBloodPressurePagingResponse;
import com.gj.hpm.dto.response.GetBloodPressureResponse;
import com.gj.hpm.dto.response.JwtClaimsDTO;
import com.gj.hpm.entity.BloodPressureRecord;
import com.gj.hpm.entity.User;
import com.gj.hpm.exception.NotFoundException;
import com.gj.hpm.repository.BloodPressureRecordRepository;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.service.BloodPressureRecordService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.Level;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.LineUtil;
import com.gj.hpm.util.MongoUtil;
import com.gj.hpm.util.ResponseUtil;
import com.mongodb.client.result.DeleteResult;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@Transactional
public class BloodPressureRecordServiceImpl implements BloodPressureRecordService {

        @Value("${hpm.app.token.property}")
        private String token;

        @Value("${hpm.app.api.key}")
        private String apiKey;

        @Value("${hpm.app.gpt.url}")
        private String apiUrl;

        @Autowired
        private BloodPressureRecordRepository stpBloodPressureRepository;

        @Autowired
        private StmUserRepository stmUserRepository;

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private RestTemplate restTemplate;

        @Override
        public BaseResponse createBloodPressure(JwtClaimsDTO dto, CreateBloodPressureRequest request) {
                // Can't find it user.
                if (!stmUserRepository.existsById(dto.getJwtId()))
                        return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Not Found ❌",
                                        "ไม่พบข้อมูลผู้ใช้งาน");
                // Created 1 hour ago.
                if (stpBloodPressureRepository
                                .existsByCreateDateAfterAndCreateById(LocalDateTime.now().minusHours(1),
                                                dto.getJwtId()))
                        return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Fail ❌",
                                        "ข้อมูลความดันโลหิตถูกบันทึกไปแล้ว ใน 1 ชั่วโมงนี้");

                BloodPressureRecord bloodPressure = new BloodPressureRecord();
                bloodPressure.setSystolicPressure(request.getSystolicPressure());
                bloodPressure.setDiastolicPressure(request.getDiastolicPressure());
                bloodPressure.setPulseRate(request.getPulseRate());
                bloodPressure.setPatient(User.builder().id(dto.getJwtId()).build());
                bloodPressure.setCreateBy(User.builder().id(dto.getJwtId()).build());
                bloodPressure.setStatusFlag(StatusFlag.ACTIVE.code());
                stpBloodPressureRepository.save(bloodPressure);

                Update update = new Update();
                String msg = new String();
                update.set("statusFlag", StatusFlag.ACTIVE.code());
                if (request.getSystolicPressure() > 175 || request.getDiastolicPressure() > 105) {
                        update.set("level", Level.DANGER);
                        msg = "ระดับ ความดันโลหิตสูงระยะรุนแรง คำแนะนำ รีบพบแพทย์โดยด่วน";
                } else if (request.getSystolicPressure() > 155
                                || request.getDiastolicPressure() > 95) {
                        update.set("level", Level.WARNING2);
                        msg = "ระดับ ความดันโลหิตสูงระยะเริ่มแรก คำแนะนำ พบแพทย์เพื่อวินิจฉัย";
                } else if (request.getSystolicPressure() > 135
                                || request.getDiastolicPressure() > 85) {
                        update.set("level", Level.WARNING1);
                        msg = "ระดับ ความดันโลหิตสูงกว่าปกติ คำแนะนำ ปรึกษาแพทย์";
                } else {
                        update.set("level", Level.NORMAL);
                        update.set("isVerified", true);
                        msg = "ระดับ ปกคิ คำแนะนำ ควบคุมอาหาร, ออกกำลังกาย, วัดความดันอยู่เสมอ";
                }
                mongoTemplate.updateFirst(new Query(Criteria.where("id").is(dto.getJwtId())), update, User.class,
                                "user");

                if (!new LineUtil().sentMessage(dto.getLineId(),
                                token, ("บันทึกผลสำเร็จ ✅\n"
                                                + "ความดันโลหิตของของคุณ " + dto.getName()
                                                + " คือ\n"
                                                + "Sys : " + request.getSystolicPressure() + ",\n"
                                                + "Dia : " + request.getDiastolicPressure() + ",\n"
                                                + "Pul : " + request.getPulseRate() + "\n " + msg)))
                        return ResponseUtil.buildBaseResponse(
                                        ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Error ❌", "ส่งข้อความไม่สำเร็จ.");

                return ResponseUtil.buildBaseResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                                "Success ✅", "บันทึกข้อมูลความดันโลหิตสำเร็จ");
        }

        @Override
        public BaseResponse uploadImage(JwtClaimsDTO dto, String base64Image) {
                if (!stpBloodPressureRepository
                                .existsByCreateDateAfterAndCreateById(LocalDateTime.now().minusHours(1),
                                                dto.getJwtId())) {
                        // Set up headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.setBearerAuth(apiKey);

                        // Create the payload
                        Map<String, Object> payload = new HashMap<>();
                        payload.put("model", "gpt-4o");

                        Map<String, Object> userMessage = new HashMap<>();
                        userMessage.put("role", "user");

                        Map<String, Object> content = new HashMap<>();
                        content.put("type", "text");
                        content.put("text", "systolic, diastolic, pulse require json from only.");

                        Map<String, Object> imageContent = new HashMap<>();
                        imageContent.put("type", "image_url");
                        imageContent.put("image_url",
                                        Map.of("detail", "low", "url", base64Image));

                        userMessage.put("content", new Object[] { content, imageContent });
                        payload.put("messages", new Object[] { userMessage });

                        // Prepare the request entity
                        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

                        try {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> response = restTemplate
                                                .exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class).getBody();

                                // Extract and return the response
                                if (response != null && response.containsKey("choices")) {
                                        @SuppressWarnings("unchecked")
                                        String result = (String) ((Map<String, Object>) ((List<Map<String, Object>>) response
                                                        .get("choices")).get(0).get("message")).get("content");
                                        String jsonString = result.substring(result.indexOf("{"));
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(jsonString);

                                        int sys = rootNode.get("systolic").asInt();
                                        int dia = rootNode.get("diastolic").asInt();
                                        int pul = rootNode.get("pulse").asInt();
                                        CreateBloodPressureRequest bloodPressureRequest = new CreateBloodPressureRequest();
                                        bloodPressureRequest.setSystolicPressure(sys);
                                        bloodPressureRequest.setDiastolicPressure(dia);
                                        bloodPressureRequest.setPulseRate(pul);
                                        return createBloodPressure(dto, bloodPressureRequest);
                                }
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }
                return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(), "Fail ❌",
                                "ข้อมูลความดันโลหิตถูกบันทึกไปแล้ว ใน 1 ชั่วโมงนี้");
        }

        @Override
        @Transactional(readOnly = true)
        public GetBloodPressureResponse getBloodPressureById(GetBloodPressureRequest request) {
                return stpBloodPressureRepository
                                .findByIdGetBloodPressureResp(request.getBloodPressureId())
                                .orElseThrow(() -> new NotFoundException(
                                                ResponseUtil.buildBaseResponse(
                                                                ApiReturn.BAD_REQUEST.code(),
                                                                ApiReturn.BAD_REQUEST.description(),
                                                                "Not Found ❌",
                                                                "ไม่พบข้อมูลความดันโลหิต")));
        }

        @Override
        @Transactional(readOnly = true)
        public List<GetBloodPressureResponse> getBloodPressureByCreateBy(GetBloodPressureCreateByRequest request) {
                return stpBloodPressureRepository
                                .findByCreateBy_Id(request.getUserId());
        }

        @Override
        @Transactional(readOnly = true)
        public GetBloodPressurePagingResponse getBloodPressurePaging(GetBloodPressurePagingRequest request) {
                Page<GetBloodPressureDetailPagingResponse> bpPage = findByAggregation(request);
                GetBloodPressurePagingResponse response = new GetBloodPressurePagingResponse();
                response.setBps(bpPage.getContent());
                response.setTotalPages(bpPage.getTotalPages());
                response.setTotalItems(bpPage.getTotalElements());
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
                long total = mongoTemplate.count(new Query(criteria), BloodPressureRecord.class, "bloodPressureRecord");
                return new PageImpl<>(results, pageable, total);
        }

        private Criteria setCriteria(GetBloodPressurePagingRequest request) {
                Criteria criteria = new Criteria();
                addCriteriaIfNotEmpty(criteria, "sys", request.getSystolicPressure());
                addCriteriaIfNotEmpty(criteria, "dia", request.getDiastolicPressure());
                addCriteriaIfNotEmpty(criteria, "pul", request.getPulseRate());
                addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());
                if (StringUtils.isNotEmpty(request.getCreateBy()))
                        criteria.and("createBy.$id").is(new ObjectId(request.getCreateBy()));
                return criteria;
        }

        @Override
        @Transactional(readOnly = true)
        public GetBloodPressureResponse getBloodPressureByToken(JwtClaimsDTO dto, GetBloodPressureRequest request) {
                return stpBloodPressureRepository
                                .findByIdAndCreateById(request.getBloodPressureId(), dto.getJwtId())
                                .orElseThrow(() -> new NotFoundException(
                                                ResponseUtil.buildBaseResponse(
                                                                ApiReturn.BAD_REQUEST.code(),
                                                                ApiReturn.BAD_REQUEST.description(),
                                                                "Not Found ❌",
                                                                "ไม่พบข้อมูลความดันโลหิต")));
        }

        @Override
        @Transactional(readOnly = true)
        public GetBloodPressurePagingResponse getBloodPressurePagingByUserId(JwtClaimsDTO dto,
                        GetBloodPressureByTokenPagingRequest request) {
                Page<GetBloodPressureDetailPagingResponse> bpPage = findByAggregationFromToken(dto.getJwtId(), request);
                GetBloodPressurePagingResponse response = new GetBloodPressurePagingResponse();
                response.setBps(bpPage.getContent());
                response.setTotalPages(bpPage.getTotalPages());
                response.setTotalItems(bpPage.getTotalElements());
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
                long total = mongoTemplate.count(new Query(criteria), BloodPressureRecord.class, "bloodPressureRecord");
                return new PageImpl<>(results, pageable, total);
        }

        private Criteria setCriteriaFromToken(String id, GetBloodPressureByTokenPagingRequest request) {
                Criteria criteria = new Criteria();
                criteria.and("createBy.$id").is(new ObjectId(id));
                addCriteriaIfNotEmpty(criteria, "sys", request.getSystolicPressure());
                addCriteriaIfNotEmpty(criteria, "dia", request.getDiastolicPressure());
                addCriteriaIfNotEmpty(criteria, "pul", request.getPulseRate());
                addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());
                return criteria;
        }

        private void addCriteriaIfNotEmpty(Criteria criteria, String field, Object value) {
                if (value != null) {
                        if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                                criteria.and(field).regex(".*" + value + ".*", "i");
                        } else if (value instanceof Integer) {
                                criteria.and(field).is(value);
                        }
                }
        }

        @Override
        public BaseResponse updateBloodPressureById(UpdateBloodPressureByIdRequest request) {
                BloodPressureRecord bloodPressure = stpBloodPressureRepository
                                .findById(request.getBloodPressureId()).orElse(null);
                if (bloodPressure == null)
                        return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Not Found ❌", "ไม่พบข้อมูลความดันโลหิต");

                bloodPressure.setSystolicPressure(request.getSystolicPressure());
                bloodPressure.setDiastolicPressure(request.getDiastolicPressure());
                bloodPressure.setPulseRate(request.getPulseRate());
                bloodPressure.setCreateBy(User.builder().id(request.getUserId()).build());
                bloodPressure.setUpdateBy(User.builder().id(request.getActionId()).build());
                stpBloodPressureRepository.save(bloodPressure);
                return ResponseUtil.buildBaseResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                                "Success ✅", "อัพเดทข้อมูลความดันโลหิตสำเร็จ");
        }

        @Override
        public BaseResponse updateBloodPressureByToken(JwtClaimsDTO dto, UpdateBloodPressureByTokenRequest request) {
                BloodPressureRecord bloodPressure = stpBloodPressureRepository
                                .findByIdAndCreateBy_Id(request.getBloodPressureId(), dto.getJwtId()).orElse(null);
                if (bloodPressure != null) {
                        bloodPressure.setSystolicPressure(request.getSystolicPressure());
                        bloodPressure.setDiastolicPressure(request.getDiastolicPressure());
                        bloodPressure.setPulseRate(request.getPulseRate());
                        bloodPressure.setUpdateBy(User.builder().id(dto.getJwtId()).build());
                        stpBloodPressureRepository.save(bloodPressure);
                        return new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                                        ApiReturn.SUCCESS.description(),
                                                        Collections.singletonList(
                                                                        new BaseDetailsResponse("Success ✅",
                                                                                        "อัพเดทข้อมูลความดันโลหิตสำเร็จ"))));
                }
                return new BaseResponse(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลความดันโลหิต"))));
        }

        @Override
        public BaseResponse deleteBloodPressureById(DeleteBloodPressureByIdRequest request) {
                boolean validation = stpBloodPressureRepository.existsById(request.getBloodPressureId());
                if (validation) {
                        stpBloodPressureRepository.deleteById(request.getBloodPressureId());
                        return new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                                        ApiReturn.SUCCESS.description(),
                                                        Collections.singletonList(
                                                                        new BaseDetailsResponse("Success ✅",
                                                                                        "ลบข้อมูลความดันโลหิตสำเร็จ"))));
                }
                return new BaseResponse(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลความดันโลหิต"))));
        }

        @Override
        public BaseResponse deleteBloodPressureByToken(JwtClaimsDTO dto, DeleteBloodPressureByTokenRequest request) {
                if (dto.getJwtId() == null || request.getBloodPressureId() == null) {
                        return ResponseUtil.buildErrorBaseResponse("Invalid Request ❌", "ข้อมูลไม่ครบถ้วน");
                }

                Query query = new Query(Criteria.where("patient.$id").is(dto.getJwtId())
                                .and("id").is(request.getBloodPressureId()));

                DeleteResult result = mongoTemplate.remove(query, BloodPressureRecord.class, "bloodPressureRecord");

                if (result.getDeletedCount() > 0) {
                        log.info("ลบข้อมูลความดันโลหิตสำเร็จสำหรับผู้ใช้: {}", dto.getJwtId());
                        return ResponseUtil.buildSuccessBaseResponse("Success ✅", "ลบข้อมูลความดันโลหิตสำเร็จ");
                } else {
                        log.warn("ไม่พบข้อมูลความดันโลหิตสำหรับการลบ: {}", request.getBloodPressureId());
                        return ResponseUtil.buildErrorBaseResponse("Not Found ❌",
                                        "ไม่พบข้อมูลความดันโลหิตที่ต้องการลบ");
                }
        }

}
