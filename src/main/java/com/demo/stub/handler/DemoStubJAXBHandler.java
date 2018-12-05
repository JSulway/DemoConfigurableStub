package com.demo.stub.handler;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.demo.stub.DemoStubManager;
import com.demo.stub.constants.DemoStubConstants;
import com.demo.stub.context.MessageContextFactoryImpl;
import com.demo.stub.exception.CompatibilityException;
import com.demo.stub.jaxb.DemoStubRequest;
import com.demo.stub.util.WrappedXMLStreamReader;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.bind.JAXBElement;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;

public class DemoStubJAXBHandler {
	
	private static final Logger logger = Logger.getLogger(DemoStubJAXBHandler.class);
	
	private static final Map<String, Schema> multipleSchemaMap = new LinkedHashMap<String, Schema>();
	
	private static final String HONOUR_ALL_SCHEMA_LOCATIONS_FEATURE_NAME = "http://apache.org/xml/features/honour-all-schemaLocations";
	
	public static JAXBBase unmarshall(final String xmlToUnmarshall, final Boolean validation, final Logger logger) throws Exception {
		logger.info("unmarshalling xml string into object");
		
		final javax.xml.stream.XMLInputFactory inputFactory = javax.xml.stream.XMLInputFactory.newInstance();
		final javax.xml.stream.XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(new BufferedReader(new StringReader(xmlToUnmarshall)));
		final WrappedXMLStreamReader wrappedReader = new WrappedXMLStreamReader(xmlStreamReader);
		JAXBBase unmarshall = unmarshall(JAXBContext.newInstance((new DemoStubRequest()).getClass()), validation, wrappedReader);
		
		logger.info("xml string unmarshalling complete");
		return unmarshall;
	}
	
	@SuppressWarnings("unchecked")
	private static <U extends JAXBBase> U unmarshall(final JAXBContext context,
			final Boolean validation, WrappedXMLStreamReader xmlReader) throws Exception{
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		if(validation){
			unmarshaller.setSchema(DemoStubJAXBHandler.createMultipleSchema(DemoStubConstants.SCHEMA_NAMES, logger));
		}
		
		U unmarshalledMessage = null;
		try{
			final Object unmarshalled = unmarshaller.unmarshal(xmlReader);
			if(unmarshalled instanceof JAXBElement){
				unmarshalledMessage = ((JAXBElement<U>) unmarshalled).getValue();
			}else{
				unmarshalledMessage = (U) unmarshalled;
			}
		}catch (final JAXBException jaxbe){
			final Throwable linkedException = jaxbe.getLinkedException();
			if(linkedException != null){
				throw new JAXBException(xmlReader.getLastParsedLocalName() + ":" + linkedException.getMessage());
			}else{
				throw new JAXBException(xmlReader.getLastParsedLocalName() + ":" + jaxbe.getMessage());
			}
		}catch (final RuntimeException rex){
			throw new CompatibilityException(rex.getMessage());
		}
		
		return unmarshalledMessage;
	}
	
	public static synchronized Schema createMultipleSchema(final List<String> schemaNames, final Logger logger) throws SAXException {
		logger.info("entering createMultipleSchema...");
		
		Schema multipleSchema = multipleSchemaMap.get(schemaNames.toString());
		if(multipleSchema == null){
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setResourceResolver(new DemoStubClasspathResourceResolver());
			schemaFactory.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_FEATURE_NAME, true);
			
			int numSources = 0;
			int schemaIndex = 0;
			
			if(schemaNames != null){
				numSources += schemaNames.size();
			}
			
			final Source[] multipleSources = new Source[numSources];
			
			Source schemaSource = null;
			
			if(schemaNames != null){
				logger.info("number of schemas " + schemaNames.size());
				for(String schemaName : schemaNames){
					logger.info("creating StreamSource for resource " + schemaName);
					schemaSource = new StreamSource(DemoStubJAXBHandler.class.getResourceAsStream(schemaName), schemaName);
					if(((StreamSource) schemaSource).getInputStream() != null){
						logger.info("Created StreamSource for Resource " + schemaSource.getSystemId());
					}
					multipleSources[schemaIndex++] = schemaSource;
				}
			}
			logger.info("created source " + multipleSources.length);
			multipleSchema = schemaFactory.newSchema(multipleSources);
			multipleSchemaMap.put(schemaNames.toString(), multipleSchema);
		}
		
		logger.info("exiting createMultipleSchema...");
		return multipleSchema;
	}
	
	public static JAXBContext getJAXBContext() throws JAXBException{
		return JAXBContext.newInstance(MessageContextFactoryImpl.DEMO_SCHEMA_PACKAGE);
	}
	
}
