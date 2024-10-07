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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
import com.gj.hpm.entity.BloodPressureRecord;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.repository.StpBloodPressureRepository;
import com.gj.hpm.service.BloodPressureService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.Level;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.LineUtil;
import com.gj.hpm.util.MongoUtil;

@Service
public class BloodPressureServiceImpl implements BloodPressureService {

        @Value("${hpm.app.token.property}")
        private String token;

        @Value("${hpm.app.api.key}")
        private String apiKey;

        @Autowired
        private StpBloodPressureRepository stpBloodPressureRepository;

        @Autowired
        private StmUserRepository stmUserRepository;

        @Autowired
        private MongoTemplate mongoTemplate;

        @Autowired
        private RestTemplate restTemplate;

        @Transactional
        @Override
        public BaseResponse createBloodPressure(String id, CreateBloodPressureRequest request) {
                if (!stmUserRepository.existsById(id))
                        return new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                        ApiReturn.BAD_REQUEST.description(),
                                                        Collections.singletonList(new BaseDetailsResponse("Not Found ❌",
                                                                        "ไม่พบข้อมูลผู้ใช้งาน"))));
                if (!stpBloodPressureRepository
                                .existsByCreateDateAfterAndCreateById(LocalDateTime.now().minusHours(1), id)) {
                        BloodPressureRecord bloodPressure = new BloodPressureRecord();
                        bloodPressure.setSystolicPressure(request.getSys());
                        bloodPressure.setDiastolicPressure(request.getDia());
                        bloodPressure.setPulseRate(request.getPul());
                        bloodPressure.setStatusFlag(StatusFlag.ACTIVE.code());
                        bloodPressure.setCreateBy(User.builder().id(id).build());
                        bloodPressure.setUpdateBy(User.builder().id(id).build());
                        stpBloodPressureRepository.save(bloodPressure);

                        User user = stmUserRepository.findById(id).orElse(null);
                        user.setStatusFlag(StatusFlag.ACTIVE.code());
                        if (Integer.parseInt(request.getSys()) > 179 || Integer.parseInt(request.getDia()) > 109) {
                                user.setLevel(Level.DANGER);
                        } else if (Integer.parseInt(request.getSys()) > 160
                                        || Integer.parseInt(request.getDia()) > 100) {
                                user.setLevel(Level.WARNING2);
                        } else if (Integer.parseInt(request.getSys()) > 139
                                        || Integer.parseInt(request.getDia()) > 89) {
                                user.setLevel(Level.WARNING1);
                        } else {
                                user.setLevel(Level.NORMAL);
                                user.setVerified(true);
                        }
                        stmUserRepository.save(user);

                        if (StringUtils.isNotBlank(user.getLineId())) {
                                if (!new LineUtil().sentMessage(user.getLineId(),
                                                token, ("บันทึกผลสำเร็จ ✅\n"
                                                                + "ความดันโลหิตของของคุณ " + user.getFirstName()
                                                                + " คือ\n"
                                                                + "Sys : " + request.getSys() + ",\n"
                                                                + "Dia : " + request.getDia() + ",\n"
                                                                + "Pul : " + request.getPul())))
                                        return new BaseResponse(
                                                        new BaseStatusResponse(
                                                                        ApiReturn.BAD_REQUEST.code(),
                                                                        ApiReturn.BAD_REQUEST
                                                                                        .description(),
                                                                        Collections
                                                                                        .singletonList(
                                                                                                        new BaseDetailsResponse(
                                                                                                                        "Error ❌",
                                                                                                                        "ส่งข้อความไม่สำเร็จ."))));
                        }

                        return new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                                        ApiReturn.SUCCESS.description(),
                                                        Collections.singletonList(
                                                                        new BaseDetailsResponse("Success ✅",
                                                                                        "บันทึกข้อมูลความดันโลหิตสำเร็จ"))));
                }
                return new BaseResponse(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Fail ❌",
                                                                "ข้อมูลความดันโลหิตถูกบันทึกไปแล้ว ใน 1 ชั่วโมงนี้"))));
        }

        @Transactional
        @Override
        public BaseResponse getBloodPressureFromImage(MultipartFile image) {
                // String fileName = imageController.processImage(image);
                BaseResponse response = new BaseResponse();
                // response.setStatus(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                // ApiReturn.SUCCESS.description(),
                // Collections.singletonList(new BaseDetailsResponse("Success ✅",
                // fileName))));
                return response;
        }

        @Transactional
        @Override
        public GetBloodPressureResponse getBloodPressureById(GetBloodPressureRequest request) {
                GetBloodPressureResponse response = stpBloodPressureRepository
                                .findByIdGetBloodPressureResp(request.getBloodPressureId()).orElse(null);
                if (response != null)
                        return response;
                response = new GetBloodPressureResponse();
                response.setStatus(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลความดันโลหิต"))));
                return response;
        }

        @Transactional
        @Override
        public List<GetBloodPressureResponse> getBloodPressureByCreateBy(GetBloodPressureCreateByRequest request) {
                List<GetBloodPressureResponse> response = stpBloodPressureRepository
                                .findByCreateBy_Id(request.getUserId());
                if (!response.isEmpty())
                        return response;
                GetBloodPressureResponse resp = new GetBloodPressureResponse();
                resp.setStatus(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Not Found ❌", "ไม่พบข้อมูลความดันโลหิต"))));
                response.add(resp);
                return response;
        }

        @Transactional
        @Override
        public GetBloodPressureResponse getBloodPressureByToken(String id, GetBloodPressureRequest request) {
                GetBloodPressureResponse response = stpBloodPressureRepository
                                .findByIdAndCreateById(request.getBloodPressureId(), id).orElse(null);
                if (response != null)
                        return response;
                response = new GetBloodPressureResponse();
                response.setStatus(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                ApiReturn.BAD_REQUEST.description(),
                                Collections.singletonList(
                                                new BaseDetailsResponse("Not Found ❌", "พบข้อมูลความดันโลหิต"))));
                return response;
        }

        @Transactional
        @Override
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
                addCriteriaIfNotEmpty(criteria, "sys", request.getSys());
                addCriteriaIfNotEmpty(criteria, "dia", request.getDia());
                addCriteriaIfNotEmpty(criteria, "pul", request.getPul());
                addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());
                if (StringUtils.isNotEmpty(request.getCreateBy()))
                        criteria.and("createBy.$id").is(new ObjectId(request.getCreateBy()));
                return criteria;
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
                addCriteriaIfNotEmpty(criteria, "sys", request.getSys());
                addCriteriaIfNotEmpty(criteria, "dia", request.getDia());
                addCriteriaIfNotEmpty(criteria, "pul", request.getPul());
                addCriteriaIfNotEmpty(criteria, "statusFlag", request.getStatusFlag());
                return criteria;
        }

        private void addCriteriaIfNotEmpty(Criteria criteria, String field, String value) {
                if (StringUtils.isNotEmpty(value))
                        criteria.and(field).regex(".*" + value + ".*");
        }

        @Transactional
        @Override
        public BaseResponse updateBloodPressureById(UpdateBloodPressureByIdRequest request) {
                BloodPressureRecord bloodPressure = stpBloodPressureRepository
                                .findById(request.getBloodPressureId()).orElse(null);
                if (bloodPressure != null) {
                        bloodPressure.setSystolicPressure(request.getSys());
                        bloodPressure.setDiastolicPressure(request.getDia());
                        bloodPressure.setPulseRate(request.getPul());
                        bloodPressure.setCreateBy(User.builder().id(request.getUserId()).build());
                        bloodPressure.setUpdateBy(User.builder().id(request.getActionId()).build());
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
        public BaseResponse updateBloodPressureByToken(String id, UpdateBloodPressureByTokenRequest request) {
                BloodPressureRecord bloodPressure = stpBloodPressureRepository
                                .findByIdAndCreateBy_Id(request.getBloodPressureId(), id).orElse(null);
                if (bloodPressure != null) {
                        bloodPressure.setSystolicPressure(request.getSys());
                        bloodPressure.setDiastolicPressure(request.getDia());
                        bloodPressure.setPulseRate(request.getPul());
                        bloodPressure.setUpdateBy(User.builder().id(id).build());
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
        public BaseResponse deleteBloodPressureByToken(String id, DeleteBloodPressureByTokenRequest request) {
                boolean verify = stpBloodPressureRepository.existsByIdAndCreateById(request.getBloodPressureId(), id);
                if (verify) {
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
        public BaseResponse uploadImage(String base64Image) {
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("api_key", apiKey);
                requestBody.put("image_data", base64Image);

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
                content.put("text", "sys, dia, pul require.");

                Map<String, Object> imageContent = new HashMap<>();
                imageContent.put("type", "image_url");
                imageContent.put("image_url", Map.of("url", "data:image/png;base64," + base64Image));

                userMessage.put("content", new Object[] { content, imageContent });
                payload.put("messages", new Object[] { userMessage });

                // Prepare the request entity
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

                // Make the API call
                String apiUrl = "https://api.openai.com/v1/chat/completions";
                try {
                        Map<String, Object> response = restTemplate
                                        .exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class)
                                        .getBody();

                        // Extract and return the response
                        if (response != null && response.containsKey("choices")) {
                                Map<String, Object> choice = (Map<String, Object>) ((List<Map<String, Object>>) response
                                                .get("choices"))
                                                .get(0).get("message");
                                // Map<String, Object> message = (Map<String, Object>) choice.get("message");
                                return null;
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }

                return null;
        }
}
