package com.demo.stub.exception;


public interface ExceptionProviderInterface {
	
	/**
	 * Returns error code associated with the excpetion
	 * @return
	 */
	public String getErrorCode();
	
	/**
	 * Returns the message to be logged for this exception event
	 * @return
	 */
	public String getExceptionMessage();
	
	/**
	 * Returns the type of this exception event
	 * 
	 * @return
	 */
	public CommonExceptionType getExceptionType();
}
