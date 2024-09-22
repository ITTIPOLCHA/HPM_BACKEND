package com.gj.hpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.dto.request.PasswordChangeRequest;
import com.gj.hpm.dto.request.PasswordForgotRequest;
import com.gj.hpm.dto.request.SignInRequest;
import com.gj.hpm.dto.request.SignUpRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.JwtResponse;
import com.gj.hpm.service.UserService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.ResponseUtil;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/system")
public class SystemController {

        @Autowired
        private UserService userService;

        @GetMapping("/ping")
        public ResponseEntity<?> ping() {
                return ResponseUtil.buildSuccessResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                                "สำเร็จ ✅", "ปิง สำเร็จ");
        }

        @PostMapping("/signIn")
        public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest req) {
                try {
                        JwtResponse response = userService.signIn(req);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseUtil.buildErrorResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(),
                                        "เกิดข้อผิดพลาด ❌",
                                        "เข้าสู่ระบบไม่สำเร็จ.");
                }
        }

        @PostMapping("/signUp")
        public ResponseEntity<BaseResponse> signUp(@Valid @RequestBody SignUpRequest req) {
                try {
                        BaseResponse response = userService.signUp(req);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseUtil.buildErrorResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(),
                                        "เกิดข้อผิดพลาด ❌",
                                        "สมัครสมาชิกไม่สำเร็จ.");
                }
        }

        @PostMapping("/setInactive")
        public ResponseEntity<BaseResponse> setInactive() {
                try {
                        BaseResponse response = userService.setInactive();
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        return ResponseUtil.buildErrorResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(),
                                        "เกิดข้อผิดพลาด ❌",
                                        "ไม่สามารถเปลี่ยนสถานะเป็น Inactive ได้.");
                }
        }

        @PostMapping("/changePassword")
        public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token,
                        @RequestBody PasswordChangeRequest request) {
                try {
                        BaseResponse response = userService.changePassword(token,
                                        request);
                        return ResponseEntity.ok().body(response);
                } catch (Exception e) {
                        return ResponseUtil.buildErrorResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(),
                                        "เกิดข้อผิดพลาด ❌",
                                        "ไม่สามารถเปลี่ยนรหัสผ่านได้.");
                }
        }

        @PostMapping("/forgotPassword")
        public ResponseEntity<?> forgotPassword(@RequestBody PasswordForgotRequest request) {
                try {
                        BaseResponse response = userService.forgotPassword(request);
                        return ResponseEntity.ok().body(response);
                } catch (Exception e) {
                        return ResponseUtil.buildErrorResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(),
                                        "เกิดข้อผิดพลาด ❌",
                                        "ไม่สามารถขอเปลี่ยนรหัสผ่านได้.");
                }
        }

}
