package com.demo.stub.exception;

public interface ErrorLogMessageProvider {

	/**
	 * Returns the error code associated with the error event
	 * 
	 * @return
	 */
	public String getErrorCode();
	
	/**
	 * Returns the message to be logged for this error event. The message can
	 * contain placeholders ({n}) as for any other loggable message
	 * 
	 * @return
	 */
	public String getErrorLogMessage();
	
}
