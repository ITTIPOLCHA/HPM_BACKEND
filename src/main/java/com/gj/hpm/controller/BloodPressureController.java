package com.gj.hpm.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.gj.hpm.config.security.jwt.JwtUtils;
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
import com.gj.hpm.service.BloodPressureService;

import jakarta.servlet.http.HttpServletRequest;

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
            BaseResponse response = bloodPressureService.updateBloodPressureByToken(jwtUtils.getIdFromHeader(token),
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
    public ResponseEntity<?> deleteBloodPressureByToken(@RequestHeader("Authorization") String token,
            @RequestBody DeleteBloodPressureByTokenRequest request) {
        try {
            BaseResponse response = bloodPressureService.deleteBloodPressureByToken(jwtUtils.getIdFromHeader(token),
                    request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestHeader("Authorization") String token, HttpServletRequest request)
            throws IOException {
        try {
            MultipartFile imageFile = ((MultipartHttpServletRequest) request).getFile("file");
            byte[] imageByte = new byte[0];

            if (imageFile != null) {
                imageByte = imageFile.getBytes();
            }

            BaseResponse response = bloodPressureService.uploadImage(jwtUtils.getIdFromHeader(token),Base64.encodeBase64String(imageByte));
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
