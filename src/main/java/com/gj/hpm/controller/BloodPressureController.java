package com.gj.hpm.controller;

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
import com.gj.hpm.dto.request.GetBloodPressureByTokenPagingRequest;
import com.gj.hpm.dto.request.GetBloodPressurePagingRequest;
import com.gj.hpm.dto.request.GetBloodPressureRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetBloodPressurePagingResponse;
import com.gj.hpm.dto.response.GetBloodPressureResponse;
import com.gj.hpm.service.BloodPressureService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/bp")
public class BloodPressureController {
    @Autowired
    private BloodPressureService bloodPressureService;
    @Autowired
    private JwtUtils jwtUtils;

    // ! C
    @PostMapping("/createBloodPressure")
    public ResponseEntity<?> createBloodPressure(@RequestHeader("Authorization") String token,
            @RequestBody CreateBloodPressureRequest request) {
        try {
            BaseResponse response = bloodPressureService.createBloodPressure(jwtUtils.getIdFromHeader(token),
                    request);
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
                    jwtUtils.getIdFromHeader(token),
                    request);
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

    @PostMapping("/u/getBloodPressurePagingByUserId")
    public ResponseEntity<?> getBloodPressurePagingByUserId(@RequestHeader("Authorization") String token,
            @RequestBody GetBloodPressureByTokenPagingRequest request) {
        try {
            GetBloodPressurePagingResponse response = bloodPressureService
                    .getBloodPressurePagingByUserId(jwtUtils.getIdFromHeader(token), request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // // ! U
    // @PostMapping("/updateBloodPressure")
    // public ResponseEntity<?> updateBloodPressure(@RequestHeader("Authorization")
    // String token,
    // @RequestBody CreateBloodPressureRequest request) {
    // try {
    // BaseResponse response = bloodPressureService.updateBloodPressure(request);
    // return ResponseEntity.ok().body(response);
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

    // // ! D
    // @PostMapping("/deleteBloodPressure")
    // public ResponseEntity<?> deleteBloodPressure(@RequestHeader("Authorization")
    // String token,
    // @RequestBody CreateBloodPressureRequest request) {
    // try {
    // BaseResponse response = bloodPressureService.deleteBloodPressure(request);
    // return ResponseEntity.ok().body(response);
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }
}
