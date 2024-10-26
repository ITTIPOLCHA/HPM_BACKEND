package com.gj.hpm.service.impl;

import java.time.LocalDateTime;
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
import com.gj.hpm.dto.PageableDto;
import com.gj.hpm.dto.request.CreateBloodPressureRequest;
import com.gj.hpm.dto.request.DeleteBloodPressureByIdRequest;
import com.gj.hpm.dto.request.DeleteBloodPressureByTokenRequest;
import com.gj.hpm.dto.request.GetBloodPressureByTokenPagingRequest;
import com.gj.hpm.dto.request.GetBloodPressureCreateByRequest;
import com.gj.hpm.dto.request.GetBloodPressurePagingRequest;
import com.gj.hpm.dto.request.GetBloodPressureRequest;
import com.gj.hpm.dto.request.UpdateBloodPressureByIdRequest;
import com.gj.hpm.dto.request.UpdateBloodPressureByTokenRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetBloodPressureDetailPagingResponse;
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
import com.mongodb.client.result.UpdateResult;

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
                if (!stmUserRepository.existsById(dto.getJwtId())) {
                        return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Not Found ❌",
                                        "ไม่พบข้อมูลผู้ใช้งาน");
                }

                // Created 1 hour ago.
                if (stpBloodPressureRepository
                                .existsByCreateDateAfterAndCreateById(LocalDateTime.now().minusHours(1),
                                                dto.getJwtId())) {
                        return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Fail ❌",
                                        "ข้อมูลความดันโลหิตถูกบันทึกไปแล้ว ใน 1 ชั่วโมงนี้");
                }

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
                if (request.getSystolicPressure() >= 140 && request.getDiastolicPressure() < 90) {
                        update.set("level", Level.ISOLATED);
                        msg = "ระดับ ความดันโลหิตสูงเฉพาะซิสโตลิก คือ ตัวบนสูงกว่าเท่ากับ 140 และตัวล่างน้อยกว่า 90";
                } else if (request.getSystolicPressure() >= 180 || request.getDiastolicPressure() >= 110) {
                        update.set("level", Level.GRADE3);
                        msg = "ระดับ ความดันโลหิตสูงระยะรุนแรง คำแนะนำ รีบพบแพทย์โดยด่วน";
                } else if (request.getSystolicPressure() >= 160 || request.getDiastolicPressure() >= 100) {
                        update.set("level", Level.GRADE2);
                        msg = "ระดับ ความดันโลหิตสูงระยะปานกลาง คำแนะนำ โทรปรึกษาแพทย์เพื่อวินิจฉัย";
                } else if (request.getSystolicPressure() >= 140 || request.getDiastolicPressure() >= 90) {
                        update.set("level", Level.GRADE1);
                        msg = "ระดับ ความดันโลหิตสูงระยะเริ่มแรก คำแนะนำ พบแพทย์เพื่อวินิจฉัย";
                } else if (request.getSystolicPressure() >= 130 || request.getDiastolicPressure() >= 85) {
                        update.set("level", Level.HIGH);
                        update.set("verified", true);
                        msg = "ระดับ ความดันโลหิตสูงกว่าปกติ แต่ยังไม่เป็นโรคความดันโหลิตสูง คำแนะนำ ควบคุมอาหาร, การจำกัดเกลือในอาหาร, ออกกำลังกาย, วัดความดันอยู่เสมอ, การลดการดื่มแอลกอฮอล์";
                } else if (request.getSystolicPressure() >= 120 && request.getSystolicPressure() <= 129
                                && request.getDiastolicPressure() < 85) {
                        update.set("level", Level.NORMAL);
                        update.set("verified", true);
                        msg = "ระดับ ปกคิ คำแนะนำ ควบคุมอาหาร, ออกกำลังกาย, วัดความดันอยู่เสมอ";
                } else {
                        update.set("level", Level.OPTIMAL);
                        update.set("verified", true);
                        msg = "ระดับ เหมาะสม สิ่งที่ทำอยู่ดีอยู่แล้วพยายามต่อไปครับ";
                }
                mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(new ObjectId(dto.getJwtId()))), update,
                                User.class,
                                "user");

                if (!new LineUtil().sentMessage(dto.getLineId(),
                                token, ("บันทึกผลสำเร็จ ✅\n"
                                                + "ความดันโลหิตของของคุณ " + dto.getName()
                                                + " คือ\n"
                                                + "Sys : " + request.getSystolicPressure() + ",\n"
                                                + "Dia : " + request.getDiastolicPressure() + ",\n"
                                                + "Pul : " + request.getPulseRate() + "\n " + msg))) {
                        return ResponseUtil.buildBaseResponse(
                                        ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Error ❌", "ส่งข้อความไม่สำเร็จ.");
                }

                return ResponseUtil.buildBaseResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                                "Success ✅", "บันทึกข้อมูลความดันโลหิตสำเร็จ");
        }

        @Override
        public BaseResponse uploadImage(JwtClaimsDTO dto, String base64Image) {
                if (!stpBloodPressureRepository
                                .existsByCreateDateAfterAndCreateById(LocalDateTime.now().minusHours(1),
                                                dto.getJwtId())) {
                        // Set up
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.setBearerAuth(apiKey);
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
                                        int systolic = rootNode.get("systolic").asInt();
                                        int diastolic = rootNode.get("diastolic").asInt();
                                        int pulse = rootNode.get("pulse").asInt();
                                        CreateBloodPressureRequest bloodPressureRequest = new CreateBloodPressureRequest();
                                        bloodPressureRequest.setSystolicPressure(systolic);
                                        bloodPressureRequest.setDiastolicPressure(diastolic);
                                        bloodPressureRequest.setPulseRate(pulse);
                                        return createBloodPressure(dto, bloodPressureRequest);
                                }
                        } catch (Exception e) {
                                return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                                ApiReturn.BAD_REQUEST.description(), "Fail ❌",
                                                e.getMessage());
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
                                .findByPatient_Id(request.getUserId());
        }

        @Override
        @Transactional(readOnly = true)
        public BaseResponse getBloodPressurePaging(GetBloodPressurePagingRequest request) {
                Page<GetBloodPressureDetailPagingResponse> content = findByAggregation(request);
                return new PageableDto<>(content);
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
                                aggregation, "bloodPressureRecord", GetBloodPressureDetailPagingResponse.class);
                List<GetBloodPressureDetailPagingResponse> results = aggregationResults.getMappedResults();
                long total = mongoTemplate.count(new Query(criteria), BloodPressureRecord.class, "bloodPressureRecord");
                return new PageImpl<>(results, pageable, total);
        }

        private Criteria setCriteria(GetBloodPressurePagingRequest request) {
                Criteria criteria = new Criteria();
                addCriteriaIfNotEmpty(criteria, "systolicPressure", request.getSystolicPressure());
                addCriteriaIfNotEmpty(criteria, "diastolicPressure", request.getDiastolicPressure());
                addCriteriaIfNotEmpty(criteria, "pulseRate", request.getPulseRate());
                addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());
                if (StringUtils.isNotEmpty(request.getPatient()))
                        criteria.and("patient.$id").is(new ObjectId(request.getPatient()));
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
        public BaseResponse getBloodPressurePagingByUserId(JwtClaimsDTO dto,
                        GetBloodPressureByTokenPagingRequest request) {
                Page<GetBloodPressureDetailPagingResponse> content = findByAggregationFromToken(dto.getJwtId(),
                                request);
                return new PageableDto<>(content);
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
                                aggregation, "bloodPressureRecord", GetBloodPressureDetailPagingResponse.class);
                List<GetBloodPressureDetailPagingResponse> results = aggregationResults.getMappedResults();
                long total = mongoTemplate.count(new Query(criteria), BloodPressureRecord.class, "bloodPressureRecord");
                return new PageImpl<>(results, pageable, total);
        }

        private Criteria setCriteriaFromToken(String id, GetBloodPressureByTokenPagingRequest request) {
                Criteria criteria = new Criteria();
                criteria.and("patient.$id").is(new ObjectId(id));
                addCriteriaIfNotEmpty(criteria, "systolicPressure", request.getSystolicPressure());
                addCriteriaIfNotEmpty(criteria, "diastolicPressure", request.getDiastolicPressure());
                addCriteriaIfNotEmpty(criteria, "pulseRate", request.getPulseRate());
                addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());
                return criteria;
        }

        @SuppressWarnings("null")
        private void addCriteriaIfNotEmpty(Criteria criteria, String field, Object value) {
                if (value != null && !value.equals(0)) {
                        if (value instanceof String && StringUtils.isNotBlank((String) value)) {
                                criteria.and(field).regex(".*" + value + ".*", "i");
                        } else if (value instanceof Integer) {
                                criteria.and(field).regex("^" + value + ".*").orOperator(Criteria.where(field).is(value));
                                // criteria.and(field).is(value);
                        }
                }
        }

        @Override
        public BaseResponse updateBloodPressureById(UpdateBloodPressureByIdRequest request) {
                Update update = new Update();
                update.set("systolicPressure", request.getSystolicPressure());
                update.set("diastolicPressure", request.getDiastolicPressure());
                update.set("pulseRate", request.getPulseRate());
                update.set("updateBy", User.builder().id(request.getActionId()).build());
                update.set("updateDate", LocalDateTime.now());
                UpdateResult result = mongoTemplate.updateFirst(
                                new Query(Criteria.where("_id").is(new ObjectId(request.getBloodPressureId()))
                                                .and("patient.$id").is(new ObjectId(request.getUserId()))),
                                update, BloodPressureRecord.class, "bloodPressureRecord");
                if (result.getMatchedCount() > 0) {
                        return ResponseUtil.buildSuccessBaseResponse("Success ✅", "อัพเดทข้อมูลความดันโลหิตสำเร็จ");
                } else {
                        return ResponseUtil.buildErrorBaseResponse("Not Found ❌",
                                        "ไม่พบข้อมูลความดันโลหิต");
                }
        }

        @Override
        public BaseResponse updateBloodPressureByToken(JwtClaimsDTO dto, UpdateBloodPressureByTokenRequest request) {
                Update update = new Update();
                update.set("systolicPressure", request.getSystolicPressure());
                update.set("diastolicPressure", request.getDiastolicPressure());
                update.set("pulseRate", request.getPulseRate());
                update.set("updateBy", User.builder().id(dto.getJwtId()).build());
                update.set("updateDate", LocalDateTime.now());
                UpdateResult result = mongoTemplate.updateFirst(
                                new Query(Criteria.where("_id").is(new ObjectId(request.getBloodPressureId()))
                                                .and("patient.$id").is(new ObjectId(dto.getJwtId()))),
                                update, BloodPressureRecord.class, "bloodPressureRecord");
                if (result.getMatchedCount() > 0) {
                        return ResponseUtil.buildSuccessBaseResponse("Success ✅", "อัพเดทข้อมูลความดันโลหิตสำเร็จ");
                } else {
                        return ResponseUtil.buildErrorBaseResponse("Not Found ❌",
                                        "ไม่พบข้อมูลความดันโลหิต");
                }
        }

        @Override
        public BaseResponse deleteBloodPressureById(DeleteBloodPressureByIdRequest request) {
                if (request.getBloodPressureId() == null)
                        return ResponseUtil.buildErrorBaseResponse("Invalid Request ❌", "ข้อมูลไม่ครบถ้วน");

                Query query = new Query(Criteria.where("_id").is(new ObjectId(request.getBloodPressureId())));
                DeleteResult result = mongoTemplate.remove(query, BloodPressureRecord.class, "bloodPressureRecord");

                if (result.getDeletedCount() > 0) {
                        return ResponseUtil.buildSuccessBaseResponse("Success ✅", "ลบข้อมูลความดันโลหิตสำเร็จ");
                } else {
                        return ResponseUtil.buildErrorBaseResponse("Not Found ❌",
                                        "ไม่พบข้อมูลความดันโลหิตที่ต้องการลบ");
                }
        }

        @Override
        public BaseResponse deleteBloodPressureByToken(JwtClaimsDTO dto, DeleteBloodPressureByTokenRequest request) {
                if (dto.getJwtId() == null || request.getBloodPressureId() == null)
                        return ResponseUtil.buildErrorBaseResponse("Invalid Request ❌", "ข้อมูลไม่ครบถ้วน");

                Query query = new Query(Criteria.where("patient.$id").is(new ObjectId(dto.getJwtId()))
                                .and("_id").is(new ObjectId(request.getBloodPressureId())));
                DeleteResult result = mongoTemplate.remove(query, BloodPressureRecord.class, "bloodPressureRecord");

                if (result.getDeletedCount() > 0) {
                        return ResponseUtil.buildSuccessBaseResponse("Success ✅", "ลบข้อมูลความดันโลหิตสำเร็จ");
                } else {
                        return ResponseUtil.buildErrorBaseResponse("Not Found ❌",
                                        "ไม่พบข้อมูลความดันโลหิตที่ต้องการลบ");
                }
        }

}
