package com.axreng.backend.domain.model.dto;

import java.io.Serializable;

public class ErrorResponse implements Serializable {

    private String errMsg;

    public ErrorResponse(String errMsg) {
        this.errMsg = errMsg;
    }
}
