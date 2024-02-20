package com.gj.hpm.util;

import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ResponseStatus status;

    public ServiceFailedException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public ServiceFailedException(String errorMessage, Throwable err, ResponseStatus status) {
        super(errorMessage, err);
        this.status = status;
    }
}
