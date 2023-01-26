package com.bld.commons.utils.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.bld.commons.utils.validator.annotations.AllowedNumber;

public class AllowedNumberValidator implements ConstraintValidator<AllowedNumber, Number> {

	private AllowedNumber validNumberValues;

	@Override
	public void initialize(AllowedNumber validNumberValues) {
		this.validNumberValues = validNumberValues;
	}

	@Override
	public boolean isValid(Number value, ConstraintValidatorContext context) {
		boolean valid = false;
		if (value != null) {
			for (Double checkValue : validNumberValues.value()) {
				if (checkValue.doubleValue() == value.doubleValue()) {
					valid = true;
					break;
				}

			}
		} else
			valid = true;

		if (!valid) {
			String messageError = "The value is not valid";
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(messageError).addConstraintViolation();
		}
		return valid;
	}

}
