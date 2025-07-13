package com.meetup.server.startpoint.exception;

import com.meetup.server.global.support.error.ErrorType;
import com.meetup.server.global.support.error.GlobalException;

public class InvalidStartPointException extends GlobalException {

    public InvalidStartPointException(ErrorType errorType) {
        super(errorType);
    }
}
