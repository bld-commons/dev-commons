/*
 * @auth Francesco Baldi
 * @class com.bld.commons.utils.data.CollectionResponse.java
 */
package com.bld.commons.utils.data;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * The Class CollectionResponse.
 *
 * @param <T> the generic type
 */
@SuppressWarnings("serial")
public class CollectionResponse<T> extends ObjectResponse<Collection<T>> {

	/** The num results. */
	private Long totalCount;

	/** The page size. */
	private Integer pageSize;

	/** The page number. */
	private Integer pageNumber;

	/**
	 * Instantiates a new collection response.
	 */
	public CollectionResponse() {
		super(new ArrayList<>());
	}

	/**
	 * Instantiates a new collection response.
	 *
	 * @param data the data
	 */
	public CollectionResponse(Collection<T> data) {
		super(data);
	}
	
	

	/**
	 * Instantiates a new collection response.
	 *
	 * @param data the data
	 * @param totalCount the total count
	 * @param pageSize the page size
	 * @param pageNumber the page number
	 */
	public CollectionResponse(Collection<T> data, Long totalCount, Integer pageSize, Integer pageNumber) {
		super(data);
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}

	/**
	 * Gets the total count.
	 *
	 * @return the total count
	 */
	public Long getTotalCount() {
		return totalCount;
	}

	/**
	 * Sets the total count.
	 *
	 * @param totalCount the new total count
	 */
	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * Gets the page size.
	 *
	 * @return the page size
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * Sets the page size.
	 *
	 * @param pageSize the new page size
	 */
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * Gets the page number.
	 *
	 * @return the page number
	 */
	public Integer getPageNumber() {
		return pageNumber;
	}

	/**
	 * Sets the page number.
	 *
	 * @param pageNumber the new page number
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	

	/**
	 * Gets the next page number.
	 *
	 * @return the next page number
	 */
	@JsonProperty(value = "nextPageNumber", access = Access.READ_ONLY)
	public Integer getNextPageNumber() {
		Integer nextPageNumber = this.pageNumber;
		if (nextPageNumber != null)
			nextPageNumber++;
		return nextPageNumber;
	}

}
