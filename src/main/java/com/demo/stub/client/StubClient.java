package com.demo.stub.client;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.demo.stub.loader.DemoStubPropertyLoader;

public class StubClient {
	
	private static Logger logger = Logger.getLogger(StubClient.class);
	
	URL url = null;
	String sResponse = null;
	String certPath = null;
	String certPassword = null;
	String certType = null;
	String keyFactory = null;
	int timeout = 90000;
	String ivUser = "";
	String ivGroups = "";
	String ivServer = "";
	String soapAction = "http://www.demo.com/stub";
	
	public void sendResponse(String threadId, String resp) throws MalformedURLException{
		logger.info("Entering stub client sendResponse...");
		
		sResponse = resp;
		url = new URL(DemoStubPropertyLoader.CLIENT_URL);
		certPath = DemoStubPropertyLoader.KEYSTOREFILE;
		certPassword = DemoStubPropertyLoader.KEYSTOREPASSWORD;
		certType = DemoStubPropertyLoader.KEYSTORETYPE;
		keyFactory = DemoStubPropertyLoader.TRUSTMANAGERFACTORY;
		
		String response[] = null;
		
		HttpCaller httpCaller = new HttpCaller();
		if(certPath != null && !certPath.isEmpty()){
			logger.info("[" + threadId + "] - " + "Sending secure response to URL: " + DemoStubPropertyLoader.CLIENT_URL);
			httpCaller.Connect(url, resp, certPassword, certType, keyFactory, timeout);
			response = httpCaller.secure_HTTP_Post(url, sResponse, soapAction, ivUser, ivGroups, ivServer, timeout, certPath, certPassword, certType, keyFactory);
			for(String str: response){
				logger.info("syc response: " + str);
			}
		}else{
			logger.info("[" + threadId + "] - " + "Sending response to URL: " + DemoStubPropertyLoader.CLIENT_URL);
			httpCaller.Connect(url, resp, certPassword, certType, keyFactory, timeout);
			response = httpCaller.HTTP_Post(url, sResponse, soapAction, ivUser, ivGroups, ivServer, timeout);
			for(String str: response){
				logger.info("syc response: " + str);
			}
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		StubClient.logger = logger;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getsResponse() {
		return sResponse;
	}

	public void setsResponse(String sResponse) {
		this.sResponse = sResponse;
	}

	public String getCertPath() {
		return certPath;
	}

	public void setCertPath(String certPath) {
		this.certPath = certPath;
	}

	public String getCertPassword() {
		return certPassword;
	}

	public void setCertPassword(String certPassword) {
		this.certPassword = certPassword;
	}

	public String getCertType() {
		return certType;
	}

	public void setCertType(String certType) {
		this.certType = certType;
	}

	public String getKeyFactory() {
		return keyFactory;
	}

	public void setKeyFactory(String keyFactory) {
		this.keyFactory = keyFactory;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getIvUser() {
		return ivUser;
	}

	public void setIvUser(String ivUser) {
		this.ivUser = ivUser;
	}

	public String getIvGroups() {
		return ivGroups;
	}

	public void setIvGroups(String ivGroups) {
		this.ivGroups = ivGroups;
	}

	public String getIvServer() {
		return ivServer;
	}

	public void setIvServer(String ivServer) {
		this.ivServer = ivServer;
	}

	public String getSoapAction() {
		return soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}
}
