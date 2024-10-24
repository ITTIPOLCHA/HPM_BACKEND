package com.gj.hpm.controller;

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
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.request.UpdateUserByIdRequest;
import com.gj.hpm.dto.request.UpdateUserByTokenRequest;
import com.gj.hpm.dto.request.UpdateUserCheckStateRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetUserListByLevelResponse;
import com.gj.hpm.dto.response.GetUserListByStatusFlagResponse;
import com.gj.hpm.dto.response.GetUserResponse;
import com.gj.hpm.dto.response.JwtClaimsDTO;
import com.gj.hpm.exception.NotFoundException;
import com.gj.hpm.service.UserService;
import com.gj.hpm.util.ResponseUtil;

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
    public ResponseEntity<?> getUserById(@RequestHeader("Authorization") String token,
            @RequestBody GetUserByIdRequest request) {
        try {
            GetUserResponse response = userService.getUserById(request);
            return ResponseEntity.ok().body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(e.getResponse());
        }
    }

    // * Get By Token
    @PostMapping("/u/getUserByToken")
    public ResponseEntity<?> getUserByToken(@RequestHeader("Authorization") String token,
            @RequestBody BaseRequest request) {
        try {
            JwtClaimsDTO claims = jwtUtils.decodeJwtClaimsDTO(token);
            if (claims == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtil.buildErrorBaseResponse("Invalid Token ❌", "Token ไม่ถูกต้อง"));
            }
            GetUserResponse response = userService.getUserByToken(claims.getJwtId());
            return ResponseEntity.ok().body(response);
        } catch (NotFoundException e) {
            return ResponseEntity.badRequest().body(e.getResponse());
        }
    }

    // ? Get By Page
    @PostMapping("/a/getUserPaging")
    public ResponseEntity<?> getUserPaging(@RequestHeader("Authorization") String token,
            @RequestBody GetUserPagingRequest request) {
        try {
            BaseResponse response = userService.getUserPaging(request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/a/getUserListByLevel")
    public ResponseEntity<?> getUserListByLevel(@RequestHeader("Authorization") String token,
            @RequestBody BaseRequest request) {
        try {
            List<GetUserListByLevelResponse> response = userService.getUserListByLevel();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/a/getUserListByStatusFlag")
    public ResponseEntity<?> getUserListByStatusFlag(@RequestHeader("Authorization") String token,
            @RequestBody BaseRequest request) {
        try {
            List<GetUserListByStatusFlagResponse> response = userService.getUserListByStatusFlag();
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
            JwtClaimsDTO claims = jwtUtils.decodeJwtClaimsDTO(token);
            if (claims == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtil.buildErrorBaseResponse("Invalid Token ❌", "Token ไม่ถูกต้อง"));
            }
            BaseResponse response = userService.updateUserById(claims.getJwtId(), request);
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
            JwtClaimsDTO claims = jwtUtils.decodeJwtClaimsDTO(token);
            if (claims == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtil.buildErrorBaseResponse("Invalid Token ❌", "Token ไม่ถูกต้อง"));
            }
            BaseResponse response = userService.updateUserByToken(claims.getJwtId(), request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/a/updateUserCheckState")
    public ResponseEntity<?> updateUserCheckState(@RequestHeader("Authorization") String token,
            @RequestBody UpdateUserCheckStateRequest request) {
        try {
            BaseResponse response = userService.updateUserCheckState(request);
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
            JwtClaimsDTO claims = jwtUtils.decodeJwtClaimsDTO(token);
            if (claims == null) {
                return ResponseEntity.badRequest().body(
                        ResponseUtil.buildErrorBaseResponse("Invalid Token ❌", "Token ไม่ถูกต้อง"));
            }
            BaseResponse response = userService.deleteUserByToken(claims.getJwtId(), request);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
