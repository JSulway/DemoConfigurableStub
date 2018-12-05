package com.demo.stub.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Loads response configuration
 * 
 * @author SULWAYJO
 *
 */
public class DemoStubResponseConfigurationLoader {
	
	private List<Document> responseMessageList = new ArrayList<Document>();
	private List<String> responseMessageCaseList = new ArrayList<String>();
	private List<String> responseMessageMappingList = new ArrayList<String>();
	private Properties syncResponseMessageProps = new Properties();
	
	// Initialise sync defaults
	private String propSyncAckResponseMessage = "./messages/Sync.ACK.xml";
	private String propSyncErrResponseMessage = "NONE";
	private String propSyncSOAPFaultResponseMessage = "./messages/sync.SOAP.Fault.xml";
	private String propSyncStatusCodeOverride = "NONE";
	private int propServerSyncMinDelay = 1000;
	private int propServerSyncMaxDelay = 3000;
	private int propServerAsyncMinDelay = 1000;
	private int propServerAsyncMaxDelay = 3000;
	private int propServerInitialSyncToAsyncMessageDelay = 1000;
	
	// Synchronous response message property keys
	public static String SYNC_ACK_RESPONSE_MESSAGE = "SyncAckResponseMessage";
	public static String SYNC_ERROR_RESPONSE_MESSAGE = "SyncErrResponseMessage";
	public static String SYNC_SOAP_FAULT_RESPONSE_MESSAGE = "SyncSoapFaultResponseMessage";
	public static String SYNC_STATUS_CODE_OVERRIDE = "SyncStatusCodeOverride";
	
	// Delay property keys
	public static String SERVER_SYNC_MIN_DELAY = "ServerSyncMinDelay";
	public static String SERVER_SYNC_MAX_DELAY = "ServerSyncMaxDelay";
	public static String SERVER_ASYNC_MIN_DELAY = "ServerAsyncMinDelay";
	public static String SERVER_ASYNC_MAX_DELAY = "ServerAsyncMaxDelay";
	public static String SERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY = "ServerBetweenInitialSyncAndAnyAsyncDelays";
	
	public void loadResponseConfiguration() throws SAXException, ParserConfigurationException{
		
		Properties responseMessageProps = new Properties();
		Properties responseMessageMappingProps = new Properties();
		
		DocumentBuilderFactory factoryResponse = DocumentBuilderFactory.newInstance();
		factoryResponse.setNamespaceAware(true);
		DocumentBuilder builderResponse;
		builderResponse = factoryResponse.newDocumentBuilder();
		
		try{
			responseMessageProps.load(new FileInputStream("./config/responseMessages.properties"));
			responseMessageMappingProps.load(new FileInputStream("./config/responseMessageMapping.properties"));
			syncResponseMessageProps.load(new FileInputStream("./config/syncMessages.properties"));
			
			// obtain the responseMessages.properties the ensure order is upheld
			List<String> orderedPropertyKeyList = new ArrayList<String>();
			for(String key: responseMessageProps.stringPropertyNames()){
				orderedPropertyKeyList.add(key);
			}
			Collections.sort(orderedPropertyKeyList);
			
			// now using the ordered keys load the properties
			for(String key: orderedPropertyKeyList){
				// loads the response messages and cases from responseMessages.properties
				String responseFileAndCases = (String) responseMessageProps.getProperty(key);
				int indexOfFirstSemiColon = responseFileAndCases.indexOf(";");
				
				String message = responseFileAndCases.substring(0, indexOfFirstSemiColon);
				Document respDoc = builderResponse.parse(message);
				responseMessageList.add(respDoc);				
				responseMessageCaseList.add(responseFileAndCases.substring(indexOfFirstSemiColon+1));
				
			}
			
			for(String key : responseMessageMappingProps.stringPropertyNames()){
				// load the request to response mapping rules (responseMapping.properties)
				responseMessageMappingList.add((String) responseMessageMappingProps.getProperty(key));
			}
		}catch (IOException ex){
			ex.printStackTrace();
		}
	}

	public List<Document> getResponseMessageList() {
		return responseMessageList;
	}

	public void setResponseMessageList(List<Document> responseMessageList) {
		this.responseMessageList = responseMessageList;
	}

	public List<String> getResponseMessageCaseList() {
		return responseMessageCaseList;
	}

	public void setResponseMessageCaseList(List<String> responseMessageCaseList) {
		this.responseMessageCaseList = responseMessageCaseList;
	}

	public List<String> getResponseMessageMappingList() {
		return responseMessageMappingList;
	}

	public void setResponseMessageMappingList(List<String> responseMessageMappingList) {
		this.responseMessageMappingList = responseMessageMappingList;
	}

	public Properties getSyncResponseMessageProps() {
		return syncResponseMessageProps;
	}

	public void setSyncResponseMessageProps(Properties syncResponseMessageProps) {
		this.syncResponseMessageProps = syncResponseMessageProps;
	}

	public String getPropSyncAckResponseMessage() {
		return propSyncAckResponseMessage;
	}

	public void setPropSyncAckResponseMessage(String propSyncAckResponseMessage) {
		this.propSyncAckResponseMessage = propSyncAckResponseMessage;
	}

	public String getPropSyncErrResponseMessage() {
		return propSyncErrResponseMessage;
	}

	public void setPropSyncErrResponseMessage(String propSyncErrResponseMessage) {
		this.propSyncErrResponseMessage = propSyncErrResponseMessage;
	}

	public String getPropSyncSOAPFaultResponseMessage() {
		return propSyncSOAPFaultResponseMessage;
	}

	public void setPropSyncSOAPFaultResponseMessage(String propSyncSOAPFaultResponseMessage) {
		this.propSyncSOAPFaultResponseMessage = propSyncSOAPFaultResponseMessage;
	}

	public String getPropSyncStatusCodeOverride() {
		return propSyncStatusCodeOverride;
	}

	public void setPropSyncStatusCodeOverride(String propSyncStatusCodeOverride) {
		this.propSyncStatusCodeOverride = propSyncStatusCodeOverride;
	}

	public int getPropServerSyncMinDelay() {
		return propServerSyncMinDelay;
	}

	public void setPropServerSyncMinDelay(int propServerSyncMinDelay) {
		this.propServerSyncMinDelay = propServerSyncMinDelay;
	}

	public int getPropServerSyncMaxDelay() {
		return propServerSyncMaxDelay;
	}

	public void setPropServerSyncMaxDelay(int propServerSyncMaxDelay) {
		this.propServerSyncMaxDelay = propServerSyncMaxDelay;
	}

	public int getPropServerAsyncMinDelay() {
		return propServerAsyncMinDelay;
	}

	public void setPropServerAsyncMinDelay(int propServerAsyncMinDelay) {
		this.propServerAsyncMinDelay = propServerAsyncMinDelay;
	}

	public int getPropServerAsyncMaxDelay() {
		return propServerAsyncMaxDelay;
	}

	public void setPropServerAsyncMaxDelay(int properServerAsyncMaxDelay) {
		this.propServerAsyncMaxDelay = properServerAsyncMaxDelay;
	}

	public int getPropServerInitialSyncToAsyncMessageDelay() {
		return propServerInitialSyncToAsyncMessageDelay;
	}

	public void setPropServerInitialSyncToAsyncMessageDelay(int propServerInitialSyncToAsyncMessageDelay) {
		this.propServerInitialSyncToAsyncMessageDelay = propServerInitialSyncToAsyncMessageDelay;
	}

	public static String getSYNC_ACK_RESPONSE_MESSAGE() {
		return SYNC_ACK_RESPONSE_MESSAGE;
	}

	public static void setSYNC_ACK_RESPONSE_MESSAGE(String sYNC_ACK_RESPONSE_MESSAGE) {
		SYNC_ACK_RESPONSE_MESSAGE = sYNC_ACK_RESPONSE_MESSAGE;
	}

	public static String getSYNC_ERROR_RESPONSE_MESSAGE() {
		return SYNC_ERROR_RESPONSE_MESSAGE;
	}

	public static void setSYNC_ERROR_RESPONSE_MESSAGE(String sYNC_ERROR_RESPONSE_MESSAGE) {
		SYNC_ERROR_RESPONSE_MESSAGE = sYNC_ERROR_RESPONSE_MESSAGE;
	}

	public static String getSYNC_SOAP_FAULT_RESPONSE_MESSAGE() {
		return SYNC_SOAP_FAULT_RESPONSE_MESSAGE;
	}

	public static void setSYNC_SOAP_FAULT_RESPONSE_MESSAGE(String sYNC_SOAP_FAULT_RESPONSE_MESSAGE) {
		SYNC_SOAP_FAULT_RESPONSE_MESSAGE = sYNC_SOAP_FAULT_RESPONSE_MESSAGE;
	}

	public static String getSYNC_STATUS_CODE_OVERRIDE() {
		return SYNC_STATUS_CODE_OVERRIDE;
	}

	public static void setSYNC_STATUS_CODE_OVERRIDE(String sYNC_STATUS_CODE_OVERRIDE) {
		SYNC_STATUS_CODE_OVERRIDE = sYNC_STATUS_CODE_OVERRIDE;
	}

	public static String getSERVER_SYNC_MIN_DELAY() {
		return SERVER_SYNC_MIN_DELAY;
	}

	public static void setSERVER_SYNC_MIN_DELAY(String sERVER_SYNC_MIN_DELAY) {
		SERVER_SYNC_MIN_DELAY = sERVER_SYNC_MIN_DELAY;
	}

	public static String getSERVER_SYNC_MAX_DELAY() {
		return SERVER_SYNC_MAX_DELAY;
	}

	public static void setSERVER_SYNC_MAX_DELAY(String sERVER_SYNC_MAX_DELAY) {
		SERVER_SYNC_MAX_DELAY = sERVER_SYNC_MAX_DELAY;
	}

	public static String getSERVER_ASYNC_MIN_DELAY() {
		return SERVER_ASYNC_MIN_DELAY;
	}

	public static void setSERVER_ASYNC_MIN_DELAY(String sERVER_ASYNC_MIN_DELAY) {
		SERVER_ASYNC_MIN_DELAY = sERVER_ASYNC_MIN_DELAY;
	}

	public static String getSERVER_ASYNC_MAX_DELAY() {
		return SERVER_ASYNC_MAX_DELAY;
	}

	public static void setSERVER_ASYNC_MAX_DELAY(String sERVER_ASYNC_MAX_DELAY) {
		SERVER_ASYNC_MAX_DELAY = sERVER_ASYNC_MAX_DELAY;
	}

	public static String getSERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY() {
		return SERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY;
	}

	public static void setSERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY(String sERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY) {
		SERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY = sERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY;
	}
}
