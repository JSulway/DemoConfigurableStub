package com.demo.stub.exception;

/**
 * A set of exception types
 * 
 * @author SULWAYJO
 *
 */
public enum CommonExceptionType {
	BUSINESS("BUSINESS"),
	TECHNICAL("TECHNICAL");
	
	private String mExceptionType;
	
	CommonExceptionType(String exType){
		mExceptionType = exType;
	}
	
	public String getExceptionType(){
		return mExceptionType;
	}
}
