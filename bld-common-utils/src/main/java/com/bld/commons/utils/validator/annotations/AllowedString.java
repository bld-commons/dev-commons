package com.bld.commons.utils.validator.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.bld.commons.utils.validator.AllowedNumberValidator;

@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER })
@Constraint(validatedBy=AllowedNumberValidator.class)
public @interface AllowedString {

	public String[] value();
	
	
    public String message() default "Invalid number";
    public Class<?>[] groups() default {};
    public Class<? extends Payload>[] payload() default {};
    
    
    
}
