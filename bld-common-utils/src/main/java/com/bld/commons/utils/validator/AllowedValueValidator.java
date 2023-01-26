package com.bld.commons.utils.validator;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public abstract class AllowedValueValidator<A extends Annotation,T> implements ConstraintValidator<A, T>{

	protected abstract String getMessage();
	
	protected void setContext(boolean valid,ConstraintValidatorContext context) {
		if (!valid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(this.getMessage()).addConstraintViolation();
		}
	}


}