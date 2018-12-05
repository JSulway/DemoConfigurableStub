package com.demo.stub.exception;

/**
 * ConfigurationException - On loading configuration should anything be missing or cause issue this ConfigurationException
 * should be thrown
 * 
 * @author SULWAYJO
 *
 */
public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ConfigurationException (String message){
		super(message);
	}
	
	public ConfigurationException (Exception e){
		super(e);
	}
	
	public ConfigurationException(String message, Exception e){
		super(message, e);
	}
	
}
