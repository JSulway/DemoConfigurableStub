package com.demo.stub.errors;

import com.demo.stub.exception.CommonExceptionType;
import com.demo.stub.exception.ErrorLogMessageProvider;
import com.demo.stub.exception.ExceptionProviderInterface;

public enum DemoStubExceptionErrors implements ErrorLogMessageProvider, ExceptionProviderInterface {

	ERROR_LOADING_CONFIGURATION("MS:00001", "Exception loading configuration: {0}"),
	ERROR_ON_HANDSHAKE("MS:00002", "Exception on handshake: {0}"),
	ERROR_HTTP_SOCKET_FAILURE("MS:00003", "Exception creating socket on port");
	
	private String mCode;
	private String mLogMessage;
	private String mExceptionMessage;
	private CommonExceptionType mType = CommonExceptionType.BUSINESS;
	
	private DemoStubExceptionErrors(String pCode, String pMessage){
		mCode = pCode;
		mLogMessage = pMessage;
		mExceptionMessage = pMessage;
	}
	
	private DemoStubExceptionErrors(String pCode, String pLogMessage, CommonExceptionType pType){
		mCode = pCode;
		mLogMessage = pLogMessage;
		mExceptionMessage = pLogMessage;
		mType = pType;
	} 
	
	private DemoStubExceptionErrors(String pCode, String pLogMessage, String pExceptionMessage){
		this(pCode, pLogMessage);
		mExceptionMessage = pExceptionMessage;
	}

	public String getErrorCode() {
		return mCode;
	}
	
	public String getErrorLogMessage() {
		return mLogMessage;
	}
	
	public String getExceptionMessage() {
		return mExceptionMessage;
	}

	public CommonExceptionType getExceptionType() {
		return mType;
	}

	

	
		
}
