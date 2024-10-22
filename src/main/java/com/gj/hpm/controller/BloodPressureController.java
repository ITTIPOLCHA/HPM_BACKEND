package com.gj.hpm.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.config.security.jwt.JwtUtils;
import com.gj.hpm.dto.request.BaseRequest;
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
import com.gj.hpm.dto.response.GetBloodPressurePagingResponse;
import com.gj.hpm.dto.response.GetBloodPressureResponse;
import com.gj.hpm.dto.response.JwtClaimsDTO;
import com.gj.hpm.exception.NotFoundException;
import com.gj.hpm.service.BloodPressureRecordService;
import com.gj.hpm.util.ResponseUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/bp")
public class BloodPressureController {

    @Autowired
    private BloodPressureRecordService bloodPressureService;

    @Autowired
    private JwtUtils jwtUtils;

    // ! C
    @PostMapping("/createBloodPressure")
    public ResponseEntity<?> createBloodPressure(@RequestHeader("Authorization") String token,
            @RequestBody CreateBloodPressureRequest request) {
        try {
            BaseResponse response = bloodPressureService.createBloodPressure(jwtUtils.decodeJwtClaimsDTO(token),
                    request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestHeader("Authorization") String token, @RequestBody BaseRequest request)
            throws IOException {
        try {
            BaseResponse response = bloodPressureService.uploadImage(jwtUtils.decodeJwtClaimsDTO(token),
                    request.getRequestId());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ! R
    // ? Get By Id
    @PostMapping("/a/getBloodPressureById")
    public ResponseEntity<?> getBloodPressureById(@RequestHeader("Authorization") String token,
            @RequestBody GetBloodPressureRequest request) {
        try {
            GetBloodPressureResponse response = bloodPressureService.getBloodPressureById(request);
            return ResponseEntity.ok().body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(e.getResponse());
        }
    }

    @PostMapping("/a/getBloodPressureByCreateBy")
    public ResponseEntity<?> getBloodPressureByCreateBy(@RequestHeader("Authorization") String token,
            @RequestBody GetBloodPressureCreateByRequest request) {
        try {
            List<GetBloodPressureResponse> response = bloodPressureService.getBloodPressureByCreateBy(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ? Get By Page
    @PostMapping("/a/getBloodPressurePaging")
    public ResponseEntity<?> getBloodPressurePaging(@RequestHeader("Authorization") String token,
            @RequestBody GetBloodPressurePagingRequest request) {
        try {
            GetBloodPressurePagingResponse response = bloodPressureService.getBloodPressurePaging(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // * Get By Token
    @PostMapping("/u/getBloodPressureByToken")
    public ResponseEntity<?> getBloodPressureByToken(@RequestHeader("Authorization") String token,
            @RequestBody GetBloodPressureRequest request) {
        try {
            GetBloodPressureResponse response = bloodPressureService.getBloodPressureByToken(
                    jwtUtils.decodeJwtClaimsDTO(token),
                    request);
            return ResponseEntity.ok().body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(e.getResponse());
        }
    }

    @PostMapping("/u/getBloodPressurePagingByUserId")
    public ResponseEntity<?> getBloodPressurePagingByUserId(@RequestHeader("Authorization") String token,
            @RequestBody GetBloodPressureByTokenPagingRequest request) {
        try {
            GetBloodPressurePagingResponse response = bloodPressureService
                    .getBloodPressurePagingByUserId(jwtUtils.decodeJwtClaimsDTO(token), request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ! U
    @PostMapping("/a/updateBloodPressureById")
    public ResponseEntity<?> updateBloodPressureById(@RequestHeader("Authorization") String token,
            @RequestBody UpdateBloodPressureByIdRequest request) {
        try {
            BaseResponse response = bloodPressureService.updateBloodPressureById(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/u/updateBloodPressureByToken")
    public ResponseEntity<?> updateBloodPressureByToken(@RequestHeader("Authorization") String token,
            @RequestBody UpdateBloodPressureByTokenRequest request) {
        try {
            BaseResponse response = bloodPressureService.updateBloodPressureByToken(jwtUtils.decodeJwtClaimsDTO(token),
                    request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ! D
    @PostMapping("/a/deleteBloodPressureById")
    public ResponseEntity<?> deleteBloodPressureById(@RequestHeader("Authorization") String token,
            @RequestBody DeleteBloodPressureByIdRequest request) {
        try {
            BaseResponse response = bloodPressureService.deleteBloodPressureById(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/u/deleteBloodPressureByToken")
    public ResponseEntity<BaseResponse> deleteBloodPressureByToken(
            @RequestHeader("Authorization") String token,
            @RequestBody DeleteBloodPressureByTokenRequest request) {
        try {
            JwtClaimsDTO claims = jwtUtils.decodeJwtClaimsDTO(token);
            if (claims == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtil.buildErrorBaseResponse("Invalid Token ❌", "Token ไม่ถูกต้อง"));
            }

            BaseResponse response = bloodPressureService.deleteBloodPressureByToken(claims, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting blood pressure: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ResponseUtil.buildErrorBaseResponse("Error ❌", "เกิดข้อผิดพลาดขณะลบข้อมูลความดันโลหิต"));
        }
    }

}
