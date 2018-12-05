package com.demo.stub.context;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class DemoStubNamespaceContext implements NamespaceContext {

	public String getNamespaceURI(String prefix) {
		if(prefix == null) throw new NullPointerException("Null Prefix");
		else if("demo".equals(prefix)) return "http://com.demo/stub";
		return XMLConstants.NULL_NS_URI;
		
	}

	// this method isn't necessary for XPath processing
	public String getPrefix(String uri) {
		throw new UnsupportedOperationException();
	}

	// this method isn't necessary for XPath processing
	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String uri) {
		throw new UnsupportedOperationException();
	}

}
