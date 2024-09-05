package com.gj.hpm.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.config.security.jwt.JwtUtils;
import com.gj.hpm.config.security.services.UserDetailsImpl;
import com.gj.hpm.dto.request.PasswordChangeRequest;
import com.gj.hpm.dto.request.PasswordForgotRequest;
import com.gj.hpm.dto.request.SignInRequest;
import com.gj.hpm.dto.request.SignUpRequest;
import com.gj.hpm.dto.response.BaseDetailsResponse;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.dto.response.JwtResponse;
import com.gj.hpm.entity.ERole;
import com.gj.hpm.entity.Role;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmRoleRepository;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.service.UserService;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.Key;
import com.gj.hpm.util.Constant.Level;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.Constant.TypeSignIn;
import com.gj.hpm.util.Encryption;
import com.gj.hpm.util.LineUtil;
import com.nimbusds.jwt.JWTClaimsSet;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/system")
public class SystemController {

        @Value("${hpm.app.token.property}")
        private String token;

        @Autowired
        AuthenticationManager authenticationManager;

        @Autowired
        StmUserRepository userRepository;

        @Autowired
        StmRoleRepository roleRepository;

        @Autowired
        PasswordEncoder encoder;

        @Autowired
        JwtUtils jwtUtils;

        @Autowired
        private UserService userService;

        @GetMapping("/ping")
        public ResponseEntity<?> getMethodName() {
                return ResponseEntity.ok(new BaseResponse(
                                new BaseStatusResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                                                Collections.singletonList(new BaseDetailsResponse("Success ✅",
                                                                "Ping Success")))));
        }

        @PostMapping("/signIn")
        public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest req) {
                try {
                        JWTClaimsSet claimsSet = null;
                        String lineId = null;
                        if (StringUtils.isNotBlank(req.getLineToken())) {
                                claimsSet = jwtUtils.decodeES256Jwt(req.getLineToken());
                                lineId = claimsSet.getSubject();
                                if (TypeSignIn.line.toString().equals(req.getType())) {
                                        User user = userRepository.findByLineId(lineId)
                                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                                        "User not found with Line"));
                                        req.setEmail(user.getEmail());
                                        req.setPassword(Encryption.decodedData(user.getLineSubId()));
                                } else {
                                        User user = userRepository.findByEmail(req.getEmail())
                                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                                        "User not found with email: "
                                                                                        + req.getEmail()));
                                        user.setLineId(lineId);
                                        user.setLineName(claimsSet.getClaim(Key.name.toString()).toString());
                                        user.setPictureUrl(claimsSet.getClaim(Key.picture.toString()).toString());
                                        userRepository.save(user);
                                }
                        }
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        String jwt = jwtUtils.generateJwtToken(authentication);
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        List<String> roles = userDetails.getAuthorities().stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .collect(Collectors.toList());
                        return ResponseEntity.ok(new JwtResponse(jwt,
                                        userDetails.getId(),
                                        userDetails.getEmail(),
                                        userDetails.getName(),
                                        roles));
                } catch (Exception e) {
                        return ResponseEntity
                                        .badRequest()
                                        .body(new BaseResponse(
                                                        new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                                        ApiReturn.BAD_REQUEST.description(),
                                                                        Collections
                                                                                        .singletonList(
                                                                                                        new BaseDetailsResponse(
                                                                                                                        "Error ❌",
                                                                                                                        "เข้าสู่ระบบไม่สำเร็จ.")))));
                }
        }

        @PostMapping("/signUp")
        public ResponseEntity<BaseResponse> signUp(@Valid @RequestBody SignUpRequest req) {
                try {
                        User user = new User();
                        Set<Role> roles = new HashSet<>();
                        Role role;
                        String lineId = null;
                        String name = null;
                        String imageUrl = null;
                        if (StringUtils.isNotBlank(req.getLineToken())) {
                                JWTClaimsSet claimsSet = jwtUtils.decodeES256Jwt(req.getLineToken());
                                lineId = claimsSet.getSubject();
                                name = claimsSet.getClaim(Key.name.toString()).toString();
                                imageUrl = claimsSet.getClaim(Key.picture.toString()).toString();
                        }
                        List<BaseDetailsResponse> details = validateSignUpRequest(req, lineId);
                        if (!details.isEmpty())
                                return ResponseEntity
                                                .badRequest()
                                                .body(new BaseResponse(
                                                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                                                ApiReturn.BAD_REQUEST.description(),
                                                                                details)));
                        BeanUtils.copyProperties(req, user);
                        if (StringUtils.isBlank(req.getLineToken())) {
                                role = roleRepository.findByName(ERole.ROLE_ADMIN);
                        } else {
                                role = roleRepository.findByName(ERole.ROLE_USER);
                                user.setLineId(lineId);
                                user.setLineSubId(Encryption.encodedData(req.getPassword()));
                                user.setLineName(name);
                                user.setPictureUrl(imageUrl);
                                user.setStatusFlag(StatusFlag.INACTIVE.code());
                                user.setLevel(Level.NORMAL);
                                user.setCheckState(false);
                                if (!new LineUtil().changeRichmenu(user.getLineId(),
                                                "richmenu-199151260dddf9df54f66768a8a02f68", token))
                                        return ResponseEntity
                                                        .badRequest()
                                                        .body(new BaseResponse(
                                                                        new BaseStatusResponse(
                                                                                        ApiReturn.BAD_REQUEST.code(),
                                                                                        ApiReturn.BAD_REQUEST
                                                                                                        .description(),
                                                                                        Collections
                                                                                                        .singletonList(
                                                                                                                        new BaseDetailsResponse(
                                                                                                                                        "Error ❌",
                                                                                                                                        "เปลี่ยน Rich menu ไม่ได้.")))));
                                if (!new LineUtil().sentMessage(user.getLineId(),
                                                token, ("ระบบได้บันทึกข้อมูลของ " + req.getFirstName()
                                                                + " เรียบร้อยแล้ว✅ ท่านสามารถเลือกเมนู “ดูประวัติ” เพื่อดูประวัติการส่งผลวัดความดันโลหิต หรือ เลือกเมนู “ส่งผลวัด” เพื่อส่งผลวัดความดันโลหิตได้เลยครับ")))
                                        return ResponseEntity
                                                        .badRequest()
                                                        .body(new BaseResponse(
                                                                        new BaseStatusResponse(
                                                                                        ApiReturn.BAD_REQUEST.code(),
                                                                                        ApiReturn.BAD_REQUEST
                                                                                                        .description(),
                                                                                        Collections
                                                                                                        .singletonList(
                                                                                                                        new BaseDetailsResponse(
                                                                                                                                        "Error ❌",
                                                                                                                                        "ส่งข้อความไม่สำเร็จ.")))));
                        }
                        roles.add(role);
                        user.setRoles(roles);
                        user.setUsername(req.getEmail());
                        user.setPassword(encoder.encode(req.getPassword()));
                        user.setCreateDate(LocalDateTime.now());
                        user.setUpdateDate(LocalDateTime.now());
                        userRepository.save(user);
                        user.setCreateBy(User.builder().id(user.getId()).build());
                        user.setUpdateBy(User.builder().id(user.getId()).build());
                        userRepository.save(user);

                        return ResponseEntity.ok(new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                                        ApiReturn.SUCCESS.description(),
                                                        Collections.singletonList(new BaseDetailsResponse("Success ✅",
                                                                        "สมัครสมาชิกสำเร็จ")))));
                } catch (Exception e) {
                        return ResponseEntity
                                        .badRequest()
                                        .body(new BaseResponse(
                                                        new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                                        ApiReturn.BAD_REQUEST.description(),
                                                                        Collections
                                                                                        .singletonList(
                                                                                                        new BaseDetailsResponse(
                                                                                                                        "Error ❌",
                                                                                                                        "สมัครสมาชิกไม่สำเร็จ.")))));
                }
        }

        private List<BaseDetailsResponse> validateSignUpRequest(SignUpRequest req, String lineId) {
                List<BaseDetailsResponse> details = new ArrayList<>();
                if (userRepository.existsByEmail(req.getEmail()))
                        details.add(new BaseDetailsResponse("email", "อีเมลนี้ถูกใช้งานแล้ว"));
                if (userRepository.existsByPhone(req.getPhone()))
                        details.add(new BaseDetailsResponse("phone", "เบอร์นี้ถูกใช้งานแล้ว"));
                if (userRepository.existsByHn(req.getHn()))
                        details.add(new BaseDetailsResponse("hn", "หมายเลขผู้ป่วยนี้ถูกใช้งานแล้ว"));
                if (StringUtils.isNotBlank(lineId) && userRepository.existsByLineId(lineId))
                        details.add(new BaseDetailsResponse("line", "Line นี้ถูกใช้งานแล้ว"));
                return details;
        }

        @PostMapping("/setInactive")
        public ResponseEntity<BaseResponse> setInactive() {
                try {
                        List<User> users = userRepository.findAllUserWithLine();

                        for (User user : users) {
                                user.setStatusFlag(StatusFlag.INACTIVE.code());
                                user.setLevel(Level.NORMAL);
                                user.setCheckState(false);
                        }

                        userRepository.saveAll(users);

                        return ResponseEntity.ok(new BaseResponse(
                                        new BaseStatusResponse(ApiReturn.SUCCESS.code(),
                                                        ApiReturn.SUCCESS.description(),
                                                        Collections.singletonList(
                                                                        new BaseDetailsResponse("Success ✅",
                                                                                        "เปลี่ยนสถานะเป็น Inactive สำเร็จ")))));
                } catch (Exception e) {
                        return ResponseEntity
                                        .badRequest()
                                        .body(new BaseResponse(
                                                        new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                                                        ApiReturn.BAD_REQUEST.description(),
                                                                        Collections
                                                                                        .singletonList(
                                                                                                        new BaseDetailsResponse(
                                                                                                                        "Error ❌",
                                                                                                                        "ไม่สามารถเปลี่ยนสถานะเป็น Inactive ได้")))));
                }
        }

        @PostMapping("/changePassword")
        public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token,
                        @RequestBody PasswordChangeRequest request) {
                try {
                        BaseResponse response = userService.changePassword(jwtUtils.getIdFromHeader(token),
                                        request);
                        return ResponseEntity.ok().body(response);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                }
        }

        @PostMapping("/forgotPassword")
        public ResponseEntity<?> forgotPassword(@RequestBody PasswordForgotRequest request) {
                try {
                        BaseResponse response = userService.forgotPassword(request);
                        return ResponseEntity.ok().body(response);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                }
        }

}
