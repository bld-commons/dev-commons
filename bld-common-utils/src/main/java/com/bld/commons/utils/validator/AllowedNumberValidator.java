package com.bld.commons.utils.validator;

import javax.validation.ConstraintValidatorContext;

import com.bld.commons.utils.validator.annotations.AllowedNumber;

// TODO: Auto-generated Javadoc
/**
 * The Class AllowedNumberValidator.
 */
public class AllowedNumberValidator extends AllowedValueValidator<AllowedNumber, Number> {

	/** The allowed number. */
	private AllowedNumber allowedNumber;

	/**
	 * Initialize.
	 *
	 * @param allowedNumber the allowed number
	 */
	@Override
	public void initialize(AllowedNumber allowedNumber) {
		this.allowedNumber = allowedNumber;
	}

	/**
	 * Checks if is valid.
	 *
	 * @param value   the value
	 * @param context the context
	 * @return true, if is valid
	 */
	@Override
	public boolean isValid(Number value, ConstraintValidatorContext context) {
		boolean valid = false;
		if (value != null) {
			for (Double checkValue : this.allowedNumber.value()) {
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
		String[] values = new String[this.allowedNumber.value().length];
		for (int i = 0; i < values.length; i++)
			values[i] = String.valueOf(this.allowedNumber.value()[i]);
		String message = this.allowedNumber.message();
		if (DEFAULT_MESSAGE.equals(message))
			message += " The values allowed are: " + String.join(",", values);
		return message;
	}

}
