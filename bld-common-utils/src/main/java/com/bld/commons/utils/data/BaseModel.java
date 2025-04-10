/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.model.BaseModel.java
 */
package com.bld.commons.utils.data;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class BaseModel.
 *
 * @param <ID> the generic type
 */
@SuppressWarnings("serial")
public class BaseModel<ID> implements Serializable{

	/** The id. */
	private ID id;

	/**
	 * Instantiates a new base model.
	 */
	public BaseModel() {
		super();
	}

	/**
	 * Instantiates a new base model.
	 *
	 * @param id the id
	 */
	public BaseModel(ID id) {
		super();
		this.id = id;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(ID id) {
		this.id = id;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseModel<?> other = (BaseModel<?>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "BasicModel [id=" + id + "]";
	}
	
	

}
