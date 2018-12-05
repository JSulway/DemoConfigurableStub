package com.demo.stub;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLSocket;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.HeaderGroup;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.demo.stub.client.StubClient;
import com.demo.stub.errors.DemoStubExceptionErrors;
import com.demo.stub.handler.DemoStubJAXBHandler;
import com.demo.stub.handler.DemoStubRequestHandler;
import com.demo.stub.loader.DemoStubPropertyLoader;
import com.demo.stub.loader.DemoStubResponseConfigurationLoader;


public class DemoStubThread extends Thread {

	private Logger logger = Logger.getLogger(DemoStubThread.class);
	
	private Socket socket = null;
	private String threadId = null;
	private HeaderGroup requestHeaders = new HeaderGroup();
	
	private DemoStubResponseConfigurationLoader demoStubResponseConfigurationLoader = new DemoStubResponseConfigurationLoader();
	
	/**
	 * Constructor for SSL/TLS requests
	 * 
	 * @param threadId
	 * @param socket
	 * @param supportedCipherSuites
	 * @throws IOException
	 */
	public DemoStubThread(String threadId, SSLSocket socket, String[] supportedCipherSuites) throws IOException {
		super("DemoStubThread - SSL/TLS");
		this.socket = socket;
		this.threadId = threadId;
		socket.setEnabledCipherSuites(supportedCipherSuites);
		
		socket.startHandshake();
		
		logger.info("Loading response messages, message case, mapping and transformation configuration");
		
		try{
			demoStubResponseConfigurationLoader.loadResponseConfiguration();
			// TODO: logMessageConfiguration();
		} catch (Exception e){
			logger.error(DemoStubExceptionErrors.ERROR_LOADING_CONFIGURATION, e);
		}
	}
	
	/**
	 * Constructor for HTTP requests
	 * 
	 * @param threadId
	 * @param socket
	 */
	public DemoStubThread(String threadId, Socket socket) {
		super("DemoStubThread - HTTP");
		this.socket = socket;
		this.threadId = threadId;
		
		try{
			demoStubResponseConfigurationLoader.loadResponseConfiguration();
			// TODO: logMessageConfiguration();
		} catch (Exception e){
			logger.error(DemoStubExceptionErrors.ERROR_LOADING_CONFIGURATION, e);
		}
	}

	public void run(){
		
		try{
			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			logger.info("SocketTimeout is " + DemoStubPropertyLoader.SERVER_SOCKET_TIMEOUT);
			socket.setSoTimeout(DemoStubPropertyLoader.SERVER_SOCKET_TIMEOUT);
			InputStream in = null;
			
			logger.info("[" + threadId + "] - Receiving Request");
			
			String s  = HttpParser.readLine(socket.getInputStream(),"US-ASCII");
			while((s != null) && (s.indexOf("HTTP") < 1)) {
				s = HttpParser.readLine(socket.getInputStream(), "US-ASCII");
			}
			
			Header[] headers = HttpParser.parseHeaders(socket.getInputStream(), "US-ASCII");
			requestHeaders.setHeaders(headers);
			int expectedLength = Integer.parseInt(requestHeaders.getFirstHeader("Content-Length").getValue());
			
			String sRequest = "";
			if(expectedLength > 0){
				in = new BufferedInputStream(socket.getInputStream(), expectedLength);
				int n = 0;
				int received = 0;
				int remain = expectedLength;
				logger.info("[" + threadId + "] - Bytes Expected " + expectedLength);

				while(remain > 0){
					byte cbuff[] = new byte[remain];
					n = in.read(cbuff);
					received = received + n;
					remain = remain - n;
					logger.debug("[" + threadId + "] - Received: " + received + " Remaining: " + remain);
					sRequest = sRequest + new String(cbuff, 0, n);
				}
				
				logger.info("[" + threadId + "] - Received Request");
				logger.info(sRequest);
				logger.info("Performing unmarshall and schema validation");
				boolean passedSchemaValidation = true;
				String errorMessage = null;
				try{
					logger.info("Starting unmarshalling of XML and schema validation...");
					DemoStubJAXBHandler.unmarshall(sRequest, true, logger);
					logger.info("finished unmarshalling of XML and schema validation...");
				}catch(Exception e){
					logger.info("Exception while unmarshalling : " + e.getMessage());
					passedSchemaValidation=false;
					errorMessage = e.getMessage();
				}
				
				DemoStubRequestHandler rh = new DemoStubRequestHandler(demoStubResponseConfigurationLoader);
				
				logger.info("Constructing asynchronous and synchronous responses");
				
				if(passedSchemaValidation){
					boolean isSyncErrSet=false;
					String errProperty = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SYNC_ERROR_RESPONSE_MESSAGE);
					if(errProperty != null){
						demoStubResponseConfigurationLoader.setPropSyncErrResponseMessage(errProperty);
					}
					if(!demoStubResponseConfigurationLoader.getPropSyncErrResponseMessage().equalsIgnoreCase("NONE")){
						isSyncErrSet = true;
					}
					
					if(!isSyncErrSet){
						// If the request is OK then get and return the synchronous ACK response message
						String ackproperty = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SYNC_ACK_RESPONSE_MESSAGE);
						if(ackproperty != null){
								demoStubResponseConfigurationLoader.setPropSyncAckResponseMessage(ackproperty);
						}
						String syncAckResponse = getSyncAckResponse(sRequest, rh, demoStubResponseConfigurationLoader.getPropSyncAckResponseMessage());
						if(syncAckResponse != null){
							logger.info("[" + threadId + "] - Returning sync reply");
							logger.info(syncAckResponse);
							String syncResp = syncAckResponse;
							out.write(syncResp.getBytes());
							out.flush();
						}else{
							throw new IOException("Exception: No Sync ACK response file obtained");
						}
						
						// pause between initial sync response and the async responses for the configured amount of time
						String syncToAsyncDelay = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SERVER_INITIALSYNC_TO_ASYNCMESSAGES_DELAY);
						if(syncToAsyncDelay != null){
							try{
								demoStubResponseConfigurationLoader.setPropServerInitialSyncToAsyncMessageDelay(Integer.parseInt(syncToAsyncDelay));
							}catch(Exception e){
								// parse integer failed. continue using default
							}
						}
						
						try{
							int delayTime = demoStubResponseConfigurationLoader.getPropServerInitialSyncToAsyncMessageDelay();
							logger.info("[" + threadId + "] - Initial sync to async messages delay for " + delayTime);
							Thread.sleep(delayTime);
						}catch(InterruptedException ex){
							ex.printStackTrace();
						}
						
						List<String> sResponseList = getAsyncResponseMessages(sRequest, rh);
						
						// Then send all configured asynchronous responses back to the client
						if(sResponseList != null && !sResponseList.isEmpty()){
							logger.info("Returning async responses");
							int delayTime = getAsyncDelayTime();
							for(String sResponse:sResponseList){
								logger.info("Returning async response " + sResponse);
								logger.info("[" + threadId + "] - Async delay for " + delayTime);
								try{
									Thread.sleep(delayTime);
								}catch(InterruptedException ex){
									ex.printStackTrace();
								}
								
								if(sResponse != null & sResponse != ""){
									StubClient dsc = new StubClient();
									dsc.sendResponse(threadId, sResponse);
								}
								
							}
						}
					}else{
						// If the synchronous error request is set then get and return it
						String syncErrResponse = getSyncErrResponse(sRequest, rh, demoStubResponseConfigurationLoader.getPropSyncErrResponseMessage());
						if(syncErrResponse != null){
							logger.info("[" + threadId + "] - Returning sync reply with http status code 200");
							logger.info(syncErrResponse);
							String syncResp = syncErrResponse;
							out.write(syncResp.getBytes());
							out.flush();
						}else{
							throw new IOException("Exception: No sync ERR response file obtained");
						}
					}
				}else{
					String faultProperty = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SYNC_SOAP_FAULT_RESPONSE_MESSAGE);
					if(faultProperty != null){
						demoStubResponseConfigurationLoader.setPropSyncErrResponseMessage(faultProperty);
					}
					
					// If the synchronous error request is set then get and return it
					String syncSoapFaultResponse = getSyncSoapFaultResponse(errorMessage, rh, demoStubResponseConfigurationLoader.getPropSyncErrResponseMessage());
					if(syncSoapFaultResponse != null){
						logger.info("[" + threadId + "] - Returning sync soap fault reply with http status code 422");
						logger.info(syncSoapFaultResponse);
						String syncResp = syncSoapFaultResponse;
						out.write(syncResp.getBytes());
						out.flush();
					}else{
						throw new IOException("Exception: No sync soap fault response file obtained");
					}
				}
			}else{
				logger.info("[" + threadId + "] - no content received");
			}
			
			logger.info("[" + threadId + "] - Request Processed");
			
		}catch (Exception e){
			logger.error("Exception " + e.getMessage());
		}
	}
	
	
	private List<String> getAsyncResponseMessages(String sRequest, DemoStubRequestHandler rh) throws IOException {
		List<String> asyncResponses = new ArrayList<String>();
		try{
			asyncResponses = rh.getAsyncResponses(threadId, sRequest);
		}catch (ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (TransformerException e){
			e.printStackTrace();
		}
		return asyncResponses;
	}
	
	private String getSyncAckResponse(String sRequest, DemoStubRequestHandler rh, String syncAckResp) throws IOException {
		String syncAckResponse = "";
		try{
			syncAckResponse = rh.getSyncAckResponse(threadId, sRequest, syncAckResp);
		}catch (XPathExpressionException e){
			e.printStackTrace();
		}catch (ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (TransformerException e){
			e.printStackTrace();
		}
		
		//200 is the default for synchronous ACK but override if one was provided
		String statusproperty = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SYNC_STATUS_CODE_OVERRIDE);
		if(statusproperty != null){
			demoStubResponseConfigurationLoader.setSYNC_STATUS_CODE_OVERRIDE(statusproperty);
		}
		
		String status = "200";
		if(demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride() != null && !demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride().equalsIgnoreCase("NONE")){
			logger.info("overriding status code to " + demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride());
			status = demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride();
		}
		
		if(syncAckResponse != null){
			StringBuffer pl = new StringBuffer();
			pl.append("HTTP/1.1 ");
			pl.append(status);
			pl.append(" \r\n");
			pl.append("Server: DemoStub \r\n");
			pl.append("Date: ");
			pl.append(timeStamp());
			pl.append("\r\n");
			pl.append("Cache-Control: private, max-age=0 \r\n");
			pl.append("Content-Type: application/soap+xml; charset=utf-8 \r\n");
			pl.append("Content-Length: " + syncAckResponse.length() + " \r\n\r\n");
			pl.append(syncAckResponse);
			syncAckResponse = pl.toString();
			
			int delayTime = getSyncDelayTime();
			
			logger.info("[" + threadId + "] - sync delayed for " + delayTime);
			try{
				Thread.sleep(delayTime);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		return syncAckResponse;
	}
	
	
	private String getSyncErrResponse(String sRequest, DemoStubRequestHandler rh, String errmsg) throws IOException {
		String syncErrResponse = "";
		try{
			syncErrResponse = rh.getSyncErrResponse(syncErrResponse, sRequest, errmsg);
		}catch (XPathExpressionException e){
			e.printStackTrace();
		}catch (ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (TransformerException e){
			e.printStackTrace();
		}
		
		//200 is the default for synchronous ACK but override if one was provided
		String statusproperty = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SYNC_STATUS_CODE_OVERRIDE);
		if(statusproperty != null){
			demoStubResponseConfigurationLoader.setSYNC_STATUS_CODE_OVERRIDE(statusproperty);
		}
		
		String status = "200";
		if(demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride() != null && !demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride().equalsIgnoreCase("NONE")){
			logger.info("overriding status code to " + demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride());
			status = demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride();
		}
		
		if(syncErrResponse != null){
			StringBuffer pl = new StringBuffer();
			pl.append("HTTP/1.1 ");
			pl.append(status);
			pl.append(" \r\n");
			pl.append("Server: DemoStub \r\n");
			pl.append("Date: ");
			pl.append(timeStamp());
			pl.append("\r\n");
			pl.append("Cache-Control: private, max-age=0 \r\n");
			pl.append("Content-Type: application/soap+xml; charset=utf-8 \r\n");
			pl.append("Content-Length: " + syncErrResponse.length() + " \r\n\r\n");
			pl.append(syncErrResponse);
			syncErrResponse = pl.toString();
			
			int delayTime = getSyncDelayTime();
			
			logger.info("[" + threadId + "] - sync delayed for " + delayTime);
			try{
				Thread.sleep(delayTime);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		return syncErrResponse;
	}
	
	private String getSyncSoapFaultResponse(String errorMessage, DemoStubRequestHandler rh, String faultTemplate) throws IOException {
		String syncSoapFaultResponse = "";
		try{
			syncSoapFaultResponse = rh.getSyncSoapFaultResponse(errorMessage, faultTemplate);
		}catch (XPathExpressionException e){
			e.printStackTrace();
		}catch (ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (TransformerException e){
			e.printStackTrace();
		}
		
		//200 is the default for synchronous ACK but override if one was provided
		String statusproperty = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SYNC_STATUS_CODE_OVERRIDE);
		if(statusproperty != null){
			demoStubResponseConfigurationLoader.setSYNC_STATUS_CODE_OVERRIDE(statusproperty);
		}
		
		String status = "200";
		if(demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride() != null && !demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride().equalsIgnoreCase("NONE")){
			logger.info("overriding status code to " + demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride());
			status = demoStubResponseConfigurationLoader.getPropSyncStatusCodeOverride();
		}
		
		if(syncSoapFaultResponse != null){
			StringBuffer pl = new StringBuffer();
			pl.append("HTTP/1.1 ");
			pl.append(status);
			pl.append(" \r\n");
			pl.append("Server: DemoStub \r\n");
			pl.append("Date: ");
			pl.append(timeStamp());
			pl.append("\r\n");
			pl.append("Cache-Control: private, max-age=0 \r\n");
			pl.append("Content-Type: application/soap+xml; charset=utf-8 \r\n");
			pl.append("Content-Length: " + syncSoapFaultResponse.length() + " \r\n\r\n");
			pl.append(syncSoapFaultResponse);
			syncSoapFaultResponse = pl.toString();
			
			int delayTime = getSyncDelayTime();
			
			logger.info("[" + threadId + "] - sync delayed for " + delayTime);
			try{
				Thread.sleep(delayTime);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		return syncSoapFaultResponse;
	}
	
	private int getSyncDelayTime(){
		String syncMinDelayProp = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SERVER_SYNC_MIN_DELAY);
		if(syncMinDelayProp != null){
			try{
				demoStubResponseConfigurationLoader.setPropServerSyncMinDelay(Integer.parseInt(syncMinDelayProp));
			}catch (Exception e){
				// continue using default
			}
		}
		String syncMaxDelayProp = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SERVER_SYNC_MAX_DELAY);
		if(syncMaxDelayProp != null){
			try{
				demoStubResponseConfigurationLoader.setPropServerSyncMaxDelay(Integer.parseInt(syncMaxDelayProp));
			}catch (Exception e){
				// continue using default
			}
		}
		int delayTime = 0;
		if(demoStubResponseConfigurationLoader.getPropServerSyncMaxDelay() > demoStubResponseConfigurationLoader.getPropServerSyncMinDelay()){
			delayTime = getRandom(demoStubResponseConfigurationLoader.getPropServerSyncMinDelay(), demoStubResponseConfigurationLoader.getPropServerSyncMaxDelay());
		}else{
			delayTime = demoStubResponseConfigurationLoader.getPropServerSyncMaxDelay();
		}
		return delayTime;
	}
	
	private int getAsyncDelayTime(){
		String asyncMinDelayProp = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SERVER_ASYNC_MIN_DELAY);
		if(asyncMinDelayProp != null){
			try{
				demoStubResponseConfigurationLoader.setPropServerAsyncMinDelay(Integer.parseInt(asyncMinDelayProp));
			}catch (Exception e){
				// continue using default
			}
		}
		String asyncMaxDelayProp = demoStubResponseConfigurationLoader.getSyncResponseMessageProps().getProperty(DemoStubResponseConfigurationLoader.SERVER_ASYNC_MAX_DELAY);
		if(asyncMaxDelayProp != null){
			try{
				demoStubResponseConfigurationLoader.setPropServerAsyncMaxDelay(Integer.parseInt(asyncMaxDelayProp));
			}catch (Exception e){
				// continue using default
			}
		}
		int delayTime = 0;
		if(demoStubResponseConfigurationLoader.getPropServerAsyncMaxDelay() > demoStubResponseConfigurationLoader.getPropServerAsyncMinDelay()){
			delayTime = getRandom(demoStubResponseConfigurationLoader.getPropServerAsyncMinDelay(), demoStubResponseConfigurationLoader.getPropServerAsyncMaxDelay());
		}else{
			delayTime = demoStubResponseConfigurationLoader.getPropServerAsyncMaxDelay();
		}
		return delayTime;
	}
	
	public String timeStamp(){
		Date now = new Date();
		SimpleDateFormat form = new SimpleDateFormat("dd-MMM-YYYY HH:mm");
		return form.format(now);
	}
	
	public int getRandom(int minNumber, int maxNumber){
		Random randomGenerator = new Random();
		int seed = maxNumber - minNumber;
		int randomInt = randomGenerator.nextInt(seed);
		return minNumber + randomInt;
	}
}
