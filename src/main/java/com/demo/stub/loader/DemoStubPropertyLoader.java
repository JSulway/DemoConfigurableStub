package com.demo.stub.loader;

import java.io.FileInputStream;
import java.util.Properties;

import com.demo.stub.exception.ConfigurationException;

/**
 * These properties are loaded when the MockHoneStub is started with the values made available
 * statically from the class. Changes to the configuration file values will mean the stub will need 
 * to be restarted before they take effect.
 * @author SULWAYJO
 *
 */
public class DemoStubPropertyLoader {

	// Mandatory - required as a minimum within the demoStubStartup.properties
	public static int SERVER_PORT = 0;
	public static String CLIENT_URL = null;
	public static String KEYSTORETYPE = null;
	public static String KEYSTOREFILE = null;
	public static String KEYSTOREPASSWORD = null;
	
	// Defaults provided on these - Overrides can be provided within the demoStubStartup.properties
	public static int SERVER_SOCKET_TIMEOUT = 5000;
	public static String TRUSTMANAGERFACTORY = "SunX509";
	public static String PROTOCOL = "HTTP";
	
	public static void loadProperties() throws Exception{
		Properties props = new Properties();
		
		try{
			props.load(new FileInputStream("./config/demoStubStartup.properties"));
			
			//Mandatory properties expected to be specified in the demoStubStartup.properties
			SERVER_PORT = Integer.parseInt(props.getProperty("ServerPort"));
			CLIENT_URL = props.getProperty("ClientURL");
			KEYSTORETYPE = props.getProperty("KeyStoreType");
			KEYSTOREFILE = props.getProperty("KeyStoreFile");
			KEYSTOREPASSWORD = props.getProperty("KeyStorePassword");
		} catch (Exception e){
			throw new ConfigurationException("A mandatory property has not been specified pr is invalid in the demoStubStartup.properties file");
		}
		
		try{
			// Properties that can be specified in the startup properties
			getServerSocketTimeout(props);
			getTrustManagerFactory(props);
			getProtocol(props);
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static String getConfigurationAsString(){
		StringBuilder config = new StringBuilder();
		config.append("\n\n");
		config.append("########################");
		config.append("\n");
		config.append("Loading demoStubStartup.properties ");
		config.append("\n");
		config.append("(./config/demoStubStartup.properties)");
		config.append("\n");
		config.append("########################");
		config.append("\n");
		config.append("ServerPort = " + SERVER_PORT + "\n");
		config.append("ClientURL = " + CLIENT_URL + "\n");
		config.append("KeyStoreType = " + KEYSTORETYPE + "\n");
		config.append("KeyStoreFile = " + KEYSTOREFILE + "\n");
		config.append("KeyStorePassword = " + KEYSTOREPASSWORD + "\n");
		config.append("\n");
		
		return config.toString();		
	}
	
	private static void getTrustManagerFactory(Properties props){
		try{
			String value = props.getProperty("TrustManagerFactory");
			if(value != null){
				TRUSTMANAGERFACTORY = value;
			}
		}catch (Exception e){
			// if none provided or an exception occirs then use default
		}
	}
	
	private static void getServerSocketTimeout(Properties props){
		try{
			int value = Integer.parseInt(props.getProperty("ServerSocketTimeout"));
			SERVER_SOCKET_TIMEOUT = value;
		}catch (Exception e){
			// if none provided or an exception occirs then use default
		}
	}
	
	private static void getProtocol(Properties props){
		try{
			String value = props.getProperty("Protocol");
			if(value != null){
				PROTOCOL = value;
			}
		}catch (Exception e){
			// if none provided or an exception occirs then use default
		}
	}
	
	
}
