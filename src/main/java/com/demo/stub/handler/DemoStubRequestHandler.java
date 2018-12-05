package com.demo.stub.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.demo.stub.context.DemoStubNamespaceContext;
import com.demo.stub.loader.DemoStubResponseConfigurationLoader;

public class DemoStubRequestHandler {

	private Logger logger = Logger.getLogger(DemoStubRequestHandler.class);
	
	private DemoStubResponseConfigurationLoader demoStubResponseConfigurationLoader;
	
	public DemoStubRequestHandler(DemoStubResponseConfigurationLoader demoStubResponseConfigurationLoader){
		this.demoStubResponseConfigurationLoader = demoStubResponseConfigurationLoader;
	}
	
	/**
	 * Builds a list of asynchronous responses to be returned to the client based
	 * on the configuration provided and the request message received
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws UnsupportedEncodingException 
	 * @throws TransformerException 
	 */
	public List<String> getAsyncResponses(String threadId, String sRequest) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, TransformerException{
		logger.debug("Entering getAsyncResponses...");
		
		List<String> asyncResponses = new ArrayList<String>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		builder = factory.newDocumentBuilder();
		doc = builder.parse(new InputSource(new ByteArrayInputStream(sRequest.getBytes("utf-8"))));
		
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(new DemoStubNamespaceContext());
		
		int respNumber = 0;
		for(String messageCase : demoStubResponseConfigurationLoader.getResponseMessageCaseList() ){
			String[] caseRules = messageCase.split(";");
			int numberOfRulesForCase = caseRules.length;
			int countOfRulesMetForCase = 0;
			for(String rule : caseRules){
				try{
					if((Boolean)xpath.evaluate(rule, doc, XPathConstants.BOOLEAN)){
						countOfRulesMetForCase++;
					}
				}catch(Exception e){
					// the element specified in the rules was not present. just ignore and move onto the next
				}
			}
			
			// if all rules are met for the case then add a response to the list of async responses to be returned
			if(countOfRulesMetForCase == numberOfRulesForCase){
				Document docResponse = null;
				docResponse = demoStubResponseConfigurationLoader.getResponseMessageList().get(respNumber);
				String mappedResponse = mapRequestElementsToResponse(docResponse, xpath, threadId, doc);
				asyncResponses.add(mappedResponse);
			}
			respNumber++;
		}
		
		logger.debug("Exiting getAsyncResponses...");
		return null;
	}
	
	
	public String getSyncAckResponse(String threadId, String sRequest, String syncAckResp) throws ParserConfigurationException,
		SAXException, IOException, XPathExpressionException, TransformerException{
		logger.debug("entering getSyncAckResponse...");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		builder = factory.newDocumentBuilder();
		doc = builder.parse(new InputSource(new ByteArrayInputStream(sRequest.getBytes("utf-8"))));
		
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(new DemoStubNamespaceContext());
		
		Document respDoc = builder.parse(syncAckResp);
		String mappedResponse = mapRequestElementsToResponse(respDoc, xpath, threadId, doc);
		
		
		logger.debug("exiting getSyncAckResponse...");
		return mappedResponse;
	}
	
	public String getSyncErrResponse(String threadId, String sRequest, String errmsg) throws ParserConfigurationException,
		SAXException, IOException, XPathExpressionException, TransformerException{
		logger.debug("entering getSyncErrResponse...");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		builder = factory.newDocumentBuilder();
		doc = builder.parse(new InputSource(new ByteArrayInputStream(sRequest.getBytes("utf-8"))));
		
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		xpath.setNamespaceContext(new DemoStubNamespaceContext());
		
		Document respDoc = builder.parse(errmsg);
		String mappedResponse = mapRequestElementsToResponse(respDoc, xpath, threadId, doc);
		
		
		logger.debug("exiting getSyncErrResponse...");
		return mappedResponse;
	}
	
	public String getSyncSoapFaultResponse(String errMessage, String faultTemplate) throws ParserConfigurationException,
		SAXException, IOException, XPathExpressionException, TransformerException{
		logger.debug("entering getSyncSoapFaultResponse...");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document respDoc = builder.parse(faultTemplate);
		StringWriter sw = new StringWriter();
		StreamResult srResult = new StreamResult(sw);
		DOMSource source = new DOMSource(respDoc);
		
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty(OutputKeys.INDENT, "no");
		trans.transform(source, srResult);
		String xmlString = sw.toString();
		
		xmlString = xmlString.replaceAll("CHANGE_ME", errMessage);
		
		logger.debug("exiting getSyncSoapFaultResponse...");
		return xmlString;
	}
	
	private String mapRequestElementsToResponse(Document docResponse, XPath xpath, String threadId, Document doc) throws TransformerException{
		logger.debug("entering mapRequestElementsToResponse...");
		
		String xmlString = null;
		
		for(String mapping : demoStubResponseConfigurationLoader.getResponseMessageMappingList()){
			String[] requestXpathAndResponseXpath = mapping.split("#");
			String valueOfRequestElementToBeMapped;
			try{
				valueOfRequestElementToBeMapped = ((NodeList) xpath.evaluate(requestXpathAndResponseXpath[0], doc, XPathConstants.NODE)).item(0).getTextContent();
				((NodeList) xpath.evaluate(requestXpathAndResponseXpath[1], docResponse, XPathConstants.NODESET)).item(0).setTextContent(valueOfRequestElementToBeMapped);
			}catch(Exception e){
				// the element specified was not present in the response so just continue
			}
		}
		
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty(OutputKeys.INDENT, "no");
		
		StringWriter sw = new StringWriter();
		StreamResult srResult = new StreamResult(sw);
		DOMSource source = new DOMSource(docResponse);
		
		trans.transform(source, srResult);
		xmlString = sw.toString();
		
		logger.debug("exiting mapRequestElementsToResponse...");
		return xmlString;
	}
	
	
}
