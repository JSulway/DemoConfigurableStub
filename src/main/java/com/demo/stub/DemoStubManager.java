package com.demo.stub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.naming.ConfigurationException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.demo.stub.errors.DemoStubExceptionErrors;
import com.demo.stub.loader.DemoStubPropertyLoader;



/**
 * DemoStubManager is responsible for listening on a server socket and on accepting the message creating a new thread
 * to handle it. Each thread will refresh and load the messages resources so that configuration
 * can be updated without having to restart the stub.
 * 
 * @author SULWAYJO
 *
 */
public class DemoStubManager {

	private static final Logger logger = Logger.getLogger(DemoStubManager.class);
	
	public static void main(String[] args) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, CertificateException, FileNotFoundException, IOException{
		
		logger.info("Server starting...");
		
		
		boolean listening = true;
		int id = 0;
		
		try {
			DemoStubPropertyLoader.loadProperties();
		} catch (ConfigurationException conex){
			logger.error("Error loading mandatory startup configuration: " + conex.getMessage());
			System.exit(-1);
		} catch (Exception ex){
			logger.error("Error loading startup configuration: " + ex.getMessage());
		}
		logger.info(DemoStubPropertyLoader.getConfigurationAsString());
		
		if(!DemoStubPropertyLoader.PROTOCOL.equalsIgnoreCase("HTTP")){
			
			// SSL/TLS Requests this way			
			SSLServerSocket serverSocket = null;
			SSLServerSocketFactory sf = null;
			
			try{
				sf = getSSLContext().getServerSocketFactory();
			} catch (NoSuchAlgorithmException nsa){
				logger.error("Exception whilst attempting to obtain SSLServerSocketFactory : " + nsa.getMessage());
				System.exit(-1);
			}
			
			try{
				serverSocket = (SSLServerSocket) sf.createServerSocket(DemoStubPropertyLoader.SERVER_PORT);
			} catch (Exception e){
				logger.error("Could not listen on port : " + DemoStubPropertyLoader.SERVER_PORT);
				System.exit(-1);
			}
			logger.info("Server listening on port " + DemoStubPropertyLoader.SERVER_PORT);
			
			while(listening){
				if(id==9999){id=1;}else{id++;}
				try{
					new DemoStubThread(String.format("%04d", id),(SSLSocket) serverSocket.accept(), sf.getSupportedCipherSuites()).start();
				}catch (javax.net.ssl.SSLHandshakeException e){
					logger.error(DemoStubExceptionErrors.ERROR_ON_HANDSHAKE, e);
				}catch (SSLException sslEx){
					logger.error(DemoStubExceptionErrors.ERROR_ON_HANDSHAKE, sslEx);
				}
			}
			serverSocket.close();
		
		}else{
			// HTTP Requests this way
			ServerSocket serverSocket = null;
			try{
				serverSocket = new ServerSocket(DemoStubPropertyLoader.SERVER_PORT);
			}catch (IOException ioex){
				logger.error(DemoStubExceptionErrors.ERROR_HTTP_SOCKET_FAILURE, ioex);
			}
			
			logger.info("Server listening on port " + DemoStubPropertyLoader.SERVER_PORT);
			while(listening){
				if(id==9999){id=1;}else{id++;}
				new DemoStubThread(String.format("%04d", id),(Socket) serverSocket.accept()).start();
			}
			serverSocket.close();
		}	
	}
	
	private static SSLContext getSSLContext() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException{
		// Create keystore instance and load keystore file with password
		KeyStore keystore = KeyStore.getInstance(DemoStubPropertyLoader.KEYSTORETYPE);
		keystore.load(new FileInputStream(DemoStubPropertyLoader.KEYSTOREFILE), DemoStubPropertyLoader.KEYSTOREPASSWORD.toCharArray());
		
		// Create key manager factory and initialise with the SunX509 algorithm
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(DemoStubPropertyLoader.TRUSTMANAGERFACTORY);
		kmf.init(keystore, DemoStubPropertyLoader.KEYSTOREPASSWORD.toCharArray());
		
		// Create trust manager factory and initialise with the SunX509 algorithm
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(DemoStubPropertyLoader.TRUSTMANAGERFACTORY);
		tmf.init(keystore);
		
		// Create ssl context
		SSLContext sslContext = SSLContext.getInstance(DemoStubPropertyLoader.PROTOCOL);
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		
		// Get the SSLServerSocketFactory
		sslContext.getServerSocketFactory();
	
		return sslContext;
	}
}
