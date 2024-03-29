package com.bld.commons.utils.validator;

import com.bld.commons.utils.validator.annotations.AllowedString;

import jakarta.validation.ConstraintValidatorContext;

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
		String message=this.allowedString.message();
		if(DEFAULT_MESSAGE.equals(message))
			message+=" The values allowed are: "+String.join(",", this.allowedString.value());
		return message;
	}

}
