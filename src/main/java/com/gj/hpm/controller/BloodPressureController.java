package com.gj.hpm.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/bp")
public class BloodPressureController {
    // @Autowired
    // private BloodPressureService bloodPressureService;
    // @Autowired
    // private JwtUtils jwtUtils;

    // ! C
    // @PostMapping("/createBloodPressure")
    // public ResponseEntity<?> createBloodPressure(@RequestHeader("Authorization")
    // String token,
    // @RequestBody CreateBloodPressureRequest request) {
    // try {
    // BaseResponse response =
    // bloodPressureService.createBloodPressure(jwtUtils.getEmailFromHeader(token),
    // request);
    // return ResponseEntity.ok().body(response);
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

    // // ! R
    // // ? Get By Id
    // @PostMapping("/a/getBloodPressureById")
    // public ResponseEntity<?> getBloodPressureById(@RequestHeader("Authorization")
    // String token,
    // @RequestBody CreateBloodPressureRequest request) {
    // try {
    // BaseResponse response = bloodPressureService.getBloodPressure(request);
    // return ResponseEntity.ok().body(response);
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

    // // * Get By Token
    // @PostMapping("/u/getBloodPressureByToken")
    // public ResponseEntity<?>
    // getBloodPressureByToken(@RequestHeader("Authorization") String token,
    // @RequestBody CreateBloodPressureRequest request) {
    // try {
    // BaseResponse response = bloodPressureService.getBloodPressure(request);
    // return ResponseEntity.ok().body(response);
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

    // // ? Get By Page
    // @PostMapping("/a/getBloodPressurePaging")
    // public ResponseEntity<?>
    // getBloodPressurePaging(@RequestHeader("Authorization") String token,
    // @RequestBody CreateBloodPressureRequest request) {
    // try {
    // BaseResponse response = bloodPressureService.getBloodPressure(request);
    // return ResponseEntity.ok().body(response);
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().body(e.getMessage());
    // }
    // }

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
