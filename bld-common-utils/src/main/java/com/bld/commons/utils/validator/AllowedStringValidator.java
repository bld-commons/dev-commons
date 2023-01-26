package com.bld.commons.utils.validator;

import javax.validation.ConstraintValidatorContext;

import com.bld.commons.utils.validator.annotations.AllowedString;

public class AllowedStringValidator extends AllowedValueValidator<AllowedString, String> {

	protected AllowedString allowedString;

	@Override
	public void initialize(AllowedString allowedString) {
		this.allowedString = allowedString;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		boolean valid = false;
		if (value != null) {
			for (String v : allowedString.value()) {
				if (v.equals(value)) {
					valid = true;
					break;
				}

			}
		} else
			valid = true;

		super.setContext(valid, context);
		return valid;
	}

	@Override
	protected String getMessage() {
		return "The value is not valid";
	}

}
