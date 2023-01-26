package com.bld.commons.utils.validator;

import javax.validation.ConstraintValidatorContext;

import com.bld.commons.utils.validator.annotations.AllowedNumber;

/**
 * The Class AllowedNumberValidator.
 */
public class AllowedNumberValidator  extends AllowedValueValidator<AllowedNumber,Number>{

	/** The valid number values. */
	private AllowedNumber validNumberValues;

	/**
	 * Initialize.
	 *
	 * @param validNumberValues the valid number values
	 */
	@Override
	public void initialize(AllowedNumber validNumberValues) {
		this.validNumberValues = validNumberValues;
	}

	/**
	 * Checks if is valid.
	 *
	 * @param value the value
	 * @param context the context
	 * @return true, if is valid
	 */
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
