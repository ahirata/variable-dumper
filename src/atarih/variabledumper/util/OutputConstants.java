package atarih.variabledumper.util;

import java.util.HashMap;
import java.util.Map;

public class OutputConstants {

	public static final Map<String, String> outputMap = new HashMap<String, String>(); 
	
	private static final String CONTRUCTOR = "new {0}({1})";
	
	private static final String ARRAY_CONTRUCTOR = "new {0}[{1}]";
	
	private static final String GENERIC_CONTRUCTOR = "new {0}<{1}>({2})";
	
	private static final String ARRAY_INDEX = "{0}[{1}]";
	
	private static final String ARRAY_INDEX_VARIABLE = "{0}.get{1}()[{2}]";
	
	static {
		outputMap.put("constructor", CONTRUCTOR);
		outputMap.put("array.constructor", ARRAY_CONTRUCTOR);
		outputMap.put("generic.constructor", GENERIC_CONTRUCTOR);
		outputMap.put("array.index", ARRAY_INDEX);
		outputMap.put("array.index.variable", ARRAY_INDEX_VARIABLE);
	}
}
