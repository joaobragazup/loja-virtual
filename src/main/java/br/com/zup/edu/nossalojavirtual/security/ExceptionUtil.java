package br.com.zup.edu.nossalojavirtual.security;

import org.springframework.validation.FieldError;

public class ExceptionUtil {
    public static String getFieldAndDefaultErrorMessage(FieldError fieldError){
        return String.format("%s %s", fieldError.getField(), fieldError.getDefaultMessage());
    }
}
