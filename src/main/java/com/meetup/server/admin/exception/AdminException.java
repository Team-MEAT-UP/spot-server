package com.meetup.server.admin.exception;

import com.meetup.server.global.support.error.ErrorType;
import com.meetup.server.global.support.error.GlobalException;

public class AdminException extends GlobalException {

    public AdminException(ErrorType errorType) {
        super(errorType);
    }
}
