package com.bld.commons.utils.validator;

import javax.validation.ConstraintValidatorContext;

import com.bld.commons.utils.validator.annotations.AllowedString;

/**
 * The Class AllowedStringValidator.
 */
public class AllowedStringValidator extends AllowedValueValidator<AllowedString, String> {

	/** The allowed string. */
	protected AllowedString allowedString;

	/**
	 * Initialize.
	 *
	 * @param allowedString the allowed string
	 */
	@Override
	public void initialize(AllowedString allowedString) {
		this.allowedString = allowedString;
	}

	/**
	 * Checks if is valid.
	 *
	 * @param value the value
	 * @param context the context
	 * @return true, if is valid
	 */
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

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	@Override
	protected String getMessage() {
		return "The value is not valid";
	}

}
