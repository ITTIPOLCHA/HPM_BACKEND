package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserListByLevelResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String hospitalNumber;
    private String phoneNumber;
    private String level;
    private String pictureUrl;
    private boolean verified;
}
