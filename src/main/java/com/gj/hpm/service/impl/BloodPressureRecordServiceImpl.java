package com.gj.hpm.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.dto.response.GetBloodPressureDetailPagingResponse;
import com.gj.hpm.dto.response.GetBloodPressureOnPhotoResponse;
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
        public BaseResponse clearData() {
                LocalDateTime fiveMonthsAgo = LocalDateTime.now().minusMonths(5);
                Date dateLimit = Date.from(fiveMonthsAgo.atZone(ZoneId.systemDefault()).toInstant());

                Query query = new Query();
                query.addCriteria(Criteria.where("createDate").lt(dateLimit));

                DeleteResult result = mongoTemplate.remove(query, BloodPressureRecord.class, "bloodPressureRecord");

                if (result.getDeletedCount() > 0) {
                        return ResponseUtil.buildSuccessBaseResponse("Success ✅", "ลบข้อมูลเก่ากว่า 5 เดือนสำเร็จ");
                } else {
                        return ResponseUtil.buildErrorBaseResponse("Not Found ❌",
                                        "ไม่พบข้อมูลความดันโลหิตที่ต้องการลบ");
                }
        }

        @Override
        public BaseResponse createBloodPressure(JwtClaimsDTO dto, CreateBloodPressureRequest request) {
                if (!stmUserRepository.existsById(dto.getJwtId())) {
                        return ResponseUtil.buildBaseResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(), "Not Found ❌",
                                        "ไม่พบข้อมูลผู้ใช้งาน");
                }

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
                        msg = "ระดับ ความดันโลหิตสูงกว่าปกติ แต่ยังไม่เป็นโรคความดันโลหิตสูง คำแนะนำ ควบคุมอาหาร, การจำกัดเกลือในอาหาร, ออกกำลังกาย, วัดความดันอยู่เสมอ, การลดการดื่มแอลกอฮอล์";
                } else if (request.getSystolicPressure() >= 120 && request.getSystolicPressure() <= 129
                                && request.getDiastolicPressure() < 85) {
                        update.set("level", Level.NORMAL);
                        update.set("verified", true);
                        msg = "ระดับ ปกติ คำแนะนำ ควบคุมอาหาร, ออกกำลังกาย, วัดความดันอยู่เสมอ";
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
        public GetBloodPressureOnPhotoResponse uploadImage(JwtClaimsDTO dto, String base64Image) {
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

                GetBloodPressureOnPhotoResponse bloodPressureRequest = new GetBloodPressureOnPhotoResponse();
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
                                bloodPressureRequest.setSys(systolic);
                                bloodPressureRequest.setDia(diastolic);
                                bloodPressureRequest.setPul(pulse);
                                BaseStatusResponse status = new BaseStatusResponse();
                                status.setCode(ApiReturn.SUCCESS.code());
                                status.setDescription(ApiReturn.SUCCESS.description());
                                bloodPressureRequest.setStatus(status);
                                return bloodPressureRequest;

                        }
                } catch (Exception e) {
                        bloodPressureRequest.setSys(0);
                        bloodPressureRequest.setDia(0);
                        bloodPressureRequest.setPul(0);
                        BaseStatusResponse status = new BaseStatusResponse();
                        status.setCode(ApiReturn.BAD_REQUEST.code());
                        status.setDescription(ApiReturn.BAD_REQUEST.description());
                        bloodPressureRequest.setStatus(status);
                        return bloodPressureRequest;
                }
                return bloodPressureRequest;
        }

        @Override
        public byte[] generateExcelFile() throws IOException {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                Query query = new Query();
                LocalDateTime fourMonthsAgo = LocalDateTime.now().minusMonths(4);
                Date dateLimit = Date.from(fourMonthsAgo.atZone(ZoneId.systemDefault()).toInstant());
                query.addCriteria(Criteria.where("createDate").lt(dateLimit));
                List<GetBloodPressureResponse> result = mongoTemplate.find(query, GetBloodPressureResponse.class,
                                "bloodPressureRecord");

                if (result.isEmpty()) {
                        return null;
                }

                Map<String, List<GetBloodPressureResponse>> userRecordsMap = result.stream()
                                .collect(Collectors.groupingBy(record -> record.getCreateBy() != null
                                                ? record.getCreateBy().getFirstName() + " "
                                                                + record.getCreateBy().getLastName() + " ( "
                                                                + record.getCreateBy().getHospitalNumber() + " ) "
                                                : "Unknown"));

                Workbook workbook = new XSSFWorkbook();
                XSSFSheet summarySheet = (XSSFSheet) workbook.createSheet("Main");

                Row summaryHeader = summarySheet.createRow(0);
                summaryHeader.createCell(0).setCellValue("Patient Name ( Hospital Number )");

                int summaryRowNum = 1;

                for (Map.Entry<String, List<GetBloodPressureResponse>> entry : userRecordsMap.entrySet()) {
                        String userName = entry.getKey();
                        List<GetBloodPressureResponse> userRecords = entry.getValue();

                        userRecords.sort(Comparator.comparing(GetBloodPressureResponse::getCreateDate));

                        XSSFSheet userSheet = (XSSFSheet) workbook.createSheet(userName);

                        Row backRow = userSheet.createRow(0);
                        Cell backCell = backRow.createCell(0);
                        backCell.setCellValue("Back to Summary");

                        CreationHelper createHelper = workbook.getCreationHelper();
                        Hyperlink backToSummaryLink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
                        backToSummaryLink.setAddress("'Main'!A1");
                        backCell.setHyperlink(backToSummaryLink);
                        backCell.setCellStyle(createHyperlinkStyle(workbook));

                        List<String> headers = Arrays.asList("Date", "Systolic Pressure", "Diastolic Pressure",
                                        "Pulse Rate");
                        Row userHeaderRow = userSheet.createRow(1);
                        for (int i = 0; i < headers.size(); i++) {
                                Cell cell = userHeaderRow.createCell(i);
                                cell.setCellValue(headers.get(i));
                        }

                        int rowNum = 2;
                        for (GetBloodPressureResponse record : userRecords) {
                                Row row = userSheet.createRow(rowNum++);
                                String formattedDate = record.getCreateDate() != null
                                                ? record.getCreateDate().format(formatter)
                                                : "";
                                row.createCell(0).setCellValue(formattedDate);
                                row.createCell(1).setCellValue(Double.parseDouble(record.getSystolicPressure()));
                                row.createCell(2).setCellValue(Double.parseDouble(record.getDiastolicPressure()));
                                row.createCell(3).setCellValue(Double.parseDouble(record.getPulseRate()));
                        }

                        Row summaryRow = summarySheet.createRow(summaryRowNum++);
                        Cell userNameCell = summaryRow.createCell(0);
                        userNameCell.setCellValue(userName);

                        Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
                        hyperlink.setAddress("'" + userName + "'!A1");
                        userNameCell.setHyperlink(hyperlink);
                        userNameCell.setCellStyle(createHyperlinkStyle(workbook));

                        createChartForUserSheet(userSheet, userRecords.size(), userName);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                workbook.close();

                return outputStream.toByteArray();
        }

        private void createChartForUserSheet(XSSFSheet sheet, int recordSize, String userName) {
                XSSFDrawing drawing = sheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 2, 15, 20);
                XSSFChart chart = drawing.createChart(anchor);
                chart.setTitleText("Blood Pressure Overview : " + userName);
                chart.setTitleOverlay(false);

                XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxis.setTitle("Date");
                XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
                leftAxis.setTitle("Blood Pressure");

                XDDFDataSource<String> datesData = XDDFDataSourcesFactory.fromStringCellRange(sheet,
                                new CellRangeAddress(2, recordSize + 1, 0, 0));
                XDDFNumericalDataSource<Double> systolicData = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                                new CellRangeAddress(2, recordSize + 1, 1, 1));
                XDDFNumericalDataSource<Double> diastolicData = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                                new CellRangeAddress(2, recordSize + 1, 2, 2));
                XDDFNumericalDataSource<Double> pulseRateData = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
                                new CellRangeAddress(2, recordSize + 1, 3, 3));

                XDDFChartData dataChart = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
                XDDFChartData.Series systolicSeries = dataChart.addSeries(datesData, systolicData);
                systolicSeries.setTitle("Systolic Pressure", null);
                XDDFChartData.Series diastolicSeries = dataChart.addSeries(datesData, diastolicData);
                diastolicSeries.setTitle("Diastolic Pressure", null);
                XDDFChartData.Series pulseRateSeries = dataChart.addSeries(datesData, pulseRateData);
                pulseRateSeries.setTitle("Pulse Rate", null);

                chart.plot(dataChart);
        }

        private CellStyle createHyperlinkStyle(Workbook workbook) {
                CellStyle hyperlinkStyle = workbook.createCellStyle();
                Font hyperlinkFont = workbook.createFont();
                hyperlinkFont.setUnderline(Font.U_SINGLE);
                hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
                hyperlinkStyle.setFont(hyperlinkFont);
                return hyperlinkStyle;
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
                                criteria.and(field).is(value);
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
