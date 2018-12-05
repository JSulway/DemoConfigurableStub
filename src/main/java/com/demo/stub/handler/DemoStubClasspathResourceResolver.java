package com.demo.stub.handler;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class DemoStubClasspathResourceResolver implements LSResourceResolver {
	
	protected static final String DOM_IMPLEMENTATION_LOAD_AND_SAVE_LEVEL3_IDENTIFIER = "LS";
	
	//300fm should be enough for each schema file
	private static final int SCHEMA_FILE_MAX_SIZE = 307200;
	
	private static final String XML_SCHEMA_RESOURCE_TYPE = "http://www.w3.org/2001/XMLSCHEMA";
	
	private static Logger logger = Logger.getLogger(DemoStubClasspathResourceResolver.class);
	
	protected static Properties namespaceMapping = new Properties();
	
	protected static DOMImplementationLS domImplLS;
	
	private static Map<String, byte[]> resources = Collections.synchronizedMap(new HashMap<String, byte[]>());
	
	static{
		try{
			InputStream input = new FileInputStream("./config/adapters.schemas.properties");
			DemoStubClasspathResourceResolver.namespaceMapping.load(input);
			System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMImplementationSourceImpl");
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DemoStubClasspathResourceResolver.domImplLS = (DOMImplementationLS) registry.getDOMImplementation(DOM_IMPLEMENTATION_LOAD_AND_SAVE_LEVEL3_IDENTIFIER);
		}catch(IOException exception){
			throw new IllegalStateException("Could not initialise ", exception);
		}catch(ClassCastException exception){
			throw new IllegalStateException("Could not initialise ", exception);
		}catch(ClassNotFoundException exception){
			throw new IllegalStateException("Could not initialise ", exception);
		}catch(InstantiationException exception){
			throw new IllegalStateException("Could not initialise ", exception);
		}catch(IllegalAccessException exception){
			throw new IllegalStateException("Could not initialise ", exception);
		}
	}
	
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		if(type != null){
			type = type.trim();
		}
		if(!XML_SCHEMA_RESOURCE_TYPE.equals(type)){
			logger.info("Expected and received type " + XML_SCHEMA_RESOURCE_TYPE + " , " + type);
			throw new IllegalArgumentException("This resolve only resolves resources of type " + XML_SCHEMA_RESOURCE_TYPE);
		}
		
		if(namespaceURI != null && !namespaceURI.isEmpty()){
			logger.info("getting system id from namespace " + namespaceURI);
			systemId = getMappedSystemId(namespaceURI, systemId);
		}
		else if(systemId == null || systemId.isEmpty()){
			logger.info("neither systeId nor namespace Id have been provided : " + (Object[])null);
		}
		
		
		LSInput input = null;
		try{
			input = createLSInput(systemId);
		}catch (IOException exception){
			logger.error("IOException thrown : " + exception.getMessage());
		}
		
		return input;
	}
	
	private LSInput createLSInput(String systemId) throws IOException{
		LSInput input = null;
		if(systemId != null){
			if(systemId.charAt(0) != '/'){
				systemId = '/' + systemId;
			}
			logger.info("looking for resource : " + systemId);
			InputStream stream = null;
			if(DemoStubClasspathResourceResolver.resources.containsKey(systemId)){
				stream = new ByteArrayInputStream(DemoStubClasspathResourceResolver.resources.get(systemId));
			}else{
				stream = loadResource(systemId);
			}
			input = DemoStubClasspathResourceResolver.domImplLS.createLSInput();
			input.setByteStream(stream);
			input.setSystemId(systemId);
		}
		return input;
	}
	
	private InputStream loadResource(String systemId) throws IOException {
		InputStream in = null;
		InputStream resourceInputStream = null;
		try{
			in = this.getClass().getResourceAsStream(systemId);
			byte[] resourceBytes = null;
			if(in!=null){
				logger.info("found resource " + systemId);
				byte[] bytes = new byte[SCHEMA_FILE_MAX_SIZE];
				int bytesRead = 0;
				int offset = 0;
				while(bytesRead != -1){
					bytesRead = in.read(bytes, offset, bytes.length - offset);
					if(bytesRead != -1){
						offset += bytesRead;
					}
				}
				resourceBytes = Arrays.copyOf(bytes,  offset);
				resourceInputStream = new ByteArrayInputStream(resourceBytes);
				DemoStubClasspathResourceResolver.resources.put(systemId,  resourceBytes);
			}else{
				logger.info("could not find resource " + systemId);
			}
		}finally{
			if(in != null){
				in.close();
			}
		}
		return resourceInputStream;
	}
	
	private String getMappedSystemId(String namespaceURI, String systemId){
		String proposedSystemId = DemoStubClasspathResourceResolver.namespaceMapping.getProperty(namespaceURI);
		logger.info("got proposed system ID " + proposedSystemId);
		if(systemId != null){
			String requiredResource = null;
			int index = systemId.lastIndexOf('/');
			if(index != -1){
				requiredResource = systemId.substring(index + 1);
			}else{
				requiredResource = systemId;
			}
			if(proposedSystemId != null && proposedSystemId.endsWith(requiredResource)){
				systemId = proposedSystemId;
				logger.info("Info provided matches proposed system id so using it : " + systemId);
			}else{
				StringBuilder builder = new StringBuilder();
				builder.append(namespaceURI);
				builder.append("_");
				builder.append(requiredResource);
				systemId = DemoStubClasspathResourceResolver.namespaceMapping.getProperty(builder.toString());
				logger.info("systemId provided does not match the proposed system id. Using " + systemId);
			}
		}else{
			systemId = proposedSystemId;
		}
		return systemId;
	}

}
