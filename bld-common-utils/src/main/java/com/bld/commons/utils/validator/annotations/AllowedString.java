package com.bld.commons.utils.validator.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.bld.commons.utils.validator.AllowedStringValidator;
import com.bld.commons.utils.validator.AllowedValueValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * The Interface AllowedString.
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD, PARAMETER })
@Constraint(validatedBy=AllowedStringValidator.class)
public @interface AllowedString {

	/**
	 * Value.
	 *
	 * @return the string[]
	 */
	public String[] value();
	
	
    /**
     * Message.
     *
     * @return the string
     */
    public String message() default AllowedValueValidator.DEFAULT_MESSAGE;
    
    /**
     * Groups.
     *
     * @return the class[]
     */
    public Class<?>[] groups() default {};
    
    public Class<? extends Payload>[] payload() default {};
    
    
    
}
