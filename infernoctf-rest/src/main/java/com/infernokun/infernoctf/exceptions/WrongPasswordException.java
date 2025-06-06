package com.infernokun.infernoctf.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class WrongPasswordException extends BadCredentialsException {
    public WrongPasswordException(String msg) {
        super(msg);
    }

    public WrongPasswordException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
