package com.demo.stub.context;

import java.util.ArrayList;
import java.util.List;

public class MessageContextFactoryImpl {
	
	public static final String DEMO_SCHEMA_PACKAGE = "com.demo.stub.jaxb";
	
	protected static final String DEMO_STUB_SCHEMA_NAMESPACE = "http://www.demo.com/stub";
	
	protected static final String DEMO_STUB_SCHEMA_PREFIX = "demo";
	
	protected final static String DEMO_SCHEMA_NAME = "./xsd/demo.xsd";
	
	final List<String> schemaNames = new ArrayList<String>(){
	private static final long servialVersionUID = 1L;
		{
			add(DEMO_SCHEMA_NAME);
		}
	};
}
