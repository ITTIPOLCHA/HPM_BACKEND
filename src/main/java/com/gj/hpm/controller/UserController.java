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
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.request.UpdateUserByIdRequest;
import com.gj.hpm.dto.request.UpdateUserByTokenRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetUserPagingResponse;
import com.gj.hpm.dto.response.GetUserResponse;
import com.gj.hpm.service.UserService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    // ! C
    // * Sign up
    // ! R
    // ? Get By Id
    @PostMapping("/a/getUserById")
    public ResponseEntity<?> getUserById(@RequestBody GetUserByIdRequest request) {
        try {
            GetUserResponse response = userService.getUserById(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // * Get By Token
    @PostMapping("/u/getUserByToken")
    public ResponseEntity<?> getUserByToken(@RequestHeader("Authorization") String token,
            @RequestBody BaseRequest request) {
        try {
            GetUserResponse response = userService.getUserByToken(jwtUtils.getEmailFromHeader(token));
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ? Get By Page
    @PostMapping("/a/getUserPaging")
    public ResponseEntity<?> getUserPaging(@RequestBody GetUserPagingRequest request) {
        try {
            GetUserPagingResponse response = userService.getUserPaging(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ! U
    // ? Update By Id
    @PostMapping("/a/updateUserById")
    public ResponseEntity<?> updateUserById(@RequestHeader("Authorization") String token,
            @RequestBody UpdateUserByIdRequest request) {
        try {
            BaseResponse response = userService.updateUserById(jwtUtils.getIdFromHeader(token), request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // * Update By Token
    @PostMapping("/u/updateUserByToken")
    public ResponseEntity<?> updateUserByToken(@RequestHeader("Authorization") String token,
            @RequestBody UpdateUserByTokenRequest request) {
        try {
            BaseResponse response = userService.updateUserByToken(jwtUtils.getIdFromHeader(token), request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ! D
    // ? Delete By Id
    @PostMapping("/a/deleteUserById")
    public ResponseEntity<?> deleteUserById(@RequestBody GetUserByIdRequest request) {
        try {
            BaseResponse response = userService.deleteUserById(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // * Delete By Token
    @PostMapping("/u/deleteUserByToken")
    public ResponseEntity<?> deleteUserByToken(@RequestHeader("Authorization") String token,
            @RequestBody BaseRequest request) {
        try {
            BaseResponse response = userService.deleteUserByToken(jwtUtils.getIdFromHeader(token), request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
