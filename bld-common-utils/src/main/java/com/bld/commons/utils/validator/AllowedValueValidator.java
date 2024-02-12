package com.bld.commons.utils.validator;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * The Class AllowedValueValidator.
 *
 * @param <A> the generic type
 * @param <T> the generic type
 */
public abstract class AllowedValueValidator<A extends Annotation,T> implements ConstraintValidator<A, T>{

	public final static String DEFAULT_MESSAGE="The value is not valid.";
	
	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	protected abstract String getMessage();
	
	/**
	 * Sets the context.
	 *
	 * @param valid the valid
	 * @param context the context
	 */
	protected void setContext(boolean valid,ConstraintValidatorContext context) {
		if (!valid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(this.getMessage()).addConstraintViolation();
		}
	}


}