package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResp {
    private String message;

    public MessageResp(String message) {
        this.message = message;
    }
}
