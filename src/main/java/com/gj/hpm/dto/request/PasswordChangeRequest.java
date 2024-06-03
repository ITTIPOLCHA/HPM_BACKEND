package com.gj.hpm.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordChangeRequest extends BaseRequest {
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirm;

}
