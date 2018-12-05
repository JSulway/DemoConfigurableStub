package com.demo.stub.exception;

/**
 * CompatibilityException - facilitates handling of compatibility issues arising during request/response processing
 * 
 * @author SULWAYJO
 *
 */
public class CompatibilityException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public CompatibilityException (String message){
		super(message);
	}
	
	public CompatibilityException (Exception e){
		super(e);
	}
	
	public CompatibilityException(String message, Exception e){
		super(message, e);
	}
	
}
