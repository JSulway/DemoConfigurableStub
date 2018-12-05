package com.demo.stub.client;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MessageResult {
	
	private static final String HTTP_STATUS_CODE = "HTTP_STATUS_CODE";
	private static final String HTTP_STATUS_TEXT = "HTTP_STATUS_TEXT";
	
	public static void writeResult(String messageFile, String statusCode, String statusText){
		try{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("resultInternal");
			doc.appendChild(rootElement);
			
			Element intAtts = doc.createElement("interfaceAttributes");
			rootElement.appendChild(intAtts);
			
			Element intAtt = doc.createElement("interfaceAttribute");
			intAtts.appendChild(intAtt);
			
			Element attId = doc.createElement("attributeId");
			intAtt.appendChild(attId);
			attId.setTextContent(HTTP_STATUS_CODE);
			
			Element attValue = doc.createElement("attributeValue");
			intAtt.appendChild(attValue);
			attValue.setTextContent(statusCode);
			
			intAtt = doc.createElement("interfaceAttribute");
			intAtts.appendChild(intAtt);
			
			attId = doc.createElement("attributeId");
			intAtt.appendChild(attId);
			attId.setTextContent(HTTP_STATUS_TEXT);
			
			attValue = doc.createElement("attributeValue");
			intAtt.appendChild(attValue);
			attValue.setTextContent(statusText);
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			
			DOMSource s = new DOMSource(doc);
			StreamResult r = new StreamResult(new File(messageFile).getAbsolutePath());
			
			t.transform(s, r);
		}catch (ParserConfigurationException pce){
			pce.printStackTrace();
		}catch (TransformerException tfe){
			tfe.printStackTrace();
		}
	}

}
