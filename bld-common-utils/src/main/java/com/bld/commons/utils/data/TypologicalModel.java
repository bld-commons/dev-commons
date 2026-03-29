/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.model.TypologicalModel.java
 */
package com.bld.commons.utils.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Base model for typological (lookup/reference) entities that have both an identifier and a name.
 *
 * <p>Extends {@link BaseModel} by adding a mandatory {@code name} field, validated with
 * {@link jakarta.validation.constraints.NotBlank}. The {@link #equals(Object)} and
 * {@link #hashCode()} implementations consider both {@code id} and {@code name}.</p>
 *
 * @param <ID> the type of the entity identifier
 *
 * @author Francesco Baldi
 */
@SuppressWarnings("serial")
public class TypologicalModel<ID> extends BaseModel<ID> {

	/** The name. */
	@NotBlank
	private String name;

	/**
	 * Instantiates a new typological model.
	 */
	public TypologicalModel() {
		super();
	}

	
	/**
	 * Instantiates a new typological model.
	 *
	 * @param name the name
	 */
	public TypologicalModel(@NotBlank String name) {
		super();
		this.name = name;
	}


	/**
	 * Instantiates a new typological model.
	 *
	 * @param id the id
	 * @param name the name
	 */
	public TypologicalModel(ID id,@NotNull String name) {
		super(id);
		this.name = name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "Typological [name=" + name + ", toString()=" + super.toString() + "]";
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypologicalModel<?> other = (TypologicalModel<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
	
	
	
}
