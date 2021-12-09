package com.docotel.ki.component;

import org.springframework.security.core.AuthenticationException;

public class CantAccessException extends AuthenticationException {
    public CantAccessException(String msg) {
        super(msg);
    }
    public CantAccessException(String msg, Throwable t) {
        super(msg, t);
    }
}
