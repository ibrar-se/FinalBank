package com.logicminers.banking.account.exception;

import java.util.SplittableRandom;

public class AccountBusinessException extends RuntimeException{
    private final String errorCode;

    public AccountBusinessException(String message , String errorCode){
         super(message);
         this.errorCode = errorCode;
    }

    public String getErrorCode(){
        return errorCode;
    }
}