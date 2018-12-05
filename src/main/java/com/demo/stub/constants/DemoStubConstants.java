package com.demo.stub.constants;

import java.util.ArrayList;
import java.util.List;

public class DemoStubConstants {
	
	private final static String DEMO_SCHEMA_NAME = "./xsd/demo.xsd";
	
	public static final List<String> SCHEMA_NAMES = new ArrayList<String>(){
		private static final long servialVersionUID = 1L;
		{
			add(DEMO_SCHEMA_NAME);
		}
	};
}
