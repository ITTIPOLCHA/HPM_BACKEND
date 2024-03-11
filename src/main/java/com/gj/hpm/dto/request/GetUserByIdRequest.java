package com.gj.hpm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserByIdRequest extends BaseRequest{
    @NotBlank(message = "{notEmpty.message}")
    private String userId;
}
