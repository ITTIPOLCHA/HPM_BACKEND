package com.gj.hpm.dto.request;

import java.io.Serializable;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Validated
public class BaseRequest implements Serializable {
	private String requestId;
}
