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
	
	private static final String ASSIGNMENT_TO = "{0} {1} = {2};";
	
	private static final String ASSIGNMENT_TO_GENERIC = "{0}<{1}> {2} = {3};";
	
	private static final String ASSIGNMENT_TO_MAP_DECLARATION = "{0}<{1}, {2}> {3} = {4};";
	
	private static final String SET_TO = "{0}.set{1}({2});";
	
	private static final String ADD_TO = "{0}.add({1});";
	
	private static final String PUT_TO = "{0}.put({1});";
	
	static {
		outputMap.put("constructor", CONTRUCTOR);
		outputMap.put("array.constructor", ARRAY_CONTRUCTOR);
		outputMap.put("generic.constructor", GENERIC_CONTRUCTOR);
		outputMap.put("array.index", ARRAY_INDEX);
		outputMap.put("array.index.variable", ARRAY_INDEX_VARIABLE);
		outputMap.put("assignment.to", ASSIGNMENT_TO);
		outputMap.put("assignment.to.generic", ASSIGNMENT_TO_GENERIC);
		outputMap.put("assignment.to.map.declaration", ASSIGNMENT_TO_MAP_DECLARATION);
		outputMap.put("set.to", SET_TO);
		outputMap.put("add.to", ADD_TO);
		outputMap.put("put.to", PUT_TO);
	}
}
