package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtClaimsDTO {
    private String subject;
    private String jwtId;
    private String role;
    private String name;
    private String lineId;
}
