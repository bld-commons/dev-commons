/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class com.bld.commons.connection.utils.ValidatorUtils.java
 */
package com.bld.commons.connection.utils;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * The Class ValidatorUtils.
 */
@Component
public class ValidatorUtils {

	/** The Constant logger. */
	private final static Log logger = LogFactory.getLog(ValidatorUtils.class);

	/** The Constant VALIDATOR. */
	private final static Validator VALIDATOR=getValidator();
	
	
	/**
	 * Gets the validator.
	 *
	 * @return the validator
	 */
	private static Validator getValidator() {
		ValidatorFactory valdiatorFactory = Validation.buildDefaultValidatorFactory(); 
		return  valdiatorFactory.getValidator();
	}
	
	

	
	/**
	 * Check validatr build class.
	 *
	 * @param obj the obj
	 * @throws Exception the exception
	 */
	public static void checkValidatrBuildClass(Object obj) throws Exception {
		Set<ConstraintViolation<Object>> failedValidations = VALIDATOR.validate(obj);
		if (!failedValidations.isEmpty()) {
			for (ConstraintViolation<Object> failedValidation : failedValidations) {
				logger.error(failedValidation.getPropertyPath()+": "+failedValidation.getMessage());
				throw new Exception(failedValidation.getPropertyPath()+": "+failedValidation.getMessage());
			}
		}
	}
	
}
