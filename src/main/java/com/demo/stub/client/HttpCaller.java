package com.demo.stub.client;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

public class HttpCaller {
	
	private static final int  DEFAULT_TIMEOUT = 120000; // milliseconds
	private static final int DEFAULT_SSH_PORT = 443;
	private static final String HTTP_STATUS_FAIL = "400";
	private static final String HTTP_STATUS_FAIL_TEXT = "A connection problem has occurred";
	
	private boolean useConnect = false;
	HttpClient HClient = null;
	HostConfiguration HHost = null;
	
	public HttpCaller(){
		super();
	}
	
	// Make an http request where parameters are passed as data
	public String[] HTTP_Post(URL Url, String data, String soapAction, String ivUser, String ivGroups, String ivServer, int httpTimeout){
		
		String response[] = new String[3];
		
		try{
			if(!useConnect){
				HostConfiguration host = setupHostConfiguration( Url );
				HttpClient client = createHttpClient(host, httpTimeout);
				
				response = callPost(client, Url, soapAction, data, ivUser, ivGroups, ivServer);
			}else{
				response = callPost(HClient, Url, soapAction, data, ivUser, ivGroups, ivServer);
			}
		}catch(URIException uriex){
			response[0] = HTTP_STATUS_FAIL;
			response[1] = HTTP_STATUS_FAIL_TEXT;
			response[2] = uriex.toString();
		}catch(IOException ioex){
			response[0] = HTTP_STATUS_FAIL;
			response[1] = HTTP_STATUS_FAIL_TEXT;
			response[2] = ioex.toString();
		}
		
		return response;
	}
	
	public void Connect(URL url, String certPath, String certPW, String certType, String keyFactory, int httpTimeout){
		if(useConnect == true){
			return;
		}
		
		useConnect=true;
		try{
			HHost = setupHostConfiguration( url );
			HClient = createHttpClient( HHost, httpTimeout);
			
			if(certPath != null){
				URI uri = new URI(url.toString(),false);
				HHost = addCertificatePathAndPassword(certPath, certPW, certType, keyFactory, uri, HHost);
				HClient = createHttpClient( HHost, httpTimeout );
			}else{
				//
			}
		}catch(URIException uriex){
			uriex.printStackTrace();
		}catch(IOException ioex){
			ioex.printStackTrace();
		}catch (Exception ex){
			ex.printStackTrace();		}
	}
	
	public String[] secure_HTTP_Post(URL url, String data, String soapAction, String ivUser, String ivGroups, String ivServer, int httpTimeout, String certPath, String certPW, String certType, String keyFactory){
		
		String response[] = new String[3];
		
		try{
			if(useConnect){
				response = callPost(HClient, url, soapAction, data, ivUser, ivGroups, ivServer);
			}else{
				HostConfiguration host = null;
				HttpClient client = null;
				
				host = setupHostConfiguration( url );
				
				if(certPath != null){
					URI uri = new URI(url.toString(), false);
					host = addCertificatePathAndPassword(certPath, certPW, certType, keyFactory, uri, host);
					client = createHttpClient(host, httpTimeout);
				}else{
					client = createHttpClient(host, httpTimeout);
				}
				response = callPost(client, url, soapAction, data, ivUser, ivGroups, ivServer);
			}
		}catch(URIException uriex){
			response[0] = HTTP_STATUS_FAIL;
			response[1] = HTTP_STATUS_FAIL_TEXT;
			response[2] = uriex.toString();
		}catch(IOException ioex){
			response[0] = HTTP_STATUS_FAIL;
			response[1] = HTTP_STATUS_FAIL_TEXT;
			response[2] = ioex.toString();
		}catch(Exception ex){
			response[0] = HTTP_STATUS_FAIL;
			response[1] = HTTP_STATUS_FAIL_TEXT;
			response[2] = ex.toString();
		}
		
		return response;
	}
	
	@SuppressWarnings("deprecation")
	private String[] callPost(HttpClient client, URL url, String soapAction, String data, String ivUser, String ivGroups, String ivServer) throws IOException {
		URI uri = new URI(url.toString(), false);
		PostMethod post = new PostMethod(uri.getPathQuery());
		
		if(soapAction != null){
			post.setRequestHeader("Content-Type","application/soap+xml; charset=utf-8");
			post.setRequestHeader("SOAPAction", soapAction);
			// Note IVuser and IVgroups must have a hyphen and not an underscore as this is what webseal passes in the real world
			if (ivUser != null) post.setRequestHeader("iv-user", ivUser);
			if (ivGroups != null) post.setRequestHeader("iv-groups", ivGroups);
			if (ivServer != null) post.setRequestHeader("iv_server_name", ivServer);
		}else{
			post.setRequestHeader("Host", client.getHostConfiguration().getHost());
		}
		
		post.setRequestBody(data);
		String response[] = new String[3];
		
		try{
			client.executeMethod(post);
		}catch(HttpException he){
			response[0] = HTTP_STATUS_FAIL;
			response[1] = HTTP_STATUS_FAIL_TEXT;
			response[2] = he.toString();
		}
		
		response[0] = Integer.toString(post.getStatusCode());
		response[1] = post.getStatusText();
		response[2] = post.getResponseBodyAsString();
		
		return response;
	}
	
	private HostConfiguration setupHostConfiguration(URL url) throws URIException {
		URI uri = new URI(url.toString(), false);
		HostConfiguration host = new HostConfiguration();
		host.setHost(uri);
		return host;
	}
	
	@SuppressWarnings("deprecation")
	private HttpClient createHttpClient(HostConfiguration host, int httpTO) throws URIException {
		// generate the client for this request
		HttpClient client = new HttpClient();
		client.setHostConfiguration(host);
		if(httpTO > 0){
			client.setTimeout(httpTO);
		}else{
			client.setTimeout(DEFAULT_TIMEOUT);
		}
		return client;
	}
	
	private HostConfiguration addCertificatePathAndPassword( String certificatePath, String certificatePassword, String certType, String keyFactory, URI uri, HostConfiguration host) throws URIException, Exception {
		CertProtocolSocketFactory sslFactory = new CertProtocolSocketFactory(certificatePath, certificatePassword, certType, keyFactory);
		int port = uri.getPort() != -1 ? uri.getPort() : DEFAULT_SSH_PORT;
		@SuppressWarnings("deprecation")
		Protocol certHTTPS = new Protocol("https", sslFactory, port);
		
		host.setHost(uri.getHost(),uri.getPort(),certHTTPS);
		return host;
	}
}
