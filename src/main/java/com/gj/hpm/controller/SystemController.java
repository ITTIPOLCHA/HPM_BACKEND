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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.config.security.jwt.JwtUtils;
import com.gj.hpm.config.security.services.UserDetailsImpl;
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
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.Key;
import com.gj.hpm.util.Constant.Level;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.Constant.TypeSignIn;
import com.gj.hpm.util.Encryption;
import com.nimbusds.jwt.JWTClaimsSet;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/system")
public class SystemController {

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

    @GetMapping("/ping")
    public ResponseEntity<?> getMethodName() {
        return ResponseEntity.ok(new BaseResponse(
                new BaseStatusResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                        Collections.singletonList(new BaseDetailsResponse("Success ✅", "Ping Success")))));
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest req) {
        try {
            if (TypeSignIn.line.toString().equals(req.getType())) {
                JWTClaimsSet claimsSet = jwtUtils.decodeES256Jwt(req.getLineToken());
                String lineId = claimsSet.getSubject();
                User user = userRepository.findByLineId(lineId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with Line ID: " + lineId));
                req.setEmail(user.getEmail());
                req.setPassword(Encryption.decodedData(user.getLineSubId()));
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
                            new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(), ApiReturn.BAD_REQUEST.description(),
                                    Collections
                                            .singletonList(
                                                    new BaseDetailsResponse("Error ❌", "เข้าสู่ระบบไม่สำเร็จ.")))));
        }
    }

    @PostMapping("/signUp")
    public ResponseEntity<BaseResponse> signUp(@Valid @RequestBody SignUpRequest req) {
        try {
            List<BaseDetailsResponse> details = validateSignUpRequest(req);
            if (!details.isEmpty())
                return ResponseEntity
                        .badRequest()
                        .body(new BaseResponse(
                                new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                                        ApiReturn.BAD_REQUEST.description(),
                                        details)));
            User user = createUserFromSignUpRequest(req);
            setRoleAndAdditionalInfo(user, req);
            userRepository.save(user);
            return ResponseEntity.ok(new BaseResponse(
                    new BaseStatusResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                            Collections.singletonList(new BaseDetailsResponse("Success ✅", "สมัครสมาชิกสำเร็จ")))));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new BaseResponse(
                            new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(), ApiReturn.BAD_REQUEST.description(),
                                    Collections
                                            .singletonList(
                                                    new BaseDetailsResponse("Error ❌", "สมัครสมาชิกไม่สำเร็จ.")))));
        }
    }

    private List<BaseDetailsResponse> validateSignUpRequest(SignUpRequest req) {
        List<BaseDetailsResponse> details = new ArrayList<>();
        if (userRepository.existsByEmail(req.getEmail()))
            details.add(new BaseDetailsResponse("email", "อีเมลนี้ถูกใช้งานแล้ว"));
        if (userRepository.existsByPhone(req.getPhone()))
            details.add(new BaseDetailsResponse("phone", "เบอร์นี้ถูกใช้งานแล้ว"));
        if (userRepository.existsByHn(req.getHn()))
            details.add(new BaseDetailsResponse("hn", "หมายเลขผู้ป่วยนี้ถูกใช้งานแล้ว"));
        return details;
    }

    private User createUserFromSignUpRequest(SignUpRequest req) {
        User user = new User();
        BeanUtils.copyProperties(req, user);
        user.setUsername(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        LocalDateTime now = LocalDateTime.now();
        user.setCreateDate(now);
        user.setUpdateDate(now);
        user.setStatusFlag(StatusFlag.INACTIVE.code());
        user.setLevel(Level.NORMAL.toString());
        userRepository.save(user);
        user.setCreateBy(User.builder().id(user.getId()).build());
        user.setUpdateBy(User.builder().id(user.getId()).build());
        return user;
    }

    private void setRoleAndAdditionalInfo(User user, SignUpRequest req) {
        Set<Role> roles = new HashSet<>();
        Role role;
        if (StringUtils.isBlank(req.getLineToken())) {
            role = roleRepository.findByName(ERole.ROLE_ADMIN);
        } else {
            role = roleRepository.findByName(ERole.ROLE_USER);
            JWTClaimsSet claimsSet = jwtUtils.decodeES256Jwt(req.getLineToken());
            String name = claimsSet.getClaim(Key.name.toString()).toString();
            String imageUrl = claimsSet.getClaim(Key.picture.toString()).toString();
            user.setLineId(claimsSet.getSubject());
            user.setLineSubId(Encryption.encodedData(req.getPassword()));
            user.setLineName(name);
            user.setPictureUrl(imageUrl);
        }
        roles.add(role);
        user.setRoles(roles);
    }

    @PostMapping("/setInactive")
    public ResponseEntity<BaseResponse> setInactive() {
        try {
            // ดึงข้อมูลผู้ใช้ทั้งหมดจากฐานข้อมูล
            List<User> users = userRepository.findAllUserWithLine();

            // วนลูปผู้ใช้แต่ละคนเพื่อเปลี่ยนค่า statusFlag เป็น "Inactive"
            for (User user : users) {
                user.setStatusFlag(StatusFlag.INACTIVE.code());
            }

            // บันทึกการเปลี่ยนแปลงลงในฐานข้อมูล
            userRepository.saveAll(users);

            // ส่งคำตอบกลับว่าการดำเนินการเสร็จสิ้น
            return ResponseEntity.ok(new BaseResponse(
                    new BaseStatusResponse(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(),
                            Collections.singletonList(
                                    new BaseDetailsResponse("Success ✅", "เปลี่ยนสถานะเป็น Inactive สำเร็จ")))));
        } catch (Exception e) {
            // หากเกิดข้อผิดพลาด ส่งคำตอบกลับว่าไม่สามารถดำเนินการได้
            return ResponseEntity
                    .badRequest()
                    .body(new BaseResponse(
                            new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(), ApiReturn.BAD_REQUEST.description(),
                                    Collections
                                            .singletonList(
                                                    new BaseDetailsResponse("Error ❌",
                                                            "ไม่สามารถเปลี่ยนสถานะเป็น Inactive ได้")))));
        }
    }

}
