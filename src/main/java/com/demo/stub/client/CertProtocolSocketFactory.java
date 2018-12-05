package com.demo.stub.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

public class CertProtocolSocketFactory implements SecureProtocolSocketFactory {

	SSLContext sslContext;
	
	public CertProtocolSocketFactory(){
		super();
	}
	
	public CertProtocolSocketFactory(String keyFile, String password, String type, String factory) throws Exception {
		try{
			//Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider)
			if(type == null){
				type = "PKCS12";
			}
			KeyStore ks = KeyStore.getInstance(type); // was in plugin PKCS12
			ks.load(new FileInputStream(keyFile),password.toCharArray());
			
			if(factory == null){
				factory = "IbmX509";
			}
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(factory); // was SunX509 in webservices plugin
			kmf.init(ks,  password.toCharArray());
			
			SSLContext ctx = SSLContext.getInstance("HTTP");
			ctx.init(kmf.getKeyManagers(), null, null);
			
			this.sslContext = ctx;
		}catch(NoSuchAlgorithmException algex){
			throw new Exception(algex.getMessage());
		}catch(KeyStoreException ksex){
			throw new Exception(ksex.getMessage());
		}catch(IOException ioex){
			throw new Exception(ioex.getMessage());
		}catch(CertificateException certex){
			throw new Exception(certex.getMessage());
		}catch(UnrecoverableKeyException unreckeyex){
			throw new Exception(unreckeyex.getMessage());
		}catch(KeyManagementException kmex){
			throw new Exception(kmex.getMessage());
		}
	}
	
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
			throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(host, port, clientHost, clientPort);
	}

	public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort, HttpConnectionParams params)
			throws IOException, UnknownHostException, ConnectTimeoutException {
		return sslContext.getSocketFactory().createSocket(host, port, clientHost, clientPort);
	}

	public Socket createSocket(Socket host, String port, int clientHost, boolean clientPort)
			throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(host, port, clientHost, clientPort);
	}

}
