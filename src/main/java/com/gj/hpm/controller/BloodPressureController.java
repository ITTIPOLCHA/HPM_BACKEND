package com.gj.hpm.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
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
    private RestTemplate restTemplate;
    
    @Autowired
    private BloodPressureService bloodPressureService;
    @Autowired
    private JwtUtils jwtUtils;

    @Value("${hpm.app.api.key}")
    private String apiKey;

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

    @PostMapping(value = "/getBloodPressureFromImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getBloodPressureFromImage(@RequestParam("image") MultipartFile image) {
        try {
            if (image == null) {
                return ResponseEntity.badRequest().body("Image is required.");
            }
            BaseResponse response = bloodPressureService.getBloodPressureFromImage(image);
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
        MultipartFile imageFile = ((MultipartHttpServletRequest) request).getFile("file");
        byte[] imageByte = new byte[0];

        if (imageFile != null)
            imageByte = imageFile.getBytes();

        // แปลงไฟล์ภาพเป็น Base64
        String base64Image = Base64.encodeBase64String(imageByte);

        // เตรียมข้อมูลที่ต้องการส่งไปยัง Flask API
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
        content.put("text", "I want the blood pressure value from this image as JSON sys, dia, pul text.");

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
            Map<String, Object> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class)
                    .getBody();

            // Extract and return the response
            if (response != null && response.containsKey("choices")) {
                Map<String, Object> choice = (Map<String, Object>) ((List<Map<String, Object>>) response.get("choices"))
                        .get(0).get("message");
                // Map<String, Object> message = (Map<String, Object>) choice.get("message");
                return new ResponseEntity<>((String) choice.get("content"),
                        HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Could not extract text from image.",
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
