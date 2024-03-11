package com.gj.hpm.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserPagingRequest extends BasePaginationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String hn;
    private String statusFlag;
}
