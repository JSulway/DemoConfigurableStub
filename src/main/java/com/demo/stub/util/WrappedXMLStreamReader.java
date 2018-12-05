package com.demo.stub.util;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

public class WrappedXMLStreamReader extends StreamReaderDelegate {

	private final XMLStreamReader xmlStreamReader;
	private String lastParsedLocalName;
	private String lastParsedAttributeNamespace;
	private String lastParsedAttributeLocalName;
	
	public WrappedXMLStreamReader(final XMLStreamReader xmlStreamReader){
		super(xmlStreamReader);
		this.xmlStreamReader = xmlStreamReader;
	}
	
	public String getLastParsedLocalName(){
		return this.lastParsedLocalName;
	}
	
	/**
	 * Overridden to cache the last element name parsed from the input stream
	 */
	@Override
	public String getLocalName(){
		lastParsedLocalName = xmlStreamReader.getLocalName();
		return lastParsedLocalName;
	}
	
	@Override
	public String getAttributeNamespace(final int arg){
		lastParsedAttributeNamespace = xmlStreamReader.getAttributeNamespace(arg);
		return lastParsedAttributeNamespace;
	}
	
	@Override
	public String getAttributeLocalName(final int arg){
		lastParsedAttributeLocalName  = xmlStreamReader.getAttributeLocalName(arg);
		return lastParsedAttributeLocalName;
	}
	
}
